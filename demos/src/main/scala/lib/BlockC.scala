package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import Builtins.Bounded

/**
  * @author Roberto Casadei
  *
  */
trait BlockC { self: AggregateProgram with SensorDefinitions =>

  def smaller[V: Bounded](a: V, b: V):Boolean =
    implicitly[Bounded[V]].compare(a,b)<0

  def findParent[V: Bounded](potential: V): ID = {
    mux(smaller(minHood { nbr(potential) }, potential)) {
      minHood { nbr { (potential, mid()) } }._2
    } {
      Int.MaxValue
    }
  }

  def C[V: Bounded](potential: V, acc: (V, V) => V, local: V, Null: V): V = {
    rep(local) { v =>
      acc(local, foldhood(Null)(acc) {
        mux(nbr(findParent(potential)) == mid()) {
          nbr(v)
        }{
          nbr(Null)
        }
      })
    }
  }
}
