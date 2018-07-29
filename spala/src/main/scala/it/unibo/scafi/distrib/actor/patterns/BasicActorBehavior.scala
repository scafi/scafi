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

  val logger = akka.event.Logging(context.system, this)

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
