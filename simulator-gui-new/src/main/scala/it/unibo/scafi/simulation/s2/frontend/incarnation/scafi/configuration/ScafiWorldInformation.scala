package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration

import it.unibo.scafi.simulation.s2.frontend.configuration.information.WorldInformation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiDeviceProducers
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.space.SpatialAbstraction.Bound
import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle

/**
 * describe main world information in scafi context
 */
trait ScafiWorldInformation extends WorldInformation[scafiWorld.DEVICE_PRODUCER, scafiWorld.S]

object ScafiWorldInformation {
  private val standardShape = Some(Rectangle(3, 3))
  private class ScafiWorldInformationImpl(
      override val shape: Option[scafiWorld.S],
      override val boundary: Option[Bound],
      override val deviceProducers: Iterable[scafiWorld.DEVICE_PRODUCER]
  ) extends ScafiWorldInformation

  /**
   * a standard information used to initialize a scafi world:
   *   - the shape of node is rectangle
   *   - the world hasn't boundary
   *   - the device attached on node are described by {@see ScafiDeviceProducers.standardConfiguration}
   */
  val standard: ScafiWorldInformation =
    new ScafiWorldInformationImpl(standardShape, None, ScafiDeviceProducers.standardConfiguration)

  /**
   * allow to create a scafi seed
   * @param shape
   *   the shape of node
   * @param boundary
   *   the world boundary
   * @param deviceSeed
   *   the device seed
   * @return
   *   scafi seed created
   */
  def apply(
      shape: Option[scafiWorld.S] = standard.shape,
      boundary: Option[Bound] = standard.boundary,
      deviceSeed: Iterable[scafiWorld.DEVICE_PRODUCER] = standard.deviceProducers
  ): ScafiWorldInformation = new ScafiWorldInformationImpl(shape, boundary, deviceSeed)
}
