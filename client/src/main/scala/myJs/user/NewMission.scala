package myJs.user

import java.math.BigInteger


import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.Utils._
import myJs.myPkg._
import org.scalajs.dom.Element
import org.scalajs.dom
import org.scalajs.dom._
import org.w3c.dom.html.HTMLSelectElement
import scalatags.Text.all.{value, _}

import scala.scalajs.js.JSConverters._
import myJs.myPkg.Implicits._
import myJs.Tool._
import org.scalajs.dom.raw.HTMLFormElement

import scala.scalajs.js.JSON

import myJs.myPkg.jquery._
import myJs.myPkg.bootstrap.Bootstrap.default._

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("NewMission")
object NewMission {

  @JSExport("init")
  def init = {
    bootStrapValidator
    g.$("#form").bootstrapValidator("revalidateField", "missionName")

  }

  @JSExport("myRun")
  def myRun = {
    val bv = jQuery("#form").data("bootstrapValidator")
    bv.validate()
    val valid = bv.isValid().asInstanceOf[Boolean]
    if (valid) {
      val formData = new FormData(document.getElementById("form").asInstanceOf[HTMLFormElement])
      $(":disabled").attr("disabled", false)
      val element = div(id := "content",
        span(id := "info", "正在运行",
          span(id := "progress", "。。。")), " ",
        img(src := "/assets/images/running2.gif", cls := "runningImage", width := 30, height := 20)
      ).render
      val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())
      val index = layer.alert(element, layerOptions)
      val url = g.jsRoutes.controllers.MissionController.newMission().url.toString
      val xhr = new XMLHttpRequest
      xhr.open("post", url)
      xhr.upload.onprogress = progressHandlingFunction
      xhr.onreadystatechange = (e) => {
        if (xhr.readyState == XMLHttpRequest.DONE) {
          val data = xhr.response
          val rs = JSON.parse(data.toString).asInstanceOf[js.Dictionary[js.Any]]
          layer.close(index)
          val valid = rs("valid").asInstanceOf[Boolean]
          if (valid) {
            window.location.href = g.jsRoutes.controllers.UserController.missionManageBefore().url.toString
          } else {
            g.swal("Error", rs.myGet("message"), "error")
          }
        }
      }
      xhr.send(formData)
    }
  }

  def bootStrapValidator = {
    val url = g.jsRoutes.controllers.MissionController.missionNameCheck().url.toString
    val maxNumber = Double.MaxValue
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "missionName" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "任务名不能为空!"
            ),
            "remote" -> js.Dictionary(
              "message" -> "任务名已存在!",
              "extension" -> "csv",
              "url" -> url,
              "type" -> "POST",
              "delay" -> 1000
            ),
          )
        ),
        "id" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "id不能为空!"
            ),
          )
        ),
        "fqFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "fq文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "fq文件格式不正确!",
              "extension" -> "txt",
            ),
          )
        ),

      )
    )
    g.$("#form").bootstrapValidator(dict)

  }


}
