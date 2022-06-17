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

import com.typesafe.scalalogging.Logger

import java.awt.event.{ActionEvent, ActionListener}
import it.unibo.scafi.simulation.frontend.model.NodeValue
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum
import it.unibo.scafi.simulation.frontend.utility.Utils
import it.unibo.scafi.simulation.frontend.view.{MyPopupMenu, SensorOptionPane}

import javax.swing.JOptionPane
import scala.util.Try

/**
 * Utility object containing methods that are useful for any class that uses [[MyPopupMenu]].
 * */
object PopupMenuUtils {
  private val logger = Logger[PopupMenuUtils.type]
  /**Adds the actions to execute whenever the appropriate observation event occurs.
   * @param popupMenu        the popup menu that will fire the events
   * @param toggleNeighbours the function to call whenever the event "Toggle Neighbours" occurs
   * @param controller       an instance of [[GeneralController]] that will receive and handle most of the events */
  def addPopupObservations(popupMenu: MyPopupMenu, toggleNeighbours: () => Unit, controller: GeneralController): Unit = {
    popupMenu.addObservation("Toggle Neighbours", toListener(_ => toggleNeighbours()))
    popupMenu.addObservation("Id", toListener(_ => controller.setShowValue(NodeValue.ID)))
    popupMenu.addObservation("Export", toListener(_ => controller.setShowValue(NodeValue.EXPORT)))
    popupMenu.addObservation("Position", toListener(_ => controller.setShowValue(NodeValue.POSITION)))
    popupMenu.addObservation("Position in GUI",
      toListener(_ => controller.setShowValue(NodeValue.POSITION_IN_GUI)))
    popupMenu.addObservation("Nothing", toListener(_ => controller.setShowValue(NodeValue.NONE)))
    addSensorAndGenericPopupObservations(popupMenu, controller)
  }

  private def toListener(action: ActionEvent => Unit): ActionListener = new ActionListener {
    override def actionPerformed(actionEvent: ActionEvent): Unit = action(actionEvent)
  }

  private def addSensorAndGenericPopupObservations(popupMenu: MyPopupMenu, controller: GeneralController): Unit = {
    popupMenu.addObservation("Sensor", toListener(_ => Try {
      val sensorName = JOptionPane.showInputDialog("Sensor to be shown (e.g.: " +
        SensorEnum.sensors.map(_.name).mkString(", ") + ")")
      if(sensorName != null) controller.setShowValue(NodeValue.SENSOR(sensorName))
    }))
    popupMenu.addObservation("Generic observation", toListener(_ => Try {
      val observationStringRepr =
        JOptionPane.showInputDialog("Statement on export (e.g.: >= int 5)\nThe spaces in the middle are required")
      setGenericObservation(observationStringRepr, controller)
    }))
  }

  private def setGenericObservation(obs: String, controller: GeneralController): Unit = {
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
      case exception: Exception =>
        logger.error(s"Cannot manage ${v} for this reason: " + exception)
        false
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

  /**Adds the actions to execute whenever a popup "Action" menu is pressed.
   * @param controller an instance of [[GeneralController]] that will receive and handle most of the events
   * @param popupMenu  the popup menu that will fire the events */
  def addPopupActions(controller: GeneralController, popupMenu: MyPopupMenu): Unit = {
    /* for(Action a :ActionEnum.values()){
               gui.getSimulationPanel().getPopUpMenu().addAction(a.getName(), e->{});
           }*/
    popupMenu.addAction("Source", toListener(_ => controller.setSensor(SensorEnum.SOURCE.name, true)))
    popupMenu.addAction("Obstacle",
      toListener(_ => controller.setSensor(SensorEnum.OBSTACLE.name, true)))
    popupMenu.addAction("Not Source",
      toListener(_ => controller.setSensor(SensorEnum.SOURCE.name, false)))
    popupMenu.addAction("Not Obstacle",
      toListener(_ => controller.setSensor(SensorEnum.OBSTACLE.name, false)))
    popupMenu.addAction("Set Sensor", toListener(_ => {
      val sensPane = new SensorOptionPane("Set Sensor", controller)
      sensPane.addOperator("=")
      for (s <- SensorEnum.sensors)  sensPane.addSensor(s.name)
    }))
  }

}
