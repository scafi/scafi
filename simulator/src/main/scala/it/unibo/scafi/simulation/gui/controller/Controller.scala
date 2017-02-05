package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.config.SimpleRandomSettings
import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.model.{Node, Sensor, SimulationManager}
import it.unibo.scafi.simulation.gui.model.implementation.{NetworkImpl, NodeImpl, SensorEnum, SimulationImpl}
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.view.GuiNode
import it.unibo.scafi.simulation.gui.view.NodeInfoPanel
import it.unibo.scafi.simulation.gui.view.SimulationPanel
import it.unibo.scafi.simulation.gui.view.SimulatorUI
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.SpaceHelper

import scala.collection.immutable.List
import java.awt._

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
object Controller {
  private var SINGLETON: Controller = null

  def getIstance: Controller = {
    if (SINGLETON == null) SINGLETON = new Controller
    SINGLETON
  }
}

class Controller () {
  private var gui: SimulatorUI = null
  protected[controller] var simManager: SimulationManager = null
  final private[controller] var nodes: Map[Int, (Node, GuiNode)] = Map[Int, (Node, GuiNode)]()
  private var valueShowed: String = "EXPORT"
  private var controllerUtility: ControllerPrivate = null

  def setGui(simulatorGui: SimulatorUI) {
    this.gui = simulatorGui
    this.controllerUtility = new ControllerPrivate(gui)
  }

  def setSimManager(simManager: SimulationManager) {
    this.simManager = simManager
  }

  def getNeighborhood: Map[Node, Set[Node]] = this.simManager.simulation.network.neighbourhood

  def startSimulation(numNodes: Int,
                      topology: String,
                      policyNeighborhood: Any,
                      runProgram: Any,
                      deltaRound: Double,
                      strategy: Any,
                      sensorValues: String) {
    val sensorVals: Map[String,Any] = Utils.parseSensors(sensorValues)
    sensorVals.foreach(kv => SensorEnum.sensors += new Sensor(kv._1, kv._2))

    val ncols: Long = Math.sqrt(numNodes).round
    var positions: List[Point2D] = List[Point2D]()
    if (topology == "Grid") {
      val nPerSide = Math.sqrt(numNodes)
      positions = SpaceHelper.GridLocations(new GridSettings(nPerSide.toInt, nPerSide.toInt, 1.0/nPerSide, 1.0/nPerSide))
    }
    else {
      positions = SpaceHelper.RandomLocations(new SimpleRandomSettings(0.0, 1.0), numNodes)
    }

    var i: Int = 0
    positions.foreach(p =>  {
      val node: Node = new NodeImpl(i, new java.awt.geom.Point2D.Double(p.x, p.y))
      val guiNode: GuiNode = new GuiNode(node)
      guiNode.setLocation(Utils.calculatedGuiNodePosition(node.position))
      this.nodes +=  i -> (node -> guiNode)
      gui.getSimulationPanel.add(guiNode, 0)
      i = i + 1
    })
    val simulation: Simulation = new SimulationImpl
    simulation.network = new NetworkImpl(this.nodes.mapValues(_._1), policyNeighborhood)
    simulation.setDeltaRound(deltaRound)
    simulation.setRunProgram(runProgram)
    simulation.setStrategy(strategy)

    simManager.simulation = simulation
    simManager.setPauseFire(deltaRound)
    simManager.start()
    controllerUtility.addObservation()
    controllerUtility.addAction()
    controllerUtility.enableMenu(true)
    System.out.println("START")
  }

  def resumeSimulation() {
    System.out.println("RESUME")
    simManager.resume()
  }

  def stopSimulation() {
    System.out.println("STOP")
    simManager.stop()
  }

  def stepSimulation(n_step: Int) {
    simManager.step(n_step)
  }

  def pauseSimulation() {
    simManager.pause()
  }

  def clearSimulation() {
    simManager.stop()
    gui.setSimulationPanel(new SimulationPanel)
    controllerUtility.enableMenu(false)
    this.nodes = Map()
  }

  /* Shows info about the selected node */
  def showInfoPanel(node: GuiNode, showed: Boolean) {
    if (showed) {
      if (node.getInfoPanel == null) {
        val info: NodeInfoPanel = new NodeInfoPanel(node)
        nodes.values.foreach(kv => {
          val (n,g) = kv
          if (g == node) info.setId(n.id)
        })
        node.setInfoPanel(info)
      }
      controllerUtility.calculatedInfo(node.getInfoPanel)
      gui.getSimulationPanel.add(node.getInfoPanel, 0)
    }
    else {
      gui.getSimulationPanel.remove(node.getInfoPanel)
    }
    controllerUtility.revalidateSimulationPanel()
  }

  /* Shows background image in the simulation */
  def showImage(img: Image, showed: Boolean) {
    if (showed) {
      gui.getSimulationPanel.setBackgroundImage(img)
    }
    else {
      gui.getSimulationPanel.setBackgroundImage(null)
    }
    controllerUtility.revalidateSimulationPanel()
  }

  def moveNode(guiNode: GuiNode, position: Point) {
    controllerUtility.revalidateSimulationPanel()
    val n = guiNode.node
    n.position = Utils.calculatedNodePosition(position)
    simManager.simulation.setPosition(n)

    if (guiNode.getInfoPanel != null)
      controllerUtility.calculatedInfo(guiNode.getInfoPanel)
  }

  def setShowValue(value: String) {
    this.valueShowed = value
  }

  def updateValue() {
    valueShowed match {
      case "ID" =>
        nodes.values.foreach{ case (n, g) => g.setValueToShow(n.id + "") }
      case "EXPORT" =>
        nodes.values.foreach(kv => {
          val (n,g) = kv
          val str = if (n.export.isInstanceOf[Double])
                       f"${n.export.toString.toDouble}%5.2g"
                    else
                       n.export.toString()
          g.setValueToShow(str)
        })
      case "NONE" =>
        nodes.values.foreach{ case (n, g) => g.setValueToShow("") }
    }
  }

  def selectNodes(area: Rectangle) {
    gui.getSimulationPanel.setRectSelection(area)
    var selectedNodes: Set[GuiNode] = Set[GuiNode]()
    nodes.values.foreach(kv => {
      val (n,g) = kv
      try {
        // The rect contains the mean point of GuiNode
        if (area.contains(new Point(g.getLocation().x + (g.getWidth() / 2), g.getLocation().y + (g.getHeight() / 2)))) {
          g.setSelected(true)
          selectedNodes += g
        } else {
          g.setSelected(false)
        }
      } catch {
        case ex: Any => ex.printStackTrace()
      }
    })
  }

  def moveNodeSelect(p: Point) {
    nodes.values.foreach(kv => {
      val (n,g) = kv
      if (g.isSelected()) {
        val pos = g.getLocation()
        g.setLocation(pos.x + p.x, pos.y + p.y)
        moveNode(g, g.getLocation())
      }
    })
  }

  def setSensor(sensorName: String, value: Any) {
    controllerUtility.setSensor(sensorName, value)
  }

  def checkSensor(sensor: String, operator: String, value: String) {
    controllerUtility.checkSensor(sensor, operator, value)
  }
}