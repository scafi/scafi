/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.controller

import java.awt.{Image, Point, Rectangle}
import javax.swing.SwingUtilities

import it.unibo.scafi.config.{GridSettings, SimpleRandomSettings}
import it.unibo.scafi.simulation.frontend.SettingsSpace.NbrHoodPolicies
import it.unibo.scafi.simulation.frontend.model._
import it.unibo.scafi.simulation.frontend.model.implementation.{NetworkImpl, NodeImpl, SensorEnum, SimulationManagerImpl}
import it.unibo.scafi.simulation.frontend.utility.Utils
import it.unibo.scafi.simulation.frontend.view.{ConfigurationPanel, GuiNode, NodeInfoPanel, SimulationPanel, SimulatorUI}
import it.unibo.scafi.simulation.frontend.{Settings, Simulation, SimulationImpl}
import it.unibo.scafi.space.{Point2D, SpaceHelper}

import scala.collection.immutable.List
import scala.util.Try

object Controller {
  private var SINGLETON: Controller = null
  private val gui = new SimulatorUI

  def getInstance: Controller = {
    if (SINGLETON == null) SINGLETON = new Controller
    SINGLETON
  }

  def getUI: SimulatorUI = gui

  def startup: Unit = {
    SwingUtilities.invokeLater(() => {
      Controller.getInstance.setGui(gui)
      val simulationManagerImpl = new SimulationManagerImpl()
      simulationManagerImpl.setUpdateNodeFunction(Controller.getInstance.updateNodeValue)
      Controller.getInstance.setSimManager(simulationManagerImpl)
      if (Settings.ShowConfigPanel) new ConfigurationPanel
      else Controller.getInstance.startSimulation()
    })
  }
}

class Controller () {
  private[frontend] var gui: SimulatorUI = null
  protected[frontend] var simManager: SimulationManager = null
  final private[controller] var nodes: Map[Int, (Node, GuiNode)] = Map[Int, (Node, GuiNode)]()
  private var valueShowed: NodeValue = NodeValue.EXPORT
  private var controllerUtility: ControllerPrivate = null
  private val updateFrequency = Settings.Sim_NumNodes / 4.0
  private var counter = 0
  private var observation: Any=>Boolean = (_)=>false

  def setObservation(obs: Any=>Boolean): Unit = {
    this.observation = obs
  }

  def updateNodePositions() = {
    this.nodes.foreach(x => {
      val (node,guiNode) = x._2
      val dim = Utils.getSizeGuiNode()
      val pos = Utils.calculatedGuiNodePosition(node.position2d)
      guiNode.setNodeLocation(pos.x,pos.y)
      guiNode.setSize(dim)
    })
  }

  def setObservation(obs: String): Unit = {
    val split = obs.trim.split(" ", 2)
    val (operatorStr, valueStr) = (split(0), split(1))
    val (valueType, value) = Utils.parseValue(valueStr)

    def anyToDouble(any: Any): Option[Double] = {
      if(valueType=="bool"){
        if(any.asInstanceOf[Boolean]==true) Some(1.0) else Some(0.0)
      }
      else if(valueType=="int" || valueType=="double"){
        Some(any.asInstanceOf[Double])
      }
      else{
        None
      }
    }

    val obsFun = (v:Any) => {
      try {
        (operatorStr, anyToDouble(v), anyToDouble(value)) match {
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
    }
    setObservation(obsFun)
  }

  def isObserved(id: Int): Boolean = {
    this.observation(nodes(id)._1.export)
  }

  def getObservation(): Any=>Boolean = this.observation

  def setGui(simulatorGui: SimulatorUI) {
    this.gui = simulatorGui
    this.controllerUtility = new ControllerPrivate(gui)
  }

  def setSimManager(simManager: SimulationManager) {
    this.simManager = simManager
  }

  def getNeighborhood: Map[Node, Set[Node]] =
    try{ this.simManager.simulation.network.neighbourhood } catch { case _: Throwable => Map()}

  def getNodes: Iterable[(Node,GuiNode)] = this.nodes.values

  def startSimulation() {
    /* TODO println("Configuration: \n topology=" + Settings.Sim_Topology +
      "; \n nbr radius=" + Settings.Sim_NbrRadius +
      ";\n numNodes=" + Settings.Sim_NumNodes +
      ";\n delta=" + Settings.Sim_DeltaRound +
      ";\n sensors = " + Settings.Sim_Sensors)
    */

    val numNodes = Settings.Sim_NumNodes
    val topology = Settings.Sim_Topology
    val runProgram = Settings.Sim_ProgramClass
    val deltaRound = Settings.Sim_DeltaRound
    val strategy = Settings.Sim_ExecStrategy
    val sensorValues = Settings.Sim_Sensors
    val policyNeighborhood: NbrPolicy = getNeighborhoodPolicy
    val configurationSeed = Settings.ConfigurationSeed
    val simulationSeed = Settings.SimulationSeed
    val randomSensorSeed = Settings.RandomSensorSeed

    setupSensors(sensorValues)

    val ncols: Long = Math.sqrt(numNodes).round
    var positions: List[Point2D] = List[Point2D]()
    import it.unibo.scafi.simulation.frontend.SettingsSpace.Topologies._
    if (List(Grid, Grid_LoVar, Grid_MedVar, Grid_HighVar) contains topology) {
      val nPerSide = Math.sqrt(numNodes)
      val tolerance = topology match {
        case Grid => 0
        case Grid_LoVar => Settings.Grid_LoVar_Eps
        case Grid_MedVar => Settings.Grid_MedVar_Eps
        case Grid_HighVar => Settings.Grid_HiVar_Eps
      }
      val (stepx, stepy, offsetx, offsety) = (1.0/nPerSide, 1.0/nPerSide, 0.05, 0.05)
      positions = SpaceHelper.gridLocations(new GridSettings(nPerSide.toInt, nPerSide.toInt, stepx , stepy, tolerance, offsetx, offsety), configurationSeed)
    } else {
      positions = SpaceHelper.randomLocations(new SimpleRandomSettings(0.05, 0.95), numNodes, configurationSeed)
    }

    var i: Int = 0
    positions.foreach(p =>  {
      val node: Node = new NodeImpl(i, new Point2D(p.x, p.y))
      val guiNode: GuiNode = new GuiNode(node)
      val pt = Utils.calculatedGuiNodePosition(node.position2d)
      guiNode.setNodeLocation(pt.x, pt.y)
      this.nodes +=  i -> (node,guiNode)
      //gui.getSimulationPanel.add(guiNode, 0)
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
    // TODO: System.out.println("START")
  }

  def resumeSimulation() {
    // TODO: System.out.println("RESUME")
    simManager.resume()
  }

  def stopSimulation() {
    // TODO: System.out.println("STOP")
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

  def moveNode(node: Node, guiNode: GuiNode) {
    if(Settings.Sim_realTimeMovementUpdate) controllerUtility.revalidateSimulationPanel()
    simManager.simulation.setPosition(node)

    if (guiNode.getInfoPanel != null)
      controllerUtility.calculatedInfo(guiNode.getInfoPanel)
  }

  def setShowValue(kind: NodeValue) {
    this.valueShowed = kind
  }

  def selectionAttempted = this.gui.center.getCaptureRect.width!=0

  def updateNodeValue(nodeId: Int): Unit = {
    val (node, guiNode) = this.nodes(nodeId)

    // TODO: refactoring with a more effective actuation model (e.g., similar to the sensing model)
    var vec: (Double, Double) = Try(Settings.Movement_Activator(node.export).asInstanceOf[(Double, Double)]) getOrElse(0.0, 0.0)
    if(vec._1 != 0.0 || vec._2 != 0.0) {
      val point = node.position
      var newX: Double = point.x + vec._1
      var newY: Double = point.y + vec._2

      val newP = Utils.calculatedGuiNodePosition(new Point2D(newX, newY))
      guiNode.setNodeLocation(newP.x, newP.y)
      node.position = new Point2D(newX, newY)
      moveNode(node, guiNode)
    }

    var outputString: String = Try(Settings.To_String(node.export)).getOrElse(null)
    if(outputString != null && !outputString.equals("")) {
      valueShowed match {
        case NodeValue.ID => guiNode.setValueToShow(node.id.toString)
        case NodeValue.EXPORT => guiNode.setValueToShow(formatExport(node.export))
        case NodeValue.POSITION => guiNode.setValueToShow(formatPosition(node.position2d))
        case NodeValue.POSITION_IN_GUI => guiNode.setValueToShow(formatPosition(Utils.calculatedGuiNodePosition(node.position2d)))
        case NodeValue.SENSOR(name) => guiNode.setValueToShow(node.getSensorValue(name).toString)
        case _ => guiNode.setValueToShow("")
      }
    }
    counter = counter + 1
    if (counter % updateFrequency == 0) {
      this.gui.revalidate()
      this.gui.repaint()
    }

  }

  def updateValue() {
    valueShowed match {
      case NodeValue.ID =>
        nodes.values.foreach { case (n, g) => g.setValueToShow(n.id + "") }
      case NodeValue.EXPORT =>
        nodes.values.foreach { case (n, g) => g.setValueToShow(formatExport(n.export)) }
      case NodeValue.POSITION =>
        nodes.values.foreach { case (n, g) => g.setValueToShow(formatPosition(n.position2d)) }
      case NodeValue.POSITION_IN_GUI =>
        nodes.values.foreach { case (n, g) => g.setValueToShow(formatPosition(Utils.calculatedGuiNodePosition(n.position2d))) }
      case NodeValue.SENSOR(name) =>
        nodes.values.foreach { case (n, g) => g.setValueToShow(n.getSensorValue(name).toString) }
      case _ => nodes.values.foreach{ case (n, g) => g.setValueToShow("") }
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
        g.setNodeLocation(pos.x + p.x, pos.y + p.y)
        moveNode(g, g.getLocation())
      }
    })
  }

  def setSensor(sensorName: String, value: Any) {
    controllerUtility.setSensor(sensorName, value)
  }

  def getSensor(s: String): Option[Any] =
    controllerUtility.getSensorValue(s)

  def checkSensor(sensor: String, operator: String, value: String) =
    controllerUtility.checkSensor(sensor, operator, value)
}
