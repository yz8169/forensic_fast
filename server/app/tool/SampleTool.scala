package tool

import java.io.File

import models.Tables._

/**
 * Created by Administrator on 2020/1/8
 */
object SampleTool {

  def getUserSampleDir(userId: Int) = {
    val userIdDir = Tool.getUserIdDir(userId)
    new File(userIdDir, "sample")
  }

  def getSampleIdDir(row: SampleRow) = {
    val userMissionFile = getUserSampleDir(row.userId)
    new File(userMissionFile, row.id)
  }


}
