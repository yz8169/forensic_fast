package myJs

import argonaut._
import Argonaut._

/**
 * Created by Administrator on 2020/1/16
 */
object Codecs {

  case class StatData(column: String, aCount: String, tCount: String, cCount: String, gCount: String, nCount: String,
                      q1: String, med: String, mean: String, q3: String)

  object StatData {
    implicit def statDataCodecJson: CodecJson[StatData] =
      casecodec10(StatData.apply, StatData.unapply)("column", "A_Count", "T_Count",
        "C_Count", "G_Count", "N_Count", "Q1", "med", "mean", "Q3")
  }

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

  object StrReadsData {
    implicit def strReadsDataCodecJson: CodecJson[StrReadsData] =
      casecodec6(StrReadsData.apply, StrReadsData.unapply)("Locus", "GenoType", "Reads",
        "Qc", "ProductSize", "Kind")
  }

  case class SnpReadsData(override val locus: String, override val genotype: String, override val reads: String,
                          override val qc: String, override val productSize: String) extends
    ReadsData

  object SnpReadsData {
    implicit def snpReadsDataCodecJson: CodecJson[SnpReadsData] =
      casecodec5(SnpReadsData.apply, SnpReadsData.unapply)("Locus", "GenoType", "Reads",
        "Qc", "ProductSize")
  }

  case class SeqData(locus: String, genotype: String, typedAllele: String, reads: String, repeatSeq: String)

  object SeqData {
    implicit def seqDataCodecJson: CodecJson[SeqData] =
      casecodec5(SeqData.apply, SeqData.unapply)("Locus", "GenoType", "typedAllele", "Reads",
        "repeatSeq")
  }

}
