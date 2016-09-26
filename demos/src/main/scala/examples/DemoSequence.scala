package examples

/**
 * @author Mirko Viroli
 * This program is used to launch simulations on a grid-like network.
 */

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

object DemoSequence extends AggregateProgram with MyLib {
  def mySensor():Int = sense[Int]("sensor")

  def rep2(f: Double=>Double): Double = rep(0.0){f}

  def gradient(source: Boolean): Double =
    rep(Double.MaxValue){
      distance => mux(source) { 0.0 } {
        foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }

  def gradient2(source: Boolean): Double =
    rep(Double.MaxValue){
      distance => mux(source) { 0.0 } {
        minHood{ nbr{distance}+nbrvar[Double](NBR_RANGE_NAME) }
      }
    }

  //override def main() = 1
  //override def main() = 11 // can work with Scala builtin types
  //override def main() = "Prova" // I mean, any type
  //override def main() = List(1,2)  // also Scala objects
  //override def main() = java.lang.Math.random()  // and any Java/Scala library
  //override def main() = 4+5 // I support builtin operations
  //override def main() = mid() // This is a builtin sensor
  //override def main() = sense[Int]("sensor") // This is an ad-hoc sensor
  //override def main() = sense[Boolean]("obstacle") // That's another
  //override def main() = Tuple2(sense[Boolean]("obstacle"),sense[Int]("sensor")) // Tuples!!
  //override def main() = mySensor() // Can define and use functions!!
  //override def main() = rep(0){x => x+1} // Rep works
  //override def main() = rep(0){_+1} // Can use Scala shorthands, if you like them
  //override def main() = rep2( _ + Math.random()) // Can pass code!
  //override def main() = rep2( _ + rep[Double](0,x => x+Math.random())) // I mean, also spatio-temporal code!

  //override def main() = foldhood(0)(_+_){1} // counting neighbours (myself always included)
  //override def main() = foldhood(0)(_+_){nbr(mySensor())} // fold+nbr!!!
  //def isMe = nbr[Int](mid())==mid()
  //override def main() = foldhood(0)(_+_){if (nbr(mid())==mid()) 0 else 1} // counting neighbours (myself excluded)
  /*
  override def main() = foldhood(List[Int]())(_++_){
    mux(isMe){List[Int]()}{ List(nbr(mid())) }
  }*/// gathering neighbours' id

  //override def main() = foldhood(0.0)(_+_){nbr{nbrvar[Double](NBR_RANGE_NAME)}}// gathering sum of distances
  //override def main() = foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y){if (mid()==nbr(mid())) Double.MaxValue else nbrvar[Double]("nbrRange")}// gathering min of distances excluded myself

  //Finally some self-org
  //def main() = gradient(mySensor()==1)
  //def main() = gradient2(mySensor()==1) // look at minHood's power
  //def main() = G[String](mySensor()==1,sense[String]("label"), x=>x, nbrvar[Double]("nbrRange"))
  //def main() = branch(sense[Boolean]("obstacle")){100.0}{ distanceTo(mySensor()==1) }
  //def main() = distanceBetween(isSource, sense[Boolean]("obstacle"))
  //def main() = channel(sense("sensor")==1,sense("sensor2")==1,1)

  def isSource = sense[Boolean]("source")
  def isObstacle = sense[Boolean]("obstacle")

  //def main() = distanceTo(isSource)
  // The following one is BUGGY
  //def main() = (mid(), findParent(distanceTo(isSource)), distanceTo(isSource))
  /*
  def main() = {
    val d = distanceTo(isSource)
    (mid(), findParent(d), d)
  }*/

  //def main() = (average(isSource, sense[Double]("value")), sense[Double]("value"))

  def potentialField = distanceTo(isSource)

  /*def main() = C(distanceTo(isSource),
    (_:Double)+(_:Double),
    1,//1.0,
    0.0)*/

  //def main() = field { nbr(mid()+"-"+findParent(distanceTo(isSource))) }.toString()

  /*def main() = rep(("",0.0)) { v =>
    val isParent = field { nbr(findParent(potentialField))}.contains(mid())
    (mux(isParent)("yes")("no"), potentialField)
  }*/

  //def main = T[Int](1000, 0, (i:Int)=>i-1)
  //def main = timer[Int](1000)
  //def main = limitedMemory("xxx", "exp", 1000)

  //def main = S(3, 1)
  //def main = minId()
  def main = S2(3)

  //def main = distanceAvoidingObstacles(isSource, isObstacle)
  //def main = broadcastRegion(isObstacle, isSource, 1)
  //def main = broadcastRegion(isObstacle, isSource, "a") // WEIRD, shows Some("Z") (?!?!?)
  //def main = groupSize(isObstacle)

  //def main = recentEvent(isObstacle, 500)

  //def main = alignedMap2(sense[Map[Char,Boolean]]("map"), distanceTo(_:Boolean))

  //def main = forte15example

  //def main = crowdWarning(1, 2, 2, 10)
}


object DemoSequenceLauncher extends App {

  val net = simulatorFactory.gridLike(
    n = 10,
    m = 10,
    stepx = 1,
    stepy = 1,
    eps = 0.0,
    rng = 1.1)

  // For channel:
  /*
  val net = simulatorFactory.gridLike(
    n = 10,
    m = 10,
    stepx = 1,
    stepy = 1,
    eps = 1.0,
    rng = 1.5)
  */

  /*
  net.addSensor(name = "sensor", value = 0)
  net.chgSensorValue(name = "sensor", ids = Set(1), value = 1)
  net.addSensor(name = "source", value = false)
  net.chgSensorValue(name = "source", ids = Set(3), value = true)
  net.addSensor(name = "sensor2", value = 0)
  net.chgSensorValue(name = "sensor2", ids = Set(98), value = 1)
  net.addSensor(name = "obstacle", value = false)
  net.chgSensorValue(name = "obstacle", ids = Set(44,45,46,54,55,56,64,65,66), value = true)
  net.addSensor(name = "label", value = "no")
  net.chgSensorValue(name = "label", ids = Set(1), value = "go")

  val rand = new scala.util.Random(3)
  net.addSensor(name = "value", value = 0.0)

  var sum = 0.0
  for(i <- 1 to 100; rnd = rand.nextInt(100).toDouble) {
    sum += rnd
    net.chgSensorValue(name = "value", ids = Set(i-1), value = rnd)
  }
  println("MEAN: " + sum/100)
  */

  /*
  // For aligned map:
  val net = simulatorFactory.gridLike(
    n = 1,
    m = 3,
    stepx = 1,
    stepy = 1,
    eps = 0.0,
    rng = 1.1)
  net.addSensor(name = "map", value = Map[Int,Boolean]())
  for(i <- 0 to 3) {
    val mi = Map[Char,Boolean]( ('a' to ('c' - (if(i==1)1 else 0)).toChar).map(k => (k, k==('a'+i).toChar)).toSeq:_* )
    net.chgSensorValue(name = "map", ids = Set(i), value = mi)
  }
  */

  var v = java.lang.System.currentTimeMillis()

  net.executeMany(
    node = DemoSequence,//new HopGradient("source"),
    size = 1000000,
    action = (n,i) => {
      if (i % 1000 == 0) {
        println(net)
        val newv = java.lang.System.currentTimeMillis()
        println(newv-v)
        println(net.context(4))
        v=newv
      }
    })
}
