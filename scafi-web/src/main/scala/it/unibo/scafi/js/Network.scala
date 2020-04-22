package it.unibo.scafi.js

import it.unibo.scafi.js.d3.d3facade
import it.unibo.scafi.js.jsnetworkx.{Graph, Network}
import it.unibo.scafi.js.sigma.Sigma

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("net")
object NetUtils {
  @JSExport
  def utils(): Network.type = Network

  @JSExport
  def graph(): Graph = new Graph()

  @JSExport
  def d3(): d3facade.type = d3facade

  @JSExport
  def sigma(): Sigma.type = Sigma
}