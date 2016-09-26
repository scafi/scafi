package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.{Platform => DistributedPlatform}

import akka.actor._
import it.unibo.scafi.platform.Platform.PlatformDependency


/**
 * @author Roberto Casadei
 *
 */

trait Platform extends DistributedPlatform
  with PlatformMessages
  with PlatformActors
  with PlatformSchedulers
  with PlatformDevices
  with PlatformSensors
  with PlatformAPIFacade
  with PlatformCodeMobilitySupport {
  thisVery: PlatformDependency =>

  /**
   * This structure packages together info about a neighbor.
   */
  case class NbrInfo(nid: ID,
                     export: Option[EXPORT] = None,
                     mailbox: Option[ActorRef] = None,
                     path: Option[String] = None){
    override def hashCode = nid.hashCode
    override def equals(other: Any) = other match {
      case NbrInfo(id,_,_,_) => id == nid
      case _ => false
    }
  }
}

object Platform {
  type Subcomponent = Platform with PlatformDependency
}