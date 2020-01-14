package shared.plotly.layout

sealed abstract class HoverMode(val label: String) extends Product with Serializable

object HoverMode {

  case object Closest extends HoverMode("closest")

  case object YMode extends HoverMode("y")

}
