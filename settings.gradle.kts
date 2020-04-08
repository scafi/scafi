rootProject.name = "scafi"

include("commons", "core", "stdlib-ext",
        "spala", "distributed",
        "simulator", "simulator-gui", "simulator-gui-new", "renderer-3d",
        "tests",
        "demos", "demos-new", "demos-distributed")

project(":commons").name = "scafi-commons"
project(":core").name = "scafi-core"
project(":stdlib-ext").name = "scafi-stdlib-ext"
project(":tests").name = "scafi-tests"
project(":spala").name = "spala"
project(":distributed").name = "scafi-distributed"
project(":simulator").name = "scafi-simulator"
project(":simulator-gui").name = "scafi-simulator-gui"
project(":renderer-3d").name = "scafi-renderer-3d"
project(":demos").name = "scafi-demos"
project(":simulator-gui-new").name = "scafi-simulator-gui-new"
project(":demos-new").name = "scafi-demos-new"
project(":demos-distributed").name = "scafi-demos-distributed"
