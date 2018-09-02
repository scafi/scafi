package it.unibo.scafi.simulation.gui.configuration.seed

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateConcept

/**
  * device configuration is used to describe a sequence of device producer
  * @tparam D the type of device producer
  */
trait DeviceConfiguration[D <: AggregateConcept#DeviceProducer] {
  /**
    * @return a sequence of device producer
    */
  def devices : Iterable[D]
}
