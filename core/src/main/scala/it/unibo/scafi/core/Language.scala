package it.unibo.scafi.core

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 * This trait defines a component that requires to be "attached" to Core
 * It defines a trait with the "syntax" of language constructs (based on field-calculus)
 *
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
    def mid(): ID
    def nbr[A](expr: => A): A
    def rep[A](init: A)(fun: (A) => A): A
    def foldhood[A](init: => A)(aggr: (A, A) => A)(expr: => A): A
    def aggregate[A](f: => A): A
    def sense[A](name: LSNS): A
    def nbrvar[A](name: NSNS): A
  }

}
