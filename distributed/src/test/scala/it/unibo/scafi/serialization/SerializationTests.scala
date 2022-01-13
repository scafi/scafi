package it.unibo.scafi.serialization

import it.unibo.scafi.distrib.actor.serialization.BasicSerializers.mapAnyWrites
import it.unibo.scafi.incarnations.{BasicAbstractActorIncarnation, BasicAbstractIncarnation}
import play.api.libs.json.{JsValue, Json, Writes}
import org.scalatest.funsuite.AnyFunSuite

object SerializationTestsIncarnation extends BasicAbstractActorIncarnation {
  override type Time = Nothing
  override type ProfileSettings = Nothing
  override val settingsFactory: SerializationTestsIncarnation.SettingsFactory = null
  override type P = Nothing
  override val platformFactory: SerializationTestsIncarnation.DistributedPlatformFactory = null
  override type PlatformFacade = Nothing
  override type SystemFacade = Nothing
}

import SerializationTestsIncarnation._

class SerializationTestProgram extends AggregateProgram  {
  override def main(): Any = rep(0)(_ + 1)
}

class SerializationTests extends AnyFunSuite {

  test("It should be possible to serialize exports"){
    val program = new SerializationTestProgram()
    val c1 = factory.context(selfId = 0, exports = Map(), lsens = Map(), nbsens = Map())
    val e1: ComputationExport = program.round(c1)

    import platformSerializer._

    val json = Json.toJson(e1)
    val back: EXPORT = Json.fromJson[ComputationExport](json).get
    assert(e1 == back)
  }

}
