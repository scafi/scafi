package it.unibo.scafi.test.unit

import it.unibo.scafi.incarnations.Incarnation
import it.unibo.scafi.test.CoreTestIncarnation
import org.scalatest.{FunSpec, Matchers}
import it.unibo.scafi.core.Engine

/**
 * Created by: Roberto Casadei
 * Created on date: 12/11/15
 */

class TestExecutionTemplate extends FunSpec with Matchers {

  describe("Execution Template") {

    def getSemantics = CoreTestIncarnation

    val semantics = getSemantics
    import semantics._

    describe("Status implementation") {

      it("when empty") {
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ASSERT
        status.path shouldEqual factory.emptyPath()
        status.index shouldBe 0
        status.neighbour shouldBe None
      }

      it("should allow un/folding in the context of a neighbor") {
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ASSERT
        status.neighbour shouldBe None

        // ACT
        val s1 = status.foldInto(7)
        val s2 = status.foldInto(8)
        val s3 = status.foldOut()

        s1.neighbour shouldBe Some(7)
        s1.isFolding shouldBe true
        s2.neighbour shouldBe Some(8)
        s2.isFolding shouldBe true
        s3.neighbour shouldBe None
        s3.isFolding shouldBe false
      }

      it("should work as a stack"){
        // ARRANGE
        val status = ExecutionTemplate.Status()
        val root = factory.emptyPath()

        // ACT
        val s1 = status.push()
        val s2 = s1.foldInto(7).nest(Nbr(2)).push()
        val s3 = s2.foldInto(8).nest(Rep(4)).incIndex().push()
        val s4 = s3.pop()
        val s5 = s4.foldOut().push()
        val s6 = s5.pop()
        val s7 = s6.pop()
        val s8 = s7.pop()

        // ASSERT
        intercept[Exception]{ s8.pop() }
        s4.index shouldBe 1
        s4.neighbour shouldBe Some(8)
        s4.path.matches(root / Rep(4) / Nbr(2))

        s6.index shouldBe 1
        s6.neighbour shouldBe None
        s6.path.matches(root / Rep(4) / Nbr(2))

        s7.index shouldBe 0
        s7.neighbour shouldBe Some(7)
        s7.path.matches(root / Nbr(2))

        s8.index shouldBe 0
        s8.neighbour shouldBe None
        s8.path.matches(root)
      }

      it("should use indexes to avoid clashes"){
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ASSERT
        status.index shouldBe 0

        // ACT + ASSERT (increment)
        status.incIndex().index shouldBe 1
        status.incIndex().incIndex().incIndex().index shouldBe 3

        // ACT + ASSERT (reset index after nesting)
        status.incIndex().incIndex().nest(Nbr(0)).incIndex().index shouldBe 1
      }
    } // END describe("Status implementation")

  }
}
