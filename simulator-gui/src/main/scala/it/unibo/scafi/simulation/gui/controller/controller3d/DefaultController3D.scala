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

import it.unibo.scafi.simulation.gui.SettingsSpace.NbrHoodPolicies
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, NbrPolicy, Node, SimulationManager}
import it.unibo.scafi.simulation.gui.model.implementation.{NetworkImpl, NodeImpl, SensorEnum, SimulationImpl, SimulationManagerImpl}
import it.unibo.scafi.simulation.gui.view.ui3d.{DefaultSimulatorUI3D, SimulatorUI3D}
import it.unibo.scafi.space.Point3D

import scala.util.Random

//TODO
class DefaultController3D extends Controller3D {
  private val simulationManager: SimulationManager = new SimulationManagerImpl()
  private val gui: SimulatorUI3D = DefaultSimulatorUI3D(this)

  def startup(): Unit = { //TODO: complete this
    val topology = Settings.Sim_Topology
    val sensorValues = Settings.Sim_Sensors
    val policyNeighborhood: NbrPolicy = Settings.Sim_Policy_Nbrhood match {
      case NbrHoodPolicies.Euclidean => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
      case _ => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
    }
    val configurationSeed = Settings.ConfigurationSeed
    val nodes = (1 to Settings.Sim_NumNodes).map(index => Entry[Int, Node](index, new NodeImpl(index, getRandomPosition)))
    
    simulationManager.simulation.network = new NetworkImpl(nodes, policyNeighborhood)
    simulationManager.simulation.setDeltaRound(Settings.Sim_DeltaRound)
    simulationManager.simulation.setRunProgram(Settings.Sim_ProgramClass)
    simulationManager.simulation.setStrategy(Settings.Sim_ExecStrategy)
    simulationManager.setPauseFire(Settings.Sim_DeltaRound)
    simulationManager.start()
    enableMenu(true)
  }

  private def getRandomPosition: Point3D = {
    val MAX_DISTANCE = 10000
    new Point3D(randomDouble(MAX_DISTANCE), randomDouble(MAX_DISTANCE), randomDouble(MAX_DISTANCE))
  })

  private def randomDouble(maxValue: Int): Double = Random.nextInt(maxValue).toDouble

  def stopSimulation(): Unit = simulationManager.stop()

  def pauseSimulation(): Unit = simulationManager.pause()

  def resumeSimulation(): Unit = simulationManager.resume()

  def clearSimulation(): Unit = {
    simulationManager.stop()
    gui.reset()
    enableMenu(false)
  }

  private def enableMenu(enabled: Boolean): Unit = { //TODO: remove copy-paste
    gui.getJMenuBar.getMenu(1).setEnabled(enabled) //Simulation
    gui.getJMenuBar.getMenu(1).getItem(0).getComponent.setEnabled(enabled)
    gui.getJMenuBar.getMenu(1).getItem(1).getComponent.setEnabled(!enabled)
    gui.getJMenuBar.getMenu(0).getSubElements()(0).getSubElements()(0).getComponent.setEnabled(!enabled) //new Simulation
  }

  def handleNumberButtonPress(sensorIndex: Int): Unit = {
    val simulation = simulationManager.simulation
    val selectedNodesIDs = gui.getSimulationPanel.getSelectedNodesIDs()
    val selectedNodes = simulation.network.nodes.filter(node => selectedNodesIDs.contains(node._2.id.toString)).values
    selectedNodes.foreach(node => {
      val sensorName = getSensorName(sensorIndex)
      val sensorValue = node.getSensorValue(sensorName)
      sensorValue match {case value: Boolean => simulation.setSensor(sensorName, !value, selectedNodes.toSet)}
    })
  }

  private def getSensorName(sensorIndex: Int): String = (sensorIndex match {
    case 1 => SensorEnum.SENS1
    case 2 => SensorEnum.SENS2
    case 3 => SensorEnum.SENS3
    case 4 => SensorEnum.SENS4
  }).name

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
  def apply(): DefaultController3D = new DefaultController3D()
}
