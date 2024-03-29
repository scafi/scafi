/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.{ActionEvent, ComponentAdapter, ComponentEvent}
import javax.swing._

import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum
import it.unibo.scafi.simulation.frontend.utility.Utils

/**
  * This is the general frame that contains all panel
  */
class SimulatorUI() extends JFrame("SCAFI Simulator") {
  private[frontend] var center: SimulationPanel = new SimulationPanel(Controller.getInstance)
  final private val menuBarNorth: JMenuBar = new MenuBarNorth(Controller.getInstance)
  private var oldDim: Dimension = null

  setSize(Utils.getFrameDimension)
  oldDim = Utils.getFrameDimension
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  val panel: JPanel = new JPanel(new BorderLayout)
  panel.add(this.center, BorderLayout.CENTER)
  setContentPane(panel)
  this.setJMenuBar(menuBarNorth)

  val imap: InputMap = panel.getInputMap()
  val amap: ActionMap = panel.getActionMap()
  val ctrl = Controller.getInstance
  imap.put(KeyStroke.getKeyStroke('1'), SensorEnum.SENS1.name)
  amap.put(SensorEnum.SENS1.name, createSensorAction[Boolean](SensorEnum.SENS1.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('2'), SensorEnum.SENS2.name)
  amap.put(SensorEnum.SENS2.name, createSensorAction[Boolean](SensorEnum.SENS2.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('3'), SensorEnum.SENS3.name)
  amap.put(SensorEnum.SENS3.name, createSensorAction[Boolean](SensorEnum.SENS3.name, default = false, map = !_))
  imap.put(KeyStroke.getKeyStroke('4'), SensorEnum.SENS4.name)
  amap.put(SensorEnum.SENS4.name, createSensorAction[Boolean](SensorEnum.SENS4.name, default = false, map = !_))

  imap.put(KeyStroke.getKeyStroke("DOWN"), "Quicker")
  imap.put(KeyStroke.getKeyStroke("UP"), "Slower")
  amap.put("Quicker", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = if(currVal-10 < 0) 0 else currVal-10
    // TODO: println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))
  amap.put("Slower", createAction((e: ActionEvent)=>{
    val currVal = ctrl.simManager.simulation.getDeltaRound()
    val newVal = currVal+10
    // TODO: println(s"Setting delta round = $newVal")
    ctrl.simManager.simulation.setDeltaRound(newVal)
  }))
  imap.put(KeyStroke.getKeyStroke('p'), "FontBigger")
  amap.put("FontBigger", createAction((e: ActionEvent)=>{
    ctrl.gui.getSimulationPanel.increaseFontSize()
  }))
  imap.put(KeyStroke.getKeyStroke('o'), "FontSmaller")
  amap.put("FontSmaller", createAction((e: ActionEvent)=>{
    ctrl.gui.getSimulationPanel.decreaseFontSize()
  }))
  imap.put(KeyStroke.getKeyStroke('q'), "Quit")
  amap.put("Quit", createAction(e=>System.exit(0)))

  this.addComponentListener(new ComponentAdapter() {
    override def componentResized(e: ComponentEvent): Unit = {
      super.componentResized(e)
      Utils.setDimensionFrame(getSize)
      Controller.getInstance.updateNodePositions()
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

  def setSimulationPanel(simPanel: SimulationPanel): Unit = {
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
      //TODO println(s"Setting '$sensorName' from ${currVal} to ${newVal}")
      ctrl.setSensor(sensorName, newVal)
    })
  }
}
