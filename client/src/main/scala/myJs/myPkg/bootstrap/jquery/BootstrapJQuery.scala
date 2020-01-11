package myJs.myPkg.bootstrap.jquery

import org.scalajs.jquery.JQuery

import scala.scalajs.js

/**
 * Created by Administrator on 2019/12/31
 */

@scalajs.js.native
trait BootstrapJQuery extends scalajs.js.Object {
  def tab(options: scalajs.js.Any): JQuery = scalajs.js.native
  def carousel(options: scalajs.js.Any = ???): JQuery = scalajs.js.native
  def modal(options: scalajs.js.Any = ???): JQuery = scalajs.js.native
  def tooltip(options: scalajs.js.Any = ???): JQuery = scalajs.js.native
  def popover(options: scalajs.js.Any = ???): JQuery = js.native
}