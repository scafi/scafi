package it.unibo.scafi.core

/**
 * @author Mirko Viroli
 * @author Roberto Casadei
 *
 * This trait is the root of the family polymorphism (i.e., component-based) hierarchy.
 * It provides the basic interfaces and types
 *
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

  trait Callable {
    type MainResult
    def main(): MainResult
  }

  trait Export {
    def root[A](): A
  }

  trait Context {
    def selfId: ID
    def exports(): Iterable[(ID,EXPORT)]
    def sense[T](lsns: LSNS): Option[T]
    def nbrSense[T](nsns: NSNS)(nbr: ID): Option[T]
  }

  trait Interop[T] extends Serializable {
    def toString: String
    def fromString(s: String): T
  }

  trait LinearizableTo[T,N] extends Serializable {
    def toNum(v: T): N
    def fromNum(n: N): T
  }
  trait Linearizable[T] extends LinearizableTo[T,Int]

  def log(msg: String) = {}

  implicit val linearID: Linearizable[ID]
  implicit val interopID: Interop[ID]
  implicit val interopLSNS: Interop[LSNS]
  implicit val interopNSNS: Interop[NSNS]
}
