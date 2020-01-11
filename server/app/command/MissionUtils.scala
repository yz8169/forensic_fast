package command

import java.io.File

import dao.MissionDao
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import models.Tables._
import play.api.mvc.RequestHeader
import tool.Tool

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import implicits.Implicits._
import tool.Pojo.MissionDirData

/**
 * Created by yz on 2018/6/13
 */
object MissionUtils {

  def getMissionDir(missionId: Int, outDir: File) = {
    val missionIdDir = new File(outDir, missionId.toString).createDirectoryWhenNoExist
    val workspaceDir = new File(missionIdDir, "workspace").createDirectoryWhenNoExist
    val resultDir = new File(missionIdDir, "result").createDirectoryWhenNoExist
    val logFile = new File(missionIdDir, "log.txt")
    MissionDirData(workspaceDir, resultDir, logFile)
  }



}
