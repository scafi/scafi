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

package it.unibo.scafi.distrib.actor.patterns

import akka.actor.{Actor, ActorRef}
import it.unibo.scafi.distrib.actor.{MsgRemoveObserver, MsgAddObserver}

/**
 * Represents the behavior of an 'observable' actor.
 * Responsibilities:
 *   - Keeps track of observers via {{observersManagementBehavior}}
 *   - Provide a method for notifying observers {{NotifyObservers}}
 *   - Provides a template method {{CurrentStateMessage} for building
 *     the default notification message out of the internal state
 * TODO: one thing that might be done is the management of multiple *topics* by
 *   - Keeping a different set of observers for each topic
 *   - Using a partial function (TopicType => Option[Any]) for building a topic-specific msg
 *   - Defining a "language" for expressing (combinations of) topics
 */
trait ObservableActorBehavior {
  actor: Actor =>

  /* Abstract members */

  def currentStateMessage: Any = { throw new NotImplementedError() }

  /* Key structures */

  val observers: scala.collection.mutable.Set[ActorRef] = scala.collection.mutable.Set()

  /* Behavior */

  def observersManagementBehavior: Receive = {
    case MsgAddObserver(o) => { observers += o; observerAdded(o) }
    case MsgRemoveObserver(o) => observers -= o
  }

  def notifyObservers(): Unit = {
    val currState = currentStateMessage
    observers.foreach(o => o ! currState)
  }

  def notifyObservers(msg: Any): Unit = observers.foreach(o => o ! msg)

  def observerAdded(ref: ActorRef): Unit = { }
}
