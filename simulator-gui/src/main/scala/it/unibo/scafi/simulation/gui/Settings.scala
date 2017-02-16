package it.unibo.scafi.simulation.gui

import java.awt.Color


object Settings {
  import SettingsSpace._

  var Sim_Policy_Nbrhood = NbrHoodPolicies.Euclidean

  var Sim_ExecStrategy = ExecStrategies.Random

  var Sim_Topology = Topologies.Random
  var Sim_Sensors = "someSensor bool true\nanotherSensor int 77"
  var Sim_ProgramClass = ""
  var Sim_NbrRadius = 0.15
  var Sim_DeltaRound = 0
  var Sim_NumNodes = 100
  var ShowConfigPanel = true

  var RandomSeed = System.nanoTime()
  var Grid_HiVar_Eps = 0.16
  var Grid_MedVar_Eps = 0.09
  var Grid_LoVar_Eps = 0.02

  var Size_Device_Relative = 100

  var Color_background = java.awt.Color.white
  var Color_selection = new java.awt.Color(30,30,30,30)
  var Color_device = java.awt.Color.black
  var Color_device1 = java.awt.Color.red
  var Color_device2 = java.awt.Color.green
  var Color_device3 = java.awt.Color.blue
  var Color_link = java.awt.Color.lightGray
  var Color_actuator = java.awt.Color.yellow

  var Led_Activator: Any=>Boolean = (_)=>false
  var To_String: Any=>String = null

}
