package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.seed.WorldInformation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiDeviceProducers, scafiWorld}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle

/**
  * describe world seed in scafi context
  */
trait ScafiWorldInformation extends WorldInformation[scafiWorld.DEVICE_PRODUCER,scafiWorld.B,scafiWorld.S]

object ScafiWorldInformation {
  private class ScafiWorldInformationImpl(override val shape : Option[scafiWorld.S],
                                          override val boundary : Option[scafiWorld.B],
                                          override val deviceProducers : Iterable[scafiWorld.DEVICE_PRODUCER]) extends ScafiWorldInformation

  /**
    * a standard seed used to initialize a scafi world
    */
  val standard : ScafiWorldInformation = new ScafiWorldInformationImpl(Some(Rectangle(5,5)), None, ScafiDeviceProducers.standardConfiguration)

  /**
    * allow to create a scafi seed
    * @param shape the shape of node
    * @param boundary the world boundary
    * @param deviceSeed the device seed
    * @return scafi seed created
    */
  def apply(shape : Option[scafiWorld.S] = standard.shape,
            boundary : Option[scafiWorld.B] = standard.boundary,
            deviceSeed: Iterable[scafiWorld.DEVICE_PRODUCER] =  standard.deviceProducers): ScafiWorldInformation = new ScafiWorldInformationImpl(shape,boundary,deviceSeed)
}