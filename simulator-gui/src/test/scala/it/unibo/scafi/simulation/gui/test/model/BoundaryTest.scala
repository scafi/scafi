package it.unibo.scafi.simulation.gui.test.model
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.test.help.AbstractWorldImpl
import org.scalatest.{FunSpec, Matchers}

class BoundaryTest extends FunSpec with Matchers {
  val checkThat = new ItWord
  val world = new AbstractWorldImpl
  val width = 10
  val height = 10
  world.boundary = Some(world.ShapeBoundary(Rectangle(width,height)))

  checkThat("I can put node only inside the world") {
    assert(world.insertNode(new world.NodeBuilder(0,Point3D(0,0,0))))

    assert(world.moveNode(0,Point3D(1,0,0)))
    assert(world.moveNode(0,Point3D(0,9,0)))
    assert(world.moveNode(0,Point3D(9,7,0)))

    assert(!world.moveNode(0,Point3D(-1,0,0)))
    assert(!world.moveNode(0,Point3D(0,11,0)))
    assert(!world.moveNode(0,Point3D(11,0,0)))

  }

  world.boundary = Some(world.ShapeBoundary(Rectangle(width,height), (Point3D(1,1,0),Rectangle(2,2))))

  checkThat("in inclusive bound i can't add node") {
    world.clear()
    assert(!world.insertNode(new world.NodeBuilder(0,Point3D(2,2,0))))
    assert(world.insertNode(new world.NodeBuilder(0,Point3D(9,9,0))))
    assert(world.moveNode(0,Point3D(5,5,0)))
    assert(world.moveNode(0,Point3D(0,0,0)))

  }
}
