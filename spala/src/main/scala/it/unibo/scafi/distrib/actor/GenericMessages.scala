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

import akka.actor.{Props, ActorRef}

/**
 * Here we define *general* message types.
 * Essentially to provide strongly-typed containers and
 *  linguistic abstractions for messages.
 */

// SCAFI MESSAGES MARKER TRAIT

/* It must extend "Serializable"
 * See here: https://groups.google.com/forum/embed/#!topic/akka-user/fhC8tv8fjlE
 */
trait ScafiMessage extends Serializable

// GENERIC

case class MsgWithInput[K,V](name: K, value: V)
case class MsgPropagate(msg: Any)

// ACTOR LIFECYCLE

object GoOn extends Serializable
object MsgStart extends Serializable
object MsgPause extends Serializable
object MsgStop extends Serializable
object MsgResume extends Serializable
object MsgShutdown extends Serializable

// OBSERVER PATTERN

case class MsgAddObserver(o: ActorRef)
case class MsgRemoveObserver(o: ActorRef)

// LOW-LEVEL

case class MsgAck(corr: Option[Any])
case class MsgCreateActor(props: Props, name: Option[String] = None, corr: Option[Any] = None)
case class MsgCreationAck(ref: ActorRef, name: String, corr: Option[Any] = None)

case class SystemMsgClassNotFound(className: String)
case class MsgRequestClass(className: String)
case class MsgWithClass(name: String, code:Array[Byte])
case class MsgWithClasses(classes: Map[String,Array[Byte]], corr: Option[Any])
