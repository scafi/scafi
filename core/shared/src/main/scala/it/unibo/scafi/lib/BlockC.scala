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

    /**
      * Collects values down a potential field.
      * @param potential the field providing the direction of the collection process
      * @param acc the function describing how values are to be collected
      * @param local the value field providing the value to be collected for each device
      * @param Null zero value for accumulation and for neighbours for which the current device is *not* a parent in the field
      * @tparam P type of the potential field
      * @tparam V type of the values to be collected/accumulated
      * @return the accumulation value for each device according to its position in the potential field
      */
    def C[P: Bounded, V](potential: P, acc: (V, V) => V, local: V, Null: V): V =
      rep(local) { v =>
        acc(local, foldhood(Null)(acc) {
          mux(nbr(findParent(potential)) == mid()) { nbr(v) } { nbr(Null) }
        })
      }

    /**
      * @return the ID of the "parent device" along a `potential` field, i.e.,
      * the neighbour with the smallest value of the potential field.
      */
    def findParent[V: Bounded](potential: V): ID = {
      val (minPotential,devIdWithMinPotential) = minHood { nbr{ (potential, mid) } }
      mux(smaller(minPotential, potential)) {
        devIdWithMinPotential
      } {
        implicitly[Bounded[ID]].top
      }
    }

    /**
      * @return the parent device UID of the current device along field `potential`,
      *         or None if no such a parent is there (so that the device is parent of itself).
      */
    def findParentOpt[V: Bounded](potential: V): Option[ID] = {
      val minPotential = minHood { nbr{ (potential, mid) } }
      Some(minPotential).filter(p => smaller(p._1, potential)).map(_._2)
    }

    /**
      * @return the collection of the number of devices for which `predicate` is true
      */
    def collectCount(alongPotentialField: Double, predicate: Boolean): Long =
      C[Double,Long](alongPotentialField, _+_, if(predicate) 1 else 0, 0)

    /**
      * @return the collect field of the mean of the `value` field
      */
    def collectMean(alongPotentialField: Double, value: Double): Double = {
      val numNodesInArea = collectCount(alongPotentialField, true)
      val collectedValue = C[Double,Double](alongPotentialField, _+_, value, 0)
      collectedValue/numNodesInArea
    }

    /**
      * Collects a map by merging map entries
      * @param downTo potential field
      * @param local maps to be merged
      * @param merge function specifying how to merge entries with the same key
      * @return the collect field of the merged maps
      */
    def collectMaps[K,V](downTo: Double, local: Map[K,V], merge: (K,V,V)=>V = (k: K, v1: V, v2: V) => v1) =
      C[Double, Map[K,V]](downTo, (m1,m2) => {
        (m1 ++ m2) ++ (m1.keySet.intersect(m2.keySet)).map(k => k -> merge(k,m1(k),m2(k)))
      }, local, Map.empty)

    /**
      * Collects a set by merging sets
      */
    def collectSets[T](downTo: Double, local: Set[T]): Set[T] =
      C[Double, Set[T]](downTo, _.union(_), local, Set.empty)

    /**
      * Collects a set by merging values
      */
    def collectIntoSet[T](downTo: Double, local: T): Set[T] =
      collectSets(downTo, Set(local))

    /**
      * Collects values as a map by preserving the sources of those values
      */
    def collectValuesByDevices[T](downTo: Double, local: T): Map[ID,T] =
      collectSets(downTo, Set(mid -> local)).toMap

    private def smaller[V: Bounded](a: V, b: V): Boolean =
      implicitly[Bounded[V]].compare(a, b) < 0
  }

}
