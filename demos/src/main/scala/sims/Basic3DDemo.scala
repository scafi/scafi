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
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

object Basic3DDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_3D = true //enables the 3d renderer
  //Settings.Size_Device_Relative = 80 //makes the nodes a bit bigger
  //Settings.Sim_Draw_Sensor_Radius = true //this is visible only using high Sim_Sensor_Radius values like 500
  //Settings.Sim_Sensor_Radius = 500
  //Settings.Led_Activator = _ => true
  Settings.Color_device = Color.DARK_GRAY
  Settings.Color_selection = Color.MAGENTA
  Settings.Color_link = Color.green //the default color is not as visible
  Settings.Sim_ProgramClass = "sims.Basic3DProgram"
  Settings.Sim_NbrRadius = 1500 // neighbourhood radius, set this between 100 and 1200 or so
  Settings.Sim_NumNodes = 100 // don't go too high, more than 300 causes a lot of stuttering
  Settings.ShowConfigPanel = false
  launch()
}

class Basic3DProgram extends AggregateProgram {
  override def main(): Int = rep(0)(_ + 1)
}
