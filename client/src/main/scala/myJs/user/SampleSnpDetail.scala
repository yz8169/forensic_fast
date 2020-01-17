package myJs.user

import myJs.Codecs.{SeqData, SnpReadsData, StrReadsData}
import myJs.myPkg.jquery.{$, JQuery, JQueryAjaxSettings, JQueryXHR}
import myJs.user.SampleDetail.{getGenotypeCol, refreshTable, scatterPlot}
import scalatags.Text.all._
import shared.VarTool.{genotypeStr, locusStr}

import scala.scalajs.js
import scala.scalajs.js.JSON
import shared.implicits.Implicits._
import argonaut._
import Argonaut._
import myJs.Tool
import myJs.Utils.g
import myJs.myPkg.{PlotlyConfigOptions, myPlotly}
import org.scalajs.dom.ext.Ajax
import shared.plotly.{Bar, Pie}
import shared.plotly.Sequence.{Doubles, Strings}
import shared.plotly.element.Anchor.Top
import shared.plotly.element.Color.StringColor
import shared.plotly.element.{Anchor, AxisType, HoverInfo, Marker}
import shared.plotly.layout.HoverMode.Closest
import shared.plotly.layout.Ref.{Paper, XRef}
import shared.plotly.layout.{Annotation, Axis, Font, Layout, Margin}
import scala.concurrent.ExecutionContext.Implicits.global
import shared.implicits.Implicits._

/**
 * Created by Administrator on 2020/1/16
 */
object SampleSnpDetail {

  def str2SnpReadsDatas(rs: String) = {
    rs.decodeOption[List[SnpReadsData]].getOrElse(Nil)
  }

  def getSnpReadsData(idStr: String) = {
    val url = g.jsRoutes.controllers.SampleController.getSnpReadsData().url.toString
    Ajax.get(url = s"${url}?id=${idStr}").map { xhr =>
      str2SnpReadsDatas(xhr.responseText)
    }

  }

  def snpShow(datas: List[SnpReadsData], seqDatas: List[SeqData]) = {
    val filterDatas = datas
    val parentId = "snp"
    snpBarPlot(filterDatas, $(s"#${
      parentId
    } #barChart"))
    scatterPlot(filterDatas, $(s"#${
      parentId
    } #scatterChart"))
    SampleSnpDetail.fillSnpReadsData(filterDatas, seqDatas, $(s"#${
      parentId
    } #data"))
    val ts = filterDatas.map {
      x =>
        (x.locus, x.genotype)
    }
    val filterSeqData = seqDatas.filter {
      x =>
        val t = (x.locus, x.genotype)
        ts.contains(t)
    }
    val jq = $(s"#${
      parentId
    } #seqTable")
    refreshTable(Tool.stripNulls(filterSeqData.asJson), jq)

  }

  def snpBarPlot(filterDatas: List[SnpReadsData], jq: JQuery) = {
    val map = filterDatas.map { x =>
      (x.genotype + x.locus, x.reads)
    }
    val xs = map.map(_._1).indices
    val reads = map.map(_._2)
    val dbColors = List("#A01DDD", "#FFA142")
    val locuss = filterDatas.map(_.locus)
    val distLocus = locuss.distinct
    val locusIndex = distLocus.map {
      x =>
        locuss.indexOf(x)
    }
    val genotypes = filterDatas.map(_.genotype)
    val sizeMap = filterDatas.map { x =>
      (x.locus, x.genotype)
    }.groupMap(_._1)(_._2).view.mapValues(_.size).toMap

    val locusGenotypesMap = filterDatas.map {
      x =>
        (x.locus, x.genotype)
    }.groupSeqMap
    val colors = locusGenotypesMap.zipWithIndex.flatMap {
      case (inMap, i) =>
        val colorStr = if (i % 2 == 0) {
          dbColors(0)
        } else dbColors(1)
        val color = new StringColor(colorStr)
        inMap._2.map(x => color)
    }.toList
    val texts = filterDatas.map {
      x =>
        s"Locus:${
          x.locus
        }<br>Allele Name:${
          x.genotype
        }<br>Depth:${
          x.reads
        }"
    }
    val plotData = Seq(
      Bar(
        x = xs,
        y = reads,
        marker = Marker(
          color = colors
        ),
        width = 0.5,
        hoverinfo = HoverInfo.Text,
        text = texts
      )
    )
    val annotations = locusIndex.zip(distLocus).map {
      case (index, locus) =>
        val size = sizeMap(locus)
        val x = (index + ((size - 1) / 2.0))
        Annotation(
          x = x,
          y = -0.08,
          showarrow = false,
          text = s"${
            locus
          }",
          xref = XRef,
          yref = Paper,
          textangle = 90.0,
          yanchor = Top,
          xanchor = Anchor.Center,
          font = Font(size = 8)
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
        tickfont = Font(size = 6)
      ),
      yaxis = Axis(
        title = s"Number of reads",
        autorange = true
      ),
      annotations = annotations,
      margin = Margin(b = 120, r = 0, t = 0),
      hovermode = Closest,
      width = 1100
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(jq, plotData, layout, config)
  }

  def fillSnpReadsData(datas: List[SnpReadsData], seqDatas: List[SeqData], jq: JQuery) = {
    case class GenotypeData(genotype: String, reads: String)
    val genotypeMap = datas.map { x =>
      (x.locus, GenotypeData(x.genotype, x.reads))
    }.groupSeqMap
    val qcMap = datas.map { x =>
      (x.locus, x.qc)
    }.filter { x => x._2 != "" }.toMap
    val first1Height = 30
    val html = genotypeMap.map { case (key, genotypes) =>
      val hasQc = qcMap.get(key).isDefined
      val myBackgroundColor = "#FABD82"
      val seqDataRow = seqDatas.filter(_.locus == key)
      div(cls := "form-group", marginRight := 15, height := 120, marginBottom := 20,
        table(cls := " table table-bordered strTable", width := 150,
          tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
            td(colspan := 2,
              a(key, color := "#FFFFFF", onclick := s"SampleDetail.snpDetailShow('${Tool.stringify(seqDataRow.asJson)}')"),
              fontWeight := "bold", backgroundColor := "#5E738B", paddingTop := 3, paddingBottom := 3)
          ),
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              backgroundColor := myBackgroundColor, genotypes.map { inData =>
                val colStr = getGenotypeCol(inData.genotype)
                td(inData.genotype, color := colStr, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          } else {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              genotypes.map { inData =>
                val colStr = getGenotypeCol(inData.genotype)
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3, color := colStr)
              })
          },
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              backgroundColor := myBackgroundColor, genotypes.map { inData =>
                td(inData.reads, width := "50%", fontSize := "12px", paddingTop := 3, paddingBottom := 3)
              }
            )
          } else {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              genotypes.map { inData =>
                td(inData.reads, width := "50%", fontSize := "12px", paddingTop := 3, paddingBottom := 3)
              }
            )
          },
          qcMap.get(key).map { qc =>
            tr(height := first1Height, maxHeight := first1Height, backgroundColor := myBackgroundColor,
              td(colspan := 2, fontSize := "12px", qc, paddingTop := 3, paddingBottom := 3)
            )
          }
        )
      )
    }.mkString("&nbsp;")
    jq.html(html)
  }

  def snpDetailPlot(seqDatas: List[SeqData]) = {
    val xs = seqDatas.map(_.genotype)
    val reads = seqDatas.map(_.reads)
    val plotData = Seq(
      Pie(
        labels = xs,
        values = reads,
        marker = Marker(
        ),
        hoverinfo = HoverInfo.Y,
        width = 0.5
      )
    )

    val layout = Layout(
      title = s"",
      xaxis = Axis(
        title = s"",
        `type` = AxisType.Category,
        tickangle = 0.0,
        autorange = true,
        tickfont = Font(size = 10)
      ),
      yaxis = Axis(
        title = s"Number of reads",
        autorange = true
      ),
      margin = Margin(b = 0,
        t = 0,
        l = 0,
        r = 0),
      width = 400,
      height = 400,
      hovermode = Closest
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot($("#snpModal #detailChart"), plotData, layout, config)
  }

}
