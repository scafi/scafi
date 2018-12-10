package it.unibo.scafi.simulation

import it.unibo.scafi.simulation.MetaActionManager.MetaAction

import scala.collection.mutable



/**
  * a concept used to enqueue meta action (action
  * that operate at simulation level) and when
  * it is require process and execute action on
  * simulation
  */
trait MetaActionManager {
  protected var actionQueue : mutable.Queue[MetaAction] = mutable.Queue.empty

  /**
    * add a meta action to queue list
    * @param action the action to add in queue
    */
  def add(action : MetaAction) : Unit = actionQueue enqueue action

  /**
    * execute all action in queue
    * and clear the action queue
    */
  def process() : Unit
}

object MetaActionManager {
  trait MetaAction
  /**
    * an empty meta action
    */
  case object EmptyAction extends MetaAction

  /**
    * used to enqueue multiple action in the same meta action
    * @param meta the meta action to process together
    */
  case class MultiAction(meta : MetaAction*) extends MetaAction
}
