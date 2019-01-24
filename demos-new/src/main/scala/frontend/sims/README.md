# Instruction to use scafi front-end

In this file is described **how** to interact with a **scafi** simulation launched by this front-end and how to configure simulation with the **API** exposed by the front-end.

# Usage
## Configuration
You have many ways to run a scafi simulation: in the file **DemoLauncher** you find two different ways to configure and run a simulation, that are via console or via GUI.

To run scafi program you can use also **ScafiProgramBuilder**, it expose the **API** to configure
a simulation.

For example, a code that run a **Demo** program is:
```scala
ScafiProgramBuilder (
    Random(node = 1000,width = 1000, height = 1000),
    SimulationInfo(program = classOf[Demo],
    RadiusSimulation(40)
  ).launch())
```
the three main argument are:
- **World initializer** : describe a strategy to initialize a simulation map, in this moment
are supported two type of initialization:
    - *Random*: create a random set of node,
    - *Grid*: create a world with grid like spacing.
- **SimulationInfo**: describe the main information used to create and manage a simulation. The main thing is the aggregate program.
- **Simulation initializer** : describe a strategy to initialize a simulation logic, in this moment there is
only one strategy, *RadiusSimulation*, that create a simulation with fixed neighbour radius.

## Gesture

The main gesture you can use to interact with simulation environment are:

- **Zoom** you can zoom in scafi scene with pinch to zoom or with mouse wheel 
- **Drag** you can move the entire scafi scene by clicking the right mouse button and moving the cursor over the pane
- **Select** you can select a set of scafi nodes clicking on pane with the left mouse button and moving the cursor. The resulting circle shows the selection area, the nodes inside of that circle are selected.

## Selection area

After the selection you can interact with the simulation by the keyboard button chosen in the configuration phase.
The standard configuration use these keys:
- **1,..,4** enable the sensor(i) where i is the number typed.

To disable the selection you made you can click once with the left mouse button outside the selection area.

If you click with the left mouse button and keep it pressed inside the selection area, you can **drag** the nodes selected in an other position.

## Other keypad

There is a set of other keys you can use on a scafi simulation:

- **5** stop the simulation cycle
- **6** continue the simulation stopped
- **7** restart the simulation 
- **CTRL + L** show the log pane. To hide retype **CTRL + L**
- **CTRL + K** show the console, here you can type a set of command chosen in the configuration phase. These commands are listed typing **list-command**.
The output of commands is showed in log pane, in the section **command**. To hide retype **CTRL + K**
- **CTRL + Z** undo the last command executed by the front-end. 

#### Deep into configuration

the front-end **API** allow to change many other things of scafi simulation:

- **Output strategy**: change the representation of node and device in a simulation, at this moment there are five strategies: [standard value = *StandardFXOutput*]
    - *StandardFXOutput* : show the node with its shape, a led is showed like a circle with some color, other sensors are display using a label
    - *FastFXOutput* : less computational expensive output, shows only led and node with it shape.
    - *GradientFXOutput* : show numeric sensor (a temperature, a distance,..) with a color following the *HSL* color codification (the number are mapped into a range of 0 to 360)
    - *ImageFXOutput* : the same output of *StandardFXOutput* but to each node is associated the same image.
    - *SenseFXOutput* : each led sensor have a image associated, when the sensor is on the image is showed.
- **Neighbour render** : hide or show the neighbour network. [standard value = true]
- **Performance Policy** : allow to change the refresh time of view.
- **Command mapping**: you can change the mapping of each keyboard keys with an action.
- **World Information**: group the main information to configuration world environment (device set of each node, node shape, world boundary).

at the last, in **SimlationInfo** the are two other importants thing that can be useful in many context:

- *Export evaluation* : you can add a different evaluation to an export produced by scafi simulation to change the output of programs
- *MetaAction producer* : at each export produced you can associate a production of meta action (for example the node movement).


