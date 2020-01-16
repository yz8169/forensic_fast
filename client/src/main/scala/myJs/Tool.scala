package myJs

import argonaut.{Json, PrettyParams}
import myJs.myPkg._
import myJs.myPkg.plotly.Plotly.printer
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myPkg.jquery._
import myPkg.Implicits._
import org.scalajs.dom.Element
import shared.plotly.internals.BetterPrinter

import scala.scalajs.js.JSON

/**
 * Created by yz on 2019/3/6
 */
@JSExportTopLevel("Tool")
object Tool {

  val zhInfo = "信息"
  val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())

  val zhRunning = "正在运行"
  val myElement = div(id := "content")(
    span(id := "info")(zhRunning),
    " ",
    img(src := "/assets/images/running2.gif", width := 30, height := 20, cls := "runningImage")
  ).render

  def element(info: String) = {
    div(id := "content",
      span(id := "info", info,
        span(id := "progress", "")), " ",
      img(src := "/assets/images/running2.gif", width := 30, height := 20, cls := "runningImage")
    ).render
  }

  val loadingElement = element("加载数据")

  def extractor(query: String) = {
    val result = js.RegExp("([^,]+)$").exec(query)
    if (result != null && result(1).isDefined) {
      result(1).toString.trim
    } else ""
  }

  val printer = BetterPrinter(PrettyParams.nospace.copy(dropNullKeys = true))

  def stripNulls(json: Json): js.Any = {
    // Remove empty objects
    JSON.parse(printer.render(json))
  }

  def stringify(json: Json): js.Any = {
    val any = stripNulls(json)
    JSON.stringify(any)
  }

  @JSExport("expand")
  def expand(y: Element) = {
    val tool = $(y).parent().find(".tools a:last")
    $(tool).click()

  }

  val highlighterF = (y: Typeahead, item: String) => {
    val input = y.query
    val query = extractor(input).replaceAll("[\\-\\[\\]{}()*+?.,\\\\^$|#\\s]", "\\$&")
    item.replaceAll(s"(?i)${query}", s"<strong>${query}</strong>")
  }

  def fillByName(rs: js.Dictionary[String], name: String) = {
    val valOp = rs.get(name)
    valOp.foreach { value =>
      $(s":input[name='${name}']").`val`(value)
    }
  }

  def fillByNames(rs: js.Dictionary[String], names: Seq[String]) = {
    names.foreach { name =>
      fillByName(rs, name)
    }
  }

  def fillByNames(rs: js.Dictionary[String]) = {
    val names = $(".fillByName").mapElems { y =>
      $(y).attr("name").toString
    }.toArray
    names.foreach { name =>
      fillByName(rs, name)
    }
  }


  def checkedByNames(rs: js.Dictionary[String], names: Seq[String]) = {
    names.foreach { name =>
      checkedByName(rs, name)
    }
  }

  def checkedByName(rs: js.Dictionary[String], name: String) = {
    rs.get(name) match {
      case Some(value) => $(s":input[name='${name}']").attr("checked", true)
      case None => $(s":input[name='${name}']").attr("checked", false)
    }
  }

  def fillByNames(rs: js.Dictionary[String], formId: String) = {
    val names = $(s"#${formId} .fillByName").mapElems { y =>
      $(y).attr("name").toString
    }.toArray
    names.foreach { name =>
      fillByName(rs, name, formId)
    }
  }

  def fillByIds(rs: js.Dictionary[String]) = {
    val ids = $(s".fillById").mapElems { y =>
      $(y).attr("id").toString
    }.toArray
    ids.foreach { name =>
      fillById(rs, name)
    }
  }

  def fillByName(rs: js.Dictionary[String], name: String, formId: String) = {
    val value = rs(name)
    $(s"#${formId} :input[name='${name}']").`val`(value)
  }

  def fillById(rs: js.Dictionary[String], id: String) = {
    val value = rs(id)
    $(s"#${id}").text(value)
  }

  def fillByNameAndTriggers(rs: js.Dictionary[String], names: List[String]) = {
    names.foreach { name =>
      fillByNameAndTrigger(rs, name)
    }
  }

  def fillByNameAndTriggers(rs: js.Dictionary[String]) = {
    val names = $(s".fillByNameAndTrigger").mapElems { y =>
      $(y).attr("name").toString
    }.toArray
    names.foreach { name =>
      fillByNameAndTrigger(rs, name)
    }
  }

  def fillByNameAndTrigger(rs: js.Dictionary[String], name: String) = {
    val value = rs(name)
    println(value)
    $(s":input[name='${name}']").`val`(value).trigger("onchange")
  }

  @JSExport("fileInput")
  def fileInput = {
    val options = FileInputOptions.showPreview(false).browseLabel("选择...").removeLabel("删除文件").language("zh")
    $(".file").fileinput(options)
  }


}
