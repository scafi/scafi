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

import scala.language.higherKinds

import it.unibo.utils.BiMap

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
    def setLocation(e: E, p: P)
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

  override type SPACE[E] = Basic3DSpace[E]

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

  class Basic3DSpace[E](var elemPositions: Map[E,P],
                        override val proximityThreshold: Double = EuclideanStrategy.DefaultProximityThreshold)
    extends MutableMetricSpace[E]
    with EuclideanStrategy
    with Serializable {
    def add(e: E, p: P): Unit = elemPositions += (e -> p)
    def getLocation(e: E): P = elemPositions(e)
    def getAll(): Iterable[E] = elemPositions.keys
    def remove(e: E): Unit = elemPositions -= e
    def setLocation(e: E, p: P): Unit = add(e, p)

    override def getNeighbors(e: E): Iterable[E] = getNeighborsWithDistance(e) map (_._1)
    override def getNeighborsWithDistance(e: E): Iterable[(E, Double)] = {
      val p1 = getLocation(e)
      getAll().
        map(e2 => (e2, getDistance(getLocation(e), getLocation(e2))))
        .filter(tp => tp._1 != e && neighbouring(p1,getLocation(tp._1)))
    }

    override def getAt(p: P): Option[E] = elemPositions.find(_._2 == p).map(_._1)

    override def contains(e: E): Boolean = elemPositions.contains(e)
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
}
