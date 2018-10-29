package frontend.sims
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.ScafiInformation

object SizeConversion {
  def normalSizeToWorldSize(size : (Double,Double)) : (Double,Double) =
    (size._1 * ScafiInformation.configuration.worldInitializer.size._1,
      size._2 * ScafiInformation.configuration.worldInitializer.size._2)
}
