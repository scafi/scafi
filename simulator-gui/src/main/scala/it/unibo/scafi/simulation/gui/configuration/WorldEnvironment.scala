package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.model.core.World

/**
  * world environment, describe world configuration
  * @tparam W the world type
  */
trait WorldEnvironment[W <: World] {
  /**
    * @return world
    */
  def world : W

  /**
    * @return world command space
    */
  def commandSpace : CommandSpace
}
