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
import org.scalafx.extras._
import scalafx.geometry.Point3D
import scalafx.scene.{AmbientLight, CacheHint, Node}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.paint.{Color, Material, PhongMaterial}
import scalafx.scene.shape.{Box, Cylinder, DrawMode, Sphere}
import scalafx.scene.text.Font
import scalafx.scene.transform.{Rotate, Translate}

/**
 * This object contains methods to create the elements of the 3d JavaFx scene such as labels, cubes, spheres, lines, etc.
 * */
object Rendering3DUtils {
  private var materialCache: Map[Color, Material] = Map()

  /** Creates a light that illuminates the whole scene with a constant illumination, without any low light parts.
   * @return the ambient light */
  def createAmbientLight: AmbientLight = new AmbientLight()

  /** Creates a 2d label, which can also be used in a 3d scene.
   * @param textString the text that should be displayed
   * @param fontSize the font size to be used
   * @param position the position where the label should be placed
   * @return the label */
  def createLabel(textString: String, fontSize: Int, position: Point3D): Label = {
    val label = new Label(){
      font = new Font(fontSize)
      text = textString
    }
    label.moveTo(position)
    optimize(label) match {case label: Label => label}
  }

  /** Creates a 3d cube.
   * @param size the length of the side of the cube
   * @param color the color of the cube
   * @param position the position where the cube should be placed
   * @return the cube */
  def createCube(size: Double, color: Color, position: Point3D = Point3D.Zero): Box = {
    val box = new Box(size, size, size)
    box.setColor(color)
    box.moveTo(position)
    optimize(box) match {case box: Box => box}
  }

  /** Creates a 2d square image with one color. This should be used instead of shapes for performance reasons.
   * @param size the length of the side of the image
   * @param color the color of the image
   * @param position the position where the image should be placed
   * @return the ImageView containing the image */
  def createSquareImage(size: Double, color: Color, position: Point3D = Point3D.Zero): ImageView = {
    val IMAGE_SIZE = 1
    val IMAGE_SCALE = 10
    val image = new WritableImage(IMAGE_SIZE, IMAGE_SIZE)
    image.getPixelWriter.setColor(0, 0, color)
    val imageView = new ImageView(image)
    imageView.setScale(IMAGE_SCALE)
    imageView.moveTo(position)
    imageView
  }

  /** Creates a sphere that is rendered as a wireframe, with lines linking consecutive vertices, colored with a half
   * transparent gray.
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
   * @return the sphere */
  def createFilledSphere(radius: Double, position: Point3D): Sphere =
    createSphere(radius, Color.Black, position, drawOutlineOnly = false)

  /** Creates a sphere that can be rendered as outline or as a filled sphere.
   * @param radius the desired radius of the sphere
   * @param color the desired color of the sphere
   * @param position the position where the sphere should be placed
   * @param drawOutlineOnly if it is true only the sphere will be rendered as wireframe
   * @return the sphere */
  def createSphere(radius: Double, color: Color, position: Point3D, drawOutlineOnly: Boolean): Sphere = {
    val MESH_DIVISIONS = 5 //this is low for performance reasons
    val sphere = new Sphere(radius, MESH_DIVISIONS) {material = createMaterial(color)}
    sphere.moveTo(position)
    if(drawOutlineOnly) sphere.setDrawMode(DrawMode.Line)
    optimize(sphere) match {case sphere: Sphere => sphere}
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
  def createLine(points: (Point3D, Point3D), visible: Boolean, color: java.awt.Color, thickness: Double): Cylinder = {
    val line = createCylinder(points._1, points._2, thickness)
    line.setColor(color)
    line.setVisible(visible)
    optimize(line) match {case line: Cylinder => line}
  }

  private final def optimize(node: Node): Node = {node.cache = true; node.setCacheHint(CacheHint.Speed); node}

  /**
   * Creates a 3d cylinder. From https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
   * */
  private def createCylinder(origin: Point3D, target: Point3D, thickness: Double) = {
    val differenceVector = target.subtract(origin)
    val lineMiddle = target.midpoint(origin)
    val moveToMidpoint = new Translate(lineMiddle.getX, lineMiddle.getY, lineMiddle.getZ)
    val axisOfRotation = differenceVector.crossProduct(Rotate.YAxis)
    val angle = FastMath.acos(differenceVector.normalize.dotProduct(Rotate.YAxis))
    val rotateAroundCenter = new Rotate(-Math.toDegrees(angle), new Point3D(axisOfRotation))
    val line = new Cylinder(thickness, differenceVector.magnitude, 3) //low divisions for performance reasons
    line.getTransforms.addAll(moveToMidpoint, rotateAroundCenter)
    line
  }
}
