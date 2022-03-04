/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.patterns

import akka.actor.Actor
import akka.event.LoggingAdapter

/**
 * @author Roberto Casadei
 * Represents a structure for a basic actor, factoring out commonly desired features, such as:
 *   - A behavior compositionally organized according to some semantics
 *   - A {{logger}} for logging
 * The semantics of messages is the following:
 *   - Input/information messages: provide some input, sender expects NO reply
 *   - Query messages: are the requests in a request-response pattern
 *   - Command messages: are requests for performing some operation (possibly to
 *       the internal state); the sender expects a reply/confirmation
 */
trait BasicActorBehavior { selfActor: Actor =>

  def receive: Receive =
    workingBehavior
      .orElse(inputManagementBehavior)
      .orElse(queryManagementBehavior)
      .orElse(commandManagementBehavior)

  /* Utility members */

  val logger: LoggingAdapter = akka.event.Logging(context.system, this)

  /* Behavior */

  // To handle incoming information
  def inputManagementBehavior: Receive = Map.empty

  // To handle information requests
  def queryManagementBehavior: Receive = Map.empty

  // To handle external commands
  def commandManagementBehavior: Receive = Map.empty

  // To handle some work
  def workingBehavior: Receive = Map.empty
}
