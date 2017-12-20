package it.unibo.scafi.simulation.gui.model.core

/**
  * a generic device that could be attached to a node
  */
trait Device {
  /**
    * the name of device
    */
  type NAME
  /**
    * the node where the device is attached
    */
  type NODE <: Node

  val node : NODE

  val name : NAME

  /**
    * enable the device
    */
  def enable

  /**
    * disable the device
    */
  def disable
}

/**
  * a sensor device
  */
trait Sensor extends Device {
  type VALUE <: SensorValue

  def value : VALUE

  trait SensorValue
}

/**
  * an actuator device
  */
trait Actuator extends Device {
  type ACTION <: ActuatorAction
  //TODO aggiungere un'astrazione di tempo??
  type TIME

  /**
    * execute an action
    */
  def exec(dt : TIME)


  trait ActuatorAction
}


