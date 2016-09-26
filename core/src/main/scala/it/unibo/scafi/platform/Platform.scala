package it.unibo.scafi.platform

import it.unibo.scafi.core.{Language, Engine, Core}

/**
 * @author mirko
 *
 * This trait defines a component that requires to be "attached" to Core
 * It defines a whole platform view, with a NETWORK type modelling the whole computational system
 *
 */

trait Platform { self: Platform.PlatformDependency =>

  type NETWORK <: Network

  trait Network {
    def ids: Set[ID]
    def neighbourhood(id: ID): Set[ID]
    def localSensor[A](name: LSNS)(id: ID): A
    def nbrSensor[A](name: NSNS)(id: ID)(idn: ID): A
    def export(id: ID): Option[EXPORT]
    def exports(): Map[ID, Option[EXPORT]]
  }

}

object Platform {
  type PlatformDependency = Core with Engine
}
