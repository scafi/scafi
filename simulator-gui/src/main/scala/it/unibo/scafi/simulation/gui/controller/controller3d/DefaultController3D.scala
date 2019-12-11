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

package it.unibo.scafi.simulation.gui.controller.controller3d

import java.awt.Image

import it.unibo.scafi.simulation.gui.controller.controller3d.helper.NodeUpdater.updateNode
import it.unibo.scafi.simulation.gui.controller.controller3d.helper.{SensorSetter, ControllerStarter}
import it.unibo.scafi.simulation.gui.controller.{ControllerUtils, PopupMenuUtils}
import it.unibo.scafi.simulation.gui.model._
import it.unibo.scafi.simulation.gui.view.ConfigurationPanel
import it.unibo.scafi.simulation.gui.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import javax.swing.{JFrame, SwingUtilities}

class DefaultController3D(simulation: Simulation, simulationManager: SimulationManager) extends Controller3D {
  private var gui: SimulatorUI3D = _
  private var nodeValueTypeToShow: NodeValue = NodeValue.EXPORT
  private var observation: Option[Any => Boolean] = None

  def startup(): Unit = {
    simulation.setController(this)
    startGUI()
    PopupMenuUtils.addPopupObservations(gui.customPopupMenu,
      () => gui.getSimulationPanel.toggleConnections(), this)
    PopupMenuUtils.addPopupActions(this, gui.customPopupMenu)
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    startSimulation()
  }

  private def startGUI(): Unit = SwingUtilities.invokeAndWait(() => {
    gui = DefaultSimulatorUI3D(this)
    ControllerStarter.setupGUI(gui)
    if (Settings.ShowConfigPanel) new ConfigurationPanel(this)
  })

  override def getUI: JFrame = gui

  def setShowValue(valueType: NodeValue): Unit = {this.nodeValueTypeToShow = valueType}

  def getNodeValueTypeToShow: NodeValue = this.nodeValueTypeToShow

  override def startSimulation(): Unit = {
    simulationManager.setUpdateNodeFunction(updateNode(_, gui, simulation, this))
    ControllerStarter.startSimulation(simulation, gui, simulationManager)
  }

  override def stopSimulation(): Unit = simulationManager.stop()

  override def pauseSimulation(): Unit = simulationManager.pause()

  override def resumeSimulation(): Unit = simulationManager.resume()

  override def stepSimulation(stepCount: Int): Unit = simulationManager.step(stepCount)

  override def clearSimulation(): Unit = {
    simulationManager.stop()
    ControllerUtils.enableMenu(enabled = false, gui.getJMenuBar, gui.customPopupMenu)
    gui.reset()
  }

  def handleNumberButtonPress(sensorIndex: Int): Unit =
    SensorSetter(gui.getSimulationPanel, simulation).handleNumberButtonPress(sensorIndex)

  def shutDown(): Unit = System.exit(0)

  def decreaseFontSize(): Unit = gui.getSimulationPanel.decreaseFontSize()

  def increaseFontSize(): Unit = gui.getSimulationPanel.increaseFontSize()

  def slowDownSimulation(): Unit = simulationManager.simulation.setDeltaRound(getSimulationDeltaRound + 10)

  private def getSimulationDeltaRound: Double = simulationManager.simulation.getDeltaRound()

  def speedUpSimulation(): Unit = {
    val currentDeltaRound = getSimulationDeltaRound
    val newValue = if (currentDeltaRound - 10 < 0) 0 else currentDeltaRound - 10
    simulationManager.simulation.setDeltaRound(newValue)
  }

  override def selectionAttempted: Boolean = gui.getSimulationPanel.isAttemptingSelection

  override def showImage(img: Image, showed: Boolean): Unit = () //do nothing

  override def setObservation(observation: Any => Boolean): Unit = {this.observation = Option(observation)}

  override def getObservation(): Any => Boolean = observation match {
    case Some(observation) => observation;
    case None => _ => false
  }

  override def setSensor(sensorName: String, value: Any): Unit =
    SensorSetter(gui.getSimulationPanel, simulation).setSensor(sensorName, value, selectionAttempted)

  override def isObservationSet: Boolean = observation.isDefined

  override def isLedActivatorSet: Boolean = Settings.Led_Activator(true)
}

object DefaultController3D {
  def apply(simulation: Simulation, simulationManager: SimulationManager): DefaultController3D =
    new DefaultController3D(simulation, simulationManager)
}
