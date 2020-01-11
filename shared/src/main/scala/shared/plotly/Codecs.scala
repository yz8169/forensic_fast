package shared.plotly

import argonaut._
import argonaut.ArgonautShapeless._
import Argonaut._
import ArgonautShapeless._
import shared.plotly.internals.ArgonautCodecsExtra
import shared.plotly.internals.ArgonautCodecsInternals._
import shared.plotly.layout._
import shared.plotly._

object Codecs extends ArgonautCodecsExtra {

  implicit val argonautEncodeTrace = EncodeJson.of[Trace]
  implicit val argonautDecodeTrace = DecodeJson.of[Trace]

  implicit val argonautEncodeLayout = EncodeJson.of[Layout]
  implicit val argonautDecodeLayout = DecodeJson.of[Layout]

}
