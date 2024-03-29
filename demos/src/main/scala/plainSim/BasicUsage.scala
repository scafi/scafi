package plainSim

import it.unibo.scafi.incarnations.BasicAbstractIncarnation

object MyIncarnation extends BasicAbstractIncarnation

import MyIncarnation._

class BasicUsageProgram extends AggregateProgram  {
  override def main(): Any = rep(0)(_ + 1)
}

object BasicUsage extends App {
  val program = new BasicUsageProgram()
  val c1: CONTEXT = factory.context(selfId = 0, exports = Map(), lsens = Map(), nbsens = Map())
  val e1: EXPORT = program.round(c1)

  val c2: CONTEXT = factory.context(0, Map(0 -> e1))
  val e2: EXPORT = program.round(c2)

  println(s"c1=$c1\ne1=$e1\n\nc2=$c2\ne2=$e2")
}
