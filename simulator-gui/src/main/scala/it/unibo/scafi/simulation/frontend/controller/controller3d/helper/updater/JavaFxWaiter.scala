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

package it.unibo.scafi.simulation.frontend.controller.controller3d.helper.updater

import it.unibo.scafi.renderer3d.manager.NetworkRenderer3D
import it.unibo.scafi.simulation.gui.Settings
import org.fxyz3d.geometry.MathUtils

/**
 * This class provides back pressure in case of the javaFx thread struggling to keep up with the requests.
 * */
private[updater] class JavaFxWaiter(gui3d: NetworkRenderer3D) {

  private var waitCounterThreshold = -1 //not initialized
  private var javaFxWaitCounter = waitCounterThreshold

  /**
   * Blocks until the javaFx thread is free of requests, but only from time to time, This is ideal to be used in a loop.
   * */
  def waitForJavaFxIfNeeded(): Unit = synchronized {
    val MIN_WAIT_COUNTER = 10 //minimum amount of iterations without waiting on javaFx
    val MAX_WAIT_COUNTER = 40 //maximum amount of iterations without waiting on javaFx
    if(waitCounterThreshold == -1){ //looking at the scene complexity to find out the value for waitCounterThreshold
      val counterThreshold = 1000000 / Math.pow(Settings.Sim_NumNodes*Settings.Sim_NbrRadius*20, 1.4)
      waitCounterThreshold = MathUtils.clamp(counterThreshold, MIN_WAIT_COUNTER, MAX_WAIT_COUNTER).toInt
    }
    javaFxWaitCounter = javaFxWaitCounter - 1
    if(javaFxWaitCounter <= 0) {
      javaFxWaitCounter = waitCounterThreshold
      gui3d.blockUntilThreadIsFree()
    }
  }
}

object JavaFxWaiter {
  def apply(gui3d: NetworkRenderer3D): JavaFxWaiter = new JavaFxWaiter(gui3d)
}
