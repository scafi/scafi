package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.SimulationManager


/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
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