package it.unibo.scafi.test.functional.stdlib

import it.unibo.scafi.test.FunctionalTestIncarnation._
import it.unibo.scafi.test.functional.{ScafiAssertions, ScafiTestUtils}
import org.scalatest._


class TestBlockG extends FunSpec with BeforeAndAfterEach {
  import ScafiAssertions._
  import ScafiTestUtils._

  var net: Network with SimulatorOps = manhattanNet(detachedNodesCords = Set((2,2)))
  val infinity: Double = Double.PositiveInfinity

  def restartNetwork(): Unit = {
    net = manhattanNet(detachedNodesCords = Set((2,2)))
    net.addSensor(name = "source", value = false)
  }
  override protected def beforeEach(): Unit = restartNetwork()

  private[this] trait TestProgram extends AggregateProgram with StandardSensors with BlockG

  describe("BlockG") {
    describe("Should support distanceTo - hop") {
      it("Should work if no source is specified") {
        exec(new TestProgram {
          override def main(): Any = distanceTo(sense[Boolean]("source"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, infinity
        )).toMap)(net)
      }
      it("Should work with multiple sources"){
        net.chgSensorValue("source", Set(0,4), true)

        exec(new TestProgram {
          override def main(): Any = distanceTo(sense[Boolean]("source"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          0, 1, 1,
          1, 0, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("Should react to a source change") {
        net.chgSensorValue("source", Set(0), true)

        exec(new TestProgram {
          override def main(): Any = distanceTo(sense[Boolean]("source"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          0, 1, 2,
          1, 1, 2,
          2, 2, infinity
        )).toMap)(net)

        net.chgSensorValue("source", Set(0), false)
        net.chgSensorValue("source", Set(4), true)


        exec(new TestProgram {
          override def main(): Any = distanceTo(sense[Boolean]("source"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 0, 1,
          1, 1, infinity
        )).toMap)(net)
      }
    }
  }

}
