package it.unibo.scafi.simulation.gui.controller

import it.unibo.scafi.config.GridSettings
import it.unibo.scafi.config.SimpleRandomSettings
import it.unibo.scafi.simulation.gui.{Settings, Simulation}
import it.unibo.scafi.simulation.gui.SettingsSpace.Topologies._
import it.unibo.scafi.simulation.gui.model._
import it.unibo.scafi.simulation.gui.model.implementation._
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.view.{ConfigurationPanel, GuiNode, NodeInfoPanel, SimulationPanel, SimulatorUI}
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.SpaceHelper

import scala.collection.immutable.List
import java.awt._
import javax.swing.SwingUtilities

import it.unibo.scafi.simulation.gui.SettingsSpace.NbrHoodPolicies


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

  def startup: Unit = {
    SwingUtilities.invokeLater(new Runnable() {
      def run() {
        Controller.getIstance.setGui(new SimulatorUI)
        Controller.getIstance.setSimManager(new SimulationManagerImpl)
        if(Settings.ShowConfigPanel) new ConfigurationPanel
        else Controller.getIstance.startSimulation()
      }
    })
  }
}

class Controller () {
  private var gui: SimulatorUI = null
  protected[gui] var simManager: SimulationManager = null
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

  def startSimulation() {

    println("Configuration: \n topology=" + Settings.Sim_Topology +
      "; \n nbr radius=" + Settings.Sim_NbrRadius +
      ";\n numNodes=" + Settings.Sim_NumNodes +
      ";\n delta=" + Settings.Sim_DeltaRound +
      ";\n sensors = " + Settings.Sim_Sensors)

    val numNodes = Settings.Sim_NumNodes
    val topology = Settings.Sim_Topology
    val runProgram = Settings.Sim_ProgramClass
    val deltaRound = Settings.Sim_DeltaRound
    val strategy = Settings.Sim_ExecStrategy
    val sensorValues = Settings.Sim_Sensors
    val policyNeighborhood: NbrPolicy = Settings.Sim_Policy_Nbrhood match {
      case NbrHoodPolicies.Euclidean => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
      case _ => EuclideanDistanceNbr(Settings.Sim_NbrRadius)
    }

    val sensorVals: Map[String,Any] = Utils.parseSensors(sensorValues)
    sensorVals.foreach(kv => SensorEnum.sensors += new Sensor(kv._1, kv._2))

    val ncols: Long = Math.sqrt(numNodes).round
    var positions: List[Point2D] = List[Point2D]()
    if (List(Grid, Grid_LoVar, Grid_MedVar, Grid_HighVar) contains topology) {
      val nPerSide = Math.sqrt(numNodes)
      val tolerance = topology match {
        case Grid => 0
        case Grid_LoVar => Settings.Grid_LoVar_Eps
        case Grid_MedVar => Settings.Grid_MedVar_Eps
        case Grid_HighVar => Settings.Grid_HiVar_Eps
      }
      val (stepx, stepy, offsetx, offsety) = (1.0/nPerSide, 1.0/nPerSide, 0.05, 0.05)
      positions = SpaceHelper.GridLocations(new GridSettings(nPerSide.toInt, nPerSide.toInt, stepx , stepy, tolerance, offsetx, offsety))
    }
    else {
      positions = SpaceHelper.RandomLocations(new SimpleRandomSettings(0.05, 0.95), numNodes)
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

  def formatExport(v: Any) = {
    if (v.isInstanceOf[Double])
      f"${v.toString.toDouble}%5.2g"
    else
      v.toString
  }

  def updateNodeValue(nodeId: Int): Unit = {
    val (node, guiNode) = this.nodes(nodeId)
    valueShowed match {
      case "ID" => guiNode.setValueToShow(node.id.toString)
      case "EXPORT" => guiNode.setValueToShow(formatExport(node.export))
      case _ => guiNode.setValueToShow("")
    }
  }

  def updateValue() {
    valueShowed match {
      case "ID" =>
        nodes.values.foreach{ case (n, g) => g.setValueToShow(n.id + "") }
      case "EXPORT" =>
        nodes.values.foreach(kv => {
          val (n,g) = kv
          g.setValueToShow(formatExport(n.export))
        })
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
        g.setLocation(pos.x + p.x, pos.y + p.y)
        moveNode(g, g.getLocation())
      }
    })
  }

  def setSensor(sensorName: String, value: Any) {
    controllerUtility.setSensor(sensorName, value)
  }

  def getSensor(s: String): Option[Any] = {
    controllerUtility.getSensorValue(s)
  }

  def checkSensor(sensor: String, operator: String, value: String) {
    controllerUtility.checkSensor(sensor, operator, value)
  }
}