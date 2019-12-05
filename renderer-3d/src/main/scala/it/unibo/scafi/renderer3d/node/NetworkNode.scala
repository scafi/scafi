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

package it.unibo.scafi.renderer3d.node

import javafx.scene.Node
import scalafx.geometry.Point3D
import scalafx.scene.Camera
import scalafx.scene.paint.Color

trait NetworkNode extends Node{

  def updateText(text: String): Unit

  def rotateTextToCamera(camera: Camera): Unit

  def setNodeColor(color: Color): Unit

  def setSelectionColor(color: Color): Unit

  def select(): Unit

  def deselect(): Unit

  def setLabelScale(scale: Double): Unit

  def getNodePosition: Point3D

  val UID: String
}
