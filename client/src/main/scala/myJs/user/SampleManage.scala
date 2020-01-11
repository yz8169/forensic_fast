package myJs.user

import myJs.Utils._
import myJs.myPkg.Implicits._
import myJs.myPkg.{ColumnOptions, LayerOptions, Swal, SwalOptions, TableOptions}
import org.scalajs.dom._
import scalatags.Text.TypedTag
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.myPkg.jquery._
import myJs.Tool._
import myJs.myPkg.bootstrap.Bootstrap.default._

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("SampleManage")
object SampleManage {

  val typeTagEmpty = List[TypedTag[String]]()


  @JSExport("init")
  def init = {
    initTable

    refreshTable()
    SampleDetail.showDetail("584")

  }

  @JSExport("refreshUser")
  def refreshTable(f: () => js.Any = () => ()) = {
    val url = g.jsRoutes.controllers.SampleController.getAllSample().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      $("#table").bootstrapTable("load", data)
      f()
    }
    $.ajax(ajaxSettings)

  }

  val operateColumn = js.Array(
    ColumnOptions.field("操作").title("操作").formatter(operateFmt))

  def tbFmt(columnName: String): js.Function = (v: js.Any) => columnName match {
    case _ => v
  }

  def initTable = {
    val tableId = "table"
    val columnNames = js.Array("id", "lane", "gender", "kind", "updateMission", "updateTime")
    val columns = columnNames.map { columnName =>
      val title = columnName match {
        case "id" => "样本编号"
        case "lane" => "Lane"
        case "gender" => "gender"
        case "kind" => "type"
        case "updateMission" => "任务来源"
        case "updateTime" => "更新时间"
        case _ => columnName
      }
      val fmt = tbFmt(columnName)
      ColumnOptions.field(columnName).title(title).sortable(true).formatter(fmt)
    }.concat(operateColumn)
    val options = TableOptions.columns(columns)
    $(s"#${tableId}").bootstrapTable(options)
  }

  @JSExport("operateFmt")
  def operateFmt: js.Function = {
    (v: js.Any, row: js.Dictionary[js.Any]) =>
      val deleteStr = a(
        title := "删除",
        cursor.pointer,
        onclick := s"SampleManage.deleteData('" + row("id") + "')",
        target := "_blank",
        span(
          em(cls := "fa fa-close")
        )
      )
      val viewStr = a(
        title := "查看详细",
        cursor.pointer,
        onclick := s"SampleDetail.showDetail('" + row("id") + "')",
        target := "_blank",
        span(
          em(cls := "fa fa-eye")
        )
      )
      Array(viewStr, deleteStr).mkString("&nbsp;")

  }

  @JSExport("deleteData")
  def deleteData(id: String) = {
    val options = SwalOptions.title("").text("确定要删除此数据吗？").`type`("warning").showCancelButton(true).
      showConfirmButton(true).confirmButtonClass("btn-danger").confirmButtonText("确定").closeOnConfirm(false).
      cancelButtonText("取消").showLoaderOnConfirm(true)
    Swal.swal(options, () => {
      val url = g.jsRoutes.controllers.SampleController.deleteSampleById().url.toString
      val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${id}").
        `type`("delete").contentType("application/json").success { (data: js.Any, status: String, e: JQueryXHR) =>
        refreshTable { () =>
          Swal.swal(SwalOptions.title("成功").text("删除成功").`type`("success"))
        }
      }.error { (data: JQueryXHR, status: String, e: String) =>
        Swal.swal(SwalOptions.title("错误").text("删除失败").`type`("error"))
      }
      $.ajax(ajaxSettings)

    })
  }

}
