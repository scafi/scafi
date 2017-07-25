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

package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, NbrPolicy, Network, Node}

class NetworkImpl(val nodes: Map[Int,Node], var neighbourhoodPolicy: NbrPolicy) extends Network {
  calculateNeighbours

  def neighbourhood: Map[Node, Set[Node]] = {
    return calculateNeighbours
  }

  def observableValue: Set[String] = {
    Set[String]("Neighbours",
      "Id",
      "Export",
      "A sensor",
      "None")
  }

  private def calculateNeighbours: Map[Node, Set[Node]] = {
    var neighbours = Set[Node]()
    var res = Map[Node, Set[Node]]()

    var nbrRadius: Double = neighbourhoodPolicy match {
      case EuclideanDistanceNbr(radius) => radius
      case _ => Settings.Sim_NbrRadius
    }

    for (n <- nodes.values) {
      neighbours = Set()
      n.removeAllNeghbours
      for (n1 <- nodes.values) {
        val distance: Double = Math.hypot(n.position.getX - n1.position.getX, n.position.getY - n1.position.getY)
        if (distance <= nbrRadius) {
          neighbours += n1
        }
      }
      n.addAllNeighbours(neighbours)
      res += n -> neighbours
    }
    return res
  }
}
