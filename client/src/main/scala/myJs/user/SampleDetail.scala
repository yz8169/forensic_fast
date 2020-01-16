package myJs.user

import myJs.Tool
import myJs.Utils._
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{ColumnOptions, PlotlyConfigOptions, Swal, SwalOptions, TableOptions, myPlotly}
import shared.plotly._
import shared.plotly.Sequence.{Doubles, Strings}
import shared.plotly.element.{Anchor, AxisType, HoverInfo, Marker, ScatterMode}
import shared.plotly.layout.Ref.{Paper, XRef}
import shared.plotly.layout.{Annotation, Axis, BarMode, Font, HoverMode, Layout, Margin, Ref}
import scalatags.Text.{TypedTag, all}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import org.scalajs.dom._
import shared.plotly.element.Anchor.{Center, Middle, Top}
import shared.plotly.element.Color.StringColor

import scala.scalajs.js.JSON
import shared.implicits.Implicits._
import scalatags.Text.all._
import shared.plotly.element.ScatterMode.Markers
import shared.plotly.layout.HoverMode.{Closest, YMode}
import shared.VarTool._
import shared.implicits.Implicits._
import argonaut._
import Argonaut._
import myJs.Codecs.{ReadsData, SeqData, SnpReadsData, StatData, StrReadsData}
import myJs.implicits.Implicits._

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("SampleDetail")
object SampleDetail {
  val autoStrId = "autoStr"
  val yStrId = "yStr"
  val xStrId = "xStr"
  val snpId = "snp"

  @JSExport("init")
  def init = {

    autoInit
    yInit
    xInit
    snpInit
    initStrTable

  }

  def autoInit = {
    val jq = $(s"#${autoStrId} #seqTable")
    initStrSeqTable(jq)
  }

  def yInit = {
    val jq = $(s"#${yStrId} #seqTable")
    initStrSeqTable(jq)
  }

  def xInit = {
    val jq = $(s"#${xStrId} #seqTable")
    initStrSeqTable(jq)
  }

  def snpInit = {
    val jq = $(s"#${snpId} #seqTable")
    initSnpSeqTable(jq)
    initSnpDetailTable
  }

  def refreshTable(data: js.Any, jq: JQuery, f: () => js.Any = () => ()) = {
    jq.bootstrapTable("load", data)
    f()
  }

  def refreshTableByJson(json: Json, jq: JQuery, f: () => js.Any = () => ()) = {
    val data = Tool.stripNulls(json)
    jq.bootstrapTable("load", data)
    f()
  }

  def initStrTable = {
    val columnNames = js.Array(genotypeStr, "typedAllele", "Reads", "repeatSeq")
    val columns = columnNames.map { columnName =>
      val title = columnName match {
        case x if x == genotypeStr => "Allele Name"
        case "typedAllele" => "Typed Allele"
        case "Reads" => "Reads"
        case "repeatSeq" => "Repeat Sequence"
        case _ => columnName
      }
      ColumnOptions.field(columnName).title(title).sortable(true)
    }
    val options = TableOptions.columns(columns)
    $(s"#locusModal #detailTable").bootstrapTable(options)
  }

  def initSnpDetailTable = {
    val columnNames = js.Array(genotypeStr, "typedAllele", "Reads")
    val columns = columnNames.map { columnName =>
      val title = columnName match {
        case x if x == genotypeStr => "Allele Name"
        case "typedAllele" => "Typed Allele"
        case "Reads" => "Reads"
        case _ => columnName
      }
      ColumnOptions.field(columnName).title(title).sortable(true)
    }
    val options = TableOptions.columns(columns)
    $("#snpModal #detailTable").bootstrapTable(options)
  }

  def initStrSeqTable(jq: JQuery) = {
    val columnNames = js.Array("Locus", genotypeStr, "typedAllele", "Reads", "repeatSeq")
    val columns = columnNames.map { columnName =>
      val title = columnName match {
        case "Locus" => "Locus"
        case x if x == genotypeStr => "Allele Name"
        case "typedAllele" => "Typed Allele"
        case "Reads" => "Reads"
        case "repeatSeq" => "Repeat Sequence"
        case _ => columnName
      }
      ColumnOptions.field(columnName).title(title).sortable(true)
    }
    val options = TableOptions.columns(columns)
    jq.bootstrapTable(options)
  }

  def initSnpSeqTable(jq: JQuery) = {
    val columnNames = js.Array("Locus", genotypeStr, "typedAllele", "Reads")
    val columns = columnNames.map { columnName =>
      val title = columnName match {
        case "Locus" => "Locus"
        case x if x == genotypeStr => "Allele Name"
        case "typedAllele" => "Typed Allele"
        case "Reads" => "Reads"
        case _ => columnName
      }
      ColumnOptions.field(columnName).title(title).sortable(true)
    }
    val options = TableOptions.columns(columns)
    jq.bootstrapTable(options)
  }

  def fillBasicData(idStr: String)(f: () => js.Any = () => ()) = {
    val id = "basicInfo"
    val url = g.jsRoutes.controllers.SampleController.getBasicData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        val first1Height = 30
        val row1 = List("Interlocus Balance", "Single Source", "Total Depth", "Available Depth")
        val row2 = List("iSNP Loci Typed", "Autosomal Loci Typed", "X Loci Typed", "Y Loci Typed")
        val row3 = List("STR Average Depth", "STR Depth STD", "STR Num. of Depth < 30", "STR Num. of Depth < 100")
        val row4 = List("iSNP Average Depth", "iSNP Depth STD")

        def getRow(rows: List[String], dict: js.Dictionary[String]) = {
          rows.map { key =>
            val value = dict(key)
            div(cls := "form-group", marginRight := 15, height := 60, marginBottom := 20,
              table(cls := " table table-bordered strTable", width := 200,
                tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
                  td(colspan := 2, key, fontWeight := "bold", backgroundColor := "#5E738B", paddingTop := 3, paddingBottom := 3)
                ),
                tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
                  td(value, width := "50%", paddingTop := 3, paddingBottom := 3)
                ),
              )
            )
          }.mkString("&nbsp;")
        }

        val html = rs.map { case dict =>
          div(
            all.raw(getRow(row1, dict)),
            br,
            all.raw(getRow(row2, dict)),
            br,
            all.raw(getRow(row3, dict)),
            br,
            all.raw(getRow(row4, dict))
          )
        }.mkString("&nbsp;")
        $(s"#${id}").html(html)
        f()
      }
    $.ajax(ajaxSettings)
  }

  def getStrReadsDatas(rs: js.Array[js.Dictionary[String]]) = {
    JSON.stringify(rs).decodeOption[List[StrReadsData]].getOrElse(Nil)
  }

  def getSeqDatas(rs: js.Array[js.Dictionary[String]]) = {
    JSON.stringify(rs).decodeOption[List[SeqData]].getOrElse(Nil)
  }

  @JSExport("strDetailShow")
  def strDetailShow(filterRs: js.Array[js.Dictionary[String]]) = {
    val seqDatas = getSeqDatas(filterRs)
    val locus = seqDatas.head.locus
    val modalId = "locusModal"
    $(".locus").text(locus)
    refreshTable(filterRs, $(s"#${modalId} #detailTable"))
    strDetailPlot(seqDatas)
    jQuery(s"#${modalId}").modal("show")
  }

  @JSExport("snpDetailShow")
  def snpDetailShow(filterRs: js.Array[js.Dictionary[String]]) = {
    val seqDatas = getSeqDatas(filterRs)
    val locus = seqDatas.head.locus
    $(".locus").text(locus)
    val modalId = "snpModal"
    refreshTable(filterRs, $(s"#${modalId} #detailTable"))
    SampleSnpDetail.snpDetailPlot(seqDatas)
    jQuery(s"#${modalId}").modal("show")
  }

  def fillReadsData(datas: List[StrReadsData], seqDatas: List[SeqData], jq: JQuery) = {
    case class GenotypeData(genotype: String, reads: String)
    val genotypeMap = datas.map { x =>
      (x.locus, GenotypeData(x.genotype, x.reads))
    }.toList.groupSeqMap
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
              a(key, color := "#FFFFFF", onclick := s"SampleDetail.strDetailShow(${Tool.stringify(seqDataRow.asJson)})"),
              fontWeight := "bold", backgroundColor := "#5E738B", paddingTop := 3, paddingBottom := 3)
          ),
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              backgroundColor := myBackgroundColor, genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          } else {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
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

  def getGenotypeCol(genoType: String) = {
    genoType match {
      case "A" => "#FABD82"
      case "T" => "#3B3E42"
      case "C" => "#7D8246"
      case "G" => "#843D3D"
    }
  }

  def statPlot(statDatas: List[StatData])(f: () => js.Any = () => ()) = {
    val chartId = "statChart"
    val xs = statDatas.map(_.column)
    val as = statDatas.map(_.aCount)
    val ts = statDatas.map(_.tCount)
    val cs = statDatas.map(_.cCount)
    val gs = statDatas.map(_.gCount)
    val ns = statDatas.map(_.nCount)
    case class PlotData(name: String, data: List[String])
    val binWidth = 1
    val plotData = List(PlotData("A", as), PlotData("T", ts), PlotData("C", cs), PlotData("G", gs), PlotData("N", ns)).
      reverse.map { x =>
      val texts = xs.zip(x.data).map { case (xPos, yPos) =>
        s"${x.name}<br>Pos:${xPos}<br>Count:${yPos}"
      }
      Bar(
        x = xs,
        y = x.data,
        marker = Marker(
        ),
        width = binWidth,
        text = texts,
        hoverinfo = HoverInfo.Text,
        name = x.name,
      )
    }

    val tickVals = (1 :: (0 to 400 by 50).toList).map(_.toDouble)
    val layout = Layout(
      title = s"",
      xaxis = Axis(
        title = s"Read Position",
        `type` = AxisType.Linear,
        tickangle = 0.0,
        autorange = true,
        tickfont = Font(size = 10),
        tickvals = tickVals,
        ticktext = tickVals,
      ),
      yaxis = Axis(
        title = s"Number of reads",
        autorange = true
      ),
      margin = Margin(b = 40, l = 60, r = 0, t = 0),
      hovermode = Closest,
      barmode = BarMode.Stack,
      width = 550,
      height = 400
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(chartId, plotData, layout, config)
    f()
  }

  def boxPlot(statDatas: List[StatData])(f: () => js.Any = () => ()) = {
    val chartId = "boxPlotChart"
    val xs = statDatas.map(_.column)
    val binWidth = 1
    val q1s = statDatas.map(_.q1)
    val meds = statDatas.map(_.med)
    val means = statDatas.map(_.mean)
    val q3s = statDatas.map(_.q3)
    case class MyData(name: String, data: List[String])
    val plotData = List(MyData("Q1", q1s), MyData("Median", meds), MyData("Mean", means), MyData("Q3", q3s)).reverse.map { x =>
      val texts = xs.zip(x.data).map { case (xPos, yPos) =>
        s"${x.name}<br>Pos:${xPos}<br>Quality:${yPos}"
      }
      Scatter(
        values = xs,
        secondValues = x.data,
        name = x.name,
        marker = Marker(
        ),
        text = texts,
        hoverinfo = HoverInfo.Text,
      )
    }
    val layout = Layout(
      title = s"",
      xaxis = Axis(
        title = s"Read Position",
        `type` = AxisType.Linear,
        tickangle = 0.0,
        autorange = true,
        tickfont = Font(size = 10),
      ),
      yaxis = Axis(
        title = s"Sequencing Qualitiy",
        autorange = true
      ),
      margin = Margin(b = 40, r = 0, t = 0),
      hovermode = Closest,
      width = 550,
      height = 400
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(chartId, plotData, layout, config)
    f()
  }

  def strDetailPlot(strSeqDatas: List[SeqData]) = {
    val chartId = "strDetailChart"
    val xs = strSeqDatas.map(_.genotype)
    val reads = strSeqDatas.map(_.reads)
    val plotData = Seq(
      Bar(
        x = xs,
        y = reads,
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
      margin = Margin(b = 20,
        t = 20,
        r = 0),
      width = 200,
      height = 300,
      hovermode = Closest
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(s"${
      chartId
    }", plotData, layout, config)
  }

  def getReadsData(idStr: String)(f: List[StrReadsData] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getReadsData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${
      url
    }?id=${
      idStr
    }").`type`("get").
      success {
        (data: js.Any, status: String, e: JQueryXHR) =>
          val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
          val readsDatas = getStrReadsDatas(rs)
          f(readsDatas)
      }
    $.ajax(ajaxSettings)
  }

  def getStatData(idStr: String)(f: List[StatData] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getStatData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${
      url
    }?id=${
      idStr
    }").`type`("get").
      success {
        (data: js.Any, status: String, e: JQueryXHR) =>
          val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
          val statDatas = JSON.stringify(rs).decodeOption[List[StatData]].getOrElse(Nil)
          f(statDatas)
      }
    $.ajax(ajaxSettings)
  }

  def autoShow(datas: List[StrReadsData], sepDatas: List[SeqData]) = {
    val filterDatas = datas.filter(_.kind == "Autosomal")
    val parentId = "autoStr"
    barPlot(filterDatas, $(s"#${
      parentId
    } #barChart"))
    scatterPlot(filterDatas, $(s"#${
      parentId
    } #scatterChart"))
    fillReadsData(filterDatas, sepDatas, $(s"#${
      parentId
    } #data"))
    val ts = filterDatas.map {
      x =>
        (x.locus, x.genotype)
    }
    val filterSeqData = sepDatas.filter {
      x =>
        val t = (x.locus, x.genotype)
        ts.contains(t)
    }
    val jq = $("#autoStr #seqTable")
    refreshTable(Tool.stripNulls(filterSeqData.asJson), jq)

  }

  def yShow(datas: List[StrReadsData], seqDatas: List[SeqData]) = {
    val filterDatas = datas.filter(_.kind == "Y")
    val parentId = "yStr"
    barPlot(filterDatas, $(s"#${
      parentId
    } #barChart"))
    scatterPlot(filterDatas, $(s"#${
      parentId
    } #scatterChart"))
    fillReadsData(filterDatas, seqDatas, $(s"#${
      parentId
    } #data"))
    val ts = filterDatas.map { x =>
      (x.locus, x.genotype)
    }
    val filterSeqData = seqDatas.filter { x =>
      val t = (x.locus, x.genotype)
      ts.contains(t)
    }
    val jq = $("#yStr #seqTable")
    refreshTable(Tool.stripNulls(filterSeqData.asJson), jq)

  }

  def xShow(datas: List[StrReadsData], seqDatas: List[SeqData]) = {
    val filterDatas = datas.filter(_.kind == "X")
    val parentId = "xStr"
    barPlot(filterDatas, $(s"#${
      parentId
    } #barChart"))
    scatterPlot(filterDatas, $(s"#${
      parentId
    } #scatterChart"))
    fillReadsData(filterDatas, seqDatas, $(s"#${
      parentId
    } #data"))
    val ts = filterDatas.map { x =>
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
    refreshTableByJson(filterSeqData.asJson, jq)

  }

  def barPlot(filterDatas: List[StrReadsData], jq: JQuery) = {
    val xs = filterDatas.map(x => x.genotype + x.locus).indices
    val reads = filterDatas.map(x => x.reads)
    val dbColors = List("#A01DDD", "#FFA142")
    val locuss = filterDatas.map(_.locus)
    val distLocus = locuss.distinct
    val locusIndex = distLocus.map { x =>
      locuss.indexOf(x)
    }
    val genotypes = filterDatas.map(_.genotype)
    val sizeMap = filterDatas.map { x =>
      (x.locus, x.genotype)
    }.groupMap(_._1)(_._2).view.mapValues(_.size).toMap
    val locusGenotypesMap = filterDatas.map { x =>
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
        xs,
        reads,
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
          font = Font(size = 12)
        )
    }
    val layout = Layout(
      title = s"",
      xaxis = Axis(
        title = s"",
        `type` = AxisType.Linear,
        tickvals = xs,
        ticktext = genotypes,
        tickangle = 0.0,
        autorange = true,
        tickfont = Font(size = 10)
      ),
      yaxis = Axis(
        title = s"Number of reads",
        autorange = true
      ),
      annotations = annotations,
      margin = Margin(b = 120),
      hovermode = Closest,
      width = 1000
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(jq, plotData, layout, config)
  }

  def getSeqData(idStr: String)(f: List[SeqData] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getSeqData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${
      url
    }?id=${
      idStr
    }").contentType("application/json").
      `type`("get").success {
      (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        val seqDatas = getSeqDatas(rs)
        f(seqDatas)
    }
    $.ajax(ajaxSettings)
  }

  def getSnpSeqData(idStr: String)(f: js.Array[js.Dictionary[String]] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getSnpSeqData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${
      url
    }?id=${
      idStr
    }").contentType("application/json").
      `type`("get").success {
      (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        f(rs)
    }
    $.ajax(ajaxSettings)
  }

  @JSExport("showDetail")
  def showDetail(idStr: String) = {
    $("#result").hide()
    val index = layer.load(1, Tool.layerOptions.shade(js.Array("0.1", "#fff")))
    $(".id").text(idStr)
    fillBasicData(idStr) { () =>
      getStatData(idStr) {
        statDatas =>
          statPlot(statDatas)()
          boxPlot(statDatas)()

          getReadsData(idStr) { readsDatas =>
            getSeqData(idStr) {
              seqDatas =>
                autoShow(readsDatas, seqDatas)
                yShow(readsDatas, seqDatas)
                xShow(readsDatas, seqDatas)

                SampleSnpDetail.getSnpReadsData(idStr) {
                  snpReadsData =>
                    getSnpSeqData(idStr) {
                      snpSeqData =>
                        SampleSnpDetail.snpShow(snpReadsData, snpSeqData)
                        $("#result").show()
                        layer.close(index)
                        val target_top = $("#result").offset().top
                        jQuery("html,body").animate(js.Dictionary("scrollTop" -> target_top), JQueryAnimationSettings.duration(800))
                    }
                }
            }
          }
      }


    }

  }

  def scatterPlot(datas: List[ReadsData], jq: JQuery) = {
    val xs = datas.map(_.productSize)
    val reads = datas.map(_.reads)
    val texts = datas.map { x =>
      s"Locus:${
        x.locus
      }<br>Allele Name:${
        x.genotype
      }<br>Length(bp):${
        x.productSize
      }<br>Depth:${
        x.reads
      }"
    }

    val plotData = Seq(
      Scatter(
        xs,
        reads,
        marker = Marker(
        ),
        mode = ScatterMode(Markers),
        hoverinfo = HoverInfo.Text,
        text = texts,
      )
    )
    val layout = Layout(
      title = s"",
      xaxis = Axis(
        title = s"Length(bp)",
        `type` = AxisType.Linear,
        tickangle = 0.0,
        autorange = true,
        tickfont = Font(size = 10)
      ),
      yaxis = Axis(
        title = s"Number of reads",
        autorange = true
      ),
      margin = Margin(b = 40, l = 80, t = 0, r = 0),
      hovermode = Closest,
      width = 700,
      height = 500
    )
    val config = PlotlyConfigOptions.displayModeBar(false)
    myPlotly.newPlot(jq, plotData, layout, config)
  }

}
