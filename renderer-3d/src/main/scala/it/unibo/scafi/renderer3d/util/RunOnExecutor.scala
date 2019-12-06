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

package it.unibo.scafi.renderer3d.util

import it.unibo.scafi.simulation.gui.controller.ControllerImpl
import it.unibo.scafi.simulation.gui.controller.controller3d.DefaultController3D
import it.unibo.scafi.simulation.gui.model.implementation.{SimulationImpl, SimulationManagerImpl}

object BasicSpatialIncarnation extends BasicAbstractSpatialSimulationIncarnation {
  override type P = Point3D

  def launch(): Unit =
    if (Settings.Sim_3D) {
      val simulatorManager = new SimulationManagerImpl()
      DefaultController3D(SimulationImpl(simulatorManager), simulatorManager).startup()
    } else {
      ControllerImpl.startup
    }
}

  def onExecutor[R](operation: => R): Unit = {
    val task = new Task[R] {
      override def call(): R = operation
    }
    threadPool.submit(task)
  }
}
