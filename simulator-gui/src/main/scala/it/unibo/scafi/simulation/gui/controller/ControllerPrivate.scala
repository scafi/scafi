package it.unibo.scafi.simulation.gui.controller

import java.awt.event.{ActionEvent, ActionListener}

import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.view.GuiNode
import it.unibo.scafi.simulation.gui.view.NodeInfoPanel
import it.unibo.scafi.simulation.gui.view.SensorOptionPane
import it.unibo.scafi.simulation.gui.view.SimulatorUI

/**
  * This class is a wrapper for all private Controller methods.
  * Created by Varini on 23/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
class ControllerPrivate (val gui: SimulatorUI) {
  final private val controller: Controller = Controller.getIstance

  implicit def toActionListener(f: ActionEvent => Unit) = new ActionListener {
    def actionPerformed(e: ActionEvent) { f(e) }
  }

  def setSensor(sensorName: String, value: Any) {
    var applyAll: Boolean = true
    for (ng <- controller.nodes.values) {
      val (n,g) = ng
      if (g.isSelected) {
        applyAll = false
        n.setSensor(sensorName, value)
        var set = Set[Node]()
        set += n
        controller.simManager.simulation.setSensor(sensorName, value.toString.toBoolean, set)
        setImage(sensorName, value, g)
      }
    }
    if (applyAll) {
      controller.nodes.values.foreach{ case (n, g) => {
        n.setSensor(sensorName, value)
        setImage(sensorName, value, g)
      }}
      controller.simManager.simulation.setSensor(sensorName, value)
    }
  }

  def getSensorValue(s: String) = {
    controller.simManager.simulation.getSensorValue(s)
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
        infoPanel.addInfo("position", "x: " + f"${n.position.getX() * 100}%.02f" // * MyDimension.getFrameDimension().getWidth() se volessi vedere le posizioni int dei frame
          + " \t y: " + f"${n.position.getY() * 100}%.02f")
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
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Show Neighbours", (e:ActionEvent) => gui.getSimulationPanel.showNeighbours(true))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Hide Neighbours", (e:ActionEvent) => gui.getSimulationPanel.showNeighbours(false))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Id", (e:ActionEvent) => controller.setShowValue("ID"))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Export", (e:ActionEvent) => controller.setShowValue("EXPORT"))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Nothing", (e:ActionEvent) => controller.setShowValue("NONE"))
    this.gui.getSimulationPanel.getPopUpMenu.addObservation("Sensor", (e:ActionEvent) => {
      val sensPane = new SensorOptionPane("Observe Sensor");
      sensPane.addOperator("=")
      sensPane.addOperator(">")
      sensPane.addOperator(">=")
      sensPane.addOperator("<")
      sensPane.addOperator("<=")
      sensPane.addOperator("!=")
      for (s <- SensorEnum.sensors) sensPane.addSensor(s.name)
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