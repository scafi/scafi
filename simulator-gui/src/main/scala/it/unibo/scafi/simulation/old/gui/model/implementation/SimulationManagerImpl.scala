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

package it.unibo.scafi.simulation.old.gui.model.implementation

import it.unibo.scafi.simulation.old.gui.Simulation
import it.unibo.scafi.simulation.old.gui.controller.Controller
import it.unibo.scafi.simulation.old.gui.model.SimulationManager

class SimulationManagerImpl extends SimulationManager { self =>
  var simulation: Simulation = null
  private var step_num: Int = Integer.MAX_VALUE
  private var i: Int = 0
  private var isStopped: Boolean = false
  private var pauseFire: Double = 100.0
  private var simulationThread: Thread = null

  def setPauseFire(pauseFire: Double) {
    this.pauseFire = pauseFire
  }

  def start() {
    simulationThread = getMyThread
    isStopped = false
    this.simulationThread.start()
  }

  def resume() {
    i = 0
    this.step_num = Integer.MAX_VALUE
    simulationThread = getMyThread
    simulationThread.start()
  }

  def stop(): Unit = {
    this.isStopped = true
  }

  def pause() {
    this.step_num = 0
  }

  def step(num_step: Int) {
    i = 0
    this.step_num = num_step
    simulationThread = getMyThread
    simulationThread.start()
  }

  private def getMyThread: Thread = {
    return new Thread() {
      // Each iteration runs the round for a single node
      override def run() {
        while (i < step_num && !self.isStopped) {{
            // Core logic
            runSingleSimulationStep()
            if(pauseFire>0) {
              try {
                Thread.sleep((pauseFire).longValue)
              }
              catch {
                case e: InterruptedException => e.printStackTrace()
              }
            }
            i += 1
          }
        }
        this.interrupt()
      }
    }
  }

  private def runSingleSimulationStep() {
    val exp = simulation.getRunProgram.apply
    simulation.network.nodes(exp._1).export = exp._2.root()
    Controller.getInstance.updateNodeValue(exp._1)
  }
}
