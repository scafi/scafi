/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  with PlatformCodeMobilitySupport {
  /**
   * This structure packages together info about a neighbor.
   */
  case class NbrInfo(nid: ID,
                     export: Option[EXPORT] = None,
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
