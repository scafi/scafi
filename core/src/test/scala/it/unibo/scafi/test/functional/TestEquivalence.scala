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

  checkThat("the following programs are equivalent") {
    val random = new Random(0)
    val execSequence = Stream.continually(Random.nextInt(3)).take(100)
    val devicesAndNbrs = fullyConnectedTopologyMap(List(0,1,2))

    // nbr.nbr : to be ignored
    assertEquivalence(devicesAndNbrs, execSequence){
      foldhood(0)(_+_){
        nbr{mid()+nbr{mid()}}
      }
    }{
      2 * foldhood(0)(_+_){nbr{mid()}}
    }

    // rep.nbr : to be ignored
    assertEquivalence(devicesAndNbrs, execSequence){
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

    // fold.init nbr : to be ignored
    assertEquivalence(devicesAndNbrs, execSequence){
      foldhood(0)(_+_){
        foldhood(nbr{mid()})(_+_){1}
      }
    }{
      foldhood(0)(_+_){1} * foldhood(mid())(_+_){1}
    }

    // fold.fold : basic
    assertEquivalence(devicesAndNbrs, execSequence){
      foldhood(0)(_+_){
        foldhood(0)(_+_){1}
      }
    }{
      Math.pow(foldhood(0)(_+_){1}, 2)
    }

    // fold.fold : performance
    assertEquivalence(devicesAndNbrs, execSequence){
      foldhood(0)(_+_){
        foldhood(0)(_+_){
          foldhood(0)(_+_){
            foldhood(0)(_+_){
              foldhood(0)(_+_){
                foldhood(0)(_+_){
                  foldhood(0)(_+_){
                    foldhood(0)(_+_){
                      foldhood(0)(_+_){
                        foldhood(0)(_+_){1}}}}}}}}}}
    }{
      Math.pow(foldhood(0)(_+_){1}, 10)
    }
  }

}
