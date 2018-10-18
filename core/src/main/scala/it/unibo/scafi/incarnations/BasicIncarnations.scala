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

package it.unibo.scafi.incarnations

import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.BasicTimeAbstraction
import it.unibo.utils.{Interop, Linearizable}

/**
 * An aggregate-programming system is ultimately created
 * as an object taking code from
 *  - Engine (aggregate programming machine),
 *  - Features (all linguistic elements), and
 *  - Simulation (to simulate).
 * It gives final concrete types for sensor IDs and device IDs
 */

trait BasicAbstractIncarnation extends Incarnation {
  override type LSNS = String
  override type NSNS = String
  override type ID = Int
  override type EXECUTION = AggregateInterpreter

  override val LSNS_POSITION: String = "position"
  override val LSNS_TIME: String = "currentTime"
  override val LSNS_TIMESTAMP: String = "timestamp"
  override val LSNS_DELTA_TIME: String = "deltaTime"
  override val LSNS_RANDOM: String = "randomGenerator"
  override val NBR_RANGE_NAME: String = "nbrRange"
  override val NBR_DELAY: String = "nbrDelay"
  override val NBR_LAG: String = "nbrLag"
  override val NBR_VECTOR: String = "nbrVector"

  @transient implicit override val linearID: Linearizable[ID] = new Linearizable[ID] {
    override def toNum(v: ID): Int = v
    override def fromNum(n: Int): ID = n
  }
  @transient implicit override val interopID: Interop[ID] = new Interop[ID] {
    def toString(id: ID): String = id.toString
    def fromString(str: String) = str.toInt
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

class AbstractTestIncarnation
  extends BasicAbstractIncarnation
    with BasicSpatialAbstraction
    with BasicTimeAbstraction {
}
