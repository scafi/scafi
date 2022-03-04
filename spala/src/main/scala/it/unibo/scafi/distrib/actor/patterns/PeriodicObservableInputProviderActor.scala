/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.patterns

import akka.actor.Actor
import it.unibo.scafi.distrib.actor.{GoOn, MsgWithInput}
import akka.event.LoggingAdapter

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

  protected val logger: LoggingAdapter = akka.event.Logging(context.system, this)

  /* Reactive behaviors */

  def receive: Receive = workingBehavior
    .orElse(inputManagementBehavior)
    .orElse(observersManagementBehavior)

  def workingBehavior: Receive = {
    case GoOn => {
      doJob()
      handleLifecycle()
    }
  }

  def inputManagementBehavior: Receive = Map.empty

  /* Passive behavior */

  def doJob(): Unit = {
    this.value = provideNextValue()
    this.value.foreach(_ => notifyObservers())
  }

  override def currentStateMessage: Any = {
    MsgWithInput(name,value)
  }
}
