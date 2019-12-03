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

import it.unibo.scafi.simulation.gui.controller.controller3d.Controller3D
import it.unibo.scafi.simulation.gui.view.MenuBarNorth
import javax.swing.JMenuItem

class MenuBarNorth3D(controller: Controller3D) extends MenuBarNorth {

  setupActions()

  private def setupActions(): Unit = {
    removeAllMenuActions()
    simulation.setEnabled(false) //TODO
    close.addActionListener(_ => controller.clearSimulation())
    addImage.setEnabled(false)
    removeImage.setEnabled(false)
    setupStartPauseButtons()
    step.setEnabled(false) //TODO
    stop.addActionListener(_  => controller.stopSimulation())
  }

  private def removeAllMenuActions(): Unit = {
    removeActionListeners(simulation)
    removeActionListeners(close)
    removeActionListeners(start)
    removeActionListeners(pause)
    removeActionListeners(stop)
  }

  private def setupStartPauseButtons(): Unit = {
    start.addActionListener(_  => {
      controller.resumeSimulation()
      start.setEnabled(false)
      pause.setEnabled(true)
    })
    pause.addActionListener(_  => {
      controller.pauseSimulation()
      start.setEnabled(true)
      pause.setEnabled(false)
    })
  }

  private def removeActionListeners(menuElement: JMenuItem): Unit =
    menuElement.getActionListeners.foreach(menuElement.removeActionListener)
}

object MenuBarNorth3D {
  def apply(controller: Controller3D): MenuBarNorth3D = new MenuBarNorth3D(controller)
}
