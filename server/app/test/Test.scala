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

//    Tool.produceReadsFile(dir)

    Tool.produceSnpReadsFile(dir)

//    Tool.produceSeqFile(dir)

    Tool.produceSnpSeqFile(dir)


  }

}
