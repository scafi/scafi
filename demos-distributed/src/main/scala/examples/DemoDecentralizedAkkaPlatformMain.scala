/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples

/**
 * Demo program with:
 * - Decentralized (peer-to-peer) actor platform
 * - Settings specified programmatically
 * - Manual setup
 */

import scala.concurrent.duration._

// STEP 1: CHOOSE INCARNATION
import it.unibo.scafi.incarnations.BasicActorP2P._

// STEP 2: DEFINE MAIN PROGRAM
object DemoDecentralizedAkkaPlatformMain extends App {
  val SENSOR_SRC = "source"

  // STEP 3: DEFINE AGGREGATE PROGRAM SCHEMA
  trait DemoLocalActorProgram extends AggregateProgram with Serializable {
    def hopGradient(source: Boolean): Int = {
      rep(10){
        hops => { mux(source){ 0 } {
          1+minHood[Int](nbr[Int]{ hops }) } }
      }
    }

    def main() = {
      Thread.sleep(500)
      hopGradient(sense(SENSOR_SRC))
    }
    // foldhood(0)(_+_){1}
  }

  // STEP 4: DEFINE SETTINGS
  var settings = Settings(
    aggregate = AggregateApplicationSettings(
      name = "test1",
      program = () => Some(new DemoLocalActorProgram {})
    ),
    profile = P2PActorSystemSettings(),
    execution = ExecutionSettings(scope =
    //Global(null)
    DeviceDelegated(DelayedDeviceExecStrategy(Some(10.second)))
    // DeviceDelegated(PeriodicDeviceExecStrategy())
    ))

  // STEP 5: SETUP PLATFORM
  val platform = PlatformConfigurator.setupPlatform(settings.platform,
    settings.profile)

  // STEP 6: CREATE AGGREGATE APPLICATION
  val sys = platform.newAggregateApplication(settings.aggregate,
    settings.profile,
    settings.execution.scope)

  // STEP 7: CREATE AND CONFIGURE DEVICES
  val dm1 = sys.newDevice(1)
  val dm2 = sys.newDevice(2)

  dm1.addSensor(SENSOR_SRC, () => true)
  dm2.addSensor(SENSOR_SRC, () => false)

  // How to start a GUI for a device
  //sys.actorSys.actorOf(DevGUIActor.props(dm1.actorRef))
  //sys.actorSys.actorOf(DevGUIActor.props(dm2.actorRef))
}
