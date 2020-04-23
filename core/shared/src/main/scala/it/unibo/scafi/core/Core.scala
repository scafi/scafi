/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.core

/**
 * This trait is the root of the family polymorphism (i.e., component-based) hierarchy.
 * It provides the basic interfaces and types
 */

trait Core {

  /**
   *  Name of local sensors (sensors receiving information from a node)
   */
  type LSNS

  /**
   *  Name of neighbourhood sensors (sensors receiving information from neighbours, like estimated distances)
   */
  type NSNS

  /**
   *  The unique identifier of a node
   */
  type ID

  /**
   *  The output of a computation round in a node
   *  Bounded to have at least a root element, as of Export interface
   */
  type EXPORT <: Export

  /**
   *  The input of a computation round in a node
   *  Bounded as of Context interface
   */
  type CONTEXT <: Context

  /**
   *  A computation round, as an I/O function
   */
  type EXECUTION <: (CONTEXT => EXPORT)

  trait Export {
    def root[A](): A
  }

  trait Context {
    def selfId: ID
    def exports(): Iterable[(ID,EXPORT)]
    def sense[T](lsns: LSNS): Option[T]
    def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T]
  }
}
