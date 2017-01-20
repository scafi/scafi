package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Vector;

/**
 * This class represent the panel where the user can
 * configure a new simulation
 *
 * Created by chiara on 19/10/16.
 */
public class ConfigurationPanel extends JDialog implements PropertyChangeListener {

    private final Controller controller = Controller.getIstance();
    private final JLabel err;
    private final GridBagConstraints gbc;
    private int y = 0;  //used in the addRows method

    //Fields for data entry
    private final JFormattedTextField nodeNumberField;
    private final JFormattedTextField neinghborsAreaField;
    private final JFormattedTextField deltaRoundField;
    private final JComboBox<String> topologyField;
    private final JTextField runProgram;
    private final JTextField strategy;
    private final JButton addFile;

    //Button for starting the simulation
    private JButton submitButton;

    public ConfigurationPanel(){

        //initial settings
        setTitle("Configuration");
        setSize(Utils.getConfPanelDim());
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        //Create the text fields and set them up.
        nodeNumberField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        nodeNumberField.setValue(20);
        nodeNumberField.setColumns(10);
        nodeNumberField.addPropertyChangeListener(this);

        //Create the text fields and set them up.
        Vector<String> vtop = new Vector<>();
        vtop.add("Random");
        vtop.add("Grid");
        topologyField = new JComboBox<String>(vtop);
        topologyField.addPropertyChangeListener(this);

        deltaRoundField = new JFormattedTextField(NumberFormat.getNumberInstance());
        deltaRoundField.setValue(10.0);
        deltaRoundField.setColumns(10);
        deltaRoundField.addPropertyChangeListener(this);

        neinghborsAreaField = new JFormattedTextField(NumberFormat.getNumberInstance());
        neinghborsAreaField.setValue(0.2);
        neinghborsAreaField.setColumns(10);
        neinghborsAreaField.addPropertyChangeListener(this);

        runProgram = new JTextField();
        runProgram.setText("sims.");
        runProgram.setColumns(10);
        runProgram.addPropertyChangeListener(this);

        strategy = new JTextField();
        strategy.setColumns(10);
        strategy.addPropertyChangeListener(this);

        addFile = new JButton("File");
        addFile.addActionListener(e->{
            //TODO aggiungi filter?
            JFileChooser choose = new JFileChooser();
            choose.showOpenDialog(this);
            addFile.setText(choose.getSelectedFile().getName());
            //controller.startSimulation(choose.getSelectedFile());
        });

        err = new JLabel("Error! Invalid input");
        err.setForeground(Color.red);
        err.setVisible(false);

        JPanel p1 = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();

        //Rows
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 0, 0, 10);
        insertRow("Number of nodes: ", nodeNumberField, p1);
        //insertRow("Topology", topologyField, p1);
        //insertRow("Neighborhood policy: ", neinghborsAreaField, p1);
        //insertRow("∆ round: ", deltaRoundField, p1);
        insertRow("Run program: ", runProgram, p1);
        //insertRow("Strategy: ", strategy, p1);
        //insertRow("Add configuration file: ", addFile, p1);

        //Button
        submitButton = new JButton("Start");
        gbc.gridx = 2;
        gbc.gridy = y;
        gbc.insets = new Insets(20,10,0,0);
        gbc.anchor = GridBagConstraints.CENTER;
        p1.add(submitButton, gbc);
        y++;

        //error label
        gbc.gridx = 2;
        p1.add(err, gbc);

        setContentPane(p1);

        //Start
        submitButton.addActionListener(e->{
            try {
                int nNodes = ((Number) nodeNumberField.getValue()).intValue();
                double policyNeighborhood = ((Number) neinghborsAreaField.getValue()).doubleValue();
                double deltaRound = ((Number) deltaRoundField.getValue()).doubleValue();
                String runP = runProgram.getText();
                String str = strategy.getText();
                String topology = topologyField.getSelectedItem() != null ? topologyField.getSelectedItem().toString() : "";
                controller.startSimulation(nNodes, topology, policyNeighborhood, runP, deltaRound, str);
                dispose();
            }catch (Exception ex){
                ex.printStackTrace();
                showErr(0);
            }
        });
        setVisible(true);
    }

    /**
     * Check's if the field's input is valid
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source == nodeNumberField) {
            if(!nodeNumberField.isEditValid()){
                showErr(0);
            } else {
                err.setVisible(false);
            }
        } else if (source == deltaRoundField) {
            if(!deltaRoundField.isEditValid()){
                showErr(1);
            }else {
                err.setVisible(false);
            }
        } else if (source == neinghborsAreaField) {
            if(!neinghborsAreaField.isEditValid()){
                showErr(2);
            }else {
                err.setVisible(false);
            }
        }
    }

    /**
     * Put String on left and JComponent on right of p.
     * @param name
     * @param comp
     * @param p
     */

    private void insertRow(final String name, final JComponent comp, final JPanel p){
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.LINE_END;
        p.add(new JLabel(name), gbc);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.LINE_START;
        p.add(comp, gbc);
        y++;
    }

    /**
     *Show the "error" label
     * @param y
     */
    private void showErr(final int y){
        gbc.gridx = 2;
        gbc.gridy = y;
        err.setVisible(true);
        getContentPane().add(err, gbc);
    }
}
