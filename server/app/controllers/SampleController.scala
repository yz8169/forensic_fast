package controllers

import java.io.File

import actor.MissionManageActor
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import command.{CommandUtils, MissionUtils}
import dao._
import javax.inject.{Inject, Singleton}
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import tool.{FormTool, SampleTool, Tool, WebTool}
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by Administrator on 2019/8/8
 */
@Singleton
class SampleController @Inject()(cc: ControllerComponents, formTool: FormTool
                                )(implicit val system: ActorSystem,
                                  implicit val modeDao: ModeDao,
                                  implicit val missionDao: MissionDao,
                                  implicit val userDao: UserDao,
                                  implicit val sampleDao: SampleDao,
                                  implicit val materializer: Materializer,
                                ) extends
  AbstractController(cc) {

  import FormTool._

  def sampleManageBefore = Action { implicit request =>
    val data = formTool.missionIdOptionForm.bindFromRequest().get
    val missionName = s"reRun_${Tool.generateMissionName}"
    Ok(views.html.user.sampleManage(missionName, data.missionId))
  }

  def getAllSample = Action.async {
    implicit request =>
      val userId = WebTool.getUserId
      sampleDao.selectAll(userId).map {
        x =>
          val array = Utils.getArrayByTs(x)
          Ok(Json.toJson(array))
      }
  }

  def deleteSampleById = Action.async {
    implicit request =>
      val data = sampleIdForm.bindFromRequest().get
      val userId = WebTool.getUserId
      sampleDao.deleteById(userId, data.id).map {
        x =>
          Ok(Json.toJson("success"))
      }
  }

  def getReadsData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val readsFile = Tool.getReadsFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(readsFile)
        Ok(json)
    }
  }

  def getSnpReadsData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val readsFile = Tool.getSnpReadsFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(readsFile)
        Ok(json)
    }
  }

  def getSeqData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val readsFile = Tool.getSeqFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(readsFile)
        Ok(json)
    }
  }

  def getSnpSeqData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val readsFile = Tool.getSnpSeqFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(readsFile)
        Ok(json)
    }
  }

  def getBasicData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val readsFile = Tool.getBasicFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(readsFile)
        Ok(json)
    }
  }

  def getStatData = Action.async { implicit request =>
    val data = sampleIdForm.bindFromRequest().get
    val userId = WebTool.getUserId
    sampleDao.selectById(userId, data.id).map {
      x =>
        val sampleIdDir = SampleTool.getSampleIdDir(x)
        val file = Tool.getStatFile(sampleIdDir)
        val json = Utils.getTxtFileJsonNoLower(file)
        Ok(json)
    }
  }

}
