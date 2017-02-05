package it.unibo.scafi.simulation.gui.view

import javax.swing._
import javax.swing.border.LineBorder
import java.awt._
import java.awt.event.MouseEvent

/**
  * This is the most important panel in wich will be executed the simulation
  * Created by Varini on 19/10/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class SimulationPanel() extends JDesktopPane {
  final private val neighborsPanel: NeighborsPanel = new NeighborsPanel //pannello visualizzazione vicini
  private var bkgImage: Image = null
  final private val captureRect: Rectangle = new Rectangle //rettangolo di selezione
  final private val popup: MyPopupMenu = new MyPopupMenu //menu tasto destro

  this.setBackground(Color.decode("#9EB3C2")) //azzurro
  setBorder(new LineBorder(Color.black))
  this.add(neighborsPanel, 1)
  val motion: SimulationPanelMouseListener = new SimulationPanelMouseListener(this)
  this.addMouseListener(motion) //gestisco quando appare il pannello delle opzioni
  this.addMouseMotionListener(motion) //creo e gestisco l'era di selezione

  override def paintComponent(g: Graphics) {
    if (bkgImage != null) {
      // Shows background image
      g.drawImage(bkgImage, 0, 0, this.getWidth, this.getHeight, this)
    }
    if (captureRect != null) {
      // Shows selection area
      g.setColor(Color.lightGray)
      g.drawRect(captureRect.getX.toInt, captureRect.getY.toInt, captureRect.getWidth.toInt, captureRect.getHeight.toInt)
      g.setColor(new Color(255, 255, 255, 150))
      g.fillRect(captureRect.getX.toInt, captureRect.getY.toInt, captureRect.getWidth.toInt, captureRect.getHeight.toInt)
    }
  }

  /**
    * Set the background image
    *
    * @param bkgImage
    */
  def setBackgroundImage(bkgImage: Image) {
    this.bkgImage = bkgImage
  }

  /**
    * Shows the panel representing the neighbourhood
    *
    * @param show
    */
  def showNeighbours(show: Boolean) {
    //mostro il pannello che visualizza i collegamenti con i vicini
    neighborsPanel.setVisible(show)
    this.revalidate()
    this.repaint()
  }

  def setRectSelection(r: Rectangle) {
    this.captureRect.setRect(r)
  }

  def getCaptureRect: Rectangle = {
    return captureRect
  }

  def maybeShowPopup(e: MouseEvent) {
    if (e.isPopupTrigger) {
      popup.show(e.getComponent, e.getX, e.getY)
    }
  }

  def getPopUpMenu: MyPopupMenu = {
    return this.popup
  }
}