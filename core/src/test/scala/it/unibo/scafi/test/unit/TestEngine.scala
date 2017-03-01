package it.unibo.scafi.test.unit

import it.unibo.scafi.test.CoreTestIncarnation
import org.scalatest.{FunSpec, Matchers}
import it.unibo.scafi.core.{Core, Engine}

/**
 * Created by: Roberto Casadei
 * Created on date: 12/11/15
 */

class TestEngine extends FunSpec with Matchers {

  describe("ConcreteSemantics") {

    def getEngine = CoreTestIncarnation

    val engine = getEngine
    import engine._

    describe("Export implementation") {
      it("should support the reading and writing of exports") {
        // ARRANGE
        val exp: Export with ExportOps = factory.emptyExport()
        val emptyPath = factory.emptyPath()
        val path1 = factory.path(Nbr(0), Rep(0))

        // ASSERT (initial, empty state)
        exp.get[Nothing](emptyPath) shouldBe None
        exp.get[Nothing](path1) shouldBe None

        // ACT + ASSERT (insertion at root path)
        exp.put(emptyPath, "foo")
        exp.get[String](emptyPath) shouldBe Some("foo")
        exp.get[String](emptyPath).get shouldBe exp.root[String]()

        // ACT + ASSERT (insertion at different path)
        exp.put(path1, "bar")
        exp.get[String](path1).get shouldBe "bar"

        // ACT + ASSERT (overwriting with a different data type)
        exp.put(emptyPath, 77)
        exp.get[Int](emptyPath).get shouldBe 77
      }
    } // END describe("Export implementation")


    describe("Context implementation") {
      it ("should support the reading of properties and slots") {
        // ARRANGE
        val pathEmpty = factory.emptyPath()
        val exportForId1 = factory.export(pathEmpty -> "one")
        val pathNbr = factory.emptyPath().push(Nbr(0))
        val exportForId5 = factory.export(pathNbr -> "five")
        val ctx = new ContextImpl(
          selfId = 1,
          exports = Map(1 -> exportForId1, 5 -> exportForId5),
          localSensor = Map("s1" -> 77, "s2" -> false),
          nbrSensor = Map("x" -> Map(4 -> false))
        )

        // ASSERT on fields
        ctx.selfId shouldBe 1
        ctx.exports.size shouldBe 2
        ctx.localSensor shouldEqual Map("s1" -> 77, "s2" -> false)
        ctx.nbrSensor shouldEqual Map("x" -> Map(4 -> false))

        // ASSERT readslot
        ctx.readSlot(2, pathEmpty) shouldBe None
        ctx.readSlot(1, pathEmpty) shouldEqual Some("one")
        ctx.readSlot(5, pathEmpty) shouldBe None
        ctx.readSlot(5, pathNbr) shouldEqual Some("five")
      }
    } // END describe("Context implementation")

    describe("Path implementation") {
      it("should allow the reading and writing of paths") {
        // ARRANGE
        val emptyPath: Path with Equals = new PathImpl(List())

        // ACT
        val path1 = emptyPath.push(Nbr(0))
        val path2a = path1.push(Rep(0))
        val tail1a = path2a.pull()
        val tail2a = tail1a.pull()

        // ASSERT
        intercept[Exception]{ tail2a.pull() }
        path1.matches(new PathImpl(List(Nbr(0)))) shouldBe true
        path2a.matches(new PathImpl(List(Rep(0), Nbr(0)))) shouldBe true
        tail1a.matches(new PathImpl(List(Nbr(0)))) shouldBe true
        tail2a.matches(new PathImpl(List())) shouldBe true
      }
    } // END describe("Path implementation")
  } // END describe("Engine")
}
