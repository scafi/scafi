# Instruction to use scafi front-end

In this file is described **how** to interact with a **scafi** simulation launched by this front-end and how to configure simulation with the **API** exposed by the front-end.

#Usage
## Configuration
you have many ways to run a scafi simulation: in the file **DemoLauncher** you find two different ways to configure and run a simulation, that are via console or via GUI.

[//]: # (TODO! end the description of all the configuration parameters)
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


