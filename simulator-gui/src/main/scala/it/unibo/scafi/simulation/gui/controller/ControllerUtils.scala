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
import it.unibo.scafi.simulation.gui.SettingsSpace.Topologies.{Grid, Grid_HighVar, Grid_LoVar, Grid_MedVar}
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, Sensor}
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.view.MyPopupMenu
import it.unibo.scafi.space.{Point2D, Point3D}
import javax.swing.JMenuBar

/**
 * Utility object containing methods that are useful for any [[Controller]].
 * */
private[controller] object ControllerUtils {

  /**Obtains the neighborhood policy from the constants available in [[Settings]].
   * @param radius the neighborhood radius
   * @return the neighborhood policy */
  def getNeighborhoodPolicy(radius: Double = Settings.Sim_NbrRadius): EuclideanDistanceNbr =
    Settings.Sim_Policy_Nbrhood match {
      case NbrHoodPolicies.Euclidean => EuclideanDistanceNbr(radius)
      case _ => EuclideanDistanceNbr(radius)
    }

  /**Obtains the 3D neighborhood policy from the constants available in [[Settings]].
   * @return the 3D neighborhood policy */
  def get3DNeighborhoodPolicy: EuclideanDistanceNbr = {
    val radius = Settings.Sim_NbrRadius
    getNeighborhoodPolicy(if(Settings.Sim_3D_Reduce_Sparsity) radius * 1.65 else radius)
  }

  /**Adds to [[SensorEnum.sensors]] all the provided sensors.
   * @param sensors the sensors to parse and add
   * @return Unit, since it has the side effect of setting the sensors. */
  def setupSensors(sensors: String): Unit =
    Utils.parseSensors(sensors).foreach(entry => SensorEnum.sensors += Sensor(entry._1, entry._2))

  /**Enables or disables the provided menu bar and popup menu.
   * @param enabled this is used to understand whether the menus should be enabled or disabled
   * @param menuBar the menu bar to enable or disable
   * @param popupMenu the popup menu to enable or disable
   * @return Unit, since it has the side effect of enabling or disabling the menus. */
  def enableMenu(enabled: Boolean, menuBar: JMenuBar, popupMenu: MyPopupMenu): Unit = {
    popupMenu.getSubElements()(1).getComponent.setEnabled(enabled) //menu Observation
    popupMenu.getSubElements()(2).getComponent.setEnabled(enabled) //menu Action
    enableMenuBar(enabled, menuBar)
  }

  private def enableMenuBar(enable: Boolean, jMenuBar: JMenuBar): Unit = {
    jMenuBar.getMenu(1).setEnabled(enable) //Simulation
    jMenuBar.getMenu(1).getItem(0).getComponent.setEnabled(enable)
    jMenuBar.getMenu(1).getItem(1).getComponent.setEnabled(!enable)
    jMenuBar.getMenu(0).getSubElements()(0).getSubElements()(0).getComponent.setEnabled(!enable) //new Simulation
  }

  /**@param value the value to format
   * @return the formatted String value */
  def formatExport(value: Any): String = value match {
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

  /**@param pos the position to format
   * @return the formatted position of type String */
  def formatPosition(pos: Point2D): String = s"(${formatDouble(pos.x)} ; ${formatDouble(pos.y)})"

  /**@param pos the position to format
   * @return the formatted position of type String */
  def formatPosition(pos: Point3D): String = s"${formatDouble(pos._1)};${formatDouble(pos._2)};${formatDouble(pos._2)}"

  private def formatDouble(value: Double): String = f"$value%5.2g"

  /** This formats the position removing the trailing part, rounding the value. Parentheses are omitted to reduce clutter
   *@param position the position to format
   * @return the formatted position of type String */
  def formatAndRoundPosition(position: Product2[Double, Double]): String =
    s"${formatAndRound(position._1)};${formatAndRound(position._2)}"

  private def formatAndRound(value: Double): String = f"$value%.0f"

  /**@param pos the position to format
   * @return the formatted position of type String */
  def formatPosition(pos: java.awt.Point): String = s"(${pos.getX.toInt}; ${pos.getY.toInt})"

  /**@param topology the network topology
   * @return the tolerance related to the provided topology */
  def getTolerance(topology: String): Double = topology match {
    case Grid => 0
    case Grid_LoVar => Settings.Grid_LoVar_Eps
    case Grid_MedVar => Settings.Grid_MedVar_Eps
    case Grid_HighVar => Settings.Grid_HiVar_Eps
  }
}
