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

package it.unibo.scafi.renderer3d.fps_counter

import javafx.scene.control.Tooltip
import scalafx.animation.AnimationTimer
import scalafx.scene.Scene

import scala.collection.mutable

/**
 * Simple class to add a tooltip on the current scene showing the current frames per second.
 * */
object FPSCounter {
  private val TOOLTIP_X_POSITION = 140
  private val TOOLTIP_Y_POSITION = 5
  private val FRAME_TIMES_LENGTH = 100
  private val frameTimes = new mutable.ArraySeq[Long](FRAME_TIMES_LENGTH)
  private var frameTimeIndex = 0
  private var arrayFilled = false

  /**
   * Adds to the specified scene a tooltip showing the frames per second.
   * @param scene the scene where the tooltip will be added
   * */
  def addToScene(scene: Scene): Unit = {
    val tooltip = new Tooltip("")
    val frameRateMeter = AnimationTimer(updateTimesAndShowLabel(_, tooltip, scene))
    frameRateMeter.start()
  }

  /**
   * From https://stackoverflow.com/questions/28287398/what-is-the-preferred-way-of-getting-the-frame-rate-of-a-javafx-application
   * */
  private def updateTimesAndShowLabel(now: Long, tooltip: Tooltip, scene: Scene): Unit = {
    val oldFrameTime = frameTimes(frameTimeIndex)
    frameTimes(frameTimeIndex) = now
    frameTimeIndex = (frameTimeIndex + 1) % FRAME_TIMES_LENGTH
    if (frameTimeIndex == 0) arrayFilled = true
    if (arrayFilled) {
      showUpdatedLabel(now, tooltip, scene, oldFrameTime)
    }
  }

  /** The tooltip will appear on the top right corner. */
  private def showUpdatedLabel(now: Long, tooltip: Tooltip, scene: Scene, oldFrameTime: Long): Unit = {
    val window = scene.getWindow
    val elapsedNanoseconds = now - oldFrameTime
    val nanosecondsPerFrame = elapsedNanoseconds / FRAME_TIMES_LENGTH
    val frameRate = 1000000000.0 / nanosecondsPerFrame
    val maximumTimeDifference = frameTimes.sliding(2).map{case Seq(x, y, _*) => y - x}.max
    tooltip.setText("FPS: %.1f\nMax time: %.1f ms".format(frameRate, maximumTimeDifference/1000000d))
    if(window.isFocused){
      tooltip.show(scene.getChildren.get(0), window.getX + window.getWidth - TOOLTIP_X_POSITION,
        TOOLTIP_Y_POSITION + window.getY)
    } else {
      tooltip.hide()
    }
  }
}
