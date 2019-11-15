/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

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
