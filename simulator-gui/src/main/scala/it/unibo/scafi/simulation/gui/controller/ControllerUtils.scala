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

package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.SettingsSpace.NbrHoodPolicies
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, Sensor}
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.space.{Point2D, Point3D}
import javax.swing.JMenuBar

private[controller] object ControllerUtils {

  def getNeighborhoodPolicy: EuclideanDistanceNbr =
    Settings.Sim_Policy_Nbrhood match {
      case NbrHoodPolicies.Euclidean => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
      case _ => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
    }

  def setupSensors(sensors: String): Unit =
    Utils.parseSensors(sensors).foreach(entry => SensorEnum.sensors += Sensor(entry._1, entry._2))

  def enableMenuBar(enable: Boolean, jMenuBar: JMenuBar): Unit = {
    jMenuBar.getMenu(1).setEnabled(enable) //Simulation
    jMenuBar.getMenu(1).getItem(0).getComponent.setEnabled(enable)
    jMenuBar.getMenu(1).getItem(1).getComponent.setEnabled(!enable)
    jMenuBar.getMenu(0).getSubElements()(0).getSubElements()(0).getComponent.setEnabled(!enable) //new Simulation
  }

  def formatExport(value: Any): String =
    value match {
      case doubleValue: Double =>
        if (doubleValue == Double.MaxValue){
          "inf"
        } else if (doubleValue == Double.MinValue) {
          "-inf"
        } else {
          f"${value.toString.toDouble}%5.2f"
        }
      case _ => value.toString
    }

  def formatPosition(pos: Point2D): String = s"(${formatDouble(pos.x)} ; ${formatDouble(pos.y)})"

  private def formatDouble(value: Double): String = f"(${value}%5.2g"

  def formatPosition(pos: Point3D): String =
    s"(${formatDouble(pos.x)} ; ${formatDouble(pos.y)} ; ${formatDouble(pos.z)})"

  def formatPosition(pos: java.awt.Point): String = s"(${pos.getX.toInt}; ${pos.getY.toInt})"
}