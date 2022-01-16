/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.space

import it.unibo.scafi.space.SpatialAbstraction.Bound
import it.unibo.scafi.space.optimization.nn.NNIndex
import it.unibo.utils.BiMap

import scala.collection.concurrent.TrieMap
import scala.language.higherKinds


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

object SpatialAbstraction {
  case class Bound(inclusive: Shape, exclusive: List[(Shape,Point3D)] = List.empty) {
    import optimization._
    def accept(p: Point3D) : Boolean = {
      inclusive.contains(p) && exclusive.forall(x => !x._1.contains(p - x._2))
    }
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

    override def add(e: E, p: P): Unit = {
      elemPositions += (e -> p)
      if (!neighbourhoodMap.contains(e)) neighbourhoodMap += (e -> Set())
    }
    override def getAt(p: P): Option[E] = elemPositions.find(_._2 == p).map(_._1)

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

  /**
    * a space that used quad tree index to compute neighbour
    * @param pos the position of noe
    * @param radius radius of neighbour range
    * @tparam E the type of node
    */
  class QuadTreeSpace[E](pos : Map[E,P], radius : Double, bound : Option[Bound] = None) extends Space3D[E](pos,radius) {
    private val nMap: TrieMap[E, Set[E]] = TrieMap.empty
    //TODO CREATE AN INDEX THAT INCREASE HIS SIZE WITH NODE POSITIONING
    private val neighbourIndex: NNIndex[E] = NNIndex(pos)
    override def setLocation(e: E, p: P): Unit = if(bound.isEmpty || bound.exists(_.accept(p))) {
      resetNeighbours(e)
      neighbourIndex -= (elemPositions(e))
      neighbourIndex += (p -> e)
      elemPositions += e -> p
      calculateNeighbours(e)
      addNeighbours(e)
    }

    private def resetNeighbours(e: E): Unit = {nMap.get(e).last.foreach {x => { nMap += x -> (nMap(x) - e) }}}

    private def addNeighbours(e: E): Unit = {nMap.get(e).last.foreach {x => { nMap += x -> (nMap(x) + e) }}}
    private def calculateNeighbours(e: E): Unit = {
      val neigh: (E, Set[E]) = e -> (neighbourIndex.neighbours(elemPositions(e), radius).map {
        _._2
      }.toSet)
      this.nMap += neigh
    }


    override def add(e: E, p: P): Unit = {
      neighbourIndex += (p -> e)
      super.add(e,p)
    }
    override def remove(e: E): Unit = {
      neighbourIndex -= (elemPositions(e))
      super.remove(e)
    }
    //TODO CHECK IF TWO ELEMENTS ARE IN THE SAME POSITION
    override def getNeighbors(e: E): Iterable[E] = {
      if(!nMap.contains(e)) {
        calculateNeighbours(e)
      }
      if(nMap.contains(e)) {
        nMap(e)
      } else {
        List()
      }
    }
    override def getNeighborsWithDistance(e: E): Iterable[(E, D)] = {
      val p = elemPositions(e)
      getNeighbors(e) map {x => x -> elemPositions(x).distance(p)}
    }

    override def getAt(p: P): Option[E] = neighbourIndex.get(p)

    override def contains(e: E): Boolean = elemPositions.contains(e)

  }
}
