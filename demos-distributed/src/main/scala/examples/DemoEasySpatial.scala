/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples

/**
 * Demo program with:
 * - Centralized actor platform
 * - Command-line **main program schema**
 * - Spatial
 */

// STEP 1: CHOOSE INCARNATION

import java.util.concurrent.TimeUnit

import akka.actor.Props
import it.unibo.scafi.incarnations.BasicActorSpatial._
import it.unibo.scafi.space.Point2D

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

// STEP 2: DEFINE MAIN PROGRAM
object DemoEasySpatial extends CmdLineMain {
  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait MyAggregateProgram extends AggregateProgram with Serializable {
    override def main(): Any = foldhood(0){_+_}(1)
  }

  class PositionSensor(val pos: Point2D)
    extends PeriodicObservableSensorActor[Point2D](LocationSensorName) {
    /**
     * Template method with the responsibility of producing the next value to be provided, if any.
     * @return {Some(v)} for a value 'v' or {None} if there is no value to return
     */
    override def provideNextValue(): Option[Point2D] = Some(pos)

    val rnd = new Random()
    override var workInterval: FiniteDuration = FiniteDuration((100+rnd.nextInt(2000)), TimeUnit.SECONDS)
  }

  override def programBuilder: Some[ProgramContract] = Some(new MyAggregateProgram {})

  var k = 0
  override def onDeviceStarted(dm: DeviceManager, sys: SystemFacade): Unit = {
    var j = k
    /*
    dm.addSensor(LocationSensorName, () => {
      println("SENSOR called")
      Point2D(j,0)
    })
    */
    val snsActor = sys.actorSys.actorOf(
      Props(classOf[PositionSensor], Point2D(j,0)))
    dm.addPushSensor(snsActor)

    dm.addActuator("actuator1", _ match {
      case value: Int => {
        println("ACTUATOR ==> RESULT: "+ value)
      }
      case _ => 
    })

    k = k+1
    if(k>=2){
      sys.start()
    }
  }
}

/**
 * Configurations:
 * (0) --tmstart -p 9000 -h 127.0.0.1 --sched-global rr --loglevel debug
 * (1) -P 9000 -H 127.0.0.1 -p 9005 -e 1;2;3 --sched-global rr --loglevel debug
 */
