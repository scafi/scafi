/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

/**
  * Demo 5-A
  * - Client/server system
  * - (Dynamic) "Spatial" network
  * - Sensors are attached to devices
  * - A common GUI for all devices
  * - Command-line configuration
  */

import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.{BasicSpatialAbstraction, Point2D}
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import examples.gui.server.{DevViewActor => ServerBasedDevViewActor}

object Demo5A_Platform extends SpatialServerBasedActorPlatform with BasicSpatialAbstraction with BasicAbstractActorIncarnation {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  val SourceSensorName: String = "source"
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E, P)]): SPACE[E] = new Basic3DSpace(elems.toMap) {
    override val proximityThreshold = 1.1
  }
}

import demos.{Demo5A_Platform => Platform}

class Demo5A_AggregateProgram extends Platform.ScafiStandardAggregateProgram {
  def hopGradient(source: Boolean): Double = {
    rep(Double.PositiveInfinity){
      hops => { mux(source) { 0.0 } { 1 + minHood(nbr { hops }) } }
    }
  }
  def main(): Double = hopGradient(sense("source"))
}

object Demo5A_MainProgram extends Platform.CmdLineMain {
  override def refineSettings(s: Platform.Settings): Platform.Settings = {
    s.copy(profile = s.profile.copy(
      devGuiActorProps = ref => Some(ServerBasedDevViewActor.props(Platform, ref))
    ))
  }
  override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
    val devInRow = ServerBasedDevViewActor.DevicesInRow
    dm.addSensorValue(Platform.LocationSensorName, Point2D(dm.selfId%devInRow,(dm.selfId/devInRow).floor))
    dm.addSensorValue(Platform.SourceSensorName, dm.selfId==4)
    dm.start
  }
}

object Demo5A_ServerMain extends Platform.ServerCmdLineMain
