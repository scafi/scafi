package it.unibo.scafi.test

import CoreTestIncarnation._
import org.scalactic.Equality

import scala.collection.{Map, mutable}

/**
  * @author Roberto Casadei
  *
  */

trait CoreTestUtils {
  def ctx(selfId: Int,
          exports: Map[Int,ExportImpl] = Map(),
          lsens: Map[String,Any] = Map(),
          nbsens: Map[String, Map[Int,Any]] = Map())
         (implicit node: EXECUTION): CONTEXT =
    new ContextImpl(selfId, exports, lsens, nbsens)

  def assertEquivalence(nbrs: Map[ID,List[ID]], execOrder: Iterable[ID])
                   (program1: => Any)
                   (program2: => Any)
                   (implicit interpreter: EXECUTION): Boolean = {
    val states = mutable.Map[ID,ExportImpl]()

    execOrder.foreach(curr => {
      val currCtx = ctx(curr, exports = states.filter(nbrs(curr).contains(_)))
      val exp1 = interpreter.round(currCtx, program1)
      val exp2 = interpreter.round(currCtx, program2)
      if(exp1.root() != exp2.root()) throw new Exception(s"Not equivalent: \n$exp1\n$exp2\n$currCtx")
      states.put(curr, exp1)
    })
    true
  }

  implicit val exportEquality = new Equality[EXPORT] {
    override def areEqual(e: EXPORT, b: Any): Boolean = e.toString == b.toString
  }

  def fullyConnectedTopologyMap(elems: Iterable[ID]): Map[ID,List[ID]] = elems.map(elem => elem -> elems.toList).toMap
}

object CoreTestUtils extends CoreTestUtils