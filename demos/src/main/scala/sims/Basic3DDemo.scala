/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package sims

import java.awt.Color

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram
import it.unibo.scafi.simulation.gui.SettingsSpace.Topologies
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

/**
 * 3D version of BasicDemo.
 * To convert a 2D simulation to 3D: simply set Settings.Sim_3D to true. You can also use Settings.Movement_Activator_3D
 * to set a 3d movement, otherwise the 2d movement function will be used. Finally, Settings.Sim_3D_Reduce_Sparsity can
 * be set to false to disable the adjustment that avoids the sparsity increase when converting from 2d
 * to 3d. Leaving it to true lets the simulator increase Settings.Sim_NbrRadius.
 * Reduce window size if you need more FPS. This can be useful for screens with high resolutions.
 * */
object Basic3DDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_3D = true //enables the 3d renderer
  //Settings.Sim_3D_Reduce_Sparsity = false
  //Settings.Size_Device_Relative = 80 //makes the nodes a bit bigger
  //Settings.Sim_Draw_Sensor_Radius = true
  //Settings.Sim_Sensor_Radius = 0.025
  //Settings.Led_Activator = _ => scala.util.Random.nextBoolean()
  //Settings.Color_background = Color.LIGHT_GRAY
  //Settings.Sim_DrawConnections = false
  //Settings.Sim_Topology = Topologies.Grid_LoVar
  //Settings.Color_selection = new Color(30, 30, 180, 30)
  Settings.Color_device = Color.DARK_GRAY
  Settings.Color_link = new Color(230, 230, 230)
  Settings.Sim_ProgramClass = "sims.Basic3DProgram"
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius, don't go too high if there are a lot of nodes
  Settings.Sim_NumNodes = 100 // don't go too high
  Settings.ShowConfigPanel = false
  launch()
}

class Basic3DProgram extends AggregateProgram {
  override def main(): Int = rep(0)(_ + 1)
}
