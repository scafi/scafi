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

import it.unibo.scafi.simulation.gui.model.NodeValue
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.view.{MyPopupMenu, SensorOptionPane}
import javax.swing.JOptionPane

import scala.util.Try

/**
 * Utility object containing methods that are useful for any class that uses [[MyPopupMenu]].
 * */
object PopupMenuUtils {

  /**Adds the actions to execute whenever the appropriate observation event occurs.
   * @param popupMenu the popup menu that will fire the events
   * @param toggleNeighbours the function to call whenever the event "Toggle Neighbours" occurs
   * @param controller an instance of [[Controller]] that will receive and handle most of the events */
  def addPopupObservations(popupMenu: MyPopupMenu, toggleNeighbours: () => Unit, controller: Controller) {
    popupMenu.addObservation("Toggle Neighbours", _ => toggleNeighbours())
    popupMenu.addObservation("Id", _ => controller.setShowValue(NodeValue.ID))
    popupMenu.addObservation("Export", _ => controller.setShowValue(NodeValue.EXPORT))
    popupMenu.addObservation("Position", _ => controller.setShowValue(NodeValue.POSITION))
    popupMenu.addObservation("Position in GUI", _ => controller.setShowValue(NodeValue.POSITION_IN_GUI))
    popupMenu.addObservation("Nothing", _ => controller.setShowValue(NodeValue.NONE))
    addSensorAndGenericPopupObservations(popupMenu, controller)
  }

  private def addSensorAndGenericPopupObservations(popupMenu: MyPopupMenu, controller: Controller): Unit = {
    popupMenu.addObservation("Sensor", _ => Try {
      val sensorName = JOptionPane.showInputDialog("Sensor to be shown (e.g.: " +
        SensorEnum.sensors.map(_.name).mkString(", ") + ")")
      if(sensorName != null) controller.setShowValue(NodeValue.SENSOR(sensorName))
    })
    popupMenu.addObservation("Generic observation", _ => Try {
      val observationStringRepr =
        JOptionPane.showInputDialog("Statement on export (e.g.: >= int 5)\nThe spaces in the middle are required")
      setGenericObservation(observationStringRepr, controller)
    })
  }

  private def setGenericObservation(obs: String, controller: Controller): Unit = {
    val split = obs.trim.split(" ", 2)
    val (operatorStr, valueStr) = (split(0), split(1))
    val (valueType, value) = Utils.parseValue(valueStr)

    val obsFun = (v:Any) => try {
      (operatorStr, anyToDouble(v, valueType), anyToDouble(value, valueType)) match {
        case ("==", Some(v1), Some(v2)) => v1 == v2
        case (">=", Some(v1), Some(v2)) => v1 >= v2
        case (">",  Some(v1), Some(v2)) => v1 > v2
        case ("<=", Some(v1), Some(v2)) => v1 <= v2
        case ("<",  Some(v1), Some(v2)) => v1 < v2
        case _ => value.toString==v.toString
      }
    } catch {
      case ex => { println("Errore: " + ex); false }
    }
    controller.setObservation(obsFun)
  }

  private def anyToDouble(any: Any, valueType: String): Option[Double] =
    if(valueType == "bool"){
      if(any.asInstanceOf[Boolean]) Some(1.0) else Some(0.0)
    } else if(valueType=="int"){
      Some(any.asInstanceOf[Int])
    } else if(valueType=="double"){
      Some(any.asInstanceOf[Double])
    } else {
      None
    }

  /**Adds the actions to execute whenever the appropriate user event occurs.
   * @param controller an instance of [[Controller]] that will receive and handle most of the events
   * @param popupMenu the popup menu that will fire the events */
  def addPopupActions(controller: Controller, popupMenu: MyPopupMenu) {
    /* for(Action a :ActionEnum.values()){
               gui.getSimulationPanel().getPopUpMenu().addAction(a.getName(), e->{});
           }*/
    popupMenu.addAction("Source", _ => controller.setSensor(SensorEnum.SOURCE.name, true))
    popupMenu.addAction("Obstacle", _ => controller.setSensor(SensorEnum.OBSTACLE.name, true))
    popupMenu.addAction("Not Source", _ => controller.setSensor(SensorEnum.SOURCE.name, false))
    popupMenu.addAction("Not Obstacle", _ => controller.setSensor(SensorEnum.OBSTACLE.name, false))
    popupMenu.addAction("Set Sensor", _ => {
      val sensPane = new SensorOptionPane("Set Sensor")
      sensPane.addOperator("=")
      for (s <- SensorEnum.sensors)  sensPane.addSensor(s.name)
    })
  }

}
