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

package it.unibo.scafi.simulation.gui.view

import java.awt._
import java.awt.event.{ActionEvent, ComponentAdapter, ComponentEvent}
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.simulation.gui.utility.Utils

/**
  * This is the general frame that contains all panel
  */
class SimulatorUI() extends JFrame("SCAFI Simulator") {
  private[gui] var center: SimulationPanel = new SimulationPanel
  final private val menuBarNorth: JMenuBar = new MenuBarNorth
  private var oldDim: Dimension = null

  setSize(Utils.getFrameDimension)
  oldDim = Utils.getFrameDimension
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  val panel: JPanel = new JPanel(new BorderLayout)
  panel.add(this.center, BorderLayout.CENTER)
  setContentPane(panel)
  this.setJMenuBar(menuBarNorth)

  val imap = panel.getInputMap()
  val amap = panel.getActionMap()
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
    override def componentResized(e: ComponentEvent) {
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
      //TODO println(s"Setting '$sensorName' from ${currVal} to ${newVal}")
      ctrl.setSensor(sensorName, newVal)
    })
  }
}
