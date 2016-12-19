package it.unibo.scafi.test

import it.unibo.scafi.incarnations.Incarnation

/**
 * Created by: Roberto Casadei
 * Created on date: 11/12/15
 */

object CoreTestIncarnation extends Incarnation {
  override type LSNS = String
  override type NSNS = String
  override type ID = Int

  override type EXECUTION = Execution

  implicit val NBR_RANGE_NAME: NSNS = "nbrRange"

  class Execution extends ExecutionTemplate with Constructs with Builtins {
    override type MainResult = AnyRef
    override def main(): AnyRef = null
  }

  implicit override val linearID: Linearizable[ID] = new Linearizable[ID] {
    override def toNum(v: ID): Int = v
    override def fromNum(n: Int): ID = n
  }
  implicit override val interopID: Interop[ID] = new Interop[ID] {
    def toString(id: ID): String = id.toString
    def fromString(str: String) = str.toInt
  }
  implicit override val interopLSNS: Interop[LSNS] = new Interop[LSNS] {
    def toString(lsns: LSNS): String = lsns.toString
    def fromString(str: String): LSNS = str
  }
  implicit override val interopNSNS: Interop[NSNS] = new Interop[NSNS] {
    def toString(nsns: NSNS): String = nsns.toString
    def fromString(str: String): NSNS = str
  }
}