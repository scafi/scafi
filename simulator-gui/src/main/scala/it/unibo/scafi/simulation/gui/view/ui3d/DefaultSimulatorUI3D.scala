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

package it.unibo.scafi.simulation.gui.view.ui3d

import java.awt.BorderLayout
import java.awt.event.{KeyEvent, KeyListener, MouseEvent, MouseListener}

import it.unibo.scafi.renderer3d.manager.NetworkRenderingPanel
import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.view.MyPopupMenu
import javax.swing._

class DefaultSimulatorUI3D(controller: Controller3D) extends JFrame("SCAFI 3D Simulator") with SimulatorUI3D {
  private var simulationPanel: NetworkRenderingPanel = NetworkRenderingPanel()
  final private val northMenuBar: JMenuBar = MenuBarNorth3D(controller)
  final val customPopupMenu: MyPopupMenu = new MyPopupMenu(controller)

  //TODO: enable customPopupMenu
  setSize(Utils.getFrameDimension())
  setupPanelAndMenu()
  setupButtonActions()
  setupMouseActions()
  setVisible(true)

  private def setupPanelAndMenu(): Unit = {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    this.add(simulationPanel, BorderLayout.CENTER)
    this.setJMenuBar(northMenuBar)
  }

  private def setupButtonActions(): Unit =
    simulationPanel.addKeyListener(new KeyListener {
      override def keyTyped(keyEvent: KeyEvent): Unit = ()
      override def keyPressed(keyEvent: KeyEvent): Unit = ()
      override def keyReleased(keyEvent: KeyEvent): Unit = handleKeyCode(keyEvent.getKeyCode)
    })

  private def handleKeyCode(keyCode: Int): Unit = keyCode match {
      case value if KeyEvent.VK_1 to KeyEvent.VK_4 contains value =>
        controller.handleNumberButtonPress(value - KeyEvent.VK_1 + 1)
      case KeyEvent.VK_DOWN => controller.speedUpSimulation()
      case KeyEvent.VK_UP => controller.slowDownSimulation()
      case KeyEvent.VK_P => controller.increaseFontSize()
      case KeyEvent.VK_O => controller.decreaseFontSize()
      case KeyEvent.VK_Q => controller.shutDown()
      case _ => ()
    }

  private def setupMouseActions(): Unit = simulationPanel.addMouseListener(new MouseListener {
    override def mouseClicked(mouseEvent: MouseEvent): Unit = togglePopupMenu(mouseEvent)
    override def mousePressed(mouseEvent: MouseEvent): Unit = ()
    override def mouseReleased(mouseEvent: MouseEvent): Unit = ()
    override def mouseEntered(mouseEvent: MouseEvent): Unit = ()
    override def mouseExited(mouseEvent: MouseEvent): Unit = ()
  })

  private def togglePopupMenu(mouseEvent: MouseEvent): Unit =
    if(SwingUtilities.isRightMouseButton(mouseEvent)){
    if(customPopupMenu.isShowing){
      customPopupMenu.setVisible(false)
    } else {
      customPopupMenu.show(simulationPanel, mouseEvent.getX, mouseEvent.getY)
      customPopupMenu.setVisible(true)
    }
  }

  /**
   * @return simulationPanel panel
   */
  def getSimulationPanel: NetworkRenderingPanel = simulationPanel

  override def reset(): Unit = simulationPanel.resetScene()

  override def getJMenuBar: JMenuBar = this.northMenuBar
}

object DefaultSimulatorUI3D {
  def apply(controller: Controller3D): DefaultSimulatorUI3D = new DefaultSimulatorUI3D(controller)
}
