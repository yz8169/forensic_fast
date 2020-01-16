package test

import java.io.File

import dataclass.since
import utils.Utils
import implicits.Implicits._
import org.apache.commons.io.FileUtils
import tool.Tool
import implicits.Implicits._
import shapeless._
import argonaut._
import Argonaut._
import ArgonautShapeless._
import argonaut.derive._

import scala.xml.XML

/**
 * Created by Administrator on 2019/12/6
 */
object Test {

  import dataclass.data

  case class Employee(name: String, number: Int, manager: Boolean)

  case class IceCream(name: String, numCherries: Int, inCone: Boolean)

  def employeeCsv(e: Employee): List[String] =
    List(e.name, e.number.toString, e.manager.toString)

  def iceCreamCsv(c: IceCream): List[String] =
    List(c.name, c.numCherries.toString, c.inCone.toString)

  val genericEmployee = Generic[Employee].to(Employee("Dave", 123, false))

  val genericIceCream = Generic[IceCream].to(IceCream("Sundae", 1, false))

  def genericCsv(gen: String :: Int :: Boolean :: HNil): List[String] =
    List(gen(0), gen(1).toString, gen(2).toString)

  def main(args: Array[String]): Unit = {

    val repr = "Hello" :: 123 :: true :: HNil
    println(repr)

    implicit def alwaysIncludeCodecFor[T]: derive.JsonProductCodecFor[T] =
      derive.JsonProductCodecFor.alwaysIncludeDefaultValue

    case class Identity(firstName: String, lastName: String)

    case class CC(i: Int = 4, s: String = "foo")

    implicit def typeFieldJsonSumCodecFor[S]: JsonSumCodecFor[S] =
      JsonSumCodecFor(JsonSumCodec.typeField)

    case class Custom(s: String)

    object Custom {
      implicit def encode: EncodeJson[Custom] =
        EncodeJson.of[String].contramap[Custom](_.s)

      implicit def decode: DecodeJson[Custom] =
        DecodeJson.of[String].map(Custom(_))
    }

    @JsonCodec sealed trait ADT
    case class First(i: Int) extends ADT
    case class Second(s: String) extends ADT
    object ADT // this one's required


    println(Custom("a").asJson.nospaces)







    //    val dir = new File("D:\\forensic_database\\user\\13\\sample\\584")

    //    Tool.produceReadsFile(dir)

    //    Tool.produceSnpReadsFile(dir)

    //    Tool.produceSeqFile(dir)

    //    Tool.produceSnpSeqFile(dir)


  }

}
