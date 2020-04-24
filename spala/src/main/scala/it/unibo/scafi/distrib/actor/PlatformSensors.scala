/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.actor.patterns.PeriodicObservableInputProviderActor

trait PlatformSensors { self: Platform.Subcomponent =>

  abstract class PeriodicObservableSensorActor[T](name: LSensorName) extends
  PeriodicObservableInputProviderActor[LSensorName,T](name) {
    override def currentStateMessage: Any = {
      new MsgLocalSensorValue(name,value.get)
    }
  }

}
