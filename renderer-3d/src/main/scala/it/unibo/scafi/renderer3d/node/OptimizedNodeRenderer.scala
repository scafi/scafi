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

import org.fxyz3d.shapes.primitives.ScatterMesh
import scalafx.geometry.Point3D
import scalafx.scene.paint.Color
import org.scalafx.extras._
import collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap, Set => MutableSet}

/**
  Use this methods with multiple nodes, batching and reducing the method calls, otherwise it gets too slow.
 Call updateMesh when you want to show the effects of the previous method calls, but not too frequently, since
 it's costly.
* */
class OptimizedNodeRenderer {

  private val MAX_HSV_VALUE = 255
  private val scatterMesh = new ScatterMesh(java.util.Arrays.asList(), true, 1, 0)
  private val nodesColorInUI = MutableMap[org.fxyz3d.geometry.Point3D, Int]()
  private val nodesInUI = MutableSet[NetworkNode]()

  scatterMesh.setTextureModeVertices3D(MAX_HSV_VALUE, position => Integer.valueOf(nodesColorInUI(position)))

  def addNodes(nodes: List[(NetworkNode, Color)]): Unit = synchronized {
    nodesColorInUI ++= nodes.map(node => (toFxyzPoint(node._1.getNodePosition), toHSVInteger(node._2)))
    nodesInUI ++= nodes.map(_._1)
  }

  private def toHSVInteger(color: Color): Int = color.hue.toInt

  def updateMesh(): Unit = synchronized {
    val points = nodesColorInUI.keys.toList.asJava
    onFX {scatterMesh.setScatterData(points)}
  }

  private def toFxyzPoint(point: Point3D): org.fxyz3d.geometry.Point3D =
    new org.fxyz3d.geometry.Point3D(point.x, point.y, point.z)

  /**
   * Call this before actually moving the NetworkNode
   * */
  def moveNodes(nodes: List[(NetworkNode, Point3D)]): Unit = synchronized {
    nodes.foreach(node => if(isAKnownNode(node._1)){
      val oldNodePosition = toFxyzPoint(node._1.getNodePosition)
      val color = nodesColorInUI.remove(oldNodePosition)
      color.fold()(color => nodesColorInUI += (toFxyzPoint(node._2) -> color))
    })
  }

  private def isAKnownNode(node: NetworkNode): Boolean = nodesInUI.contains(node)

  /**
   * Call this only if the nodes position has not changed yet. Otherwise, call moveNodes before this.
   * */
  def setNodesColor(nodes: List[(NetworkNode, Color)]): Unit = synchronized {
    nodes.foreach(node => if(isAKnownNode(node._1)){
      nodesColorInUI.update(toFxyzPoint(node._1.getNodePosition), toHSVInteger(node._2))
    })
  }

}
