package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.configuration.seed.{DeviceConfiguration, WorldInformation}
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiDeviceSeed, scafiWorld}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle

/**
  * describe world seed in scafi context
  */
trait ScafiWorldInformation extends WorldInformation[scafiWorld.DEVICE_PRODUCER,scafiWorld.B,scafiWorld.S]

object ScafiWorldInformation {
  private class ScafiWorldInformationImpl(override val shape : Option[scafiWorld.S],
                                          override val boundary : Option[scafiWorld.B],
                                          override val deviceSeed : DeviceConfiguration[scafiWorld.DEVICE_PRODUCER]) extends ScafiWorldInformation

  /**
    * a standard seed used to initialize a scafi world
    */
  val standard : ScafiWorldInformation = new ScafiWorldInformationImpl(Some(Rectangle(5,5)), None, ScafiDeviceSeed.standardConfiguration$)

  /**
    * allow to create a scafi seed
    * @param shape the shape of node
    * @param boundary the world boundary
    * @param deviceSeed the device seed
    * @return scafi seed created
    */
  def apply(shape : Option[scafiWorld.S] = standard.shape,
            boundary : Option[scafiWorld.B] = standard.boundary,
            deviceSeed: DeviceConfiguration[scafiWorld.DEVICE_PRODUCER] =  standard.deviceSeed): ScafiWorldInformation = new ScafiWorldInformationImpl(shape,boundary,deviceSeed)
}