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

import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.utility.Utils
import javax.swing._
import java.awt._
import java.beans.PropertyVetoException
import java.nio.file.Paths

/**
  * This class represents a graphics representation of Node
  */
class GuiNode(val node: Node) extends JInternalFrame {
  final private val NODE_SYMBOL_FONT: Font = new Font("Arial", Font.BOLD, 28)
  final private val DEFAULT_FONT: Font = new Font("Arial", Font.BOLD, 14)
  final private var valueShow: JLabel = null
  final private var button: JButton = null
  final private var valueString: String = null
  private var infoPanel: NodeInfoPanel = null

  this.valueShow = new JLabel("")
  this.valueShow.setBackground(null)
  this.valueShow.setBorder(null)

  this.button = new JButton(Utils.createImageIcon("node_point.png"))
  this.button.setFont(NODE_SYMBOL_FONT)
  setSize(Utils.getSizeGuiNode)
  setBorder(null)

  val backgroundPanel: JPanel = new JPanel()
  backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS))
  backgroundPanel.setOpaque(false)
  setContentPane(backgroundPanel)
  valueShow.setFont(DEFAULT_FONT)

  // Panel showing result "above" the node
  val north: JPanel = new JPanel
  north.add(valueShow)
  north.setMaximumSize(new Dimension(100, 22))
  north.setOpaque(false)

  // Panel with button and node icon
  val pBotton: JPanel = new JPanel
  pBotton.setOpaque(false)
  pBotton.add(button)
  pBotton.setMaximumSize(pBotton.getPreferredSize)

  button.setBorder(BorderFactory.createEmptyBorder())
  button.setOpaque(false)
  button.setMaximumSize(new Dimension(10,6))

  button.addMouseListener(new GuiNodeListeners(this))
  button.addMouseMotionListener(new GuiNodeListeners(this))

  backgroundPanel.add(north)
  backgroundPanel.add(pBotton)

  revalidate()
  repaint()

  setVisible(true)

  try {
    this.setSelected(false)
  }
  catch {
    case e: PropertyVetoException => {
      e.printStackTrace()
    }
  }

  override def setSize(d: Dimension) {
    super.setSize(d)
    //val nameImg: String = Paths.get((button.getIcon.asInstanceOf[ImageIcon]).getDescription).getFileName.toString
    val dividendo: Int = if (getWidth < getHeight) getWidth  else getHeight

    //button.setIcon(Utils.getScaledImage(nameImg, dividendo / 2, dividendo / 2))

    if (d.getHeight < (Utils.getFrameDimension.getHeight / 2)) {
      this.valueShow.setFont(DEFAULT_FONT.deriveFont(DEFAULT_FONT.getSize / 2))
    }
    else {
      this.valueShow.setFont(DEFAULT_FONT)
    }

    if (infoPanel != null) {
      infoPanel.setSize(Utils.getMenuSimulationPanelDim)
      infoPanel.setLocation(this.getLocation().x + getWidth, this.getLocation().y)
    }
  }

  override def setLocation(x: Int, y: Int) {
    super.setLocation(x, y)
    if (this.infoPanel != null) {
      infoPanel.setLocation(x + getWidth, y)
    }
  }

  @throws[PropertyVetoException]
  override def setSelected(selected: Boolean) {
    //non faccio la super perchè se no non riesco a selezionarne più di uno
    if (selected) {
      //this.button.setIcon(Utils.getSelectedIcon(button.getIcon))
      this.valueShow.setForeground(Color.lightGray)
    }
    else {
      //this.button.setIcon(Utils.getNotSelectIcon(button.getIcon))
      this.valueShow.setForeground(Color.black)
    }
    isSelected = selected
  }

  def setValueToShow(text: String) {
    this.valueString = text
    this.valueShow.setText("<html>" + text.replaceAll("\n", "<br>"))
  }

  def getValueToShow(): String = this.valueString

  def setLabelFont(font: Font) {
    this.valueShow.setFont(font)
  }

  def setLabelColor(color: Color) {
    this.valueShow.setForeground(color)
  }

  def setInfoPanel(p: NodeInfoPanel) {
    this.infoPanel = p
  }

  def setImageButton(res: String) {
    val dividendo: Int = if (getWidth < getHeight) getWidth
    else getHeight
    //button.setIcon(Utils.getScaledImage(res, dividendo / 2, dividendo / 2))
  }

  def showInfo(show: Boolean) {
    infoPanel.setVisible(show)
  }

  def getInfoPanel: NodeInfoPanel = {
    return this.infoPanel
  }
}
