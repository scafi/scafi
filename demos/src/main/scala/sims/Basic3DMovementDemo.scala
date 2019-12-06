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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}
import lib.{FlockingLib, Movement2DSupport}

object Basic3DMovementDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Basic3DMovement" // starting class, via Reflection
  Settings.Sim_NbrRadius = 2000 // neighbourhood radius
  Settings.Sim_NumNodes = 200 // number of nodes
  Settings.Movement_Activator = (b: Any) => b.asInstanceOf[(Double, Double)]
  Settings.To_String = _ => ""
  launch()
}

class Basic3DMovement extends AggregateProgram
  with SensorDefinitions with FlockingLib with BlockG with Movement2DSupport {

  override def main:(Double, Double) = rep(randomMovement())(behaviour)

  private def behaviour(tuple: ((Double, Double))): (Double, Double) =
    mux(sense1) {clockwiseRotation(.5, .5)} {(.0, .0)}
}