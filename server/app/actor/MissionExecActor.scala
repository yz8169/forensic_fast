package actor

import java.io.File
import java.nio.file.Files

import akka.actor.{Actor, ActorSystem, PoisonPill}
import akka.stream.Materializer
import command.{CommandExec, CommandExecutor, MissionUtils}
import dao._
import javax.inject.Inject
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader
import tool.Pojo._
import tool._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import implicits.Implicits._
import utils.Utils


/**
 * Created by Administrator on 2019/10/24
 */
class MissionExecActor @Inject()(mission: MissionRow)(implicit val system: ActorSystem,
                                                      implicit val materializer: Materializer,
                                                      implicit val missionDao: MissionDao,
                                                      implicit val dao: MyDao
) extends Actor {

  val sampleDao = dao.sampleDao

  override def receive: Receive = {
    case "run" =>
      val newMision = mission.copy(state = "running")
      val userId = mission.userId
      val outDir = Tool.getUserMissionDir(mission.userId)
      val missionDir = MissionUtils.getMissionDir(mission.id, outDir)
      val (workspaceDir, resultDir) = (missionDir.workspaceDir, missionDir.resultDir)
      val logFile = Tool.getLogFile(workspaceDir.getParentFile)
      missionDao.update(newMision).map { x =>
        val commandExec = CommandExec().exec { b =>
          Tool.fy(workspaceDir)
        }
        val state = if (commandExec.isSuccess) {
          val configFile = Tool.getConfigFile(workspaceDir)
          val json = Json.parse(configFile.str)
          val data = FormTool.missionForm.bind(json).get
          val sample = SampleRow(data.id, mission.userId, data.lane, data.gender, data.kind, mission.missionName, new DateTime())
          val f = sampleDao.insertOrUpdate(sample).map { x =>
            val sampleIdDir = SampleTool.getSampleIdDir(sample)
            Tool.getOutputXmlFile(workspaceDir).fileCopyToDir(sampleIdDir)
            Tool.produceReadsFile(sampleIdDir)
          }
          Utils.execFuture(f)
          "success"
        } else {
          commandExec.errorInfo.toFile(logFile)
          "error"
        }
        val newMission = mission.copy(state = state, endTime = Some(new DateTime()))
        missionDao.update(newMission).map { x =>
        }
      }.onComplete {
        case Failure(exception) =>
          exception.printStackTrace()
          FileUtils.writeStringToFile(logFile, exception.toString)
          val newMission = mission.copy(state = "error", endTime = Some(new DateTime()))
          missionDao.update(newMission).map { x =>
          }
          self ! "stop"
        case Success(x) =>
          self ! "stop"
      }

    case "stop" =>
      self ! PoisonPill

  }
}
