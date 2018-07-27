package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.NearRealTimePolicy
import it.unibo.scafi.simulation.gui.demo._
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulation.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.StandardFXOutputPolicy
object ConsoleProgramBuilder extends App {
  private var launch = false
  private var node = 100
  private var radius = 20
  private val w = 800
  private val h = 600
  private var sim = ""
  private val commands : Map[String, (Any) => Unit] = Map(
    "list demo" -> ((Any) => demos.foreach(x => println(x.getSimpleName))),
    "node" -> ((n : Any) => node = n.toString.toInt),
    "demo" -> ((n : Any) => sim = n.toString),
    "radius" -> ((n : Any) => radius = n.toString.toInt),
    "launch" -> ((Any) => launch = true),
    "help" ->((Any) => commands.keys foreach (println _))
  )
  println("Welcome in scafi builder...")
  while(!launch) {
    println("insert a command")
    val command = readLine()

    val splitted = command.split(("="))
    val commandWrited = if(splitted.size == 2) splitted(0) else command
    if(!commands.contains(commandWrited)) {
      println("command don't find, use help to see all command")
    } else {
      if (splitted.size == 2) {
        commands(commandWrited)(splitted(1))
      } else {
        commands(commandWrited)()
      }
    }
  }
  ScafiProgramBuilder (
    worldInitializer = Random(node,w,h),
    simulation = RadiusSimulation(program = nameToDemoClass(sim), radius = this.radius ),
    outputPolicy = StandardFXOutputPolicy,
    neighbourRender = true,
    perfomance = NearRealTimePolicy

  ).launch()

}
