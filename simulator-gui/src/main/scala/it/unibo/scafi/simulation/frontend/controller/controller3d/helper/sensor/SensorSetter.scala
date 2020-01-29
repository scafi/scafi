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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper.sensor

trait SensorSetter {

  /**
   * Sets the specified sensor value to the new one for all the selected nodes. If the user is not attempting selection,
   * every node gets updated.
   * @param sensorName the name of the chosen sensor
   * @param value the new sensor value to set
   * @param selectionAttempted whether the user is attempting selection right now
   * */
  def setSensor(sensorName: String, value: Any, selectionAttempted: Boolean): Unit

  /** See [[it.unibo.scafi.simulation.frontend.controller.controller3d.Controller3D.handleNumberButtonPress]] */
  def handleNumberButtonPress(sensorIndex: Int): Unit
}
