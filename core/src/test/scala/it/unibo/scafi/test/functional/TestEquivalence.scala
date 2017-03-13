package it.unibo.scafi.test.functional

/**
 * Created by: Roberto Casadei
 * Created on date: 19/11/15
 */

import it.unibo.scafi.test.CoreTestIncarnation._
import it.unibo.scafi.test.CoreTestUtils
import org.scalatest._

import scala.collection.Map
import scala.util.Random

class TestEquivalence extends FunSpec with Matchers {

  val checkThat = new ItWord

  implicit val node = new BasicAggregateInterpreter
  import factory._
  import node._
  import CoreTestUtils._

  def fullyConnectedTopologyMap(elems: Iterable[ID]): Map[ID,List[ID]] = elems.map(elem => elem -> elems.toList).toMap

  class Fixture {
    val random = new Random(0)
    val execSequence = Stream.continually(Random.nextInt(3)).take(100)
    val devicesAndNbrs = fullyConnectedTopologyMap(List(0,1,2))
  }

  checkThat("nbr.nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_+_){
        nbr{mid()+nbr{mid()}}
      }
    }{
      2 * foldhood(0)(_+_){nbr{mid()}}
    }
  }

  checkThat("rep.nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_+_){
        rep (nbr{mid()}) { (old) =>
          old + nbr{old} + nbr{mid()}
        }
      }
    }{
      foldhood(0)(_+_){1} *
        rep (mid()) { (old) =>
          old * 2 + mid()
        }
    }
  }


  checkThat("fold.init nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_+_){
        foldhood(nbr{mid()})(_+_){1}
      }
    }{
      foldhood(0)(_+_){1} * foldhood(mid())(_+_){1}
    }
  }


  checkThat("fold.fold basically works") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(_+_){
        foldhood(0)(_+_){1}
      }
    }{
      Math.pow(foldhood(0)(_+_){1}, 2)
    }
  }

//  checkThat("fold.aggregate.fold is to be ignore") {
//    val fixture = new Fixture
//
//    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
//      foldhood(0)(aggregate { foldhood(0)(_+_){1} + _ + _ }){1}
//    }{
//      foldhood(0)(_+_){1}
//    }
//  }

  checkThat("fold.aggregate.nbr is to be ignored") {
    val fixture = new Fixture

    assertEquivalence(fixture.devicesAndNbrs, fixture.execSequence){
      foldhood(0)(aggregate { mid() + _ + _ }){1}
    }{
      foldhood(0)(aggregate { nbr{mid()} + _ + _ }){1}
    }
  }

}
