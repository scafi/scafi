package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;
import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * This is the panel where are represents
 * the connection of neighbors
 */

public class NeighborsPanel extends JPanel {

    Controller controller = Controller.getIstance();

    NeighborsPanel(){
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        this.setOpaque(false);
        this.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.removeAll();
        g.setColor(Color.black);
        //call the neighborhood to the network object
        controller.getNeighborhood().forEach((n,nghb)->{
            Point p1 = Utils.calculatedGuiNodePosition(n.getPosition());
            int p1x = (int)(p1.x + (Utils.getSizeGuiNode().getWidth() / 2));
            int p1y = (int)(p1.y + (Utils.getSizeGuiNode().getHeight() / 2));
            nghb.forEach(ng->{
                Point p2 = Utils.calculatedGuiNodePosition(ng.getPosition());
                int p2x = (int)(p2.x + (Utils.getSizeGuiNode().getWidth() / 2));
                int p2y = (int)(p2.y + (Utils.getSizeGuiNode().getHeight() / 2));
                g.drawLine(p1x, p1y, p2x, p2y);
            });
        });
    }
}


