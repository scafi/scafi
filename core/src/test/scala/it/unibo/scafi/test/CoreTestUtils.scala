package it.unibo.scafi.test

import CoreTestIncarnation._
import org.scalactic.Equality
import scala.collection.Map

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

  implicit val exportEquality = new Equality[EXPORT] {
    override def areEqual(e: EXPORT, b: Any): Boolean = e.toString == b.toString
  }
}

object CoreTestUtils extends CoreTestUtils