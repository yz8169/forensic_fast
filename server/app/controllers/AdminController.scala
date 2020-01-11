package controllers

import java.io.File

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import tool._
import dao._
import models.Tables._
import org.joda.time.DateTime
import play.api.libs.json.Json
import utils.Utils

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by Administrator on 2019/8/7
 */
class AdminController @Inject()(cc: ControllerComponents, formTool: FormTool, accountDao: AccountDao,
                                userDao: UserDao) extends
  AbstractController(cc) {

  def userManageBefore = Action { implicit request =>
    Ok(views.html.admin.userManage())
  }

  def logout = Action { implicit request =>
    Redirect(routes.AppController.loginBefore()).flashing("info" -> "退出登录成功!").removingFromSession("admin")
  }

  def getAllUser = Action.async { implicit request =>
    userDao.selectAll.map { x =>
      val array = Utils.getArrayByTs(x)
      Ok(Json.toJson(array))
    }
  }

  def userNameCheck = Action.async { implicit request =>
    val data = formTool.userNameForm.bindFromRequest.get
    userDao.selectByName(data.name).zip(accountDao.selectById1).map { case (optionUser, admin) =>
      optionUser match {
        case Some(y) => Ok(Json.obj("valid" -> false))
        case None =>
          val valid = if (data.name == admin.account) false else true
          Ok(Json.obj("valid" -> valid))
      }
    }
  }

  def deleteUserById = Action.async { implicit request =>
    val data = formTool.idForm.bindFromRequest().get
    userDao.deleteById(data.id).map { x =>
      Ok("success")
    }
  }

  def addUser = Action.async { implicit request =>
    val data = formTool.userForm.bindFromRequest().get
    val row = UserRow(0, data.name, data.password, new DateTime())
    userDao.insert(row).map { userId =>
      Ok(Json.toJson("success!"))
    }
  }

  def getUserById = Action.async { implicit request =>
    val data = formTool.idForm.bindFromRequest().get
    userDao.selectById(data.id).map { x =>
      Ok(WebTool.getJsonByT(x))
    }
  }

  def updateUser = Action.async { implicit request =>
    val data = formTool.userForm.bindFromRequest().get
    userDao.selectByNameSome(data.name).flatMap { dbUser =>
      val row = dbUser.copy(password = data.password)
      userDao.update(row).map { x =>
        Ok("success")
      }
    }
  }

  def changePasswordBefore = Action { implicit request =>
    Ok(views.html.admin.changePassword())
  }

  def changePassword = Action.async { implicit request =>
    val data = formTool.changePasswordForm.bindFromRequest().get
    accountDao.selectById1.flatMap { x =>
      if (data.password == x.password) {
        val row = AccountRow(x.id, x.account, data.newPassword)
        accountDao.update(row).map { y =>
          Redirect(routes.AppController.loginBefore()).flashing("info" -> "密码修改成功!").withNewSession
        }
      } else {
        Future.successful(Redirect(routes.AdminController.changePasswordBefore()).flashing("info" -> "密码错误!"))
      }
    }
  }




}
