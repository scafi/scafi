package it.unibo.scafi.simulation.gui

/**
  * @author Roberto Casadei
  *
  */

object Settings {
  object NbrHoodPolicies{
    val Euclidean = "Euclidean"
  }
  var Sim_Policy_Nbrhood = NbrHoodPolicies.Euclidean

  object ExecStrategies {
    val Random = "Random"
  }
  var Sim_ExecStrategy = ExecStrategies.Random

  var Sim_Topology = Topologies.Random
  var Sim_Sensors = "someSensor bool true\nanotherSensor int 77"
  var Sim_ProgramClass = "sims.Gradient"
  var Sim_NbrRadius = 0.15
  var Sim_DeltaRound = 10
  var Sim_NumNodes = 50
  var ShowConfigPanel = true

  object Topologies {
    val Random = "Random"
    val Grid = "Grid"
    val Grid_LoVar = "Grid LoVar"
    val Grid_MedVar = "Grid MedVar"
    val Grid_HighVar = "Grid HiVar"
  }
  var RandomSeed = System.nanoTime()
  var Grid_HiVar_Eps = 0.16
  var Grid_MedVar_Eps = 0.09
  var Grid_LoVar_Eps = 0.02
}
