/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.p2p

import it.unibo.scafi.distrib.actor.p2p.{Platform => BasePlatform}

trait SpatialPlatform extends BasePlatform {
  val LocationSensorName: LSensorName
}
