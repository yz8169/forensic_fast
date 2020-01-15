package myJs.myPkg

import argonaut.{DecodeJson, EncodeJson, Json, PrettyParams}

import scala.scalajs.js
import myJs.Utils._
import shared.plotly.Trace
import shared.plotly.internals.BetterPrinter

import scala.scalajs.js.JSON
import argonaut.Argonaut._
import shared.plotly.element.Color
import shared.plotly.internals.BetterPrinter
import shared.plotly.layout._
import argonaut._
import argonaut.ArgonautShapeless._
import myJs.myPkg.jquery.JQuery
import myJs.myPkg.jsext._
import shared.plotly.internals.ArgonautCodecsInternals._
import org.scalajs.dom._

/**
 * Created by yz on 2019/3/14
 */
package object myPlotly {
  private val printer = BetterPrinter(PrettyParams.nospace.copy(dropNullKeys = true))

  private def stripNulls(json: Json): js.Any = {
    JSON.parse(printer.render(json))

  }

  implicit val argonautEncodeConfig = EncodeJson.of[PlotlyConfig]
  implicit val argonautDecodeConfig = DecodeJson.of[PlotlyConfig]

  implicit val argonautEncodeTrace = EncodeJson.of[Trace]
  implicit val argonautDecodeTrace = DecodeJson.of[Trace]


  def newPlot(jq: JQuery, data: Seq[Trace], layout: Layout, config: PlotlyConfigOptions): Unit = {
    g.Plotly.newPlot(
      jq(0),
      stripNulls(data.asJson),
      stripNulls(layout.asJson),
      config
    )
  }

  def newPlot(div: String, data: Seq[Trace], layout: Layout, config: PlotlyConfigOptions): Unit = {
    g.Plotly.newPlot(
      div,
      stripNulls(data.asJson),
      stripNulls(layout.asJson),
      config
    )
  }

  def resize: Unit = {
    g.Plotly.Plots.resize(document.getElementById("chart"))
  }

  def plotlyAutoScale = {
    window.addEventListener("resize", (ev: Event) => {
      myPlotly.resize
    })
  }

}

final case class PlotlyConfig(
                               displayModeBar: Boolean,
                             )

object PlotlyConfig {
  def apply(
             displayModeBar: Boolean = true,
           ) =
    new PlotlyConfig(
      displayModeBar,
    )
}

object PlotlyConfigOptions extends PlotlyConfigOptionsBuilder(noOpts)

class PlotlyConfigOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[PlotlyConfigOptions, PlotlyConfigOptionsBuilder](new PlotlyConfigOptionsBuilder(_)) {

  def displayModeBar(v: Boolean) = jsOpt("displayModeBar", v)

}

trait PlotlyConfigOptions extends js.Object {

}
