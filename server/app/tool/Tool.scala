package tool

import java.io.File
import java.nio.file.Files
import java.util.concurrent.ForkJoinPool

import dao.ModeDao
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MultipartFormData, Request}
import tool.Pojo.{CommandData, IndexData, MissionData, MyDataDir}
import utils.Utils

import scala.collection.JavaConverters._
import implicits.Implicits._
import play.api.libs.json.Json

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.CollectionConverters._
import scala.xml.{Utility, XML}
import scala.xml
import scala.xml.Elem._
import shared.VarTool._


/**
 * Created by Administrator on 2019/12/5
 */
object Tool {

  val dbName = "forensic_database"
  val windowsPath = s"D:\\${dbName}"
  val playPath = new File("../").getAbsolutePath
  val linuxPath = playPath + s"/${dbName}"
  val isWindows = {
    if (new File(windowsPath).exists()) true else false
  }
  val path = {
    if (new File(windowsPath).exists()) windowsPath else linuxPath
  }
  val windowsTestDir = new File("G:\\temp")
  val linuxTestDir = new File(playPath, "workspace")
  val testDir = if (windowsTestDir.exists()) windowsTestDir else linuxTestDir
  val exampleDir = new File(path, "example")
  val userDir = new File(path, "user")
  val binDir = new File(path, "bin")
  val fastxBinDir = new File(binDir, "fastx_toolkit/bin")

  val jarPath = {
    val inPath = "C:\\workspaceForIDEA\\forensic\\server\\jars"
    val linuxInPath = linuxPath + "/jars"
    if (new File(inPath).exists()) inPath else linuxInPath
  }

  def getInfoByFile(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val columnNames = lines.head.split("\t").drop(1)
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t")
      val map = Map("geneId" -> columns(0))
      val otherMap = columnNames.zip(columns.drop(1)).map { case (columnName, data) =>
        (columnName -> data)
      }.toMap
      map ++ otherMap
    }
    (columnNames, array)
  }

  def generateMissionName = {
    (new DateTime).toString("yyyy_MM_dd_HH_mm_ss")
  }

  def createTempDirectory(prefix: String)(implicit modeDao: ModeDao) = {
    if (isTestMode) Tool.testDir else Files.createTempDirectory(prefix).toFile
  }

  def isTestMode(implicit modeDao: ModeDao) = {
    val mode = Utils.execFuture(modeDao.select)
    if (mode.test == "t") true else false
  }

  def getUserIdDir(userId: Int) = {
    new File(Tool.userDir, userId.toString)
  }

  def deleteDirectory(direcotry: File)(implicit modeDao: ModeDao) = {
    if (!isTestMode) Utils.deleteDirectory(direcotry)
  }

  def getUserMissionDir(userId: Int) = {
    val userIdDir = getUserIdDir(userId)
    new File(userIdDir, "mission")
  }

  def getDataDir(dataDir: File)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    val fqFile = WebTool.fileMoveDir("fqFile", dataDir)
    MyDataDir(dataDir, fqFile)
  }

  def getLogFile(dir: File) = {
    val file = new File(dir, "log.txt")
    "Run successfully!".toFile(file)
    file
  }

  def getInputXmlFile(dir: File) = {
    new File(dir, "input.xml")
  }

  def getInputTxtFile(dir: File) = {
    new File(dir, "input.txt")
  }

  def getOutputXmlFile(dir: File) = {
    new File(dir, "output.xml")
  }

  def fy(tmpDir: File) = {
    val inputFile = Tool.getInputXmlFile(tmpDir)
    val inputFaFile = Tool.getInputTxtFile(tmpDir)
    val outFile = Tool.getOutputXmlFile(tmpDir)
    println(Tool.jarPath)
    val command =
      s"""
         |${Tool.fastxBinDir.unixPath}/fastx_quality_stats -i ${inputFaFile.unixPath} -o stat.txt -Q 33
         |java -jar  ${new File(Tool.jarPath, "fy-1.0-SNAPSHOT.jar").unixPath} fastqToXml -i ${inputFile.getName} -o ${outFile.unixPath}
           """.stripMargin
    CommandData(tmpDir, List(command))
  }

  def getConfigFile(workspaceDir: File) = {
    new File(workspaceDir, "config.json")
  }

  def produceConfigFile(missionData: MissionData, workspaceDir: File) = {
    val configFile = getConfigFile(workspaceDir)
    val json = WebTool.getJsonByT(missionData)
    Json.stringify(json).toFile(configFile)
    configFile
  }

  def productInputXmlFile(workspaceDir: File, data: MissionData) = {
    val fqFile = Tool.getInputTxtFile(workspaceDir)
    val xml =
      s"""
         | <data>
         |        <sample>
         |          <basicInfo>
         |            <Lane>${data.lane}</Lane>
         |            <id>${data.id}</id>
         |            <gender>${data.gender}</gender>
         |            <type>${data.kind}</type>
         |            <fq>${fqFile.unixPath}</fq>
         |          </basicInfo>
         |        </sample>
         |      </data>
         |""".stripMargin

    val inputXmlFile = Tool.getInputXmlFile(workspaceDir)
    xml.toFile(inputXmlFile)
  }

  def produceReadsFile(dir: File) = {
    val xmlFile = new File(dir, "output.xml")
    case class StrSite(locus: String, productSize: String, genoType: String, reads: String)
    val xml = XML.loadFile(xmlFile)
    val strSites = (xml \\ "strSites" \\ "site").toList.map { node =>
      val locus = (node \ "@locus").text
      val productSize = (node \ "@product_size").text
      val genoType = (node \ "Genotype").text
      val reads = (node \ "Reads").text.toDouble.toInt
      StrSite(locus, productSize, genoType, reads.toString)
    }
    val strMap = strSites.map { strSite =>
      val t = (strSite.locus, strSite.genoType)
      (t, strSite.reads)
    }.toMap
    case class StrData(key: (String, String), kind: String)
    val autoStrs = (xml \\ "Autosomal_STR" \\ "autoStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        val t = (locus, node.text)
        StrData(t, "Autosomal")
      }
    }
    val yStrs = (xml \\ "Y_STR" \\ "yStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        val t = (locus, node.text)
        StrData(t, "Y")
      }
    }

    val xStrs = (xml \\ "X_STR" \\ "xStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        val t = (locus, node.text)
        StrData(t, "X")
      }
    }

    val strSiteMap = strSites.map { x =>
      val t = (x.locus, x.genoType)
      (t, x)
    }.toMap

    val locusMap = (xml \\ "Autosomal_STR" \\ "autoStr").toList.map { autoNode =>
      val locus = (autoNode \ "@Locus").text
      val qc = (autoNode \\ "QC").map { node =>
        node.text
      }.mkString(";")
      (locus, qc)
    }.toMap
    val strs = autoStrs ::: yStrs ::: xStrs

    val newLines = List(locusStr, genotypeStr, "Reads", "Qc", "ProductSize", "Kind") :: strs.map { str =>
      val t = str.key
      val autoStr = strSiteMap(t)
      List(t._1, t._2, strMap(t), locusMap.getOrElse(t._1, ""), autoStr.productSize, str.kind)
    }
    newLines.toTxtFile(Tool.getReadsFile(dir))
  }

  def produceSeqFile(dir: File) = {
    val xmlFile = new File(dir, "output.xml")
    case class StrSite(locus: String, genoType: String, typed: String, reads: String, repeatSeq: String)
    val xml = XML.loadFile(xmlFile)
    val autoStrs = (xml \\ "Autosomal_STR" \\ "autoStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        (locus, node.text)
      }
    }
    val yStrs = (xml \\ "Y_STR" \\ "yStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        (locus, node.text)
      }
    }

    val xStrs = (xml \\ "X_STR" \\ "xStr").toList.flatMap { autoNode =>
      val locus = (autoNode \ "@Locus").text
      (autoNode \\ "Genotype").map { node =>
        (locus, node.text)
      }
    }

    val strSiteMap = (xml \\ "strSites" \\ "site").toList.map { node =>
      val locus = (node \ "@locus").text
      val genoType = (node \ "Genotype").text
      val reads = (node \ "Reads").text.toDouble.toInt
      val typed = (node \ "Typed").text
      val repeatSeq = (node \ "RepeatSequence").text
      StrSite(locus, genoType, typed, reads.toString, repeatSeq)
    }.map { x =>
      val t = (x.locus, x.genoType)
      (t, x)
    }.toMap
    val strs = autoStrs ::: yStrs ::: xStrs
    val newLines = List(locusStr, genotypeStr, typedAlleleStr, readsStr, repeatSeqStr) :: strs.map { t =>
      val row = strSiteMap(t)
      List(row.locus, row.genoType, row.typed, row.reads, row.repeatSeq)
    }
    newLines.toTxtFile(Tool.getSeqFile(dir))
  }

  def getReadsFile(dir: File) = {
    new File(dir, "reads.txt")
  }

  def getSeqFile(dir: File) = {
    new File(dir, "seq.txt")
  }

  def getBasicFile(dir: File) = {
    new File(dir, "basic.txt")
  }

  def getStatFile(dir: File) = {
    new File(dir, "stat.txt")
  }


}
