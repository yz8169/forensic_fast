package myJs.user

import myJs.Utils._
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{ColumnOptions, PlotlyConfigOptions, Swal, SwalOptions, TableOptions, myPlotly}
import shared.plotly.Bar
import shared.plotly.Sequence.{Doubles, Strings}
import shared.plotly.element.{Anchor, AxisType, Marker}
import shared.plotly.layout.Ref.{Paper, XRef}
import shared.plotly.layout.{Annotation, Axis, Font, Layout, Margin, Ref}
import scalatags.Text.TypedTag
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import org.scalajs.dom._
import shared.plotly.element.Anchor.{Center, Middle, Top}
import shared.plotly.element.Color.StringColor

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("SampleDetail")
object SampleDetail {

  val typeTagEmpty = List[TypedTag[String]]()


  @JSExport("init")
  def init = {

  }

  def plotlyAutoScale = {
    window.addEventListener("resize", (ev: Event) => {
      myPlotly.resize
    })
  }

  @JSExport("showDetail")
  def showDetail(id: String) = {
    val url = g.jsRoutes.controllers.SampleController.getReadsData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${id}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        val filterArray = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        val map = filterArray.map { dict =>
          (dict("genotype") + dict("locus"), dict("reads"))
        }
        val xs = map.map(_._1).indices
        val reads = map.map(_._2).toList
        val dbColors = List("#A01DDD", "#FFA142")
        val locuss = filterArray.map { dict =>
          (dict("locus"))
        }.toList
        val distLocus = locuss.distinct
        val locusIndex = distLocus.map { x =>
          locuss.indexOf(x)
        }
        val genotypes = filterArray.map { dict =>
          dict("genotype")
        }.toList
        val sizeMap = filterArray.map { dict =>
          (dict("locus"), dict("genotype"))
        }.groupMap(_._1)(_._2).view.mapValues(_.size).toMap
        import shared.implicits.Implicits._
        val locusGenotypesMap = filterArray.map { dict =>
          (dict("locus"), dict("genotype"))
        }.toList.groupSeqMap
        val colors = locusGenotypesMap.zipWithIndex.flatMap { case (inMap, i) =>
          val colorStr = if (i % 2 == 0) {
            dbColors(0)
          } else dbColors(1)
          val color = new StringColor(colorStr)
          inMap._2.map(x => color)
        }.toList
        val plotData = Seq(
          Bar(
            xs,
            reads,
            marker = Marker(
              color = colors
            ),
            width = 0.5
          )
        )
        val annotations = locusIndex.zip(distLocus).map { case (index, locus) =>
          val size = sizeMap(locus)
          val x = (index + ((size - 1) / 2.0))
          Annotation(
            x = x,
            y = -0.08,
            showarrow = false,
            text = locus,
            xref = XRef,
            yref = Paper,
            textangle = 90.0,
            yanchor = Top,
            xanchor = Anchor.Center,
            font = Font(size = 10)
          )
        }
        val layout = Layout(
          title = s"",
          xaxis = Axis(
            title = s"",
            `type` = AxisType.Linear,
            tickvals = Doubles(xs.toList.map(_.toDouble)),
            ticktext = Strings(genotypes),
            tickangle = 0.0,
            autorange = true,
            tickfont = Font(size = 8)
          ),
          yaxis = Axis(
            title = s"Number of reads",
            autorange = true
          ),
          annotations = annotations,
          margin = Margin(b = 120)
        )
        val config = PlotlyConfigOptions.displayModeBar(false)
        myPlotly.newPlot("chart", plotData, layout, config)
      }
    $.ajax(ajaxSettings)


    //    $("#result").hide()
    //    $("#mode2,#mode3").hide()
    //    val index = layer.load(1, layerOptions.shade(js.Array("0.1", "#fff")))
    //    val rs = data.asInstanceOf[js.Dictionary[js.Any]]
    //    layer.close(index)
    //    $("#showSampleId").text(rs.myGet("sampleId"))
    //    $("#showName").text(rs.myGet("name"))
    //    g.missionJson = rs("mission")
    //    g.extraDataJson = rs("extraData")
    $("#result").show()
    val target_top = $("#result").offset().top
    jQuery("html,body").animate(js.Dictionary("scrollTop" -> target_top), JQueryAnimationSettings.duration(800))
    //    ""
  }

}
