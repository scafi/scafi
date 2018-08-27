package it.unibo.scafi.simulation.gui.test.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.analyzer
import it.unibo.scafi.simulation.gui.configuration.command.factory.AbstractMoveCommandFactory.MultiMoveCommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.factory.{AbstractMoveCommandFactory, AbstractToggleCommandFactory}
import it.unibo.scafi.simulation.gui.configuration.command.factory.AbstractToggleCommandFactory.MultiToggleCommandFactory
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.{ScafiWorldInitializer, scafiWorld}
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.util.Result.{Fail, Success}
import org.scalatest.{FunSpec, Matchers}

/**
  * test used to check the correctness of command factory that modify the world
  */
class WorldCommandTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val world = scafiWorld
  val node = 100
  val worldSize = 50
  val toggleFactory = new MultiToggleCommandFactory(scafiWorld)
  val moveCommandFactory = new MultiMoveCommandFactory(scafiWorld)

  ScafiWorldInitializer.Random(node,worldSize,worldSize).init(ScafiSeed.standard)

  checkThat("i can move node with move command") {
    val newPoint = Point3D(1,1,1)
    val map : Map[Any,Point3D] = Map(0 -> newPoint)
    val command = moveCommandFactory.create(Map(AbstractMoveCommandFactory.MoveMap -> map))
    command._1 match {
      case Fail(_) => assert(false)
      case _ =>
    }
    val oldPos = world(0).get.position
    command._2.get.make() match {
      case Fail(_) => assert(false)
      case _ =>
    }
    assert(world(0).get.position == newPoint)
    command._2.get.unmake()
    assert(world(0).get.position == oldPos)
  }

  checkThat("with wrong id type command fail") {
    val newPoint = Point3D(1,1,1)
    val map : Map[Any,Point3D] = Map("__" -> newPoint)
    val command = moveCommandFactory.create(Map(AbstractMoveCommandFactory.MoveMap -> map))
    command._1 match {
      case Success => assert(false)
      case _ =>
    }
  }
  checkThat("i can toggle a device") {
    val dev = world(0).get.getDevice(SensorName.sensor1).get
    val oldValue = dev.value
    val command = toggleFactory.create(Map(AbstractToggleCommandFactory.Name -> SensorName.sensor1,
      AbstractToggleCommandFactory.Ids -> Set(0)))
    command._1 match {
      case Fail(_) => assert(false)
      case _ =>
    }
    command._2.get.make()
    assert(dev.value != oldValue)
    command._2.get.unmake()
    assert(dev.value == oldValue)
  }

  checkThat("i can't toggle device with name type wrong or with id type wrong") {
    val commandNameError = toggleFactory.create(Map(AbstractToggleCommandFactory.Name -> 1,
      AbstractToggleCommandFactory.Ids -> Set(0)))
    commandNameError._1 match {
      case Success => assert(false)
      case _ =>
    }

    val commandIdError  = toggleFactory.create(Map(AbstractToggleCommandFactory.Name -> SensorName.sensor1,
      AbstractToggleCommandFactory.Ids -> Set(0,"_")))
    commandIdError._1 match {
      case Success => assert(false)
      case _ =>
    }
  }

  checkThat("pass other argument to factory produce error") {
    val lessArgumentError = toggleFactory.create(Map(AbstractToggleCommandFactory.Name -> SensorName.sensor1))
    lessArgumentError._1 match {
      case Success => assert(false)
      case _ =>
    }

    val moreArgumentError = toggleFactory.create(Map(AbstractToggleCommandFactory.Name -> SensorName.sensor1,
      AbstractToggleCommandFactory.Ids -> Set(0), "aname" -> "avalue"))
    moreArgumentError._1 match {
      case Success => assert(false)
      case _ =>
    }

    val wrongNameArgumentToggle = toggleFactory.create(Map("avalue" -> SensorName.sensor1,
      AbstractToggleCommandFactory.Ids -> Set(0)))

    wrongNameArgumentToggle._1 match {
      case Success => assert(false)
      case _ =>
    }

    val wrongNameArgumentMove = moveCommandFactory.create(Map("" -> Map(0 -> Point3D(0,0,0))))
    wrongNameArgumentMove._1 match {
      case Success => assert(false)
      case _ =>
    }
  }
}
