package it.unibo.scafi.core

import scala.quoted._

case class Position(file: String, line: Int, col: Int)

object Position {
  inline implicit def materialize: Position = ${ impl }

  def impl(using Quotes): Expr[Position] = {
    import quotes.reflect._
    val pos = quotes.reflect.Position.ofMacroExpansion
    val file = Expr(pos.sourceFile.path)
    val line = Expr(pos.startLine + 1)
    val col = Expr(pos.startColumn + 1)
    '{ it.unibo.scafi.core.Position($file, $line, $col) }
  }
}
