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

import it.unibo.scafi.simulation.gui.Settings
import javax.swing._
import javax.swing.border.LineBorder
import java.awt._
import java.awt.event.MouseEvent

/**
  * This is the most important panel in which the simulation will be executed.
  */
class SimulationPanel() extends JDesktopPane {
  final private val neighborsPanel: NeighborsPanel = new NeighborsPanel
  final private val valuesPanel: ValuesPanel = new ValuesPanel
  private var bkgImage: Image = null
  final private val captureRect: Rectangle = new Rectangle
  final private val popup: MyPopupMenu = new MyPopupMenu

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
