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

    // Contextual, but foundational
    def mid(): ID
    def sense[A](name: LSNS): A
    def nbrvar[A](name: NSNS): A
  }

}
