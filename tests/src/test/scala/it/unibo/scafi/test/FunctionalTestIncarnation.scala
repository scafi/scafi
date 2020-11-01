/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test

import it.unibo.scafi.incarnations.AbstractTestIncarnation
import it.unibo.scafi.languages.TypesInfo
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.Simulation

object FunctionalTestIncarnation extends AbstractTestIncarnation with Simulation with StandardLibrary {
  import it.unibo.scafi.languages.TypesInfo.Bounded
  override implicit val idBounded: Bounded[ID] = TypesInfo.Bounded.of_i
}