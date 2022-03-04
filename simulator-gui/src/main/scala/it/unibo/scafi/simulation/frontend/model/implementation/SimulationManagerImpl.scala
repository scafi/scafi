/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.model.implementation

import it.unibo.scafi.simulation.frontend.Simulation
import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.SimulationManager

class SimulationManagerImpl() extends SimulationManager { self =>
  var simulation: Simulation = null
  private var updateNodeValue = (_: Int) => ()
  private var step_num: Int = Integer.MAX_VALUE
  private var i: Int = 0
  private var isStopped: Boolean = false
  private var pauseFire: Double = 100.0
  private var simulationThread: Thread = null

  def setPauseFire(pauseFire: Double): Unit = {
    this.pauseFire = pauseFire
  }

  def start(): Unit = {
    simulationThread = getMyThread
    isStopped = false
    this.simulationThread.start()
  }

  def resume(): Unit = {
    i = 0
    this.step_num = Integer.MAX_VALUE
    simulationThread = getMyThread
    simulationThread.start()
  }

  def stop(): Unit = {
    this.isStopped = true
  }

  def pause(): Unit = {
    this.step_num = 0
  }

  def step(num_step: Int): Unit = {
    i = 0
    this.step_num = num_step
    simulationThread = getMyThread
    simulationThread.start()
  }

  def setUpdateNodeFunction(updateNodeValue: Int => Unit): Unit = {this.updateNodeValue = updateNodeValue}

  private def getMyThread: Thread = {
    new Thread() {
      // Each iteration runs the round for a single node
      override def run(): Unit = {
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

  private def runSingleSimulationStep(): Unit = {
    val exp = simulation.getRunProgram()
    simulation.network.nodes(exp._1).export = exp._2.root()
    updateNodeValue(exp._1)
  }
}
