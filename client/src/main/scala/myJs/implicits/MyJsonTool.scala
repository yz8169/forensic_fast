package myJs.implicits

import argonaut.{Json, PrettyParams}
import argonaut._
import Argonaut._
import shared.plotly.internals.BetterPrinter

import scala.scalajs.js
import scala.scalajs.js.JSON

/**
 * Created by Administrator on 2020/1/16
 */
trait MyJsonTool {

  implicit class MyJson(json: Json) {

    val printer = BetterPrinter(PrettyParams.nospace.copy(dropNullKeys = true))

    def stripNulls: js.Any = {
      JSON.parse(printer.render(json))
    }

  }

}
