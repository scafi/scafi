package it.unibo.scafi.test

import CoreTestIncarnation._
import org.scalactic.Equality

import scala.collection.{mutable}

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
    factory.context(selfId, exports, lsens, nbsens)

  def assertEquivalence[T](nbrs: Map[ID,List[ID]], execOrder: Iterable[ID], comparer:(T,T)=>Boolean = (_:Any)==(_:Any))
                   (program1: => Any)
                   (program2: => Any)
                   (implicit interpreter: EXECUTION): Boolean = {
    val states = mutable.Map[ID,(ExportImpl,ExportImpl)]()
    execOrder.foreach(curr => {
      val nbrExports = states.filterKeys(nbrs(curr).contains(_))
      val currCtx1 = ctx(curr, exports = nbrExports.mapValues(_._1).toMap)
      val currCtx2 = ctx(curr, exports = nbrExports.mapValues(_._2).toMap)

      val exp1 = interpreter.round(currCtx1, program1)
      val exp2 = interpreter.round(currCtx2, program2)
      if(!comparer(exp1.root(),exp2.root()))
        throw new Exception(s"Not equivalent: \n$exp1\n$currCtx1\n--------\n$exp2\n$currCtx2")
      states.put(curr, (exp1, exp2))
    })
    true
  }

  implicit val exportEquality = new Equality[EXPORT] {
    override def areEqual(e: EXPORT, b: Any): Boolean = e.toString == b.toString
  }

  def fullyConnectedTopologyMap(elems: Iterable[ID]): Map[ID,List[ID]] = elems.map(elem => elem -> elems.toList).toMap
}

object CoreTestUtils extends CoreTestUtils