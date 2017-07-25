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

import akka.actor.Actor
import it.unibo.scafi.distrib.actor.{GoOn, MsgWithInput}

/**
 * Abstract actor, which is an [[ObservableActorBehavior]] with [[PeriodicBehavior]], that
 *  is intended to notify its observers with messages with a key-value format.
 * @param name The name of the input provider, or the 'key' tag for the notification messages.
 * @tparam K The type of the name.
 * @tparam V The type of the value.
 */
abstract class PeriodicObservableInputProviderActor[K,V](val name: K) extends Actor
  with PeriodicBehavior
  with ObservableActorBehavior {

  /* Abstract members */

  var value: Option[V] = None

  /**
   * Template method with the responsibility of producing the next value to be provided, if any.
   * @return {Some(v)} for a value 'v' or {None} if there is no value to return
   */
  def provideNextValue(): Option[V]
  
  /* Utility members */

  protected val logger = akka.event.Logging(context.system, this)

  /* Reactive behaviors */

  def receive: Receive = workingBehavior
    .orElse(inputManagementBehavior)
    .orElse(observersManagementBehavior)

  def workingBehavior: Receive = {
    case GoOn => {
      DoJob()
      HandleLifecycle()
    }
  }

  def inputManagementBehavior: Receive = Map.empty

  /* Passive behavior */

  def DoJob() = {
    this.value = provideNextValue()
    this.value.foreach(_ => NotifyObservers())
  }

  override def CurrentStateMessage: Any = {
    MsgWithInput(name,value)
  }
}
