package it.unibo.scafi.simulation.gui



object Settings {
  import SettingsSpace._

  var Sim_Policy_Nbrhood = NbrHoodPolicies.Euclidean

  var Sim_ExecStrategy = ExecStrategies.Random

  var Sim_Topology = Topologies.Random
  var Sim_Sensors = "someSensor bool true\nanotherSensor int 77"
  var Sim_ProgramClass = "sims.Gradient"
  var Sim_NbrRadius = 0.15
  var Sim_DeltaRound = 10
  var Sim_NumNodes = 50
  var ShowConfigPanel = true

  var RandomSeed = System.nanoTime()
  var Grid_HiVar_Eps = 0.16
  var Grid_MedVar_Eps = 0.09
  var Grid_LoVar_Eps = 0.02
}
