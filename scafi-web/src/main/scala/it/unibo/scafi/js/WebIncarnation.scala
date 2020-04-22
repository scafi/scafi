package it.unibo.scafi.js

import it.unibo.scafi.incarnations.Incarnation
import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}
import it.unibo.utils.{Interop, Linearizable}

trait BasicWebIncarnation extends Incarnation with Simulation {
  override type CNAME = String
  override type ID = String
  override type EXECUTION = AggregateInterpreter

  override val LSNS_POSITION: String = "position"
  override val LSNS_TIME: String = "currentTime"
  override val LSNS_TIMESTAMP: String = "timestamp"
  override val LSNS_DELTA_TIME: String = "deltaTime"
  override val LSNS_RANDOM: String = "randomGenerator"
  override val NBR_RANGE: String = "nbrRange"
  override val NBR_DELAY: String = "nbrDelay"
  override val NBR_LAG: String = "nbrLag"
  override val NBR_VECTOR: String = "nbrVector"

  @transient implicit override val linearID: Linearizable[ID] = new Linearizable[ID] {
    override def toNum(v: ID): Int = Integer.parseInt(v)
    override def fromNum(n: Int): ID = n.toString
  }
  @transient implicit override val interopID: Interop[ID] = new Interop[ID] {
    def toString(id: ID): String = id
    def fromString(str: String): ID = str
  }
  @transient implicit override val interopCNAME: Interop[CNAME] = new Interop[CNAME] {
    def toString(lsns: CNAME): String = lsns.toString
    def fromString(str: String): CNAME = str
  }
}

object WebIncarnation extends BasicWebIncarnation
  with SpatialSimulation
  with BasicSpatialAbstraction {
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    buildSpatialContainer(elems, EuclideanStrategy.DefaultProximityThreshold)

  def buildSpatialContainer[E](elems: Iterable[(E,P)] = Iterable.empty,
                               range: Double = EuclideanStrategy.DefaultProximityThreshold): SPACE[E] =
    new Basic3DSpace(elems.toMap) with EuclideanStrategy {
      override val proximityThreshold = range
    }
}