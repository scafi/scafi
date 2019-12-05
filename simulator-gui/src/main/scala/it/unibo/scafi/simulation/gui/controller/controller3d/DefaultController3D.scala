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

import java.awt.Color

import it.unibo.scafi.simulation.gui.controller.ControllerUtils
import it.unibo.scafi.simulation.gui.model._
import it.unibo.scafi.simulation.gui.model.implementation.{NetworkImpl, SensorEnum}
import it.unibo.scafi.simulation.gui.view.ConfigurationPanel
import it.unibo.scafi.simulation.gui.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.simulation.gui.controller.controller3d.NodeUpdater._
import javax.swing.SwingUtilities

class DefaultController3D(simulation: Simulation, simulationManager: SimulationManager) extends Controller3D {
  private var gui: SimulatorUI3D = _
  private var nodeValueTypeToShow: NodeValue = NodeValue.EXPORT

  def startup(): Unit = {
    simulation.setSelectionAttemptedDependency(() => gui.getSimulationPanel.isAttemptingSelection)
    startGUI()
    ControllerUtils.setupSensors(Settings.Sim_Sensors)
    ControllerUtils.enableMenuBar(enable = true, gui.getJMenuBar)
    ControllerUtils.addPopupObservations(gui.customPopupMenu,
      () => gui.getSimulationPanel.toggleConnections(), setNodeValueTypeToShow, _ => ())
    startSimulation()
  }

  private def startSimulation(): Unit = {
    val nodes = NodesGenerator.createNodes(Settings.Sim_Topology, Settings.Sim_NumNodes, Settings.ConfigurationSeed)
    nodes.values.foreach(node => gui.getSimulationPanel.addNode(node.position, node.id.toString))
    val policyNeighborhood = ControllerUtils.getNeighborhoodPolicy
    simulation.network = new NetworkImpl(nodes, policyNeighborhood)
    simulation.setDeltaRound(Settings.Sim_DeltaRound)
    simulation.setRunProgram(Settings.Sim_ProgramClass)
    simulation.setStrategy(Settings.Sim_ExecStrategy)
    simulationManager.simulation = simulation
    simulationManager.setPauseFire(Settings.Sim_DeltaRound)
    simulationManager.setUpdateNodeFunction(updateNode(_, gui, simulation.network, () => getNodeValueTypeToShow))
    simulationManager.start()
  }

  private def startGUI(): Unit = SwingUtilities.invokeAndWait(() => {
    gui = DefaultSimulatorUI3D(this)
    val gui3d = gui.getSimulationPanel
    gui3d.setConnectionsVisible(Settings.Sim_DrawConnections)
    gui3d.setSelectionColor(Settings.Color_selection)
    gui3d.setNodesColor(Settings.Color_device)
    gui3d.setConnectionsColor(Settings.Color_link)
    gui3d.setBackground(Settings.Color_background)
    if (Settings.ShowConfigPanel) new ConfigurationPanel(() => startSimulation())
  })

  def setNodeValueTypeToShow(valueType: NodeValue): Unit = {this.nodeValueTypeToShow = valueType}

  def getNodeValueTypeToShow: NodeValue = this.nodeValueTypeToShow

  def stopSimulation(): Unit = simulationManager.stop()

  def pauseSimulation(): Unit = simulationManager.pause()

  def resumeSimulation(): Unit = simulationManager.resume()

  def clearSimulation(): Unit = {
    simulationManager.stop()
    gui.reset()
    ControllerUtils.enableMenuBar(enable = false, gui.getJMenuBar)
  }

  def handleNumberButtonPress(sensorIndex: Int): Unit = //TODO: set the node color
    getSensorName(sensorIndex).foreach(sensorName => {
      val simulation = simulationManager.simulation
      gui.getSimulationPanel.setModifiedNodesColor(SensorEnum.getColor(sensorIndex).getOrElse(Color.BLACK))
      val selectedNodesIDs = gui.getSimulationPanel.getSelectedNodesIDs
      val selectedNodes = simulation.network.nodes.filter(node => selectedNodesIDs.contains(node._2.id.toString)).values
      selectedNodes.foreach(node => {
        val sensorValue = node.getSensorValue(sensorName)
        sensorValue match {case value: Boolean => simulation.setSensor(sensorName, !value, selectedNodes.toSet)}
      })
    })

  private def getSensorName(sensorIndex: Int): Option[String] = SensorEnum.fromInt(sensorIndex).map(_.name)

  def shutDown(): Unit = System.exit(0)

  def decreaseFontSize(): Unit = gui.getSimulationPanel.decreaseFontSize()

  def increaseFontSize(): Unit = gui.getSimulationPanel.increaseFontSize()

  def slowDownSimulation(): Unit = simulationManager.simulation.setDeltaRound(getSimulationDeltaRound + 10)

  private def getSimulationDeltaRound: Double = simulationManager.simulation.getDeltaRound()

  def speedUpSimulation(): Unit = {
    val currentDeltaRound = getSimulationDeltaRound
    val newValue = if(currentDeltaRound-10 < 0) 0 else currentDeltaRound-10
    simulationManager.simulation.setDeltaRound(newValue)
  }

}

object DefaultController3D {
  def apply(simulation: Simulation, simulationManager: SimulationManager): DefaultController3D =
    new DefaultController3D(simulation, simulationManager)
}
