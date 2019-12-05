/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import java.awt.event.MouseEvent
import javax.swing._
import javax.swing.border.LineBorder

import it.unibo.scafi.simulation.frontend.Settings

/**
  * This is the most important panel in which the simulation will be executed.
  */
class SimulationPanel(clearSimulation: () => Unit) extends JDesktopPane {
  final private val neighborsPanel: NeighborsPanel = new NeighborsPanel
  final private val valuesPanel: ValuesPanel = new ValuesPanel
  private var bkgImage: Image = null
  final private val captureRect: Rectangle = new Rectangle
  final private val popup: MyPopupMenu = new MyPopupMenu(clearSimulation)

  this.setBackground(Settings.Color_background)
  setBorder(new LineBorder(Color.black))
  this.add(valuesPanel, 1)
  this.add(neighborsPanel, 2)
  val motion: SimulationPanelMouseListener = new SimulationPanelMouseListener(this)
  this.addMouseListener(motion)
  this.addMouseMotionListener(motion)

  override def paintComponent(g: Graphics) {
    if (bkgImage != null) {
      // Shows background image
      g.drawImage(bkgImage, 0, 0, this.getWidth, this.getHeight, this)
    }
    if (captureRect != null) {
      // Shows selection area
      g.setColor(Settings.Color_selection)
      g.drawRect(captureRect.getX.toInt, captureRect.getY.toInt, captureRect.getWidth.toInt, captureRect.getHeight.toInt)
      g.setColor(Settings.Color_selection)
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
    neighborsPanel.setVisible(show)
    this.revalidate()
    this.repaint()
  }

  def toggleNeighbours() {
    neighborsPanel.setVisible(!neighborsPanel.isVisible)
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

  def increaseFontSize() { this.valuesPanel.increaseFontSize() }
  def decreaseFontSize() { this.valuesPanel.decreaseFontSize() }
}
