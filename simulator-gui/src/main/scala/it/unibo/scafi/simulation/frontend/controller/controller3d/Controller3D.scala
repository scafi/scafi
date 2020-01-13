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

import it.unibo.scafi.incarnations.BasicAbstractIncarnation
import it.unibo.scafi.simulation.frontend.controller.GeneralController
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import it.unibo.scafi.simulation.gui.model.NodeValue

/**
 * Interface of the 3d controller. This is a normal [[GeneralController]] but it has some more methods.
 * */
trait Controller3D extends GeneralController{

  /**
   * Handles a number button press on the keyboard and updates the enabled sensors and colors of the selected nodes.
   * @param value the number of the keyboard button
   * */
  def handleNumberButtonPress(value: Int): Unit

  /**
   * Sets up the controller, so that it can be used.
   * */
  def startup(): Unit

  /**
   * Stops the whole app.
   * */
  def shutDown(): Unit

  /**
   * Decreases the font size of the labels in the scene.
   * */
  def decreaseFontSize(): Unit

  /**
   * Increases the font size of the labels in the scene.
   * */
  def increaseFontSize(): Unit

  /**
   * Slows down the simulation.
   * */
  def slowDownSimulation(): Unit

  /**
   * Speeds up the simulation.
   * */
  def speedUpSimulation(): Unit

  /**
   * @return the node value type that is being shown right now
   * */
  def getNodeValueTypeToShow: NodeValue

  /**
   * @return whether the observation function has been set
   * */
  def isObservationSet: Boolean

  /**
   * @return whether the led activator function has been set
   * */
  def isLedActivatorSet: Boolean

  /**
   * @return the set of the unique IDs of the nodes created at startup.
   * */
  def getCreatedNodesID: Set[Int]

}
