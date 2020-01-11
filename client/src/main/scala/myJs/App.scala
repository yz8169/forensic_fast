package myJs

import scalatags.Text.all.s

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.myPkg.jquery._

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("App")
object App {

  @JSExport("init")
  def init = {
    val shareTitle ="法医平台"
    val beforeTitle = $("#shareTitle").text()
    $("#shareTitle").text(s"${beforeTitle}-${shareTitle}")

  }

}
