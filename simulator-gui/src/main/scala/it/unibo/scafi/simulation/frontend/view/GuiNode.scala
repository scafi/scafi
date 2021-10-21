/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.beans.PropertyVetoException
import javax.swing._

import it.unibo.scafi.simulation.frontend.model.Node
import it.unibo.scafi.simulation.frontend.utility.Utils

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
  var point: Point = null

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

  override def setSize(d: Dimension): Unit = {
    super.setSize(d)
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

  override def getLocation(): Point =
    return this.point

  def setNodeLocation(x: Int, y: Int): Unit = {
    // super.setLocation(x, y)
    point = new Point(x, y)
    if (this.infoPanel != null) {
      infoPanel.setLocation(x + getWidth, y)
    }
  }

  @throws[PropertyVetoException]
  override def setSelected(selected: Boolean): Unit = {
    // don't call super, otherwise I wouldn't be able to select more than one item
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

  def setValueToShow(text: String): Unit = {
    this.valueString = text
    this.valueShow.setText("<html>" + text.replaceAll("\n", "<br>"))
  }

  def getValueToShow(): String =
    this.valueString

  def setLabelFont(font: Font): Unit =
    this.valueShow.setFont(font)

  def setLabelColor(color: Color): Unit =
    this.valueShow.setForeground(color)

  def setInfoPanel(p: NodeInfoPanel): Unit =
    this.infoPanel = p

  def setImageButton(res: String): Unit = {
    //val dividendo: Int = if (getWidth < getHeight) getWidth else getHeight
    //button.setIcon(Utils.getScaledImage(res, dividendo / 2, dividendo / 2))
  }

  def showInfo(show: Boolean): Unit =
    infoPanel.setVisible(show)

  def getInfoPanel: NodeInfoPanel =
    this.infoPanel
}
