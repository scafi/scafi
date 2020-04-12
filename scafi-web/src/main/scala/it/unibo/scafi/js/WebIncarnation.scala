package it.unibo.scafi.js

import it.unibo.scafi.incarnations.Incarnation
import it.unibo.scafi.simulation.Simulation
import it.unibo.utils.{Interop, Linearizable}

object WebIncarnation extends Incarnation with Simulation {
  override type LSNS = String
  override type NSNS = String
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
  @transient implicit override val interopLSNS: Interop[LSNS] = new Interop[LSNS] {
    def toString(lsns: LSNS): String = lsns.toString
    def fromString(str: String): LSNS = str
  }
  @transient implicit override val interopNSNS: Interop[NSNS] = new Interop[NSNS] {
    def toString(nsns: NSNS): String = nsns.toString
    def fromString(str: String): NSNS = str
  }
}