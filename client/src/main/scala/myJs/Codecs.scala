package myJs

import argonaut._
import Argonaut._
import shared.Pojo.{SnpReadsData, StrReadsData}

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

}
