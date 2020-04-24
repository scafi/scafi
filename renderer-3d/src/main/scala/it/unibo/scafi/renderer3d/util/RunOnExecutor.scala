/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.renderer3d.util

import java.util.concurrent.Executors

import javafx.concurrent.Task

/**
 * Function that can be used to execute code on a Thread Pool. This is useful whenever someone needs to run a task in a
 * different thread than the current one (for example the JavaFx one), without creating a new thread every time.
 * The chosen executor for multi threading is ideal for many short tasks.
 * */
object RunOnExecutor {

  private val singleThreadExecutor = Executors.newSingleThreadExecutor
  private val cachedThreadPool = Executors.newCachedThreadPool()

  def apply[R](operation: => R, singleThreaded: Boolean = false): Unit = {
    val task = new Task[R] {
      override def call(): R = operation
    }
    val executor = if(singleThreaded) singleThreadExecutor else cachedThreadPool
    executor.submit(task)
  }
}