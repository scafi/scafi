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

package it.unibo.scafi.distrib

trait BasePlatform {
  type ID <: Id
  type LSNS
  type NSNS
  type CONTEXT <: Context
  type EXPORT <: Export
  type FACTORY <: Factory
  type PROGRAM <: ExecutionTemplate

  trait Id {
    def toLong: Long
  }

  trait Context {

  }

  trait Export {

  }

  trait Factory {

  }

  trait ExecutionTemplate {

  }
}

/**
 * This component defines a distributed platform and in particular:
 *   - A Façade API for the configuration, setup, and execution of distributed systems
 *   - A corpus of settings and defaults for distributed aggregate systems
 *   - Some general, utility members (string representations for types such as ID, LSNS,..),
 *     used for example in the command-line parser.
 */

trait Platform extends BasePlatform
  with PlatformAPIFacade
  with PlatformSettings

object Platform {
  type Subcomponent = Platform
}
