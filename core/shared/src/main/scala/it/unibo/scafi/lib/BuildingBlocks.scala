/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.lib

trait StdLib_BuildingBlocks {
  self: StandardLibrary.Subcomponent =>

  trait BuildingBlocks extends Gradients with FieldUtils
    with BlockG with BlockC with BlockS with BlockT with TimeUtils with BlocksWithGC with StateManagement {
    self: FieldCalculusSyntax with StandardSensors =>
  }

}
