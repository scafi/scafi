package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.utility.ImageFilter;
import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent the Application menu
 * Created by Varini on 19/10/16.
 * Updated by Casadei on 3/02/17
 */
public class MenuBarNorth extends JMenuBar {

    private final List<JMenu> menus = new ArrayList<>();
    Controller controller = Controller.getIstance();

    public MenuBarNorth(){

        JMenu file = new JMenu("File");
        JMenu newFile = new JMenu("New");
        JMenuItem simulation = new JMenuItem("Scafi Simulation");

        simulation.addActionListener(e-> new ConfigurationPanel());
        newFile.add(simulation);

        JMenuItem open = new JMenuItem("Open");
        JMenuItem save = new JMenuItem("Save");
        file.add(newFile);

        JMenu simConfig = new JMenu("Simulation");
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener(e->{
            controller.clearSimulation();
        });
        JMenuItem addImage = new JMenuItem("Add Image");
        JMenuItem removeImage = new JMenuItem("Remove Image");
        removeImage.setEnabled(false);
        addImage.addActionListener(e->{
            JFileChooser choose = new JFileChooser();
            choose.addChoosableFileFilter( new ImageFilter());
            choose.setAcceptAllFileFilterUsed(false);
            choose.showOpenDialog(this.getParent());
            controller.showImage(new ImageIcon(choose.getSelectedFile().getPath()).getImage(), true);
            removeImage.setEnabled(true);
            addImage.setEnabled(false);
        });

        removeImage.addActionListener(e->{
            controller.showImage(new ImageIcon("").getImage(), false);
            removeImage.setEnabled(false);
            addImage.setEnabled(true);
        });

        int dim = (int)Utils.getIconMenuDim().getWidth(); //dimensione icone

        JMenuItem start = new JMenuItem("Start", Utils.getScaledImage("start.png",dim,dim));
        JMenuItem pause = new JMenuItem("Pause", Utils.getScaledImage("pause.png",dim,dim));
        start.addActionListener(e-> {
            controller.resumeSimulation();
            start.setEnabled(false);
            pause.setEnabled(true);
        });
        pause.addActionListener(e->{
            controller.pauseSimulation();
            start.setEnabled(true);
            pause.setEnabled(false);
        });
        start.setEnabled(false);

        JMenuItem step = new JMenuItem("Step", Utils.getScaledImage("step.png",dim,dim));
        step.addActionListener(e-> new StepDialog());

        JMenuItem stop = new JMenuItem("Stop", Utils.getScaledImage("stop.png",dim,dim));
        stop.addActionListener(e->controller.stopSimulation());
        simConfig.add(close);
        simConfig.add(addImage);
        simConfig.add(removeImage);
        simConfig.addSeparator();
        simConfig.add(start);
        simConfig.add(step);
        simConfig.add(pause);
        simConfig.add(stop);
        simConfig.setEnabled(false);

        menus.add(file);
        menus.add(simConfig);
        menus.forEach(m->add(m));
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        menus.forEach(m->{
            m.setEnabled(enabled);
        });
    }

    /**
     * This is a JDialog for request the number of step
     * that user want to skip in the simulation
     */
    private class StepDialog extends JDialog{
        StepDialog(){

            setTitle("Enter the number of steps that you want do");
            setSize(400,200);
            setLocationRelativeTo(null);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gb = new GridBagConstraints();

            JFormattedTextField n_step =  new JFormattedTextField(NumberFormat.getIntegerInstance());   //number of step field
            JLabel errMsg = new JLabel("Error! Insert an integer number");
            n_step.setColumns(10);
            JButton enter = new JButton("Ok");
            enter.addActionListener(e->{
                try {
                    controller.stepSimulation(((Number)(n_step.getValue())).intValue());
                    dispose();
                } catch(Exception ex) {
                    errMsg.setVisible(true);
                    panel.add(errMsg, gb);
                }
            });

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e->dispose());

            gb.insets = new Insets(0,0,0,10);
            gb.gridx = 0;
            gb.gridy = 1;
            gb.gridwidth = 3;
            panel.add(n_step,gb);

            gb.anchor = GridBagConstraints.LINE_END;
            gb.gridwidth = 1;
            gb.gridx = 1;
            gb.gridy = 2;
            panel.add(cancel,gb);

            gb.anchor = GridBagConstraints.LINE_START;
            gb.gridx = 2;
            gb.gridy = 2;
            panel.add(enter,gb);

            gb.gridx = 1;
            gb.gridy = 0;
            gb.gridwidth = 3;
            errMsg.setForeground(Color.red);
            errMsg.setVisible(false);
            panel.add(errMsg, gb);

            setContentPane(panel);
            setVisible(true);
        }
    }
}


