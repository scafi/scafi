package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;

import javax.swing.*;
import java.awt.*;

/**
 *Created by chiara on 12/11/16.
 */
public class SensorOptionPane extends JDialog {

    private final JComboBox<String> sensorsChoice = new JComboBox<>();
    private final JComboBox<String> operators = new JComboBox<>();
    private final Controller controller = Controller.getIstance();

    public SensorOptionPane(final String title){

        setTitle(title);
        setSize(600,300);
        this.setLocationRelativeTo(null);
        JTextField valueField = new JTextField(10);
        JButton enter = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        enter.addActionListener(e->{
            if(operators.getItemCount()>1){
                controller.checkSensor((String) sensorsChoice.getSelectedItem(), (String) operators.getSelectedItem(),valueField.getText());
            } else {
                controller.setSensor((String) sensorsChoice.getSelectedItem(), valueField.getText());
            }
            this.dispose();
        });

        cancel.addActionListener(e->this.dispose());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(5,5,5,15);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(sensorsChoice, c);

        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 0;
        panel.add(operators, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 0;

        panel.add(valueField, c);
        c.insets = new Insets(10,0,0,0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 2;
        c.gridy = 1;
        panel.add(cancel, c);

        c.anchor = GridBagConstraints.LINE_START; //bottom of space
        c.gridx = 3;       //aligned with button 2
        c.gridy = 1;       //third row
        panel.add(enter, c);

        setContentPane(panel);
        setVisible(true);
    }

    public void addSensor(final String sensorName){
        sensorsChoice.addItem(sensorName);
    }

    public void addOperator(final String operator){
        operators.addItem(operator);
    }
}
