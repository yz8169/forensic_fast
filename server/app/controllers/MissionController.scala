package controllers

import java.io.File
import java.nio.file.Files

import actor.MissionManageActor
import akka.actor.AbstractActor.Receive
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import command.{CommandUtils, MissionUtils}
import dao._
import javax.inject.{Inject, Singleton}
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.streams.ActorFlow
import play.api.libs.ws.WSClient
import tool.Pojo._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import implicits.Implicits._
import tool.{FormTool, Tool, WebTool}
import utils.Utils
import scala.language.postfixOps
import FormTool._

/**
 * Created by Administrator on 2019/8/8
 */
@Singleton
class MissionController @Inject()(cc: ControllerComponents, formTool: FormTool
                                 )(implicit val system: ActorSystem,
                                   implicit val modeDao: ModeDao,
                                   implicit val missionDao: MissionDao,
                                   implicit val sampleDao: SampleDao,
                                   implicit val userDao: UserDao,
                                   implicit val materializer: Materializer,
                                 ) extends
  AbstractController(cc) {

  implicit val dao = MyDao(missionDao, sampleDao)

  val missionManageActor = system.actorOf(
    Props(new MissionManageActor())
  )
  missionManageActor ! "ask"

  def newMissionBefore = Action { implicit request =>
    val missionName = s"project_${Tool.generateMissionName}"
    val data = formTool.idOpForm.bindFromRequest().get
    Ok(views.html.user.newMission(missionName, data.id))
  }

  def missionNameCheck = Action.async { implicit request =>
    val data = formTool.missionNameForm.bindFromRequest.get
    val userId = WebTool.getUserId
    missionDao.selectOptionByMissionName(userId, data.missionName).map { mission =>
      mission match {
        case Some(y) => Ok(Json.obj("valid" -> false))
        case None =>
          Ok(Json.obj("valid" -> true))
      }
    }
  }

  def newMission = Action.async(parse.multipartFormData) { implicit request =>
    val data = FormTool.missionForm.bindFromRequest().get
    val tmpDir = Tool.createTempDirectory("tmpDir")
    val myTmpDir = Tool.getDataDir(tmpDir)
    val userId = WebTool.getUserId
    val row = MissionRow(0, s"${data.missionName}", userId, new DateTime(), None, "preparing")
    missionDao.insert(row).flatMap(_ => missionDao.selectByMissionName(row.userId, row.missionName)).flatMap { mission =>
      val outDir = WebTool.getUserMissionDir
      val missionDir = MissionUtils.getMissionDir(mission.id, outDir)
      val (workspaceDir, resultDir) = (missionDir.workspaceDir, missionDir.resultDir)
      val dataDir = new File(workspaceDir.getParent, "data")
      FileUtils.copyDirectory(myTmpDir.tmpDir, dataDir)
      myTmpDir.fqFile.copyTo(Tool.getInputTxtFile(workspaceDir))
      val configFile = Tool.produceConfigFile(data, workspaceDir)
      Tool.productInputXmlFile(workspaceDir, data)
      Tool.deleteDirectory(myTmpDir.tmpDir)
      val newMission = mission.copy(state = "wait")
      missionDao.update(newMission).map { x =>
        Ok(Json.obj("valid" -> true))
      }
    }
  }

  def getAllMission = Action.async {
    implicit request =>
      val userId = WebTool.getUserId
      missionDao.selectAll(userId).map {
        x =>
          Future {
            val missionIds = x.map(_.id.toString)
            val missionDir = WebTool.getUserMissionDir
            missionDir.listFiles().filter {
              dir =>
                !missionIds.contains(dir.getName)
            }.foreach(Utils.deleteDirectory(_))
          }
          val array = Utils.getArrayByTs(x)
          Ok(Json.toJson(array))
      }

  }

  def downloadData = Action.async {
    implicit request =>
      val userId = WebTool.getUserId
      val data = formTool.missionIdForm.bindFromRequest().get
      val missionId = data.missionId
      missionDao.selectByMissionId(userId, missionId).map {
        mission =>
          val missionIdDir = WebTool.getMissionIdDir(missionId)
          val dataDir = new File(missionIdDir, "data")
          val dataFile = new File(missionIdDir, s"data.zip")
          if (!dataFile.exists()) ZipUtil.pack(dataDir, dataFile)
          Ok.sendFile(dataFile).withHeaders(
            CONTENT_DISPOSITION -> WebTool.getContentDisposition(s"${mission.missionName}_data.zip"),
            CONTENT_TYPE -> "application/x-download"
          )
      }
  }

  def deleteMissionById = Action.async {
    implicit request =>
      val data = formTool.missionIdForm.bindFromRequest().get
      missionDao.deleteById(data.missionId).map {
        x =>
          val workspaceDir = WebTool.getMissionWorkspaceDirById(data.missionId)
          CommandUtils.killPid(workspaceDir)
          val missionIdDir = WebTool.getMissionIdDir(data.missionId)
          Utils.deleteDirectory(missionIdDir)
          Redirect(routes.MissionController.getAllMission())
      }
  }

  def updateMissionSocket = WebSocket.accept[JsValue, JsValue] {
    implicit request =>
      val userId = WebTool.getUserId
      case class MissionAction(beforeMissions: Seq[MissionRow], action: String)
      ActorFlow.actorRef(out => Props(new Actor {
        override def receive: Receive = {
          case msg: JsValue if (msg \ "info").as[String] == "start" =>
            val beforeMissions = Utils.execFuture(missionDao.selectAll(userId))
            out ! WebTool.getJsonByTs(beforeMissions)
            system.scheduler.scheduleOnce(3 seconds, self, MissionAction(beforeMissions, "update"))
          case MissionAction(beforeMissions, action) =>
            missionDao.selectAll(userId).map {
              missions =>
                val currentMissions = missions
                if (currentMissions.size != beforeMissions.size) {
                  out ! WebTool.getJsonByTs(currentMissions)
                } else {
                  val b = currentMissions.zip(beforeMissions).forall {
                    case (currentMission, beforeMission) =>
                      currentMission.id == beforeMission.id && currentMission.state == beforeMission.state
                  }
                  if (!b) {
                    out ! WebTool.getJsonByTs(currentMissions)
                  }
                }
                system.scheduler.scheduleOnce(3 seconds, self, MissionAction(currentMissions, "update"))
            }
          case _ =>
            self ! PoisonPill
        }

        override def postStop(): Unit = {
          self ! PoisonPill
        }
      }))

  }

  def getLogContent = Action.async {
    implicit request =>
      val userId = WebTool.getUserId
      val data = formTool.missionIdForm.bindFromRequest().get
      missionDao.selectByMissionId(userId, data.missionId).map {
        mission =>
          val missionIdDir = WebTool.getMissionIdDir(data.missionId)
          val logFile = new File(missionIdDir, s"log.txt")
          val logStr = FileUtils.readFileToString(logFile, "UTF-8")
          Ok(Json.toJson(logStr))
      }
  }


}
