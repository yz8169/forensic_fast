package test

import java.io.File

import utils.Utils
import implicits.Implicits._
import org.apache.commons.io.FileUtils
import tool.Tool
import implicits.Implicits._

import scala.xml.XML

/**
 * Created by Administrator on 2019/12/6
 */
object Test {

  def main(args: Array[String]): Unit = {

    val dir = new File("D:\\forensic_database\\user\\13\\sample\\584")

    //    val xmlFile = new File(parent, "output.xml")
    //    case class MyData(availableDepth: String, totalDepth: String, interlocusBalance: String, singleSource: String,
    //                      iSNP_Loci_Typed: String, auto_Loci_Typed: String, x_Loci_Typed: String, y_Loci_Typed: String,
    //                      strAvg: String, snpAvg: String,
    //                      strSTD: String, snpSTD: String, strDepthBelow30: String, strDepthBelow100: String)
    //    val xml = XML.loadFile(xmlFile)
    //    val datas = (xml \\ "sample" \\ "calResult").toList.map { node =>
    //      val availableDepth = (node \ "availableDepth").text.toDouble.toInt.toString
    //      val totalDepth = (node \ "totalDepth").text.toDouble.toInt.toString
    //      val interlocusBalance = (node \ "interlocusBalance").text
    //      val singleSource = (node \ "singleSource").text
    //      val iSNP_Loci_Typed = (node \ "iSNP_Loci_Typed").text
    //      val auto_Loci_Typed = (node \ "auto_Loci_Typed").text
    //      val x_Loci_Typed = (node \ "x_Loci_Typed").text
    //      val y_Loci_Typed = (node \ "y_Loci_Typed").text
    //      val strAvg = (node \ "strAvg").text.toDouble.toFixed(2)
    //      val snpAvg = (node \ "snpAvg").text.toDouble.toFixed(2)
    //      val strSTD = (node \ "strSTD").text.toDouble.toFixed(2)
    //      val snpSTD = (node \ "snpSTD").text.toDouble.toFixed(2)
    //      val strDepthBelow30 = (node \ "strDepthBelow30").text.toDouble.toInt.toString
    //      val strDepthBelow100 = (node \ "strDepthBelow100").text.toDouble.toInt.toString
    //      MyData(availableDepth, totalDepth, interlocusBalance, singleSource,
    //        iSNP_Loci_Typed, auto_Loci_Typed, x_Loci_Typed, y_Loci_Typed, strAvg, snpAvg,
    //        strSTD, snpSTD, strDepthBelow30, strDepthBelow100)
    //    }
    //
    //    val newLines = List("Total Depth", "Available Depth", "Interlocus Balance", "Single Source", "iSNP Loci Typed",
    //      "Autosomal Loci Typed", "X Loci Typed", "Y Loci Typed",
    //      "STR Average Depth", "STR Depth STD", "STR Num. of Depth < 30", "STR Num. of Depth < 100",
    //      "iSNP Average Depth", "iSNP Depth STD") :: datas.map { row =>
    //      List(row.totalDepth, row.availableDepth, row.interlocusBalance, row.singleSource,
    //        row.iSNP_Loci_Typed, row.auto_Loci_Typed, row.x_Loci_Typed, row.y_Loci_Typed,
    //        row.strAvg, row.strSTD, row.strDepthBelow30, row.strDepthBelow100,
    //        row.snpAvg, row.snpSTD)
    //    }
    //    newLines.toTxtFile(Tool.getBasicFile(parent))

//    Tool.produceReadsFile(dir)
    Tool.produceSeqFile(dir)


  }

}
