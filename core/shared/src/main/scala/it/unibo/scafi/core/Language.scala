/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.core

/**
 * This trait defines a component that requires to be "attached" to Core
 * It defines a trait with the "syntax" of language constructs (based on field-calculus)
 */

trait Language { self: Core =>

  /**
   * A field-calculus "behaviour" inherits from this trait.
   * Differences wrt the paper version are:
   * - foldhood is given a "universal quantification" semantics
   * - neighbour exposes whether we are in one such quantification
   * - mid is so foundational that we put it here
   *
   * A consequence is that we do not have field types,
   * only locals: so we integrate at best with Scala
   */
  trait Constructs {
    def nbr[A](expr: => A): A
    def rep[A](init: =>A)(fun: (A) => A): A
    def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A
    def aggregate[A](f: => A): A
    def align[K,V](key: K)(comp: K => V): V

    // Contextual, but foundational
    def mid(): ID
    def sense[A](name: CNAME): A
    def nbrvar[A](name: CNAME): A
  }

}
