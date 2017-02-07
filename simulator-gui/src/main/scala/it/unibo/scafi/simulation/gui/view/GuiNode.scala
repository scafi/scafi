package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.model.Node
import it.unibo.scafi.simulation.gui.utility.Utils
import javax.swing._
import java.awt._
import java.beans.PropertyVetoException
import java.nio.file.Paths

/**
  * This class represents a graphics representation of Node
  * Created by Varini on 01/06/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class GuiNode(val node: Node) extends JInternalFrame {
  final private val DEFAULT_FONT: Font = new Font("Arial", Font.BOLD, 14)
  final private var valueShow: JLabel = null
  final private var button: JButton = null
  private var infoPanel: NodeInfoPanel = null //pannello delle informazioni del nodo

  this.valueShow = new JLabel("")
  this.button = new JButton(Utils.createImageIcon("node.png"))
  setSize(Utils.getSizeGuiNode)
  setBorder(null)

  val backgroundPanel: JPanel = new JPanel(new BorderLayout)
  backgroundPanel.setOpaque(false)
  setContentPane(backgroundPanel)
  valueShow.setFont(DEFAULT_FONT)

  // Panel showing result "above" the node
  val north: JPanel = new JPanel
  north.setOpaque(false)
  north.add(valueShow)
  backgroundPanel.add(north, BorderLayout.NORTH)

  // Panel with button and node icon
  val pBotton: JPanel = new JPanel
  pBotton.setOpaque(false)
  pBotton.add(button)
  button.setBorderPainted(false)
  button.setOpaque(false)
  button.addMouseListener(new GuiNodeListeners(this)) //listener dei movimentis
  button.addMouseMotionListener(new GuiNodeListeners(this))
  backgroundPanel.add(pBotton, BorderLayout.SOUTH)

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
    val nameImg: String = Paths.get((button.getIcon.asInstanceOf[ImageIcon]).getDescription).getFileName.toString
    val dividendo: Int = if (getWidth < getHeight) getWidth  else getHeight

    button.setIcon(Utils.getScaledImage(nameImg, dividendo / 2, dividendo / 2))

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
      this.button.setIcon(Utils.getSelectedIcon(button.getIcon))
      this.valueShow.setForeground(Color.lightGray)
    }
    else {
      this.button.setIcon(Utils.getNotSelectIcon(button.getIcon))
      this.valueShow.setForeground(Color.black)
    }
    isSelected = selected
  }

  def setValueToShow(text: String) {
    this.valueShow.setText("<html>" + text.replaceAll("\n", "<br>"))
  }

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
    button.setIcon(Utils.getScaledImage(res, dividendo / 2, dividendo / 2))
  }

  def showInfo(show: Boolean) {
    infoPanel.setVisible(show)
  }

  def getInfoPanel: NodeInfoPanel = {
    return this.infoPanel
  }
}