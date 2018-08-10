package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateConcept

/**
  * device seed is used to describe a sequence of device producer
  * @tparam D the type of device producer
  */
trait DeviceSeed[D <: AggregateConcept#DeviceProducer] {
  /**
    * @return a sequence of device producer
    */
  def devices : Iterable[D]
}
