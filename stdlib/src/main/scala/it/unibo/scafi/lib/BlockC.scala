package it.unibo.scafi.lib

import it.unibo.scafi.core.Core

/**
  * @author Roberto Casadei
  *
  */

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