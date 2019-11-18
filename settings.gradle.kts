rootProject.name = "scafi"

include("commons", "core", "stdlib-ext", "spala", "distributed", "simulator", "simulator-gui", "simulator-gui-new", "tests", "demos", "demos-new")

project(":commons").name = "scafi-commons"
project(":core").name = "scafi-core"
project(":stdlib-ext").name = "scafi-stdlib-ext"
project(":tests").name = "scafi-tests"
project(":spala").name = "spala"
project(":distributed").name = "scafi-distributed"
project(":simulator").name = "scafi-simulator"
project(":simulator-gui").name = "scafi-simulator-gui"
project(":demos").name = "scafi-demos"
project(":simulator-gui-new").name = "scafi-simulator-gui-new"
project(":demos-new").name = "scafi-demos-new"
