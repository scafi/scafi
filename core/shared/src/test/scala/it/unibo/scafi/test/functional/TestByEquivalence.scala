/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.test.functional

import it.unibo.scafi.test.CoreTestIncarnation._
import it.unibo.scafi.test.CoreTestUtils
import org.scalatest._

import scala.util.Random
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TestByEquivalence extends AnyFunSpec with Matchers {

  val checkThat = new ItWord

  implicit val node = new BasicAggregateInterpreter
  import CoreTestUtils._
  import node._

  class Fixture {
    val random = new Random(0)
    val execSequence = Stream.continually(Random.nextInt(3)).take(100)
    val devicesAndNbrs = fullyConnectedTopologyMap(List(0,1,2))
  }

  checkThat("multiple nbrs in fold work well") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){ nbr{1} + nbr{2} + nbr{mid()} }
    }{
      foldhood(0)(_ + _){ nbr{1 + 2 + mid()} }
    }
  }

  checkThat("nbr.nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){ nbr{ mid() + nbr{ mid() } } }
    }{
      2 * foldhood(0)(_ + _){ nbr{ mid() } }
    }
  }

  checkThat("rep.nbr is to be ignored on first argument") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){
        rep (nbr{mid()}) { (old) => old }
      }
    }{
      foldhood(0)(_ + _){
        rep (mid()) { (old) => old }
      }
    }
  }

  checkThat("rep.nbr is to be ignored overall") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){
        rep (nbr{mid()}) { (old) =>
          old + nbr{old} + nbr{mid()}
        }
      }
    }{
      foldhood(0)(_ + _){1} *
        rep (mid()) { (old) =>
          old * 2 + mid()
        }
    }
  }


  checkThat("fold.init nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){
        foldhood(nbr{mid()})(_ + _){1}
      }
    }{
      foldhood(0)(_ + _){1} * foldhood(mid())(_ + _){1}
    }
  }


  checkThat("fold.fold basically works") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_ + _){
        foldhood(0)(_ + _){1}
      }
    }{
      Math.pow(foldhood(0)(_ + _){1}, 2)
    }
  }

  checkThat("Performance does not degrade when nesting foldhoods"){
    val execSequence = Stream.continually(Random.nextInt(100)).take(1000)
    val devicesAndNbrs = fullyConnectedTopologyMap(0 to 99)

    // fold.fold : performance
    // NOTE: pay attention to double overflow
    assertEquivalence(devicesAndNbrs, execSequence, (x:Double,y:Double)=>Math.abs(x-y)/Math.max(Math.abs(x),Math.abs(y))<0.000001){
      foldhood(0.0)(_ + _){
        foldhood(0.0)(_ + _){
          foldhood(0.0)(_ + _){
            foldhood(0.0)(_ + _){
              foldhood(0.0)(_ + _){
                foldhood(0.0)(_ + _){
                  foldhood(0.0)(_ + _){
                    foldhood(0.0)(_ + _){
                      foldhood(0.0)(_ + _){
                        foldhood(0.0)(_ + _){1.0}}}}}}}}}}
    }{
      Math.pow(foldhood(0.0)(_ + _){1.0}, 10)
    }
  }

  checkThat("fold.aggregate.fold is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)((x,y) => aggregate { foldhood(0)(_ + _){1} + foldhood(0)(_ + _){1} + x + y }){1}
    }{
      foldhood(0)(_ + _){1}
    }
  }

  checkThat("fold.aggregate.nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(aggregate { mid() + _ + _ }){1}
    }{
      foldhood(0)(aggregate { nbr{mid()} + _ + _ }){1}
    }
  }

  checkThat("fold.nbr(f)() is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood("")(_ + _){
        nbr(mux(mid%2==0){ ()=>aggregate{"a"} }{ ()=>aggregate{"b"} })() +
          nbr(mux(mid%2!=0){ ()=>aggregate{"c"} }{ ()=>aggregate{"d"} })()
      }
    }{
      foldhood("")(_ + _){
        (mux(mid%2==0){ ()=>aggregate{"a"} }{ ()=>aggregate{"b"} })() +
          (mux(mid%2!=0){ ()=>aggregate{"c"} }{ ()=>aggregate{"d"} })()
      }
    }
  }
}
