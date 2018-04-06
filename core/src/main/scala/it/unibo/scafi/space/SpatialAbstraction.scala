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

package it.unibo.scafi.space

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.Socket

import it.unibo.scafi.space.optimization.distances.EuclideanDistanceMetric
import it.unibo.scafi.space.optimization.helper.{DenseMultiVector, MultiVector}
import it.unibo.scafi.space.optimization.nn.QuadTree

import scala.language.higherKinds
import it.unibo.utils.BiMap

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


/**
  * Component which represents a spatial abstraction
  */

trait SpatialAbstraction {
  type P // Type for "position"

  type SPACE[E] <: Space[E] // Type for "spatial container" of elements E

  trait NeighbouringRelation {
    def neighbouring(p1: P, p2: P): Boolean
  }

  def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E]

  trait Space[E] extends NeighbouringRelation {
    def contains(e: E): Boolean
    def getLocation(e: E): P
    def getAt(p: P): Option[E]
    def getAll(): Iterable[E]
    def getNeighbors(e: E): Iterable[E] = {
      val p1 = getLocation(e)
      getAll().filter(e2 => neighbouring(p1, getLocation(e2)))
    }
  }

  trait MutableSpace[E] extends Space[E] {
    def add(e: E, p: P): Unit
    def remove(e: E): Unit
    def setLocation(e: E, p: P): Unit
  }
}

trait AdHocSpatialAbstraction extends SpatialAbstraction {
  type P

  override type SPACE[E] <: AdHocSpace[E]

  abstract class AdHocSpace[E](nbrs: Map[E,Set[E]]) extends MutableSpace[E] {
    def elementToPosition(e: E): P

    private val elems = scala.collection.mutable.Map[E,Set[E]](nbrs.toSeq :_ *)
    private val positions = BiMap[E,P](nbrs.keys.map(e => (e -> elementToPosition(e))).toSeq :_ *)

    override def add(e: E, p: P): Unit = positions += (e->p)

    override def setLocation(e: E, p: P): Unit = positions += (e->p)

    override def remove(e: E): Unit = positions -= e

    override def getLocation(e: E): P = positions(e)

    override def getAll(): Iterable[E] = positions.keys

    override def getAt(p: P): Option[E] = positions.getByValue(p)

    override def neighbouring(p1: P, p2: P): Boolean = {
      (getAt(p1), getAt(p2)) match {
        case (Some(e1), Some(e2)) => elems(e1).contains(e2)
        case _ => false
      }
    }

    override def contains(e: E): Boolean = positions.contains(e)

    // Override for performance reasons (the base impl depends on neighbouring())
    override def getNeighbors(e: E): Iterable[E] = elems.getOrElse(e,Set())
  }
}

trait BasicAdHocSpatialAbstraction extends AdHocSpatialAbstraction {
  type P = Int

  override type SPACE[E] = BasicAdHocSpace[E]

  class BasicAdHocSpace[E](nbrs: Map[E,Set[E]]) extends AdHocSpace[E](nbrs) {
    override def elementToPosition(e: E): Int = e.hashCode()
  }
}

trait MetricSpatialAbstraction extends SpatialAbstraction {
  override type SPACE[E] <: Space[E] with DistanceStrategy

  type D // Type for "distance"

  implicit val positionOrdering: Ordering[P]

  trait DistanceStrategy {
    def getDistance(p1: P, p2: P): D
  }

  trait MetricSpace[E] extends Space[E] with DistanceStrategy {
    def getNeighborsWithDistance(e: E) : Iterable[(E,D)] =
      getNeighbors(e) map (elem =>
        (elem, getDistance(getLocation(elem), getLocation(e))))
  }

  trait MutableMetricSpace[E] extends MetricSpace[E] with MutableSpace[E]
}

trait BasicSpatialAbstraction extends MetricSpatialAbstraction {
  override type P <: Point3D
  override type D = Double

  override type SPACE[E] = Space3D[E]

  implicit val positionOrdering: Ordering[P] = new Ordering[P] {
    override def compare(a: P, b: P): Int = {
      if (a.z > b.z){ +1 }
      else if (a.z < b.z){ -1 }
      else if (a.y > b.y){ +1 }
      else if (a.y < b.y){ -1 }
      else if (a.x > b.x){ +1 }
      else if (a.x < b.x){ -1 }
      else { 0 }
    }
  }

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap)

  abstract class Space3D[E](var elemPositions: Map[E,P],
                    override val proximityThreshold: Double) extends MutableMetricSpace[E]
    with EuclideanStrategy
    with Serializable {
    def add(e: E, p: P): Unit = elemPositions += (e -> p)
    def getLocation(e: E): P = elemPositions(e)
    def getAll(): Iterable[E] = elemPositions.keys
    def remove(e: E): Unit = elemPositions -= e

    override def getNeighbors(e: E): Iterable[E] = getNeighborsWithDistance(e) map (_._1)

    override def getAt(p: P): Option[E] = elemPositions.find(_._2 == p).map(_._1)

    override def contains(e: E): Boolean = elemPositions.contains(e)
  }
  class Basic3DSpace[E](pos: Map[E,P],
                        proximityThreshold: Double = EuclideanStrategy.DefaultProximityThreshold)
    extends Space3D[E](pos,proximityThreshold){

    var neighbourhoodMap: Map[E, Set[(E,D)]] = initNeighbours()

    def initNeighbours(): Map[E, Set[(E,D)]] = {
      var result = Map[E, Set[(E,D)]]()
      elemPositions.foreach(elem => { result += (elem._1 -> this.calculateNeighbours(elem._1).toSet) })
      result
    }

    def setLocation(e: E, p: P): Unit = {
      add(e, p)
      var newNeighbours: Set[(E,D)] = this.calculateNeighbours(e).toSet
      var oldNeighbours: Set[(E,D)] = neighbourhoodMap(e)
      if(oldNeighbours != newNeighbours){
        var noMoreNeighbours: Set[(E,D)] = oldNeighbours.filter(el => !newNeighbours.exists(newNbr => newNbr._1==el._1))
        var brandNewNeighbours: Set[(E,D)] = newNeighbours.filter(el => !oldNeighbours.exists(old => old._1==el))
        for (elem <- noMoreNeighbours) {
          neighbourhoodMap += (elem._1 -> this.calculateNeighbours(elem._1).toSet)
        }
        for (elem <- brandNewNeighbours) {
          neighbourhoodMap += (elem._1 -> this.calculateNeighbours(elem._1).toSet)
        }
        neighbourhoodMap += (e -> newNeighbours)
      }
    }

    private def calculateNeighbours(e: E): Iterable[(E,D)] = {

      val p1 = getLocation(e)
      getAll()
        .filter(nbr => nbr != e && neighbouring(p1,getLocation(nbr)))
       .map(e2 => (e2, getDistance(getLocation(e), getLocation(e2))))
    }

    override def getNeighbors(e: E): Iterable[E] = getNeighborsWithDistance(e) map (_._1)
    override def getNeighborsWithDistance(e: E): Iterable[(E, D)] = neighbourhoodMap(e)
  }

  trait EuclideanStrategy extends DistanceStrategy
    with NeighbouringRelation with Serializable {
    val proximityThreshold: Double
    def neighbouring(p1: P, p2: P): Boolean =
      getDistance(p1,p2) <= proximityThreshold

    override def getDistance(p1: P, p2: P): Double = {
      p1.distance(p2)
    }
  }

  object EuclideanStrategy {
    val DefaultProximityThreshold: Double = 1
  }

  class Tile38Space[E](pos : Map[E,P],radius : Double) extends Space3D[E](pos,radius) {
    val RadiusTile38 = 11133
    val realRadius = radius * RadiusTile38
    var nMap: Map[E, Set[E]] = Map.empty
    val sock = new Socket("localhost",9851)
    val out = new PrintStream(sock.getOutputStream)
    val input = new BufferedReader(new InputStreamReader(sock.getInputStream))
    initTile38()
    private def initTile38() = {
      val baseString = "set scafi "
      val builder = new mutable.StringBuilder()
      elemPositions.foreach { x => {
        builder.append(baseString)
        val id = x._1
        val (xc,yc,zc) = point2tuple(x._2)
        builder.append(s"$id point $xc $yc $zc\n")
      }}
      out.println(builder.toString)
      for(i <- 0 until elemPositions.size) {input.readLine}
    }
    override def setLocation(e: E, p: P): Unit = {
      resetNeighbours(e)
      calculateNeighbours(e)
      nMap.get(e).last map {x => x -> ((nMap.get(x).getOrElse(Set.empty)) + e)} foreach {x => nMap += x}
    }

    private def resetNeighbours(e: E): Unit = {nMap.get(e).last.foreach {x => { nMap -= x }}}
    private def calculateNeighbours(e: E): Unit = {
      synchronized {
        val (x,y,z) = point2tuple(this.elemPositions(e))
        out.println(s"nearby scafi point $x $y $realRadius")
        val res = input.readLine()
        val array = new mutable.ArrayBuffer[E]()
        if(res.charAt(0) == '*') {
          if(res.charAt(1) == '2') {
            val checkNumber = input.readLine()
            val response = input.readLine().substring(1).toInt
            0 until response foreach {x => {
              val arraySize = input.readLine()
              val lengthId = input.readLine()
              val id = input.readLine.toInt
              array += id.asInstanceOf[E]
              val pointSize = input.readLine()
              val point = input.readLine()
            }
            }
          }
        }
        this.nMap += e -> array.toSet
      }

    }


    override def add(e: E, p: P): Unit = {
      val (x,y,z) = point2tuple(p)
      out.println(s"set scafi $e point $x $y $z")
      input.readLine()
      super.add(e,p)
    }
    override def remove(e: E): Unit = {
      out.println(s"del scafi $e")
      input.readLine()
      super.remove(e)
    }
    override def getNeighbors(e: E): Iterable[E] = {
      if(nMap.get(e).isEmpty) {
        calculateNeighbours(e)
      }
      nMap(e)
    }
    override def getNeighborsWithDistance(e: E): Iterable[(E, D)] = List()//neighbourhoodMap(e)
    //TODO
    override def getAt(p: P): Option[E] = None

    override def contains(e: E): Boolean = elemPositions.contains(e)

    def point2tuple(p: P) : (Double,Double,Double) = (p.x,p.y,p.z)
  }

  class QuadTreeSpace[E](pos : Map[E,P],radius : Double) extends Space3D[E](pos,radius) {
    var nMap: Map[E, Set[E]] = Map.empty
    val quadTreeIndex = new QuadTree[E](Point3D(0,0,0),Point3D(2000,2000,2000),EuclideanDistanceMetric(),200)
    initQuadTree()
    private def initQuadTree() = {
      elemPositions.foreach { x =>quadTreeIndex.insert(x._2,x._1)}
    }
    override def setLocation(e: E, p: P): Unit = {
      resetNeighbours(e)
      quadTreeIndex.remove(elemPositions(e))
      quadTreeIndex.insert(p,e)
      elemPositions += e -> p
      calculateNeighbours(e)
      resetNeighbours(e)
    }

    private def resetNeighbours(e: E): Unit = {nMap.get(e).last.foreach {x => { nMap -= x }}}
    private def calculateNeighbours(e: E): Unit = {
      synchronized {
        val neigh : (E,Set[E]) = e -> (quadTreeIndex.searchNeighbors(elemPositions(e),radius) map {_._2} toSet)
        this.nMap += neigh
      }
    }


    override def add(e: E, p: P): Unit = {
      quadTreeIndex.insert(p,e)
      super.add(e,p)
    }
    override def remove(e: E): Unit = {
      quadTreeIndex.remove(elemPositions(e))
      super.remove(e)
    }
    override def getNeighbors(e: E): Iterable[E] = {
      if(nMap.get(e).isEmpty) {
        calculateNeighbours(e)
      }
      nMap(e)
    }
    override def getNeighborsWithDistance(e: E): Iterable[(E, D)] = List()//neighbourhoodMap(e)
    //TODO
    override def getAt(p: P): Option[E] = None

    override def contains(e: E): Boolean = elemPositions.contains(e)

    implicit def pointToVector(p : Point3D): MultiVector = DenseMultiVector(p.x,p.y,p.z)

    implicit def vectorToPoint(v : MultiVector) : Point3D = {
      require(v.size == 2)
      Point3D(v(0),v(1),v(2))
    }
  }
}
