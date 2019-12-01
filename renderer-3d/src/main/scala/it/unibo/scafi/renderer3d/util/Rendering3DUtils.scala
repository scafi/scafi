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

import it.unibo.scafi.renderer3d.util.RichScalaFx._
import scalafx.geometry.Point3D
import scalafx.scene.PointLight
import scalafx.scene.control.Label
import scalafx.scene.paint.{Color, Material, PhongMaterial}
import scalafx.scene.shape.{Box, Cylinder}
import scalafx.scene.text.Font
import scalafx.scene.transform.{Rotate, Translate}

object Rendering3DUtils {
  def createLights: List[PointLight] = {
    val oneMillion = 1000000
    List(createPointLight(new Point3D(oneMillion, oneMillion, oneMillion)),
         createPointLight(new Point3D(-oneMillion, oneMillion, -oneMillion)),
         createPointLight(new Point3D(oneMillion, -oneMillion, -oneMillion)),
         createPointLight(new Point3D(-oneMillion, -oneMillion, oneMillion))
    )
  }

  private def createPointLight(position: Point3D): PointLight = {
    val light = new PointLight {lightOn = true}
    light.moveTo(position)
    light
  }

  def createLabel(string: String, fontSize: Int, position: Point3D): Label = {
    val label = new Label(){
      font = new Font(fontSize)
      text = string
    }
    label.moveTo(position)
    label
  }

  def createBox(size: Int, color: Color, position: Point3D): Box = {
    val box = new Box(size, size, size)
    box.setColor(color)
    box.moveTo(position)
    box
  }

  def createMaterial(color: Color): Material = new PhongMaterial {
    diffuseColor = color
    specularColor = color
  }

  /**
   * From https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
   * */
  def createLine(origin: Point3D, target: Point3D, thickness: Int): Cylinder = {
    val differenceVector = target.subtract(origin)
    val lineMiddle = target.midpoint(origin)
    val moveToMidpoint = new Translate(lineMiddle.getX, lineMiddle.getY, lineMiddle.getZ)
    val axisOfRotation = differenceVector.crossProduct(Rotate.YAxis)
    val angle = Math.acos(differenceVector.normalize.dotProduct(Rotate.YAxis))
    val rotateAroundCenter = new Rotate(-Math.toDegrees(angle), new Point3D(axisOfRotation))
    val line = new Cylinder(thickness, differenceVector.magnitude, 3)
    line.getTransforms.addAll(moveToMidpoint, rotateAroundCenter)
    line.setColor(Color.Black)
    line
  }

}
