package it.unibo.utils.observer
//OBSERVER PATTERN
/**
  * define a generic event source
  *
  */
trait Source {
  /**
    * observer type, a source can notify a observer class
    */
  type O <: Observer
  //TEMPLATE METHOD
  /**
    * @return the observers attached to current source
    */
  protected def observers: Iterable[O]

  /**
    * fast way to add an observer
    * @param observer want to observe current source
    * @return the source
    */
  final def <-- (observer : O): this.type = {
    attach(observer)
    this
  }


  final def <--! (observer : O): this.type =  {
    detach(observer)
    this
  }

  /**
    * add an observer to the source
    * @param observer want observe this source
    * @return false if the observer currently observe the source true otherwise
    */
  def attach(observer : O): Boolean
  /**
    * remove an observer to the source
    * @param observer want to stop observer this source
    * @return false if the observer currently doesn't observer the source true otherwise
    */
  def detach(observer: O) : Boolean

  /**
    * notify all the observer
    * @param e the event generate
    */
  def notify(e :Event) : Unit = observers foreach (_ update e)
}

/**
  * the root interface of all event
  */
trait Event

trait Observer {
  /**
    * store the event received
    * @param event the event produced
    */
  def update(event: Event) : Unit
}
