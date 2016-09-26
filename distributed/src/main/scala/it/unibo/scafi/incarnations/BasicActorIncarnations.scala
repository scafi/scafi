package it.unibo.scafi.incarnations

import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import it.unibo.scafi.distrib.actor.p2p.{Platform => P2pActorPlatform}
import it.unibo.scafi.distrib.actor.server.{Platform => ServerBasedActorPlatform}
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import it.unibo.scafi.space.{Point2D, BasicSpatialAbstraction}

/**
 * @author Roberto Casadei
 *
 */

trait BasicAbstractActorIncarnation
  extends BasicAbstractDistributedIncarnation
  with ActorPlatform

object BasicActorP2P extends BasicAbstractActorIncarnation
  with P2pActorPlatform with Serializable

object BasicActorServerBased extends BasicAbstractActorIncarnation
  with ServerBasedActorPlatform with Serializable

object BasicActorSpatial extends BasicAbstractActorIncarnation
  with SpatialServerBasedActorPlatform with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D
}