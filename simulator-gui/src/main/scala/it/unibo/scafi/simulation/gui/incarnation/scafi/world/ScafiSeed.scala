package it.unibo.scafi.simulation.gui.incarnation.scafi.world

import it.unibo.scafi.simulation.gui.configuration.{DeviceSeed, WorldSeed}
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle

/**
  * describe world seed in scafi context
  */
trait ScafiSeed extends WorldSeed[scafiWorld.DEVICE_PRODUCER,scafiWorld.B,scafiWorld.S]

object ScafiSeed {
  private class ScafiSeedImpl(override val shape : Option[scafiWorld.S],
                              override val boundary : Option[scafiWorld.B],
                              override val deviceSeed : DeviceSeed[scafiWorld.DEVICE_PRODUCER]) extends ScafiSeed

  /**
    * a standard seed used to initialize a scafi world
    */
  val standard : ScafiSeed = new ScafiSeedImpl(Some(Rectangle(2,2)), None, ScafiDeviceSeed.standardSeed)

  /**
    * allow to create a scafi seed
    * @param shape the shape of node
    * @param boundary the world boundary
    * @param deviceSeed the device seed
    * @return scafi seed created
    */
  def apply(shape : Option[scafiWorld.S] = standard.shape,
            boundary : Option[scafiWorld.B] = standard.boundary,
            deviceSeed: DeviceSeed[scafiWorld.DEVICE_PRODUCER] =  standard.deviceSeed): ScafiSeed = new ScafiSeedImpl(shape,boundary,deviceSeed)
}