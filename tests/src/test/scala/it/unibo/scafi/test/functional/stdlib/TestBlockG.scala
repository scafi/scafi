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
    net.addSensor(name = "destination", value = false)
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
      it("Should work with multiple sources") {
        net.chgSensorValue("source", Set(0, 4), true)

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
    describe("Should support broadcast - hop") {
      it("The source should broadcast its value") {
        net.chgSensorValue("source", Set(4), true)
        exec(new TestProgram {
          override def main(): Any = broadcast(sense[Boolean]("source"), mid(), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          4, 4, 4,
          4, 4, 4,
          4, 4, 8
        )).toMap)(net)
      }
      it("Should react to a source change") {
        net.chgSensorValue("source", Set(0), true)
        exec(new TestProgram {
          override def main(): Any = (broadcast(sense[Boolean]("source"), mid(), () => 1), hopGradient((sense[Boolean]("source"))))
        }, ntimes = manyManyRounds)(net)

        net.chgSensorValue("source", Set(0), false)
        net.chgSensorValue("source", Set(7), true)
        exec(new TestProgram {
          override def main(): Any = broadcast(sense[Boolean]("source"), mid(), () => 1)
        }, ntimes = manyManyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          7, 7, 7,
          7, 7, 7,
          7, 7, 8
        )).toMap)(net)
      }
      it("Should work with multiple sources") {
        net.chgSensorValue("source", Set(0, 7), true)
        exec(new TestProgram {
          override def main(): ID = broadcast(sense[Boolean]("source"), mid(), () => 1)
        }, ntimes = manyManyRounds)(net)

        //2 and 4 are equidistant from the sources -> they can assume both value
        assert(net.valueMap[ID]().forall {
          case (mid, r) if Set(2, 4) contains mid => r == 0 || r == 7
          case (mid, r) if Set(0, 1, 3) contains mid => r == 0
          case (mid, r) if Set(5, 6, 7) contains mid => r == 7
          case (8, 8) => true
          case _ => false
        })
      }
    }
    describe("Should support distanceBetween - hop") {
      it("should work in a basic scenario (single source single destination") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)

        exec(new TestProgram {
          override def main(): Any = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("the lowest distance should be broadcasted") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)
        net.chgSensorValue("destination", Set(7), true)

        exec(new TestProgram {
          override def main(): Any = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          1, 1, 1,
          1, 1, 1,
          1, 1, infinity
        )).toMap)(net)
      }
      it("produce infinity if there is no path between source and destination") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(8), true)

        exec(new TestProgram {
          override def main(): Any = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          infinity, infinity, infinity,
          infinity, infinity, infinity,
          infinity, infinity, 0
        )).toMap)(net)
      }
      it("should react to a distance change") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(1), true)

        exec(new TestProgram {
          override def main(): Any = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)

        net.chgSensorValue("destination", Set(1), false)
        net.chgSensorValue("destination", Set(6), true)

        exec(new TestProgram {
          override def main(): Any = distanceBetween(sense("source"), sense("destination"), () => 1)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          2, 2, 2,
          2, 2, 2,
          2, 2, infinity
        )).toMap)(net)
      }
    }
    describe("Channel") {
      val smallChannelWidth = 0.1
      it("should mark as true the devices on the shortest path") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(2), true)

        exec(new TestProgram {
          override def main(): Any = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it("should create multiple channels, if needed") {
        net.chgSensorValue("source", Set(0,6), true)
        net.chgSensorValue("destination", Set(2,7), true)

        exec(new TestProgram {
          override def main(): Any = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, true, true,
          false, false, false,
          true, true, false
        )).toMap)(net)
      }
      it("every device should compute false if there is no channel") {
        exec(new TestProgram {
          override def main(): Any = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          false, false, false,
          false, false, false,
          false, false, false
        )).toMap)(net)
      }
      it("should react to a change in the network") {
        net.chgSensorValue("source", Set(0), true)
        net.chgSensorValue("destination", Set(2), true)

        exec(new TestProgram {
          override def main(): Any = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)

        net.chgSensorValue("destination", Set(2), false)
        net.chgSensorValue("destination", Set(6), true)

        exec(new TestProgram {
          override def main(): Any = channel(sense("source"), sense("destination"), smallChannelWidth)
        }, ntimes = manyRounds)(net)

        assertNetworkValues((0 to 8).zip(List(
          true, false, false,
          true, false, false,
          true, false, false
        )).toMap)(net)
      }
    }
  }
}
