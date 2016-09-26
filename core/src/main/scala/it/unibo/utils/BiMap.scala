package it.unibo.utils

import scala.collection.mutable.{ Map => MMap }

/**
 * @author Roberto Casadei
 *
 */

class BiMap[X,Y](m: Map[X,Y]) {
  private val map = MMap(m.toSeq :_ *)
  private val rmap = map.map(_.swap)

  def apply(k: X) = map(k)
  def apply(v: Y, biMap: BiMap.ByValue.type) = rmap(v)

  def contains(k: X) = map.contains(k)

  def getByKey(k: X): Option[Y] = map.get(k)
  def getByValue(v: Y): Option[X] = rmap.get(v)

  def +=(tp:(X,Y)): Unit = this.+=(tp._1, tp._2)
  def +=(k:X, v:Y): Unit = { map += (k->v); rmap += (v->k)}
  def -=(k:X) = { val v = map(k); map -= k; rmap -= v }
  def =-(v:Y) = { val k = rmap(v); map -= k; rmap -= v }

  def keys = map.keys
}

object BiMap {
  object ByValue

  def apply[X,Y](m: Map[X,Y]) = new BiMap(m)
  def apply[X,Y](tpls: (X,Y)*) = new BiMap[X,Y](Map(tpls:_*))
}