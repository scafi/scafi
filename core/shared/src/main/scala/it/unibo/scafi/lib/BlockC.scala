/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BlockC {
  selfcomp: StandardLibrary.Subcomponent =>

  // scalastyle:off method.name

  import Builtins.Bounded

  implicit val idBounded: Bounded[ID]

  trait BlockC {
    self: FieldCalculusSyntax with StandardSensors =>

    def smaller[V: Bounded](a: V, b: V): Boolean =
      implicitly[Bounded[V]].compare(a, b) < 0

    def findParent[V: Bounded](potential: V): ID =
      mux(smaller(minHood { nbr(potential) }, potential)) {
        minHood { nbr { (potential, mid()) } }._2
      } {
        implicitly[Bounded[ID]].top
      }

    def C[P: Bounded, V](potential: P, acc: (V, V) => V, local: V, Null: V): V =
      rep(local) { v =>
        acc(local, foldhood(Null)(acc) {
          mux(nbr(findParent(potential)) == mid()) { nbr(v) } { nbr(Null) }
        })
      }

  }

}
