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
    *  Name of a capability, including
    *  - local sensors (sensors receiving information from a node)
    *  - neighbourhood sensors (sensors receiving information from neighbours, like estimated distances)
    */
  type CNAME

  def cnameFromString(s: String): CNAME

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

  /**
    * A generic "export", i.e., a coordination message to be emitted.
    */
  trait Export {
    /**
      * The root of the export
      * @tparam A
      * @return the root of the export structure, cast to type A
      */
    def root[A](): A
  }

  /**
    * A generic "context" affecting device-local execution of a ScaFi program.
    */
  trait Context {
    def selfId: ID
    def exports(): Iterable[(ID,EXPORT)]
    def sense[T](localSensorName: CNAME): Option[T]
    def nbrSense[T](nbrSensorName: CNAME)(nbr: ID): Option[T] = sense[ID=>T](nbrSensorName).map(_(nbr))
  }
}
