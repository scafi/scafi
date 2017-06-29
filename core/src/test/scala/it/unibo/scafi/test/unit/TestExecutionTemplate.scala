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
    import factory./

    describe("Status implementation") {

      it("when empty") {
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ASSERT
        status.path shouldEqual /
        status.index shouldBe 0
        status.neighbour shouldBe None
      }

      it("should allow un/folding in the context of a neighbor") {
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ASSERT
        status.neighbour shouldBe None

        // ACT
        val s1 = status.foldInto(Some(7))
        val s2 = status.foldInto(Some(8))

        status.neighbour shouldBe None
        status.isFolding shouldBe false
        //intercept[Exception]{ status.foldOut() }

        s1.neighbour shouldEqual Option(7)
        s1.isFolding shouldBe true

        s2.neighbour shouldEqual Option(8)
        s2.isFolding shouldBe true
      }

      it("should work as a stack"){
        // ARRANGE
        val status = ExecutionTemplate.Status()

        // ACT
        val s1 = status.push()
        val s2 = s1.foldInto(Some(7)).nest(Nbr(2)).push()
        val s3 = s2.foldInto(Some(8)).nest(Rep(4)).incIndex().push()
        val s4 = s3.pop()
        val s5 = s4.pop()
        val s6 = s5.pop()

        // ASSERT
        intercept[Exception]{ s6.pop() }

        s4.index shouldBe 1
        s4.neighbour shouldEqual Some(8)
        s4.path.matches(/ / Rep(4) / Nbr(2))

        s5.index shouldBe 0
        s5.neighbour shouldBe Some(7)
        s5.path.matches(/ / Rep(4) / Nbr(2))

        s6.index shouldBe 0
        s6.neighbour shouldEqual None
        s6.path.matches(/ / Nbr(2))
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
