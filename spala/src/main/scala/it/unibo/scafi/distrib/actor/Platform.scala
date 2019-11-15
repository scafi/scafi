/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.{Platform => DistributedPlatform}

import akka.actor._

trait Platform extends DistributedPlatform
  with PlatformMessages
  with PlatformActors
  with PlatformSchedulers
  with PlatformDevices
  with PlatformSensors
  with PlatformAPIFacade
  with PlatformView
  with PlatformCodeMobilitySupport {
  /**
   * This structure packages together info about a neighbor.
   */
  case class NbrInfo(nid: UID,
                     export: Option[ComputationExport] = None,
                     mailbox: Option[ActorRef] = None,
                     path: Option[String] = None){
    override def hashCode: Int = nid.hashCode
    override def equals(other: Any): Boolean = other match {
      case NbrInfo(id,_,_,_) => id == nid
      case _ => false
    }
  }
}

object Platform {
  type Subcomponent = Platform
}
