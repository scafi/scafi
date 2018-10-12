package sims
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

object SizeConversion {
  var worldSize : (Double,Double) = (0,0)
  def normalSizeToWorldSize(size : (Double,Double)) : (Double,Double) =
    (size._1 * worldSize._1,size._2 * worldSize._2)
}
