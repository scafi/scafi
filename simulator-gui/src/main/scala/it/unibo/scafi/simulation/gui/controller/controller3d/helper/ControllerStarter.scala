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

package it.unibo.scafi.simulation.gui.controller.controller3d.helper

import it.unibo.scafi.simulation.gui.controller.ControllerUtils
import it.unibo.scafi.simulation.gui.model.{Node, SimulationManager}
import it.unibo.scafi.simulation.gui.model.implementation.NetworkImpl
import it.unibo.scafi.simulation.gui.view.ui3d.SimulatorUI3D
import it.unibo.scafi.simulation.gui.{Settings, Simulation}

/**
 * Utility object that has methods to setup and start DefaultController3D.
 * */
private[controller3d] object ControllerStarter {

  /**
   * Sets up the simulation.
   * @param simulation the simulation that has not started yet and also has to be setup
   * @param gui the 3D view, that has to be set up
   * @param simulationManager the manager of the simulation
   * @return the set of unique IDs of the nodes that this method just created
   * */
  def setupSimulation(simulation: Simulation, gui: SimulatorUI3D, simulationManager: SimulationManager): Set[Int] = {
    val nodes = NodesGenerator.createNodes(Settings.Sim_Topology, Settings.Sim_NumNodes, Settings.ConfigurationSeed)
    setSimulationSettings(simulation, nodes)
    nodes.values
      .foreach(node => gui.getSimulationPanel.addNode(PositionConverter.controllerToView(node.position), node.id))
    simulationManager.simulation = simulation
    val deltaRound = if(Settings.Sim_NumNodes * Settings.Sim_NbrRadius >= 90) 2 else 1
    simulationManager.setPauseFire(Math.max(Settings.Sim_DeltaRound, deltaRound)) //this avoids javaFx thread flooding
    enableMenus(gui)
    nodes.keys.toSet
  }

  private def setSimulationSettings(simulation: Simulation, nodes: Map[Int, Node]): Unit = {
    val policyNeighborhood = ControllerUtils.getNeighborhoodPolicy
    simulation.network = new NetworkImpl(nodes, policyNeighborhood)
    simulation.setDeltaRound(Settings.Sim_DeltaRound)
    simulation.setRunProgram(Settings.Sim_ProgramClass)
    simulation.setStrategy(Settings.Sim_ExecStrategy)
  }

  private def enableMenus(gui: SimulatorUI3D): Unit = {
    ControllerUtils.enableMenu(enabled = true, gui.getJMenuBar, gui.customPopupMenu)
    gui.getJMenuBar.getMenu(1).getItem(1).getComponent.setEnabled(true)
  }

  /**
   * Sets up the UI.
   * @param gui the 3D view, that has to be set up
   * */
  def setupGUI(gui: SimulatorUI3D): Unit = {
    val gui3d = gui.getSimulationPanel
    if(!Settings.Sim_DrawConnections) gui3d.toggleConnections()
    gui3d.setSceneSize(PositionConverter.SCENE_SIZE)
    gui3d.setSelectionColor(Settings.Color_selection)
    gui3d.setNodesColors(Settings.Color_device, Settings.Color_movement)
    gui3d.setConnectionsColor(Settings.Color_link)
    gui3d.setBackgroundColor(Settings.Color_background)
    val sensorRadius = if(Settings.Sim_Draw_Sensor_Radius) Settings.Sim_Sensor_Radius else 0
    gui3d.setSpheresRadius(sensorRadius, 0)
    gui3d.setFilledSpheresColor(Settings.Color_actuator)
    gui3d.setNodesScale(100 / Settings.Size_Device_Relative)
  }

}
