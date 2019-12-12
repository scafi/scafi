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

import org.fxyz3d.geometry.{Point3D => FxPoint3D}
import org.fxyz3d.shapes.primitives.ScatterMesh
import scalafx.geometry.Point3D
import scalafx.scene.paint.Color
import org.scalafx.extras._

import collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap, Set => MutableSet}
import scalafx.scene.Scene

import scala.util.Random

/**
  Use this methods with multiple nodes, batching and reducing the method calls, otherwise it gets too slow.
 Call updateMesh when you want to show the effects of the previous method calls, but not too frequently, since
 it's costly.
* */
class OptimizedNodeRenderer {

  private val MAX_HSV_VALUE = 1530
  private val nodesColorInUI = MutableMap[FxPoint3D, Int]()
  private val nodesInUI = MutableSet[NetworkNode]()
  private val DEFAULT_CALLS_BEFORE_UPDATE = 10
  private var maxCallsBeforeUpdate = DEFAULT_CALLS_BEFORE_UPDATE
  private var callsBeforeUpdate = DEFAULT_CALLS_BEFORE_UPDATE
  private var scatterMesh: Option[ScatterMesh] = None
  private var defaultColor = 0

  def addNodes(nodes: List[(NetworkNode, Color)], scene: Scene): Unit = synchronized {
    nodesColorInUI ++= nodes.map(node => (toFxyzPoint(node._1.getNodePosition), toHSVInteger(node._2)))
    nodesInUI ++= nodes.map(_._1)
    updateMeshIfNeeded(scene)
  }

  private def toHSVInteger(color: Color): Int = { //TODO: this doesn't work
    var R = Math.round(255 * color.red)
    var G = Math.round(255 * color.green)
    var B = Math.round(255 * color.blue)

    R = (R << 16) & 0x00FF0000
    G = (G << 8) & 0x0000FF00
    B = B & 0x000000FF

    val result = -(0xFF000000 | R | G | B).toInt
    val OLD_PERCENT = result / MAX_HSV_VALUE
    255 * OLD_PERCENT
  }

  private def updateMeshIfNeeded(scene: Scene): Unit = {
    callsBeforeUpdate = callsBeforeUpdate - 1
    if(callsBeforeUpdate == 0) {
      onFX {createOrUpdateScatterMesh(nodesColorInUI.keys.toList.asJava, scene)}
      callsBeforeUpdate = maxCallsBeforeUpdate
    }
  }

  private def createOrUpdateScatterMesh(points: java.util.List[FxPoint3D], scene: Scene): Unit =
    if(scatterMesh.isEmpty){
      val scatterMesh1 = new ScatterMesh(points, true, 50, 0)
      scatterMesh = Option(scatterMesh1)
      scene.getChildren.add(scatterMesh1)
      scatterMesh.fold()(_.setTextureModeVertices3D(MAX_HSV_VALUE,
        position => Integer.valueOf(nodesColorInUI.getOrElse(position, defaultColor))))
    } else {
      scatterMesh.fold()(_.setScatterData(points))
    }

  private def toFxyzPoint(point: Point3D): org.fxyz3d.geometry.Point3D = new FxPoint3D(point.x, point.y, point.z)

  /**
   * Call this before actually moving the NetworkNode
   * */
  def moveNodes(nodes: List[(NetworkNode, Point3D)], scene: Scene): Unit = synchronized {
    nodes.foreach(node => if(isAKnownNode(node._1)){
      val oldNodePosition = toFxyzPoint(node._1.getNodePosition)
      val color = nodesColorInUI.remove(oldNodePosition)
      color.fold()(color => nodesColorInUI += (toFxyzPoint(node._2) -> color))
    })
    updateMeshIfNeeded(scene)
  }

  private def isAKnownNode(node: NetworkNode): Boolean = nodesInUI.contains(node)

  /**
   * Call this only if the position in NetworkNode is still up to date. Otherwise, call moveNodes before this.
   * */
  def setNodesColor(nodes: List[(NetworkNode, Color)], scene: Scene): Unit = synchronized {
    nodes.foreach(node => if(isAKnownNode(node._1)){
      nodesColorInUI.update(toFxyzPoint(node._1.getNodePosition), toHSVInteger(node._2))
    })
    updateMeshIfNeeded(scene)
  }

  def setMaxCallsBeforeUpdate(value: Int): Unit = synchronized{maxCallsBeforeUpdate = Math.max(value, 1)}

  def setDefaultColor(color: Color): Unit = synchronized {defaultColor = toHSVInteger(color)}
}

object OptimizedNodeRenderer {
  def apply(): OptimizedNodeRenderer = new OptimizedNodeRenderer()
}
