package it.unibo.scafi.simulation.gui

/**
  * @author Roberto Casadei
  *
  */

object SettingsSpace {
  object NbrHoodPolicies{
    val Euclidean = "Euclidean"
  }
  object ExecStrategies {
    val Random = "Random"
  }
  object Topologies {
    val Random = "Random"
    val Grid = "Grid"
    val Grid_LoVar = "Grid LoVar"
    val Grid_MedVar = "Grid MedVar"
    val Grid_HighVar = "Grid HiVar"
  }
  object ToStrings {
    val Default_Double = (a:Any)=> a match {
      case d: Double if d==Double.MaxValue => "inf"
      case d: Double if d==Double.MinValue => "-inf"
      case d: Double => f"$d%5.3f";
      case _ => "???"}
  }
}
