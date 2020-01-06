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

package it.unibo.scafi.simulation.gui.controller.controller3d.helper

/**
 * Utility object to convert the position of nodes from the controller to the view and back.
 * */
private[helper] object PositionConverter {

  /**
   * The length of the imaginary cube that surrounds the whole scene. This constant also determines the size of the
   * camera and also of the nodes and connections, so even if the scene is very small or big the 3d network will always
   * be visible.
   * ATTENTION: big values will cause performance problems, while small values move the labels too far away from the
   * nodes, so a value of 2000 or so is ideal. This means that the 3d points should be positioned in a 2000*2000*2000
   * space.
   * Nodes can also be positioned outside of the 2000*2000*2000 space but it's not ideal, since the camera would take
   * too much time to navigate the whole scene.
   * */
  val SCENE_SIZE = 2000

  def controllerToView(position: Product3[Double, Double, Double]): Product3[Double, Double, Double] =
    (position._1*SCENE_SIZE, position._2*SCENE_SIZE, position._3*SCENE_SIZE)

  def viewToController(position: Product3[Double, Double, Double]): Product3[Double, Double, Double] =
    (position._1/(SCENE_SIZE), position._2/(SCENE_SIZE), position._3/(SCENE_SIZE))

}
