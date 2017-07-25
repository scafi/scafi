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

package it.unibo.scafi.lib

trait Stdlib_BlockC {
  selfcomp: StandardLibrary.Subcomponent =>

  import Builtins.Bounded
  import Bounded._

  implicit val idBounded: Bounded[ID]

  trait BlockC {
    self: AggregateProgram with StandardSensors =>

    def smaller[V: Bounded](a: V, b: V): Boolean =
      implicitly[Bounded[V]].compare(a, b) < 0

    def findParent[V: Bounded](potential: V): ID = {
      mux(smaller(minHood {
        nbr(potential)
      }, potential)) {
        minHood {
          nbr {
            (potential, mid())
          }
        }._2
      } {
        implicitly[Bounded[ID]].top
      }
    }

    def C[V: Bounded](potential: V, acc: (V, V) => V, local: V, Null: V): V = {
      rep(local) { v =>
        acc(local, foldhood(Null)(acc) {
          mux(nbr(findParent(potential)) == mid()) {
            nbr(v)
          } {
            nbr(Null)
          }
        })
      }
    }
  }

}
