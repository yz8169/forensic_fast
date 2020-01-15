package myJs.user

import myJs.Tool
import myJs.Utils._
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{ColumnOptions, PlotlyConfigOptions, Swal, SwalOptions, TableOptions, myPlotly}
import shared.plotly.{Bar, Box, Pie, Scatter}
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

/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("SampleDetail")
object SampleDetail {

  val typeTagEmpty = List[TypedTag[String]]()

  @JSExport("init")
  def init = {
    autoInit
    yInit
    xInit
    snpInit
    initStrTable
  }

  def autoInit = {
    val jq = $("#autoStr #seqTable")
    initTable(jq)
  }

  def yInit = {
    val jq = $("#yStr #seqTable")
    initTable(jq)
  }

  def xInit = {
    val jq = $("#xStr #seqTable")
    initTable(jq)
  }

  def snpInit = {
    val jq = $("#snp #seqTable")
    initSnpSeqTable(jq)
    initSnpDetailTable
  }

  def plotlyAutoScale = {
    window.addEventListener("resize", (ev: Event) => {
      myPlotly.resize
    })
  }

  def refreshTable(data: js.Array[js.Dictionary[String]], jq: JQuery, f: () => js.Any = () => ()) = {
    jq.bootstrapTable("load", data)
    f()
  }

  def refreshDetailTable(filterRs: js.Array[js.Dictionary[String]], jq: JQuery, f: () => js.Any = () => ()) = {
    jq.bootstrapTable("load", filterRs)
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

  def initTable(jq: JQuery) = {
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
        val first2Height = 40
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


        val keys = List("Total Depth", "Available Depth", "Interlocus Balance", "Single Source", "iSNP Loci Typed",
          "Autosomal Loci Typed", "X Loci Typed",
          "STR Average Depth", "STR Depth STD", "STR Num. of Depth < 30", "STR Num. of Depth < 100",
          "iSNP Average Depth", "iSNP Depth STD")
        val html =
          rs.map { case dict =>
            val t = getRow(row1, dict)

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

  @JSExport("strDetailShow")
  def strDetailShow(filterRs: js.Array[js.Dictionary[String]]) = {
    val locus = filterRs.map { dict =>
      dict(locusStr)
    }.head
    val modalId = "locusModal"
    $(".locus").text(locus)
    refreshDetailTable(filterRs, $(s"#${modalId} #detailTable"))
    strDetailPlot(filterRs)

    jQuery(s"#${modalId}").modal("show")
  }

  @JSExport("snpDetailShow")
  def snpDetailShow(filterRs: js.Array[js.Dictionary[String]]) = {
    val locus = filterRs.map { dict =>
      dict(locusStr)
    }.head
    $(".locus").text(locus)
    val modalId = "snpModal"
    refreshDetailTable(filterRs, $(s"#${modalId} #detailTable"))
    snpDetailPlot(filterRs)

    jQuery(s"#${modalId}").modal("show")
  }

  def fillReadsData(rs: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]], jq: JQuery) = {
    val strMap = rs.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.toList.toSeqMap
    case class GenotypeData(genotype: String, reads: String)
    val genotypeMap = rs.map { dict =>
      (dict("Locus"), GenotypeData(dict("GenoType"), dict("Reads")))
    }.toList.groupSeqMap
    val qcMap = rs.map { dict =>
      (dict("Locus"), dict.getOrElse(qcStr, ""))
    }.filter { x => x._2 != "" }.toMap
    val first2Height = 40
    val first1Height = 30
    val html = genotypeMap.map { case (key, genotypes) =>
      val hasQc = qcMap.get(key).isDefined
      val myBackgroundColor = "#FABD82"
      val seqDataRow = seqData.filter { dict =>
        dict(locusStr) == key
      }
      div(cls := "form-group", marginRight := 15, height := 120, marginBottom := 20,
        table(cls := " table table-bordered strTable", width := 150,
          tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
            td(colspan := 2,
              a(key, color := "#FFFFFF", onclick := s"SampleDetail.strDetailShow(${JSON.stringify(seqDataRow)})"), fontWeight := "bold", backgroundColor := "#5E738B", paddingTop := 3, paddingBottom := 3)
          ),
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height, backgroundColor := myBackgroundColor,
              genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          } else {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          },
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height, backgroundColor := myBackgroundColor,
              genotypes.map { inData =>
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

  def fillSnpReadsData(rs: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]], jq: JQuery) = {
    val strMap = rs.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.toList.toSeqMap
    case class GenotypeData(genotype: String, reads: String)
    val genotypeMap = rs.map { dict =>
      (dict("Locus"), GenotypeData(dict("GenoType"), dict("Reads")))
    }.toList.groupSeqMap
    val qcMap = rs.map { dict =>
      (dict("Locus"), dict.getOrElse(qcStr, ""))
    }.filter { x => x._2 != "" }.toMap
    val first2Height = 40
    val first1Height = 30
    val html = genotypeMap.map { case (key, genotypes) =>
      val hasQc = qcMap.get(key).isDefined
      val myBackgroundColor = "#FABD82"
      val seqDataRow = seqData.filter { dict =>
        dict(locusStr) == key
      }
      div(cls := "form-group", marginRight := 15, height := 120, marginBottom := 20,
        table(cls := " table table-bordered strTable", width := 150,
          tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
            td(colspan := 2,
              a(key, color := "#FFFFFF", onclick := s"SampleDetail.snpDetailShow(${JSON.stringify(seqDataRow)})"), fontWeight := "bold", backgroundColor := "#5E738B", paddingTop := 3, paddingBottom := 3)
          ),
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height, backgroundColor := myBackgroundColor,
              genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          } else {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height,
              genotypes.map { inData =>
                td(inData.genotype, width := "50%", paddingTop := 3, paddingBottom := 3)
              })
          },
          if (hasQc) {
            tr(height := first1Height, maxHeight := first1Height, minHeight := first1Height, backgroundColor := myBackgroundColor,
              genotypes.map { inData =>
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

  //  def snpBarPlot(rs: js.Array[js.Dictionary[String]], jq: JQuery) = {
  //    case class PlotData(locus: String, aNum: String, tNum: String, cNum: String, gNum: String)
  //    val plotDatas = rs.map { dict =>
  //      (dict(locusStr), dict)
  //    }.toList.groupSeqMap.map { case (locus, dicts) =>
  //      val nums = List("A", "T", "C", "G").map { x =>
  //        dicts.filter { dict =>
  //          dict(genotypeStr) == x
  //        }.headOption.map { dict =>
  //          dict(readsStr)
  //        }.getOrElse("0")
  //      }
  //      PlotData(locus, nums(0), nums(1), nums(2), nums(3))
  //    }
  //    val as = plotDatas.map { x =>
  //      x.aNum
  //    }.toList
  //    val ts = plotDatas.map { x =>
  //      x.tNum
  //    }.toList
  //    val cs = plotDatas.map { x =>
  //      x.cNum
  //    }.toList
  //    val gs = plotDatas.map { x =>
  //      x.gNum
  //    }.toList
  //    val xs = plotDatas.map { x =>
  //      x.locus
  //    }.toList
  //    case class PlotKindData(name: String, data: List[String])
  //    val binWidth = 1
  //    val plotData = List(PlotKindData("A", as), PlotKindData("T", ts), PlotKindData("C", cs), PlotKindData("G", gs)).reverse.map { x =>
  //      val texts = xs.zip(x.data).map { case (xPos, yPos) =>
  //        s"${x.name}<br>Pos:${xPos}<br>Count:${yPos}"
  //      }
  //      Bar(
  //        xs,
  //        x.data,
  //        marker = Marker(
  //        ),
  //        width = binWidth,
  //        text = texts,
  //        hoverinfo = HoverInfo.Text,
  //        name = x.name,
  //      )
  //    }
  //
  //    val tickVals = (1 :: (0 to 400 by 50).toList).map(_.toDouble)
  //    val layout = Layout(
  //      title = s"",
  //      xaxis = Axis(
  //        title = s"Read Position",
  //        `type` = AxisType.Category,
  //        tickangle = 0.0,
  //        autorange = true,
  //        tickfont = Font(size = 10),
  //        tickvals = Doubles(tickVals),
  //        ticktext = Strings(tickVals.map(_.toString)),
  //      ),
  //      yaxis = Axis(
  //        title = s"Number of reads",
  //        autorange = true
  //      ),
  //      margin = Margin(b = 40, l = 60, r = 0, t = 0),
  //      hovermode = Closest,
  //      barmode = BarMode.Stack,
  //      width = 1000
  //    )
  //    val config = PlotlyConfigOptions.displayModeBar(false)
  //    myPlotly.newPlot(jq, plotData, layout, config)
  //  }


  def statPlot(idStr: String)(f: () => js.Any = () => ()) = {
    val chartId = "statChart"
    val url = g.jsRoutes.controllers.SampleController.getStatData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        $(".id").text(idStr)
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        val xs = rs.map { dict =>
          dict("column")
        }.toList
        val as = rs.map { dict =>
          dict("A_Count")
        }.toList
        val ts = rs.map { dict =>
          dict("T_Count")
        }.toList
        val cs = rs.map { dict =>
          dict("C_Count")
        }.toList
        val gs = rs.map { dict =>
          dict("G_Count")
        }.toList
        val ns = rs.map { dict =>
          dict("N_Count")
        }.toList
        case class PlotData(name: String, data: List[String])
        val binWidth = 1
        val plotData = List(PlotData("A", as), PlotData("T", ts), PlotData("C", cs), PlotData("G", gs), PlotData("N", ns)).reverse.map { x =>
          val texts = xs.zip(x.data).map { case (xPos, yPos) =>
            s"${x.name}<br>Pos:${xPos}<br>Count:${yPos}"
          }
          Bar(
            xs,
            x.data,
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
            tickvals = Doubles(tickVals),
            ticktext = Strings(tickVals.map(_.toString)),
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
    $.ajax(ajaxSettings)

  }

  def boxPlot(idStr: String)(f: () => js.Any = () => ()) = {
    val chartId = "boxPlotChart"
    val url = g.jsRoutes.controllers.SampleController.getStatData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        val xs = rs.map { dict =>
          dict("column")
        }.toList
        val binWidth = 1
        val q1s = rs.map { dict =>
          dict("Q1")
        }.toList
        val meds = rs.map { dict =>
          dict("med")
        }.toList
        val means = rs.map { dict =>
          dict("mean")
        }.toList
        val q3s = rs.map { dict =>
          dict("Q3")
        }.toList
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
          barmode = BarMode.Stack,
          width = 550,
          height = 400
        )
        val config = PlotlyConfigOptions.displayModeBar(false)
        myPlotly.newPlot(chartId, plotData, layout, config)
        f()
      }
    $.ajax(ajaxSettings)

  }

  def strDetailPlot(filterRs: js.Array[js.Dictionary[String]]) = {
    val chartId = "strDetailChart"
    val xs = filterRs.map { dict =>
      dict(genotypeStr)
    }.toList
    val reads = filterRs.map { dict =>
      dict(readsStr)
    }.toList
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
    myPlotly.newPlot(s"${chartId}", plotData, layout, config)

  }

  def snpDetailPlot(filterRs: js.Array[js.Dictionary[String]]) = {
    val xs = filterRs.map { dict =>
      dict(genotypeStr)
    }.toList
    val reads = filterRs.map { dict =>
      dict(readsStr)
    }.toList
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

  def getReadsData(idStr: String)(f: js.Array[js.Dictionary[String]] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getReadsData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        f(rs)
      }
    $.ajax(ajaxSettings)
  }

  def getSnpReadsData(idStr: String)(f: js.Array[js.Dictionary[String]] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getSnpReadsData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").`type`("get").
      success { (data: js.Any, status: String, e: JQueryXHR) =>
        val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
        f(rs)
      }
    $.ajax(ajaxSettings)
  }

  def autoShow(data: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]]) = {
    val filterArray = data.filter { dict =>
      dict(kindStr) == "Autosomal"
    }
    val parentId = "autoStr"
    barPlot(filterArray, $(s"#${parentId} #barChart"))
    scatterPlot(filterArray, $(s"#${parentId} #scatterChart"))
    fillReadsData(filterArray, seqData, $(s"#${parentId} #data"))
    val ts = filterArray.map { dict =>
      (dict(locusStr), dict(genotypeStr))
    }
    val filterSeqData = seqData.filter { dict =>
      val t = (dict(locusStr), dict(genotypeStr))
      ts.contains(t)
    }
    val jq = $("#autoStr #seqTable")
    refreshTable(filterSeqData, jq)

  }

  def yShow(data: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]]) = {
    val filterArray = data.filter { dict =>
      dict(kindStr) == "Y"
    }
    val parentId = "yStr"
    barPlot(filterArray, $(s"#${parentId} #barChart"))
    scatterPlot(filterArray, $(s"#${parentId} #scatterChart"))
    fillReadsData(filterArray, seqData, $(s"#${parentId} #data"))
    val ts = filterArray.map { dict =>
      (dict(locusStr), dict(genotypeStr))
    }
    val filterSeqData = seqData.filter { dict =>
      val t = (dict(locusStr), dict(genotypeStr))
      ts.contains(t)
    }
    val jq = $("#yStr #seqTable")
    refreshTable(filterSeqData, jq)

  }

  def xShow(data: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]]) = {
    val filterArray = data.filter { dict =>
      dict(kindStr) == "X"
    }
    val parentId = "xStr"
    barPlot(filterArray, $(s"#${parentId} #barChart"))
    scatterPlot(filterArray, $(s"#${parentId} #scatterChart"))
    fillReadsData(filterArray, seqData, $(s"#${parentId} #data"))
    val ts = filterArray.map { dict =>
      (dict(locusStr), dict(genotypeStr))
    }
    val filterSeqData = seqData.filter { dict =>
      val t = (dict(locusStr), dict(genotypeStr))
      ts.contains(t)
    }
    val jq = $(s"#${parentId} #seqTable")
    refreshTable(filterSeqData, jq)

  }

  def snpShow(data: js.Array[js.Dictionary[String]], seqData: js.Array[js.Dictionary[String]]) = {
    val filterArray = data
    val parentId = "snp"
    snpBarPlot(filterArray, $(s"#${parentId} #barChart"))
    scatterPlot(filterArray, $(s"#${parentId} #scatterChart"))
    fillSnpReadsData(filterArray, seqData, $(s"#${parentId} #data"))
    val ts = filterArray.map { dict =>
      (dict(locusStr), dict(genotypeStr))
    }
    val filterSeqData = seqData.filter { dict =>
      val t = (dict(locusStr), dict(genotypeStr))
      ts.contains(t)
    }
    val jq = $(s"#${parentId} #seqTable")
    refreshTable(filterSeqData, jq)

  }

  def barPlot(filterArray: js.Array[js.Dictionary[String]], jq: JQuery) = {
    val map = filterArray.map { dict =>
      (dict("GenoType") + dict("Locus"), dict("Reads"))
    }
    val xs = map.map(_._1).indices
    val reads = map.map(_._2).toList
    val dbColors = List("#A01DDD", "#FFA142")
    val locuss = filterArray.map { dict =>
      (dict("Locus"))
    }.toList
    val distLocus = locuss.distinct
    val locusIndex = distLocus.map { x =>
      locuss.indexOf(x)
    }
    val genotypes = filterArray.map { dict =>
      dict("GenoType")
    }.toList
    val sizeMap = filterArray.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.groupMap(_._1)(_._2).view.mapValues(_.size).toMap
    import shared.implicits.Implicits._
    val locusGenotypesMap = filterArray.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.toList.groupSeqMap
    val colors = locusGenotypesMap.zipWithIndex.flatMap { case (inMap, i) =>
      val colorStr = if (i % 2 == 0) {
        dbColors(0)
      } else dbColors(1)
      val color = new StringColor(colorStr)
      inMap._2.map(x => color)
    }.toList
    val texts = filterArray.map { dict =>
      s"Locus:${dict(locusStr)}<br>Allele Name:${dict(genotypeStr)}<br>Depth:${dict(readsStr)}"
    }.toList
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
    val annotations = locusIndex.zip(distLocus).map { case (index, locus) =>
      val size = sizeMap(locus)
      val x = (index + ((size - 1) / 2.0))
      Annotation(
        x = x,
        y = -0.08,
        showarrow = false,
        text = s"${locus}",
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
        tickvals = Doubles(xs.toList.map(_.toDouble)),
        ticktext = Strings(genotypes),
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

  def snpBarPlot(filterArray: js.Array[js.Dictionary[String]], jq: JQuery) = {
    val map = filterArray.map { dict =>
      (dict("GenoType") + dict("Locus"), dict("Reads"))
    }
    val xs = map.map(_._1).indices
    val reads = map.map(_._2).toList
    val dbColors = List("#A01DDD", "#FFA142")
    val locuss = filterArray.map { dict =>
      (dict("Locus"))
    }.toList
    val distLocus = locuss.distinct
    val locusIndex = distLocus.map { x =>
      locuss.indexOf(x)
    }
    val genotypes = filterArray.map { dict =>
      dict("GenoType")
    }.toList
    val sizeMap = filterArray.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.groupMap(_._1)(_._2).view.mapValues(_.size).toMap
    import shared.implicits.Implicits._
    val locusGenotypesMap = filterArray.map { dict =>
      (dict("Locus"), dict("GenoType"))
    }.toList.groupSeqMap
    val colors = locusGenotypesMap.zipWithIndex.flatMap { case (inMap, i) =>
      val colorStr = if (i % 2 == 0) {
        dbColors(0)
      } else dbColors(1)
      val color = new StringColor(colorStr)
      inMap._2.map(x => color)
    }.toList
    val texts = filterArray.map { dict =>
      s"Locus:${dict(locusStr)}<br>Allele Name:${dict(genotypeStr)}<br>Depth:${dict(readsStr)}"
    }.toList
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
    val annotations = locusIndex.zip(distLocus).map { case (index, locus) =>
      val size = sizeMap(locus)
      val x = (index + ((size - 1) / 2.0))
      Annotation(
        x = x,
        y = -0.08,
        showarrow = false,
        text = s"${locus}",
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

  def getSeqData(idStr: String)(f: js.Array[js.Dictionary[String]] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getSeqData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
      f(rs)
    }
    $.ajax(ajaxSettings)
  }

  def getSnpSeqData(idStr: String)(f: js.Array[js.Dictionary[String]] => js.Any) = {
    val url = g.jsRoutes.controllers.SampleController.getSnpSeqData().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${idStr}").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
      f(rs)
    }
    $.ajax(ajaxSettings)
  }

  @JSExport("showDetail")
  def showDetail(idStr: String) = {
    $("#result").hide()
    val index = layer.load(1, Tool.layerOptions.shade(js.Array("0.1", "#fff")))
    fillBasicData(idStr) { () =>
      statPlot(idStr) { () =>
        boxPlot(idStr) { () =>

          getReadsData(idStr) { readsData =>
            getSeqData(idStr) { seqData =>
              autoShow(readsData, seqData)
              yShow(readsData, seqData)
              xShow(readsData, seqData)

              getSnpReadsData(idStr) { snpReadsData =>
                getSnpSeqData(idStr) { snpSeqData =>
                  snpShow(snpReadsData, snpSeqData)
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

  }

  def autoScatterPlot() = {

  }

  def scatterPlot(rs: js.Array[js.Dictionary[String]], jq: JQuery) = {
    val xs = rs.map { dict =>
      dict("ProductSize")
    }.toList
    val reads = rs.map { dict =>
      dict("Reads")
    }.toList
    val texts = rs.map { dict =>
      s"Locus:${dict(locusStr)}<br>Allele Name:${dict(genotypeStr)}<br>Length(bp):${dict(productSizeStr)}<br>Depth:${dict(readsStr)}"
    }.toList
    import shared.implicits.Implicits._
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
