/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import java.awt.Color

object Settings {
  import SettingsSpace._

  var Sim_Policy_Nbrhood = NbrHoodPolicies.Euclidean

  var Sim_ExecStrategy = ExecStrategies.Random

  var Sim_3D = false
  var Sim_Topology = Topologies.Random
  var Sim_Sensors = "someSensor bool true\nanotherSensor int 77"
  var Sim_ProgramClass = ""
  var Sim_NbrRadius = 0.15
  var Sim_DeltaRound = 0
  var Sim_NumNodes = 100
  var ShowConfigPanel = true
  var Sim_DrawConnections = true
  var Sim_realTimeMovementUpdate = true
  var Sim_Draw_Sensor_Radius = false
  var Sim_Sensor_Radius = 0.03
  var SimulationSeed = System.nanoTime()
  var RandomSensorSeed = System.nanoTime()
  var ConfigurationSeed = System.nanoTime()
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
  var Color_device4 = java.awt.Color.CYAN
  var Color_link = new Color(240,240,240)
  var Color_actuator = java.awt.Color.yellow
  var Color_movement = java.awt.Color.red
  val Color_observation = new Color(200,0,0)

  var Led_Activator: Any=>Boolean = (_)=>false
  var Movement_Activator: Any=>(Double, Double) = (_)=>(0.0, 0.0)
  var To_String: Any=>String = x => x.toString

}
