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
import java.awt.event.ActionEvent
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils

/**
  * This represent a JPanel which show all information about its node
  */
class NodeInfoPanel(val node: GuiNode) extends JInternalFrame {
  final private val idJl: JLabel = new JLabel
  private var sensors: Map[String, String] = Map[String, String]()
  final private var listSensorPanel: JPanel = null
  final private val controller: Controller = Controller.getInstance

  setSize(Utils.getGuiNodeInfoPanelDim)
  setResizable(true)
  setBorder(null)
  setLayout(new BorderLayout)
  //north
  val idPanel: JPanel = new JPanel(new BorderLayout)
  idPanel.add(this.idJl, BorderLayout.CENTER)
  val closeIconDim: Int = getWidth / 10
  val close: JButton = new JButton(Utils.getScaledImage("close.png", closeIconDim, closeIconDim))
  close.setBorderPainted(false)
  close.addActionListener((e:ActionEvent) => controller.showInfoPanel(node, false))
  idPanel.add(close, BorderLayout.WEST)
  idPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black))
  add(idPanel, BorderLayout.NORTH)
  //center
  listSensorPanel = new JPanel
  listSensorPanel.setLayout(new BoxLayout(listSensorPanel, BoxLayout.Y_AXIS))
  val scroll: JScrollPane = new JScrollPane(listSensorPanel)
  add(scroll, BorderLayout.CENTER)
  setVisible(true)

  /**
    * make a new JPanel with both key and value and
    * add this to a InfoPanel
    *
    * @param key
    * @param value
    */
  def addInfo(key: String, value: String) {
    if (this.sensors.keySet.contains(key)) {
      this.sensors += key -> value
      listSensorPanel.removeAll() //Riaggiungo tutti i sensori
      sensors.foreach(kv => {
        val (s,v) = kv
        val senPanel = new JPanel(new BorderLayout())
        senPanel.add(new JLabel(s + " : " + v), BorderLayout.WEST)
        listSensorPanel.add(senPanel)
      })
    }
    else {
      this.sensors += key -> value
      val senPanel: JPanel = new JPanel(new BorderLayout) //Aggiungo la label solo del nuovo sensore
      senPanel.add(new JLabel(key + " : " + value), BorderLayout.WEST)
      listSensorPanel.add(senPanel)
    }
    this.revalidate()
    this.repaint()
  }

  def setId(id: Int) {
    this.idJl.setText("" + id)
  }

  def getId: Int = {
    return this.idJl.getText.toInt
  }
}
