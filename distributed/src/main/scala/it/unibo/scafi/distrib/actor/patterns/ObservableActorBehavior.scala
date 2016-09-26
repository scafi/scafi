package it.unibo.scafi.distrib.actor.patterns

import akka.actor.{Actor, ActorRef}
import it.unibo.scafi.distrib.actor.{MsgRemoveObserver, MsgAddObserver}

/**
 * @author Roberto Casadei
 *
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

  def CurrentStateMessage: Any = { throw new NotImplementedError() }

  /* Key structures */

  val observers: scala.collection.mutable.Set[ActorRef] = scala.collection.mutable.Set()

  /* Behavior */

  def observersManagementBehavior: Receive = {
    case MsgAddObserver(o) => { observers += o; ObserverAdded(o) }
    case MsgRemoveObserver(o) => observers -= o
  }

  def NotifyObservers() = {
    val currState = CurrentStateMessage
    observers.foreach(o => o ! currState)
  }

  def NotifyObservers(msg: Any) = observers.foreach(o => o ! msg)

  def ObserverAdded(ref: ActorRef) = { }
}
