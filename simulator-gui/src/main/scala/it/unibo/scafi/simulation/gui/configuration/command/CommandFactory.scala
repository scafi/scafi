package it.unibo.scafi.simulation.gui.configuration.command

import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandFactoryName.CommandName

/**
  * a generic factory used to create command via command arg
  */
trait CommandFactory {
  /**
    * @return the name of factory
    */
  def name : CommandName

  /**
    * create a command with the command args
    * @param arg the command arg
    * @return None if the arg is not legit some of command otherwise
    */
  def create(arg : CommandArg) : Option[Command]
}

object CommandFactory {

  /**
    * the root trait of all command argument
    */
  trait CommandArg

  /**
    * empty arg, some command can be create without argument
    */
  object EmptyArg extends CommandArg

  /**
    * a parser used to create command arg
    */
  trait CommandParser[A] {
    /**
      * try to create command arg via value passed
      * @return None if the value is not legit Some of argument otherwise
      */
    def parse(arg : A) : Option[CommandArg]

    /**
      * describe a way to use parser
      * @return a help
      */
    def help : String
  }

  /**
    * an enumeration of command factory name
    */
  object CommandFactoryName extends Enumeration {
    /**
      * the type of command name
      */
    type CommandName = Value
    /**
      * all command factory names
      */
    val Toggle, Move, Simulation, Random, Grid, Radius, Demo, Launch, WorldSeed = Value
  }

  /**
    * a utility object used to get the description associated with the command name
    */
  object CommandFactoryDescription {
    import CommandFactoryName._
    private val descriptionMap = Map(Toggle -> "toggle command: allow to on off a set of sensor",
      Move -> "move command: allow to move a set of node to another position",
      Simulation -> "simulation command: allow to stop or continue a simulation",
      Random -> "random command initializer: allow to create a random initializer",
      Grid -> "grid command initilializer: allow to create a grid initializer",
      Demo -> "demo command: allow to show and set current demo simulation",
      Launch -> "launch command: allow to launch the simulation",
      Radius -> "radius simulation command: allow to create a neighbour simulation",
      WorldSeed -> "world seed command: allow to create a seed to simulation")

    /**
      * allow to get the description associated with the name passed
      * @param name the command name
      * @return the description associated with the name
      */
    def descriptionCommand(name : CommandName) = descriptionMap(name)
  }
}