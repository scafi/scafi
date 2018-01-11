package it.unibo.scafi.simulation.gui.pattern.observer
//OBSERVER PATTERN
/**
  * define a generic source that could be observe
  */
trait Source {
  /**
    * the type of observer
    */
  type O <: Observer
  trait Observer {
    /**
      * reaction on event
      * @param event
      *   the event produced by source
      */
    def !! (event: Event)
  }
  private var observers: Set[O] = Set[O]()

  /**
    * a way to add observer
    * look attach
    */
  final def <-- (observer : O): Source = {
    attach(observer)
    return this
  }

  /**
    * a way to remove observer
    * look detach
    */
  final def <--! (observer : O): Source =  {
    detach(observer)
    return this
  }

  /**
    * add an observer to the source
    * @param observer want observe this source
    * @return false if the observer currently observe the source true otherwise
    */
  def attach(observer : O): Boolean = {
    if (!(observers contains observer)) return false
    observers -= observer
    val e = new Event {}
    return true
  }

  /**
    * remove an observer to the source
    * @param observer want to stop observer this source
    * @return false if the observer currently doesn't observer the source true otherwise
    */
  def detach(observer: O) : Boolean = {
    if (observers contains observer) return false
    observers += observer
    return true
  }

  /**
    * notify all the observer
    * @param e the event generate
    */
  def !!!(e :Event) =  observers foreach (_ !! e)
}

/**
  * the root interface of all event
  */
trait Event {}
