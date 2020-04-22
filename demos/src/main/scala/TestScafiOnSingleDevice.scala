import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import sims.Main16

object TestScafiOnSingleDeviceMain extends App {

  val program: AggregateProgram = new Main16()
  val ctx1 = factory.context(
    selfId = 1,
    exports = Map(), /* in generale li otterrò comunicando con altri nodi */
    lsens = Map("sens1" -> true),
    nbsens = Map("nbrRange" -> Map(1 -> 0.0)))
  var export1 = program.round(ctx1)
  val ctx12 = factory.context(1, Map(1 -> export1), Map("sens1" -> true), Map("nbrRange" -> Map(1 -> 0.0)))
  export1 = program.round(ctx12)

  val ctx2 = factory.context(
    selfId = 2,
    exports = Map(1 -> export1),
    lsens = Map("sens1" -> false),
    nbsens = Map("nbrRange" -> Map(1 -> 1.5, 2 -> 0.0)))
  val export2 = program.round(ctx2)

  println(export1)
  println(export2)
}
