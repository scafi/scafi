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

import scala.language.implicitConversions

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JOptionPane

import it.unibo.scafi.simulation.gui.model.{Node, NodeValue}
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.view.GuiNode
import it.unibo.scafi.simulation.gui.view.NodeInfoPanel
import it.unibo.scafi.simulation.gui.view.SensorOptionPane
import it.unibo.scafi.simulation.gui.view.SimulatorUI

/**
  * This class is a wrapper for all private Controller methods.
  */
class ControllerPrivate (val gui: SimulatorUI) {
  final private val controller: Controller = Controller.getInstance

  implicit def toActionListener(f: ActionEvent => Unit) = new ActionListener {
    def actionPerformed(e: ActionEvent) { f(e) }
  }

  def setSensor(sensorName: String, value: Any) {
    var applyAll: Boolean = true
    val ss: Set[(Node,GuiNode)] = selectedNodes
    ss.foreach(ng => {
      setImage(sensorName, value, ng._2)
      //println("Setting " + ng._1.id + " => " + sensorName + "="+value)
      ng._1.setSensor(sensorName, value)
    })
    controller.simManager.simulation.setSensor(sensorName, value.toString.toBoolean, ss.map(_._1))

    if (ss.isEmpty && !controller.selectionAttempted) {
      controller.nodes.values.foreach{ case (n, g) => {
        n.setSensor(sensorName, value)
        setImage(sensorName, value, g)
      }}
      controller.simManager.simulation.setSensor(sensorName, value)
    }
  }

  private def selectedNodes(): Set[(Node,GuiNode)] = {
    controller.nodes.values.collect { case ng if ng._2.isSelected => ng }.toSet
  }

  def getSensorValue(s: String): Option[Any] = {
    var ss: Iterable[(Node,GuiNode)] = selectedNodes
    if(ss.isEmpty && !controller.selectionAttempted) ss = controller.nodes.values
    //controller.simManager.simulation.getSensorValue(s, ss.map(_._1).toSet)
    //TODO: println(ss.map(n => n._1.id + " => " +n._1.getSensorValue(s)))
    ss.headOption.map(_._1.getSensorValue(s))
  }

  def checkSensor(sensor: String, operator: String, value: String) {
    controller.nodes.values.foreach(kv => {
      val (n,g) = kv
      operator match {
        case "=" => {
          if (n.getSensorValue(sensor).toString().equals(value))
            g.setImageButton("sensorOk.png")
          else
            g.setImageButton("node.png")
        }
        case ">" =>{
        if (Integer.valueOf(n.getSensorValue(sensor).toString()) > Integer.valueOf(value))
          g.setImageButton("sensorOk.png")
        else
          g.setImageButton("node.png")
        }
        case ">=" => {
        if (Integer.valueOf(n.getSensorValue(sensor).toString()) >= Integer.valueOf(value))
          g.setImageButton("sensorOk.png")
        else
          g.setImageButton("node.png")
        }
        case "<" => {
        if (Integer.valueOf(n.getSensorValue(sensor).toString()) < Integer.valueOf(value))
          g.setImageButton("sensorOk.png")
        else
          g.setImageButton("node.png")
        }
        case "<=" => {
        if (Integer.valueOf(n.getSensorValue(sensor).toString()) <= Integer.valueOf(value))
          g.setImageButton("sensorOk.png")
        else
          g.setImageButton("node.png")
        }
        case "!=" => {
        if (Integer.valueOf(n.getSensorValue(sensor).toString()) != Integer.valueOf(value))
          g.setImageButton("sensorOk.png")
        else
          g.setImageButton("node.png")
        }
      }
    })
  }

  def calculatedInfo(infoPanel: NodeInfoPanel) {
    controller.nodes.values.foreach(kv => {
      val (n, g) = kv
      if (n.id == infoPanel.getId) {
        infoPanel.addInfo("position", "x: " + f"${n.position.x * 100}%.02f" // * MyDimension.getFrameDimension().getWidth() se volessi vedere le posizioni int dei frame
          + " \t y: " + f"${n.position.y * 100}%.02f")
        infoPanel.addInfo("Export: ", n.export.toString())
        n.sensors.foreach { case (s, v) => infoPanel.addInfo(s.name, s.value.toString()) }
        controller.simManager.simulation.network.neighbourhood // Recalculate the nbrs
        val idNeighbour = new StringBuffer("[")
        n.neighbours.foreach(nn => idNeighbour.append(" " + nn.id + " "))
        idNeighbour.append("]")
        infoPanel.addInfo("Neighbours", idNeighbour.toString())
      }
    })
  }

  def enableMenu(enabled: Boolean) {
    gui.getSimulationPanel.getPopUpMenu.getSubElements()(1).getComponent.setEnabled(enabled) //menu Observation
    gui.getSimulationPanel.getPopUpMenu.getSubElements()(2).getComponent.setEnabled(enabled) //menu Action
    gui.getJMenuBar.getMenu(1).setEnabled(enabled) //Simulation
    gui.getJMenuBar.getMenu(1).getItem(0).getComponent.setEnabled(enabled)
    gui.getJMenuBar.getMenu(1).getItem(1).getComponent.setEnabled(!enabled)
    gui.getMenuBarNorth.getMenu(0).getSubElements()(0).getSubElements()(0).getComponent.setEnabled(!enabled) //new Simulation
  }

  def addObservation() {
    //network.getObservableValue().forEach( s -> gui.getSimulationPanel().getPopUpMenu().addObservation(s, e->{}));
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Toggle Neighbours", (e:ActionEvent) => gui.getSimulationPanel.toggleNeighbours())
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Id", (e:ActionEvent) => controller.setShowValue(NodeValue.ID))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Export", (e:ActionEvent) => controller.setShowValue(NodeValue.EXPORT))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Position", (e:ActionEvent) => controller.setShowValue(NodeValue.POSITION))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Position in GUI", (e:ActionEvent) => controller.setShowValue(NodeValue.POSITION_IN_GUI))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Nothing", (e:ActionEvent) => controller.setShowValue(NodeValue.NONE))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Sensor", (e:ActionEvent) => {
      try {
        var sensorName = JOptionPane.showInputDialog("Sensor to be shown (e.g.: " +
          SensorEnum.sensors.map(_.name).mkString(", ") + ")")
        controller.setShowValue(NodeValue.SENSOR(sensorName))
      } catch { case _: Throwable => () }
    })
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Generic observation", (e:ActionEvent) => {
      try {
        var observationStringRepr = JOptionPane.showInputDialog("Statement on export (e.g.: >= 5)")
        controller.setObservation(observationStringRepr)
      } catch { case _: Throwable => () }
    })
  }

  def addAction() {
    /* for(Action a :ActionEnum.values()){
               gui.getSimulationPanel().getPopUpMenu().addAction(a.getName(), e->{});
           }*/

    this.gui.getSimulationPanel.getPopUpMenu.addAction("Source", (e:ActionEvent) => {
      setSensor(SensorEnum.SOURCE.name, true);
    })
    this.gui.getSimulationPanel.getPopUpMenu.addAction("Obstacle", (e:ActionEvent) => {
      setSensor(SensorEnum.OBSTACLE.name, true)
    })
    this.gui.getSimulationPanel.getPopUpMenu.addAction("Not Source", (e:ActionEvent) => {
      setSensor(SensorEnum.SOURCE.name, false)
    })
    this.gui.getSimulationPanel.getPopUpMenu.addAction("Not Obstacle", (e:ActionEvent) => {
      setSensor(SensorEnum.OBSTACLE.name, false)
    })
    this.gui.getSimulationPanel.getPopUpMenu.addAction("Set Sensor", (e:ActionEvent) => {
      val sensPane = new SensorOptionPane("Set Sensor")
      sensPane.addOperator("=")
      for (s <- SensorEnum.sensors)  sensPane.addSensor(s.name)
    })
  }

  def revalidateSimulationPanel() {
    gui.getSimulationPanel.revalidate()
    gui.getSimulationPanel.repaint()
  }

  def setImage(sensorName: String, value: Any, g: GuiNode) {
    if (sensorName == SensorEnum.OBSTACLE.name && value.toString == "true") {
      g.setImageButton("sensorOkSelect.png")
    }
    else if (sensorName == SensorEnum.SOURCE.name && value.toString == "true") {
      g.setImageButton("sourceSelect.png")
    }
    else {
      g.setImageButton("nodeSelect.png")
    }
  }
}
