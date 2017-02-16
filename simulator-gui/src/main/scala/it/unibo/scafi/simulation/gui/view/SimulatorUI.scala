package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.utility.Utils
import javax.swing._
import java.awt._
import java.awt.event.{ActionEvent, ComponentAdapter, ComponentEvent}

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

/**
  * This is the general frame that contains all panel
  * Created by Varini on 19/10/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SimulatorUI() extends JFrame("SCAFI Simulator") {
  private var center: SimulationPanel = new SimulationPanel //JDesktopPane per visualizzare le simulazioni
  final private val menuBarNorth: JMenuBar = new MenuBarNorth //barra del menù in alto
  private var oldDim: Dimension = null //utilizzato per la riposizione dei nodi quando il frame viene rimpicciolito

  setSize(Utils.getFrameDimension)
  oldDim = Utils.getFrameDimension
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  val panel: JPanel = new JPanel(new BorderLayout)
  panel.add(this.center, BorderLayout.CENTER)
  setContentPane(panel)
  this.setJMenuBar(menuBarNorth)

  val imap = panel.getInputMap()
  val amap = panel.getActionMap()
  val ctrl = Controller.getIstance
  imap.put(KeyStroke.getKeyStroke('1'), SensorEnum.SENS1.name)
  amap.put(SensorEnum.SENS1.name, createSensorAction[Boolean](SensorEnum.SENS1.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('2'), SensorEnum.SENS2.name)
  amap.put(SensorEnum.SENS2.name, createSensorAction[Boolean](SensorEnum.SENS2.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('3'), SensorEnum.SENS3.name)
  amap.put(SensorEnum.SENS3.name, createSensorAction[Boolean](SensorEnum.SENS3.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke("DOWN"), "Quicker")
  imap.put(KeyStroke.getKeyStroke("UP"), "Slower")
  amap.put("Quicker", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = if(currVal-10 < 0) 0 else currVal-10
    println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))
  amap.put("Slower", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = currVal+10
    println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))

  this.addComponentListener(new ComponentAdapter() {
    override def componentResized(e: ComponentEvent) {
      super.componentResized(e)
      Utils.setDimensionFrame(getSize)
      for (i <- center.getAllFrames) {
        i.setSize(Utils.getSizeGuiNode) //ridimensionamento
        i.setLocation((i.getLocation().getX * getWidth / oldDim.getWidth().round).toInt,
          (i.getLocation().getY * getHeight / oldDim.getHeight.round).toInt)
      }
      center.getCaptureRect.setSize((center.getCaptureRect.getWidth * getWidth / oldDim.getWidth().round).toInt,
        (center.getCaptureRect.getHeight * getHeight / oldDim.getHeight.round).toInt)
      center.getCaptureRect.setLocation((center.getCaptureRect.getLocation.getX * getWidth / oldDim.getWidth().round).toInt,
        (center.getCaptureRect.getLocation.getY * getHeight / oldDim.getHeight.round).toInt)
      oldDim = getSize
    }
  })

  setVisible(true)

  /**
    * @return center panel
    */
  def getSimulationPanel: SimulationPanel = {
    return center
  }

  def setSimulationPanel(simPanel: SimulationPanel) {
    this.remove(center)
    this.add(simPanel, BorderLayout.CENTER)
    center = simPanel
    this.revalidate()
    this.repaint()
  }

  /**
    * @return application menu
    */
  def getMenuBarNorth: JMenuBar = {
    return menuBarNorth
  }

  private def createAction(f: ActionEvent => Unit): Action ={
    new AbstractAction() {
      override def actionPerformed(e: ActionEvent) = f(e)
    }
  }

  private def createSensorAction[T](sensorName: String, default: T, map: T=>T) = {
    createAction((e: ActionEvent) => {
      val currVal = ctrl.getSensor(sensorName).getOrElse(default).asInstanceOf[T]
      val newVal = map(currVal)
      println(s"Setting '$sensorName' to ${newVal}")
      ctrl.setSensor(sensorName, newVal)
    })
  }
}