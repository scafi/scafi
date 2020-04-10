/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.utils

import scala.collection.mutable.{ Map => MMap }

class BiMap[X,Y](m: Map[X,Y]) {
  private val map = MMap(m.toSeq :_ *)
  private val rmap = map.map(_.swap)

  def apply(k: X): Y = map(k)
  def apply(v: Y, biMap: BiMap.ByValue.type): X = rmap(v)

  def contains(k: X): Boolean = map.contains(k)

  def getByKey(k: X): Option[Y] = map.get(k)
  def getByValue(v: Y): Option[X] = rmap.get(v)

  def +=(tp:(X,Y)): Unit = this.+=(tp._1, tp._2)
  def +=(k:X, v:Y): Unit = { map += (k->v); rmap += (v->k)}
  def -=(k:X): Unit = { val v = map(k); map -= k; rmap -= v }
  def =-(v:Y): Unit = { val k = rmap(v); map -= k; rmap -= v }

  def keys: Iterable[X] = map.keys
}

object BiMap {
  object ByValue

  def apply[X,Y](m: Map[X,Y]): BiMap[X,Y] = new BiMap(m)
  def apply[X,Y](tpls: (X,Y)*): BiMap[X,Y] = new BiMap[X,Y](Map(tpls:_*))
}
