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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper

import it.unibo.scafi.simulation.gui.controller.ControllerUtils
import it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater.NodeUpdater
import it.unibo.scafi.simulation.gui.model.SimulationManager
import it.unibo.scafi.simulation.frontend.view.ui3d.SimulatorUI3D

/**
 * Utility object that has methods to clear and reset DefaultController3D.
 * */
private[controller3d] object ControllerResetter {

  /**
   * Clears and resets the simulation.
   * @param simulationManager the manager of the simulation
   * @param nodeUpdater the object that handles the node updates
   * @param gui the 3D view, that also has to be reset
   * */
  def resetSimulation(simulationManager: SimulationManager, nodeUpdater: NodeUpdater, gui: SimulatorUI3D): Unit = {
    simulationManager.stop()
    ControllerUtils.enableMenu(enabled = false, gui.getJMenuBar, gui.customPopupMenu)
    nodeUpdater.resetNodeCache()
    gui.reset()
  }

}
