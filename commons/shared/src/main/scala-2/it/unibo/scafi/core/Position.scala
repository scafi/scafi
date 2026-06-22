package it.unibo.scafi.core

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

case class Position(file: String, line: Int, col: Int)

object Position {
  implicit def materialize: _root_.it.unibo.scafi.core.Position = macro impl

  def impl(c: Context): c.Expr[_root_.it.unibo.scafi.core.Position] = {
    import c.universe._
    val pos = c.enclosingPosition
    val file = pos.source.path
    val line = pos.line
    val col = pos.column
    c.Expr[_root_.it.unibo.scafi.core.Position](q"_root_.it.unibo.scafi.core.Position($file, $line, $col)")
  }
}
