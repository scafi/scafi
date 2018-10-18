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

import it.unibo.scafi.lib.{Bounded, StandardLibrary}
import it.unibo.scafi.simulation.{Simulation, SpatialSimulation}
import it.unibo.scafi.space.BasicSpatialAbstraction
import it.unibo.scafi.time.BasicTimeAbstraction

trait BasicAbstractSimulationIncarnation
  extends BasicAbstractIncarnation
  with Simulation
  with BasicTimeAbstraction {

  override val LSNS_RANDOM: String = "randomGenerator"
}

object BasicSimulationIncarnation
  extends BasicAbstractSimulationIncarnation
    with StandardLibrary {

  override implicit val idBounded: Bounded[ID] = Bounded.of_i
}

class BasicAbstractSpatialSimulationIncarnation
  extends BasicAbstractSimulationIncarnation
    with SpatialSimulation
    with BasicSpatialAbstraction
