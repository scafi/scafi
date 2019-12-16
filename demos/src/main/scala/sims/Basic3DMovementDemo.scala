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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}
import lib.Movement2DSupport

object Basic3DMovementDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_3D = true //enables the 3d renderer
  Settings.Color_device = Color.DARK_GRAY
  Settings.Color_selection = Color.MAGENTA
  Settings.Color_link = Color.green //the default color is not as visible
  Settings.Sim_ProgramClass = "sims.Basic3DMovement" // starting class, via Reflection
  Settings.Sim_NbrRadius = 1500 // neighbourhood radius, don't go too high
  Settings.Sim_NumNodes = 300 // number of nodes, don't go too high, not beyond 400
  Settings.Movement_Activator_3D = (b: Any) => b.asInstanceOf[(Double, Double, Double)]
  Settings.To_String = _ => ""
  Settings.ShowConfigPanel = false
  launch()
}

class Basic3DMovement extends AggregateProgram
  with SensorDefinitions with BlockG with Movement2DSupport {

  override def main:(Double, Double, Double) = rep(random3DMovement())(behaviour)

  private def behaviour(tuple: ((Double, Double, Double))): (Double, Double, Double) =
    mux(sense1) {random3DMovement()} {(.0, .0, .0)}

  def random3DMovement(): (Double, Double, Double) = {
    def randomDouble: Double = random.nextDouble()*2000 - 1000
    (randomDouble, randomDouble, randomDouble)
  }
}