package demos

/**
 * @author Roberto Casadei
 * Demo 3
 * - Client/server system
 * - (Dynamic) "Spatial" network
 * - Sensors are attached to devices
 * - Command-line configuration
 * - Server GUI
 */

import examples.gui.ServerGUIActor
import it.unibo.scafi.distrib.actor.server.{SpatialPlatform => SpatialServerBasedActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.{Point2D, BasicSpatialAbstraction}

object Demo3_Platform extends BasicAbstractActorIncarnation
  with SpatialServerBasedActorPlatform
  with BasicSpatialAbstraction with Serializable {
  override val LocationSensorName: String = "LOCATION_SENSOR"
  override type P = Point2D

  override def buildNewSpace[E](elems: Iterable[(E,P)]): SPACE[E] =
    new Basic3DSpace(elems.toMap) {
      override val proximityThreshold = 1.1
    }
}

// STEP 2: DEFINE AGGREGATE PROGRAM SCHEMA
class Demo3_AggregateProgram extends Demo3_Platform.AggregateProgram {
  def hopGradient(source: Boolean): Double = {
    rep(Double.PositiveInfinity){
      hops => { mux(source) { 0.0 } { 1+minHood(nbr{ hops }) } }
    }
  }

  def main() = hopGradient(sense("source"))
}

// STEP 3: DEFINE MAIN PROGRAMS
object Demo3_MainProgram extends Demo3_Platform.CmdLineMain {
  override def onDeviceStarted(dm: Demo3_Platform.DeviceManager,
                               sys: Demo3_Platform.SystemFacade) = {
    val random = new scala.util.Random(System.currentTimeMillis())
    var k = 0
    var positions = (1 to 5).map(_ => random.nextInt(10))
    dm.addSensor(Demo3_Platform.LocationSensorName, () => {
      k += 1
      Point2D(if(k>=positions.size) positions.last else positions(k), 0)
    })
    dm.addSensorValue("source", dm.selfId==4)
  }
}

object Demo3_ServerMain extends Demo3_Platform.ServerCmdLineMain {
  override def refineSettings(s: Demo3_Platform.Settings) = {
    s.copy(profile = s.profile.copy(
      serverGuiActorProps = tm => Some(ServerGUIActor.props(Demo3_Platform, tm))
    ))
  }
}