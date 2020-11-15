/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}

/**
 * Demo 2
 * - Client/server system
 * - (Fixed) "Spatial" network
 * - Command-line configuration
 */

import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}

object Demo2_Platform extends BasicAbstractActorIncarnation
  with SpatialServerBasedActorPlatform
  with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) {
      override val proximityThreshold = 2.5
    }
}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo2_AggregateProgram extends Demo2_Platform.ScafiStandardAggregateProgram {
  override def main(): Any = foldhood(0){_ + _}(1)
}

// STEP 3: DEFINE MAIN PROGRAM
object Demo2_MainProgram extends Demo2_Platform.CmdLineMain {
  override def onDeviceStarted(dm: Demo2_Platform.DeviceManager,
                               sys: Demo2_Platform.SystemFacade) = {
    dm.addSensorValue(Demo2_Platform.LocationSensorName, Point2D(dm.selfId,0))
  }
}

object Demo2_Server extends Demo2_Platform.ServerCmdLineMain
