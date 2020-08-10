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
          case (mid, r) if Set(2,4) contains mid => r == 0 || r == 7
          case (mid, r) if Set(0,1,3) contains mid => r == 0
          case (mid, r) if Set(5,6,7) contains mid => r == 7
          case (8, 8) => true
          case _ => false
        })
      }
    }
}
