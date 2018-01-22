package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.{ScafiLikeWorld, ScafiPrototype, ScafiSimulationContract}
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.test.help.SimpleScafiLikeWorld
import org.scalatest.{FunSpec, Matchers}

class BasicTestIncarnation extends FunSpec with Matchers{
  val checkThat = new ItWord
  val world : ScafiLikeWorld= new SimpleScafiLikeWorld
  val fakeRandomNumber = 10
  val fakeRadius = 10
  val prototype = new ScafiPrototype {
    override def randomSeed: Long = fakeRandomNumber

    override def randomDeviceSeed: Long = fakeRandomNumber

    override def radius: Double = fakeRadius
  }

  val contract = new ScafiSimulationContract[ScafiLikeWorld,ScafiPrototype]()
  val bigNumber = 1000

  (0 to 1000) foreach {world + world.nodeFactory.create(_,Point3D(math.random,math.random,math.random),None,Set())}
  contract.initialize(world,prototype)
  val x = 1
  checkThat("an external simulation created:") {
    assert(contract.getSimulation.isDefined)
  }
  checkThat("I can't initialize two times the simulation") {
    try {
      contract.initialize(world,prototype)
      fail()
    } catch {
      case _ =>
    }
  }
  checkThat("the node must be the same") {
    assert(contract.getSimulation.get.space.elemPositions.size == world.nodes.size)

  }
  checkThat("the ids must be the same") {
    val worldIds = world.nodes map {
      _.id
    }
    assert(contract.getSimulation.get.space.elemPositions forall { x => worldIds.contains(x._1) })
  }
  val aNode = 1
  checkThat("a nodes has a set of neighbours") {
    assert(contract.getSimulation.get.neighbourhood(aNode).size > 0)
  }
}
