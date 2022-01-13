/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import org.scalatest._

import it.unibo.scafi.test.CoreTestIncarnation._
import it.unibo.scafi.test.CoreTestUtils._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TestSemanticsByRound extends AnyFunSpec with Matchers {

  val LocalValues, Alignment, Exports, FOLDHOOD, NBR, REP, BRANCH, SENSE, MID, NBRVAR, BUILTIN, Nesting, DeferredExports = new ItWord

  implicit val node = new BasicAggregateInterpreter
  import node._
  import factory._

  LocalValues("should simply evaluate to themselves") {
    // ARRANGE
    val context = ctx(selfId = 0, exports = Map())
    // ACT
    val res = round(context, { 77 }).root[Int]()
    // ASSERT
    res shouldBe 77
  }

  Alignment("should support interaction only between structurally compatible devices") {
    // ARRANGE
    val ctx1 = ctx(selfId = 0)
    // ACT + ASSERT (no neighbor is aligned)
    round(ctx1, { rep(0)(foldhood(_)(_ + _)(1)) }).root[Int]() shouldBe 1

    // ARRANGE
    val exp = Map(1 -> export(
      Rep(0) -> 1,
      Rep(0) / FoldHood(0) -> 1))
    val ctx2 = ctx(selfId = 0, exports = exp)
    // ACT + ASSERT (one neighbor is aligned)
    round(ctx2, { rep(0)(foldhood(_)(_ + _)(1)) }).root[Int]() shouldBe 2
  }

  Exports("should compose") {
    val ctx1 = ctx(selfId = 0, Map(), Map("sensor" -> 5))
    def expr1 = 1
    def expr2 = rep(7)(_ + 1)
    def expr3 = foldhood(0)(_ + _)(nbr(sense[Int]("sensor")))

    /* Given expr 'e' produces exports 'o'
     * What exports are produced by 'e + e + e + e' ?
     */
    round(ctx1, expr1 + expr1 + expr1 + expr1) shouldEqual
      export(/ -> 4)
    round(ctx1, expr2 + expr2 + expr2 + expr2) shouldEqual
      export(/ -> 32,
        Rep(0) -> 8,
        Rep(1) -> 8,
        Rep(2) -> 8,
        Rep(3) -> 8)
    round(ctx1, expr3 + expr3 + expr3 + expr3) shouldEqual
      export(/ -> 20,
        FoldHood(0) / Nbr(0) -> 5,
        FoldHood(1) / Nbr(0) -> 5,
        FoldHood(2) / Nbr(0) -> 5,
        FoldHood(3) / Nbr(0) -> 5,
        FoldHood(0) -> 5,
        FoldHood(1) -> 5,
        FoldHood(2) -> 5,
        FoldHood(3) -> 5)

    // Note: with the current implementation, the following, commented tested expression
    //  is not aligned with the previous one.
    //  Current impl of foldHood increments the index even though Nbr is not
    //  used inside. It shouldn't be an issue as foldHood is to be used with Nbr.
    // def expr4 = foldhood(0)(_+_)(nbr(sense[Int]("sensor")))
    // round(ctx1, expr4 + expr4 + foldhood(0)(_+_)(1) + expr4 + expr4) shouldEqual
    //  export(emptyPath() -> 21, path(Nbr(0)) -> 5, path(Nbr(1)) -> 5, path(Nbr(2)) -> 5,
    //    path(Nbr(3)) -> 5)

    /* Given expr 'e' produces exports 'o'
     * What exports are produced by 'rep(0){ rep(o){ e } }' ?
     */
    round(ctx1, rep(0){x => rep(0){ y => expr1 }}) shouldEqual
      export(/ -> 1,
        Rep(0) -> 1,
        Rep(0) / Rep(0) -> 1)
    round(ctx1,  rep(0){x => rep(0){ y => expr2 }}) shouldEqual
      export(/ -> 8,
        Rep(0) -> 8,
        Rep(0) / Rep(0) -> 8,
        Rep(0) / Rep(0) / Rep(0) -> 8)
    round(ctx1,  rep(0){x => rep(0){ y => expr3 }}) shouldEqual
      export(/ -> 5,
        Rep(0) -> 5,
        Rep(0) / Rep(0) -> 5,
        Rep(0) / Rep(0) / FoldHood(0) -> 5,
        Rep(0) / Rep(0) / FoldHood(0) / Nbr(0) -> 5)

    /* Testing more NBRs within foldhood
     */
    round(ctx1, foldhood(0)(_ + _){ nbr(sense[Int]("sensor")) + nbr(sense[Int]("sensor")) }) shouldEqual
      export(/ -> 10,
             FoldHood(0) -> 10,
             FoldHood(0) / Nbr(0) -> 5,
             FoldHood(0) / Nbr(1) -> 5)
  }

  FOLDHOOD("should support aggregating information from aligned neighbors") {
    // ARRANGE
    val exp1 = Map(2 -> export(/ -> "a",
                               FoldHood(0) -> "a"),
                   4 -> export(/ -> "b",
                               FoldHood(0) -> "b"))
    val ctx1 = ctx(selfId = 0, exports = exp1)
    // ACT + ASSERT
    round(ctx1, foldhood("a")(_ + _)("z")).root[String]() shouldBe "azzz"

    // ARRANGE
    val exp2 = Map( 2 -> export(/ -> "a",
                                FoldHood(0) -> "a"),
                    4 -> export(/ -> "b",
                                FoldHood(0) -> "b"))
    val ctx2 = ctx(selfId = 0, exports = exp2)
    // ACT + ASSERT (should failback to 'init' when neighbors lose alignment within foldhood)
    round(ctx2, foldhood(-5)(_ + _)(if(nbr(false)){ 0 } else { 1 })).root[Int]() shouldBe -14
  }

  NBR("needs not to be nested into fold") {
    // ARRANGE
    val ctx1 = ctx(selfId = 0)
    // ACT + ASSERT
    round(ctx1, { nbr(1) }).root[Int]() shouldBe 1
  }

  NBR("should support interaction between aligned devices") {
    // ARRANGE
    val exp1 = Map(1 -> export(/ -> "any",
                               FoldHood(0) -> 1,
                               FoldHood(0) / Nbr(0) -> 1 ),
                   2 -> export(/ -> "any",
                               FoldHood(0) -> 2,
                               FoldHood(0) / Nbr(0) -> 2))
    val ctx1 = ctx(selfId = 0, exports = exp1)
    // ACT
    val res1 = round(ctx1, foldhood(0)(_ + _)(if(nbr(mid()) == mid()) 0 else 1))
    // ASSERT
    res1.root[Int]() shouldBe 2
    res1.get(FoldHood(0) / Nbr(0)) shouldBe Some(0)
  }

  REP("should support dynamic evolution of fields") {
    // ARRANGE
    val ctx1 = ctx(selfId = 0)
    // ACT
    val exp1 = round(ctx1, { rep(9)(_ * 2) })
    // ASSERT (use initial value)
    exp1.root[Int]() shouldBe 18
    exp1.get(/(Rep(0))) shouldBe Some(18)

    // ARRANGE
    val exp = Map(0 -> export(Rep(0) -> 7))
    val ctx2 = ctx(selfId = 0, exports = exp)
    // ACT
    val exp2 = round(ctx2, { rep(9)(_ * 2) })
    // ASSERT (build upon previous state)
    exp2.root[Int]() shouldBe 14
    exp2.get(/(Rep(0))) shouldBe Some(14)
  }

  BRANCH("should support domain restriction, thus affecting the structure of exports") {
    // ARRANGE
    def program = {
      rep(0)(x => { branch(x%2==0)(7)( rep(4)(x => 4) ); x + 1 })
    }
    // ACT
    val exp = round(ctx(0), program)
    // ASSERT
    exp.root[Int]() shouldBe 1
    //exp.get(path(If(0, true), Rep(0))) shouldBe Some(7)
    //exp.get(path(If(0, false), Rep(0))) shouldBe None

    // ACT
    val ctx2 = ctx(0, Map(0 -> export(Rep(0) -> 1)))
    val exp2 = round(ctx2, program)

    exp2.root[Int]() shouldBe 2
    //exp2.get(path(If(0, true), Rep(0))) shouldBe None
    //exp2.get(path(If(0, false), Rep(0))) shouldBe Some(4)
    //exp2.get(path(Rep(0), If(0, false), Rep(0))) shouldBe Some(4)
  }

  SENSE("should simply evaluate to the last value read by sensor") {
    // ARRANGE
    val ctx1 = ctx(0, Map(), Map("a" -> 7, "b" -> "high"))
    // ACT + ASSERT (failure as no sensor 'c' is found)
    round(ctx1, { sense[Any]("a") }).root[Int]() shouldBe 7
    round(ctx1, { sense[Any]("b") }).root[String]() shouldBe "high"
  }

  SENSE("should fail if the sensor is not available") {
    // ARRANGE
    val ctx1 = ctx(0, Map(), Map("a" -> 1, "b" -> 2))
    // ACT + ASSERT (failure as no sensor 'c' is found)
    intercept[AnyRef] { round(ctx1, { sense[Any]("c") }) }
    // ACT + ASSERT (failure if an existing sensor does not provide desired kind of data)
    intercept[AnyRef] { round(ctx1, { sense[Boolean]("a") }) }
  }

  MID("should simply evaluate to the ID of the local device") {
    // ACT + ASSERT
    round(ctx(77), mid()).root[Int]() shouldBe 77
    round(ctx(8), mid()).root[Int]() shouldBe 8
  }

  NBRVAR("should work as a ''sensor'' for neighbors") {
    // ARRANGE
    val nbsens = Map("a" -> Map(0 -> 0, 1 -> 10, 2 -> 17),
                     "b" -> Map(0 -> "x", 1 -> "y", 2 -> "z"))
    val ctx1 = ctx(0, Map(1 -> export(/ -> 10, FoldHood(0) -> 10)), Map(), nbsens)
    // ACT + ASSERT
    round(ctx1, foldhood(0)((a,b) => if(a>b) a else b)(nbrvar[Int]("a")) ).root[Int]() shouldBe 10

    // ACT + ASSERT (should fail when used outside fooldhood
    intercept[Exception]{ round(ctx1, nbrvar[Int]("a")) }
  }

  NBRVAR("should fail if the neighborhood ''sensor'' is not available") {
    // ARRANGE
    val nbsens = Map("a" -> Map(0 -> 0, 1 -> 10, 2 -> 17))
    val ctx1 = ctx(0, Map(1 -> export(/ -> 10)), Map(), nbsens)
    // ACT + ASSERT (failure because of bad type)
    intercept[AnyRef]{ round(ctx1, foldhood("")(_ + _)(nbrvar[String]("a")) ) }
    // ACT + ASSERT (failure because not found)
    intercept[AnyRef]{ round(ctx1, foldhood(0)(_ + _)(nbrvar[Int]("xxx")) ) }
  }

  BUILTIN("minHood and minHood+, maxHood and maxHood+") {
    // ARRANGE
    val exp1 = Map(1 -> export(/ -> "any",
                               FoldHood(0) -> 10,
                               FoldHood(0) / Nbr(0) -> 10),
                   2 -> export(/ -> "any",
                               FoldHood(0) -> 5,
                               FoldHood(0) / Nbr(0) -> 5))
    val ctx1 = ctx(0, exp1, Map("sensor" -> 3, "sensor2" -> 20))
    // ACT + ASSERT
    round(ctx1, minHood(nbr(sense[Int]("sensor")))).root[Int] shouldBe 3
    round(ctx1, maxHood(nbr(sense[Int]("sensor2")))).root[Int] shouldBe 20

    /** N.B. foldHoodPlus, minHoodPlus, maxHoodPlus should be considered as
     **  "library" methods (not primitives), thus it may be better to not
     **  test exports for them. For now, however, we keep these tests.
     **/
    // ARRANGE
    val exp2 = Map(1 -> export(/ -> "any",
                               FoldHood(0) -> 1,
                               FoldHood(0) / Nbr(0) -> 1,
                               FoldHood(0) / Nbr(1) -> 10),
                   2 -> export(/ -> "any",
                               FoldHood(0) -> 2,
                               FoldHood(0) / Nbr(0) -> 2,
                               FoldHood(0) / Nbr(1) -> 5))
    // Note: the export on Nbr(0) is for the internal call to nbr(mid())
    val ctx2 = ctx(0, exp2, Map("sensor" -> 3, "sensor2" -> 20))
    // ACT + ASSERT
    round(ctx2, minHoodPlus(nbr(sense[Int]("sensor")))).root[Int] shouldBe 5
    round(ctx2, maxHoodPlus(nbr(sense[Int]("sensor2")))).root[Int] shouldBe 10
  }

  Nesting("REP into FOLDHOOD should be supported") {
    // ARRANGE
    val ctx1 = ctx(0, Map(1 -> export(FoldHood(0) -> 7),
                          2 -> export(FoldHood(0) -> 7)))
    def program1 = foldhood("init")(_ + _){ rep(0)(_ + 1) + "" }

    val ctx2 = ctx(0, Map(1 -> export(FoldHood(0) -> 7),
                          2 -> export(FoldHood(0) -> 7,
                                      FoldHood(0) / Nbr(0) -> 7)))
    def program2 = foldhood("init")(_ + _){ nbr { rep(0)(_ + 1) } + "" }

    // ACT + ASSERT
    val exp1 = round(ctx1, program1)
    exp1.root[String] shouldEqual "init111"
    ctx1.updateExport(0, exp1)
    round(ctx1, program1).root[String] shouldEqual "init222"

    val exp2 = round(ctx2, program2)
    assertPossibleFolds("init", List("init","7","1")) {
      exp2.root[String]
    }
    ctx2.updateExport(0, exp2)
    assertPossibleFolds("init", List("init","7","2")){
      round(ctx2, program2).root[String]
    }
  }

  Nesting("FOLDHOOD into FOLDHOOD should be supported") {
    // ARRANGE
    val ctx1 = ctx(0, Map(1 -> export(FoldHood(0) -> 7,
                                      FoldHood(0) / FoldHood(0) -> 7),
                          2 -> export(FoldHood(0) -> 7,
                                      FoldHood(0) / Nbr(0) -> 7,
                                      FoldHood(0) / FoldHood(0) -> 7,
                                      FoldHood(0) / Nbr(0) / FoldHood(0) -> 7)))

    // ACT + ASSERT
    round(ctx1, foldhood("init")(_ + _){ foldhood(0)(_ + _){ 1 } + "" } ).root[String] shouldEqual "init333"

    assertPossibleFolds("init", List("init","7","2")){
      round(ctx1, foldhood("init")(_ + _){ nbr { foldhood(0)(_ + _){ 1 } } + "" } ).root[String]
    }
  }

  DeferredExports("should work in a recurrent fashion"){
    val exp = round(ctx(0), {
      vm.newExportStack
      rep(0){ _ =>
        vm.newExportStack
        rep(0){ _ =>
          vm.newExportStack
          rep(0)(_ + 1)
          vm.mergeExport
          1
        }
        foldhood(0)(_ + _)(nbr(1))
        vm.discardExport
        1
      }
      foldhood(0)(_ + _)(nbr(1))
      vm.mergeExport
    })

    exp.get(/ / Rep(0)) should be(defined)
    exp.get(/ / FoldHood(1)) should be(defined)
    exp.get(/ / Rep(0) / Rep(0)) should not be(defined)
    exp.get(/ / Rep(0) / FoldHood(1)) should not be(defined)
    exp.get(/ / Rep(0) / Rep(0) / Rep(0)) should not be(defined)
  }

  private def assertPossibleFolds(init: Any, a: List[Any])(expr: => Any): Unit = {
    a.permutations.map(l => init + l.mkString).toList should contain (expr)
  }
}
