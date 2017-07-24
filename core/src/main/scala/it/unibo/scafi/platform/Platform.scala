package it.unibo.scafi.platform

import it.unibo.scafi.core.{Core, Engine, Language}
import it.unibo.scafi.space.SpatialAbstraction
import it.unibo.scafi.time.TimeAbstraction

/**
 * @author mirko
 *
 * This trait defines a component that requires to be "attached" to Core
 * It defines a whole platform view, with a NETWORK type modelling the whole computational system
 *
 */

trait Platform {
  self: Platform.PlatformDependency =>
}

object Platform {
  type PlatformDependency = Core with Engine
}

trait TimeAwarePlatform extends Platform {
  self: Platform.PlatformDependency with TimeAbstraction =>

  val LSNS_TIME: LSNS
  val LSNS_DELTA_TIME: LSNS
  val NBR_DELAY: NSNS
}

trait SpaceAwarePlatform extends Platform {
  self: Platform.PlatformDependency with SpatialAbstraction =>

  val LSNS_POSITION: LSNS
  val NBR_RANGE_NAME: NSNS
}

trait SpaceTimeAwarePlatform extends SpaceAwarePlatform with TimeAwarePlatform {
  self: SpaceTimeAwarePlatform.PlatformDependency =>
}

object SpaceTimeAwarePlatform {
  type PlatformDependency = Platform.PlatformDependency with SpatialAbstraction with TimeAbstraction
}

trait SimulationPlatform extends SpaceTimeAwarePlatform {
  self: SpaceTimeAwarePlatform.PlatformDependency =>

  val LSNS_RANDOM: LSNS

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

object SimulationPlatform {
  type PlatformDependency = SpaceTimeAwarePlatform.PlatformDependency
}