package controllers

import java.io.File

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import tool.{FormTool, Tool}
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Administrator on 2019/8/8
 */
class ToolController  @Inject()(cc: ControllerComponents,formTool:FormTool) extends AbstractController(cc){

  def downloadExampleData = Action {
    implicit request =>
      val data = formTool.fileNameForm.bindFromRequest().get
      val exampleDir = Tool.exampleDir
      val resultFile = new File(exampleDir, data.fileName)
      Ok.sendFile(resultFile).withHeaders(
        CONTENT_DISPOSITION -> s"attachment; filename=${
          resultFile.getName
        }",
        CONTENT_TYPE -> "application/x-download"
      )

  }

}
