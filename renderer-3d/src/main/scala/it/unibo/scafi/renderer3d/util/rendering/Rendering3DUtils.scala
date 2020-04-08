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

package it.unibo.scafi.renderer3d.util.rendering

import it.unibo.scafi.renderer3d.util.RichScalaFx._
import it.unibo.scafi.renderer3d.util.ScalaFxExtras._
import javafx.scene.Node
import javafx.scene.shape._
import javafx.scene.text.Text
import scalafx.geometry.Point3D
import scalafx.scene.paint.{Color, Material, PhongMaterial}
import scalafx.scene.text.Font
import scalafx.scene.transform.{Rotate, Translate}
import scalafx.scene.{AmbientLight, CacheHint}

/** This contains methods to create the elements of the 3d JavaFx scene such as labels, cubes, spheres, lines, etc. */
object Rendering3DUtils {
  private var materialCache: Map[Color, Material] = Map()

  /** Creates a light that illuminates the whole scene with a constant illumination, without any low light parts.
   * @return the ambient light */
  def createAmbientLight: AmbientLight = new AmbientLight()

  /** Creates a 2d text label that can also be used and rotated in a 3d scene.
   * @param textString the text that should be displayed
   * @param fontSize the font size to be used
   * @param position the position where the text label should be placed
   * @return the text label */
  def createText(textString: String, fontSize: Int, position: Point3D): Text = {
    val label = new scalafx.scene.text.Text() {font = new Font(fontSize); text = textString}.delegate
    label.moveTo(position)
    optimize(label)
  }

  /** Creates a 3d cube.
   * @param size the length of the side of the cube
   * @param color the color of the cube
   * @param position the position where the cube should be placed
   * @return the cube */
  def createCube(size: Double, color: Color, position: Point3D = Point3D.Zero): Box = {
    val box = new Box(size, size, size)
    setColorAndPosition(box, color, position)
    optimize(box)
  }

  private def setColorAndPosition(shape: Shape3D, color: Color, position: Point3D): Unit =
    {shape.setColor(color); shape.moveTo(position)}

  /** Creates a 3d pyramid.
   * @param radius the radius of the pyramid
   * @param height the height of the pyramid
   * @param color the color of the pyramid
   * @param position the position where the pyramid should be placed
   * @return the pyramid */
  def createPyramid(radius: Double, height: Double, color: Color, position: Point3D = Point3D.Zero): PyramidMesh = {
    val cone = new PyramidMesh(radius.toFloat, height.toFloat)
    setColorAndPosition(cone, color, position)
    optimize(cone)
  }

  /** Creates a wireframe sphere, with lines linking consecutive vertices, colored with a half transparent gray.
   * @param radius the desired radius of the sphere
   * @param position the position where the sphere should be placed
   * @return the sphere */
  def createOutlinedSphere(radius: Double, position: Point3D): Sphere = {
    val SPHERE_BRIGHTNESS = 100 //out of 255
    val SPHERE_OPACITY = 0.5
    val color = Color.rgb(SPHERE_BRIGHTNESS, SPHERE_BRIGHTNESS, SPHERE_BRIGHTNESS, SPHERE_OPACITY)
    createSphere(radius, color, position, drawOutlineOnly = true)
  }

  /** Creates a black sphere where the polygonal faces are rendered as solid surfaces.
   * @param radius the desired radius of the sphere
   * @param position the position where the sphere should be placed
   * @param highQuality whether the sphere should have lots of divisions or not
   * @return the sphere */
  def createFilledSphere(radius: Double, position: Point3D, highQuality: Boolean = false): Sphere =
    if(highQuality) {
      val SPHERE_DIVISIONS = 32
      val sphere = new Sphere(radius, SPHERE_DIVISIONS)
      sphere.moveTo(position)
      optimize(sphere)
    } else {
      createSphere(radius, Color.Black, position, drawOutlineOnly = false)
    }

  /** Creates a sphere that can be rendered as outline or as a filled sphere.
   * @param radius the desired radius of the sphere
   * @param color the desired color of the sphere
   * @param position the position where the sphere should be placed
   * @param drawOutlineOnly if it is true only the sphere will be rendered as wireframe
   * @return the sphere */
  def createSphere(radius: Double, color: Color, position: Point3D, drawOutlineOnly: Boolean): Sphere = {
    val MESH_DIVISIONS = 5 //this is low for performance reasons
    val sphere = new scalafx.scene.shape.Sphere(radius, MESH_DIVISIONS) {material = createMaterial(color)}.delegate
    sphere.moveTo(position)
    if(drawOutlineOnly) sphere.setDrawMode(DrawMode.LINE)
    optimize(sphere)
  }

  /** Creates a material given a color. Caching has been used, since the same materials can be requested many times.
   * @param color the chosen color
   * @return the material */
  def createMaterial(color: Color): Material = onFXAndWait {
    materialCache.getOrElse(color, {
      val material = new PhongMaterial {diffuseColor = color; specularColor = color}
      materialCache += (color -> material)
      material
    })
  }

  /** Creates a 3d line as a really thin cylinder.
   * @param points the start and end 3d points of the line
   * @param visible whether the line should be already visible or not
   * @param color the chosen color
   * @return the 3d line */
  def createLine(points: (Point3D, Point3D), visible: Boolean, color: Color, thickness: Double): Cylinder = {
    val differenceVector = points._2.subtract(points._1)
    val lineMiddle = points._2.midpoint(points._1)
    val moveToMidpoint = new Translate(lineMiddle.getX, lineMiddle.getY, lineMiddle.getZ)
    val axisOfRotation = differenceVector.crossProduct(Rotate.YAxis)
    val angle = Math.acos(differenceVector.normalize.dotProduct(Rotate.YAxis))
    val rotateAroundCenter = new Rotate(-Math.toDegrees(angle), new Point3D(axisOfRotation))
    val line = new Cylinder(thickness, differenceVector.magnitude, 3)
    line.getTransforms.addAll(moveToMidpoint, rotateAroundCenter)
    line.setVisible(visible)
    line.setColor(color)
    optimize(line)
  }

  private final def optimize[A <: Node](node: A): A = {node.setCache(true); node.setCacheHint(CacheHint.Speed); node}
}
