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

package it.unibo.scafi.simulation.frontend.controller

import java.awt.Image

import it.unibo.scafi.simulation.frontend.model.NodeValue
import javax.swing.JFrame

/**
 * An interface with the main methods used by controllers.
 * */
trait GeneralController {
  /**
   * Starts the simulation.
   * */
  def startSimulation(): Unit

  /**
   * Stops the simulation.
   * */
  def stopSimulation(): Unit

  /**
   * Pauses the simulation.
   * */
  def pauseSimulation(): Unit

  /**
   * Resumes the simulation.
   * */
  def resumeSimulation(): Unit

  /**
   * Advances the simulation by the specified number of steps.
   * @param stepCount the number of simulation steps that should be done
   * */
  def stepSimulation(stepCount: Int): Unit

  /**
   * Clears the currently running simulation.
   * */
  def clearSimulation(): Unit

  /**
   * Shows the provided background image.
   * @param img the image to be shown
   * @param showed whether the image should be showed or not
   * */
  def showImage(img: Image, showed: Boolean)

  /**
   * @return whether the user is currently trying to select nodes or not
   * */
  def selectionAttempted: Boolean

  /**
   * @return the JFrame of the UI.
   * */
  def getUI: JFrame

  /**
   * Sets the kind of value that each node's label should show.
   * @param valueKind the kind of value to be shown
   * */
  def setShowValue(valueKind: NodeValue): Unit

  /**
   * Sets the observation function.
   * @param observation the observation function
   * */
  def setObservation(observation: Any=>Boolean): Unit

  /**
   * @return the observation function
   * */
  def getObservation: Any=>Boolean

  /**
   * Sets the specified sensor value.
   * @param sensorName the name of the chosen sensor
   * @param value the value to be set
   * */
  def setSensor(sensorName: String, value: Any): Unit
}
