# scafi (**sca**la **fi**elds) #

**scafi** is a Scala-based library and framework for Aggregate Programming.
It implements a variant of the Higher-Order Field Calculus (HOFC) operational semantics,
 which is made available as a usable domain-specific language (DSL),
and provides a platform and API for simulating and executing Aggregate Computing systems and applications.

Please refer to [the scafi main website](https://scafi.github.io/) for further information.

## Overview

### Status Badges

#### Stable branch

[![Build Status](https://travis-ci.org/scafi/scafi.svg?branch=master)](https://travis-ci.org/scafi/scafi)

#### Development branch

[![Build Status](https://travis-ci.org/scafi/scafi.svg?branch=develop)](https://travis-ci.org/scafi/scafi)

## Notes for developers

### Import via Maven/sbt/Gradle

Add the dependency to scafi in your project (e.g., via sbt or Gradle)

```scala
val scafi_version = "0.3.2"

val scafi_core  = "it.unibo.apice.scafiteam" %% "scafi-core"  % scafi_version
val scafi_simulator  = "it.unibo.apice.scafiteam" %% "scafi-simulator"  % scafi_version
val scafi_simulator_gui  = "it.unibo.apice.scafiteam" %% "scafi-simulator-gui"  % scafi_version
val scafi_platform = "it.unibo.apice.scafiteam" %% "scafi-distributed"  % scafi_version

libraryDependencies ++= Seq(scafi_core, scafi_simulator, scafi_platform)
```

### Release Highlights

**0.3.2** (2018-10-19)

* Aggregate processes with `spawn` constructs
* Adjustments to operational semantics
* Refactoring of modules (e.g., `stdlib` brought into `core`)
* More features in the standard library
* Bug fixes, tests, etc.

**0.3.0** (2018-03-27)

* `stdlib` module
* `align` construct

**0.2.0** (2017-06-28)

* Several important adjustments to the core operational semantics (and more tests)
* Refactoring of the field calculus interpreter
* A basic graphical simulator has been added as a separate module `simulator-gui`
* Cross compilation for Scala 2.11 and 2.12

## People

### Main Researchers and Authors

* Mirko Viroli
* Roberto Casadei

### Research Collaborators

* Ferruccio Damiani
* Giorgio Audrito


## License ##

scafi is Open Source and available under the Apache 2 License.
