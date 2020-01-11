package myJs

import myJs.myPkg._
import org.scalajs.dom.raw.ProgressEvent

import scala.scalajs.js
import scalatags.Text.all._

import scala.math.BigDecimal.RoundingMode
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.myPkg.jquery._


/**
  * Created by yz on 2019/3/6
  */
@JSExportTopLevel("Utils")
object Utils {

  val g = js.Dynamic.global
  val layer = g.layer.asInstanceOf[Layer]

  implicit class MyJson(val json: js.Dictionary[js.Any]) {

    def myGet(key: String) = json.getOrElse(key, "").toString

  }

  val dataToggle=attr("data-toggle")
  val dataContent=attr("data-content")
  val dataContainer=attr("data-container")
  val dataPlacement=attr("data-placement")
  val dataHtml=attr("data-html")
  val dataAnimation=attr("data-animation")
  val dataTrigger=attr("data-trigger")
  val dataField=attr("data-field")
  val dataSortable=attr("data-sortable")

  def progressHandlingFunction = {
    (e: ProgressEvent) => {
      if (e.lengthComputable) {
        val percent = e.loaded / e.total * 100
        val newPercent = BigDecimal(percent).setScale(2, RoundingMode.HALF_UP)
        $("#progress").html(s"(${newPercent}%)")
        if (percent >= 100) {
          $("#info").text("正在运行")
          $("#progress").html("")
        }
      }
    }

  }

  @JSExport("setColumns")
  def setColumns(value: String) = {
    val element = $(s"input:checkbox[value='${value}']")
    if (element.is(":checked")) {
      g.$("#table").bootstrapTable("showColumn", value)
    } else {
      g.$("#table").bootstrapTable("hideColumn", value)
    }

  }




}
