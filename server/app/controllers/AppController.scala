package controllers

import java.io.File

import dao._
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import play.api.libs.json.Json
import tool._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Administrator on 2019/8/7
 */
class AppController @Inject()(cc: ControllerComponents, formTool: FormTool, accountDao: AccountDao, userDao: UserDao) extends
  AbstractController(cc) {

  def loginBefore = Action { implicit request =>
    Ok(views.html.login1())
  }

  def login = Action.async { implicit request =>
    val data = formTool.loginUserForm.bindFromRequest().get
    accountDao.selectById1.zip(userDao.select(data.name, data.password)).map { case (account, optionUser) =>
      if (data.name == account.account && data.password == account.password) {
        Redirect(routes.AdminController.userManageBefore()).addingToSession("admin" -> data.name)
      } else if (optionUser.isDefined) {
        val user = optionUser.get
        Redirect(routes.UserController.missionManageBefore()).addingToSession("user" -> data.name,
          "id" -> user.id.toString)
      } else {
        Redirect(routes.AppController.loginBefore()).flashing("info" -> "用户名或密码错误!")
      }
    }
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        controllers.routes.javascript.AdminController.getAllUser,
        controllers.routes.javascript.AdminController.userNameCheck,
        controllers.routes.javascript.AdminController.deleteUserById,
        controllers.routes.javascript.AdminController.addUser,
        controllers.routes.javascript.AdminController.getUserById,
        controllers.routes.javascript.AdminController.updateUser,

        controllers.routes.javascript.MissionController.missionNameCheck,
        controllers.routes.javascript.MissionController.newMission,
        controllers.routes.javascript.MissionController.getAllMission,
        controllers.routes.javascript.MissionController.downloadData,
        controllers.routes.javascript.MissionController.deleteMissionById,
        controllers.routes.javascript.MissionController.updateMissionSocket,
        controllers.routes.javascript.MissionController.getLogContent,

        controllers.routes.javascript.SampleController.sampleManageBefore,
        controllers.routes.javascript.SampleController.getAllSample,
        controllers.routes.javascript.SampleController.deleteSampleById,
        controllers.routes.javascript.SampleController.getReadsData,


        controllers.routes.javascript.UserController.missionManageBefore,

      )
    ).as("text/javascript")

  }


}
