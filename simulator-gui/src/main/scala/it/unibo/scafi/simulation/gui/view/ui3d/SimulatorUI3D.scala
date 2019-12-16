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

package it.unibo.scafi.simulation.gui.view.ui3d

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.view.MyPopupMenu
import javax.swing.{JFrame, JMenuBar}

/**
 * Interface of the 3D view in MVC pattern.
 * */
trait SimulatorUI3D extends JFrame{

  /**
   * Retrieves the popup menu.
   * @return the popup menu
   * */
  def customPopupMenu: MyPopupMenu

  /**
   * Retrieves the 3D simulation panel. This is the 3D renderer that contains the main API of the module renderer-3d
   * @return the 3D simulation panel
   * */
  def getSimulationPanel: NetworkRenderer3D

  /**
   * Resets the scene, deleting all the nodes and connections.
   * @return Unit, since it has the side effect of resetting the scene
   * */
  def reset(): Unit

  /**
   * Retrieves the menu bar.
   * @return the menu bar
   * */
  def getJMenuBar: JMenuBar

  def getUI: JFrame

  def setShowValue(valueKind: NodeValue): Unit

  def setObservation(observation: Any=>Boolean): Unit

  def getObservation(): Any=>Boolean

  def setSensor(sensorName: String, value: Any): Unit
}
