package shared

import argonaut.Argonaut._
import argonaut._
import argonaut.ArgonautShapeless._

/**
 * Created by Administrator on 2020/1/17
 */
object Pojo {

  trait ReadsData {
    val locus: String
    val genotype: String
    val reads: String
    val qc: String
    val productSize: String
  }

  case class StrReadsData(override val locus: String, override val genotype: String, override val reads: String,
                          override val qc: String, override val productSize: String, kind: String) extends
    ReadsData

  implicit val argonautEncodeStrReadsData = EncodeJson.of[StrReadsData]
  implicit val argonautDecodeStrReadsData = DecodeJson.of[StrReadsData]

  case class SnpReadsData(override val locus: String, override val genotype: String, override val reads: String,
                          override val qc: String, override val productSize: String) extends
    ReadsData

  implicit val argonautEncodeSnpReadsData = EncodeJson.of[SnpReadsData]
  implicit val argonautDecodeSnpReadsData = DecodeJson.of[SnpReadsData]

  case class SeqData(locus: String, genotype: String, typedAllele: String, reads: String, repeatSeq: String)

  implicit val argonautEncodeSeqData = EncodeJson.of[SeqData]
  implicit val argonautDecodeSeqData = DecodeJson.of[SeqData]

}
