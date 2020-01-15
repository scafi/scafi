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

package it.unibo.scafi.simulation.frontend.controller.controller3d

import java.awt.Image

import it.unibo.scafi.simulation.frontend.{Settings, Simulation}
import it.unibo.scafi.simulation.frontend.controller.{ControllerUtils, PopupMenuUtils}
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper._
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.sensor.DefaultSensorSetter
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater.{DefaultNodeUpdater, NodeUpdater}
import it.unibo.scafi.simulation.frontend.model.{NodeValue, SimulationManager}
import it.unibo.scafi.simulation.frontend.view.ConfigurationPanel
import it.unibo.scafi.simulation.frontend.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import javax.swing.{JFrame, SwingUtilities}

/** 3D version of the app's Controller, uses [[SimulatorUI3D]] as the view. It handles user interaction. */
class DefaultController3D(simulation: Simulation, simulationManager: SimulationManager) extends Controller3D {
  private var gui: SimulatorUI3D = _
  private var nodeValueTypeToShow: NodeValue = NodeValue.EXPORT
  private var observation: Option[Any => Boolean] = None
  private var nodeIds: Set[Int] = Set()
  private var nodeUpdater: NodeUpdater = _

  /** See [[Controller3D.startup]] */
  def startup(): Unit = {
    simulation.setController(this)
    startGUI()
    nodeUpdater = DefaultNodeUpdater(this, gui.getSimulationPanel, simulation)
    PopupMenuUtils.addPopupObservations(gui.customPopupMenu,
      () => gui.getSimulationPanel.toggleConnections(), this)
    PopupMenuUtils.addPopupActions(this, gui.customPopupMenu)
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    if (!Settings.ShowConfigPanel) startSimulation()
  }

  private def startGUI(): Unit = SwingUtilities.invokeAndWait(() => {
    gui = DefaultSimulatorUI3D(this)
    ControllerStarter.setupGUI(gui)
    if (Settings.ShowConfigPanel) new ConfigurationPanel(this)
  })

  /** See [[Controller3D.getUI]] */
  override def getUI: JFrame = gui

  /** See [[Controller3D.setShowValue]] */
  override def setShowValue(valueType: NodeValue): Unit = {this.nodeValueTypeToShow = valueType}

  /** See [[Controller3D.getNodeValueTypeToShow]] */
  override def getNodeValueTypeToShow: NodeValue = this.nodeValueTypeToShow

  /** See [[Controller3D.startSimulation]] */
  override def startSimulation(): Unit = {
    gui.reset() //this resets any accidental camera movement happened while ConfigPanel was open
    simulationManager.setUpdateNodeFunction(nodeUpdater.updateNode(_))
    nodeIds = ControllerStarter.setupSimulation(simulation, gui, simulationManager)
    simulationManager.start()
  }

  /** See [[Controller3D.stopSimulation]] */
  override def stopSimulation(): Unit = simulationManager.stop()

  /** See [[Controller3D.pauseSimulation]] */
  override def pauseSimulation(): Unit = simulationManager.pause()

  /** See [[Controller3D.resumeSimulation]] */
  override def resumeSimulation(): Unit = simulationManager.resume()

  /** See [[Controller3D.stepSimulation]] */
  override def stepSimulation(stepCount: Int): Unit = simulationManager.step(stepCount)

  /** See [[Controller3D.clearSimulation]] */
  override def clearSimulation(): Unit = {
    this.nodeValueTypeToShow = NodeValue.EXPORT
    this.observation = None
    ControllerResetter.resetSimulation(simulationManager, nodeUpdater, gui)
  }

  /** See [[Controller3D.handleNumberButtonPress]] */
  override def handleNumberButtonPress(sensorIndex: Int): Unit =
    DefaultSensorSetter(gui.getSimulationPanel, simulation, nodeUpdater).handleNumberButtonPress(sensorIndex)

  /** See [[Controller3D.shutDown]] */
  override def shutDown(): Unit = System.exit(0)

  /** See [[Controller3D.decreaseFontSize]] */
  override def decreaseFontSize(): Unit = gui.getSimulationPanel.decreaseFontSize()

  /** See [[Controller3D.increaseFontSize]] */
  override def increaseFontSize(): Unit = gui.getSimulationPanel.increaseFontSize()

  /** See [[Controller3D.slowDownSimulation]] */
  override def slowDownSimulation(): Unit = simulationManager.simulation.setDeltaRound(getSimulationDeltaRound + 10)

  private def getSimulationDeltaRound: Double = simulationManager.simulation.getDeltaRound()

  /** See [[Controller3D.speedUpSimulation]] */
  override def speedUpSimulation(): Unit = {
    val currentDeltaRound = getSimulationDeltaRound
    val newValue = if (currentDeltaRound - 10 < 0) 0 else currentDeltaRound - 10
    simulationManager.simulation.setDeltaRound(newValue)
  }

  /** See [[Controller3D.selectionAttempted]] */
  override def selectionAttempted: Boolean = gui.getSimulationPanel.isAttemptingSelection

  /** See [[Controller3D.showImage]] */
  override def showImage(image: Image, showed: Boolean): Unit =
    if(showed){
      gui.getSimulationPanel.setBackgroundImage(image)
    }  else {
      gui.getSimulationPanel.setBackgroundColor(Settings.Color_background)
    }

  /** See [[Controller3D.setObservation]] */
  override def setObservation(observation: Any => Boolean): Unit = {this.observation = Option(observation)}

  /** See [[Controller3D.getObservation]] */
  override def getObservation(): Any => Boolean = observation match {
    case Some(observation) => observation;
    case None => _ => false
  }

  /** See [[Controller3D.setSensor]] */
  override def setSensor(sensorName: String, value: Any): Unit =
    DefaultSensorSetter(gui.getSimulationPanel, simulation, nodeUpdater).setSensor(sensorName, value, selectionAttempted)

  /** See [[Controller3D.isObservationSet]] */
  override def isObservationSet: Boolean = observation.isDefined

  /** See [[Controller3D.isLedActivatorSet]] */
  override def isLedActivatorSet: Boolean = Settings.Led_Activator(true)

  /** See [[Controller3D.getCreatedNodesID]] */
  override def getCreatedNodesID: Set[Int] = nodeIds
}

object DefaultController3D {
  def apply(simulation: Simulation, simulationManager: SimulationManager): DefaultController3D =
    new DefaultController3D(simulation, simulationManager)
}
