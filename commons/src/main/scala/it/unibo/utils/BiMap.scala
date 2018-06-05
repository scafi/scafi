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
