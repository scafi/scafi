package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateConcept
import it.unibo.scafi.simulation.gui.model.core.{Shape, World}

/**
  * a seed used to initialize a world
  * @tparam D the type of device producer
  * @tparam B the type of boundary
  * @tparam S the shape type
  */
trait WorldSeed[D <: AggregateConcept#DeviceProducer,
                B <: World#Boundary,
                S <: Shape] {
  def shape : Option[S]

  def boundary : Option[B]

  def deviceSeed : DeviceSeed[D]
}
