/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.time

trait TimeAbstraction {
  type Time
}

trait BasicTimeAbstraction extends TimeAbstraction {
  type Time = java.time.Instant
}
