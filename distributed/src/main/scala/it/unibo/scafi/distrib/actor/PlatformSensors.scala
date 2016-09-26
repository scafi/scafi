package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.actor.patterns.PeriodicObservableInputProviderActor

/**
 * @author Roberto Casadei
 *
 */

trait PlatformSensors { self: Platform.Subcomponent =>

  abstract class PeriodicObservableSensorActor[T](name: LSNS) extends
  PeriodicObservableInputProviderActor[LSNS,T](name) {
    override def CurrentStateMessage: Any = {
      new MsgLocalSensorValue(name,value.get)
    }
  }

}
