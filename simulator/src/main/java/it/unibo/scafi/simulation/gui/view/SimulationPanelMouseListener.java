package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;

/**
 * This is the SimulationPanel's mouse listener
 * Created by chiara on 07/11/16.
 */

public class SimulationPanelMouseListener extends MouseAdapter {

    private final  SimulationPanel panel;
    // private final ControllerView controllerView = ControllerView.getIstance();
    private final Rectangle captureRect;
    Controller controller = Controller.getIstance();

    private final Point point = new Point();
    private boolean flag = false;
    Point start = new Point();

    SimulationPanelMouseListener(SimulationPanel simPanel){
        panel = simPanel;
        captureRect = panel.getCaptureRect();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        double a = captureRect.x + captureRect.getWidth();
        double b = captureRect.y + captureRect.getHeight();

        if(e.getX() > captureRect.x && e.getX() < a
                &&  e.getY() > captureRect.y && e.getY() < b){
            flag = true;
        } else {
            flag = false;
        }

        if(!flag) {
            for (JInternalFrame jf : panel.getAllFrames()) {  //deselezionare dei nodi
                try {
                    jf.setSelected(false);        //deseleziono tutti i nodi
                } catch (PropertyVetoException e1) {
                    e1.printStackTrace();
                }
            }


            captureRect.setBounds(0, 0, 0, 0); //= new Rectangle(); //creo una nuova area di selezione
            panel.maybeShowPopup(e); //nascondo il menu
            panel.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        start = me.getPoint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        double a = captureRect.x + captureRect.getWidth();
        double b = captureRect.y + captureRect.getHeight();

        if (!e.isMetaDown()) {

            point.x = e.getX();
            point.y = e.getY();

            if(e.getX() > captureRect.x && e.getX() < a
                    &&  e.getY() > captureRect.y && e.getY() < b){
                flag = true;
            } else {
                flag = false;
            }
        }
        panel.maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        panel.maybeShowPopup(e);
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (flag) {
            moveRectangle(me);
        } else {
            Point end = me.getPoint();

            if (end.x < start.x && end.y < start.y) {

                captureRect.setRect(end.x, end.y, start.x - end.x, start.y - end.y);// = new Rectangle(end, new Dimension(start.x - end.x, start.y - end.y));

            } else if (end.getX() > start.getX() && end.getY() < start.getY()) {

                Point start1 = new Point(start.x, end.y);
                captureRect.setRect(start1.x, start1.y, end.x - start.x, start.y - end.y); //= new Rectangle(start1, new Dimension(end.x - start.x, start.y - end.y));

            } else if (end.x > start.x && end.y > start.y) {

                captureRect.setRect(start.x, start.y, end.x - start.x, end.y - start.y); // = new Rectangle(start, new Dimension(end.x - start.x, end.y - start.y));

            } else if (end.getX() < start.getX() && end.getY() > start.getY()) {

                Point start1 = new Point(end.x, start.y);
                captureRect.setRect(start1.x, start1.y, start.x - end.x, end.y - start.y); // = new Rectangle(start1, new Dimension(start.x - end.x, end.y - start.y));
            }

            panel.setRectSelection(captureRect);
            panel.repaint();
            controller.selectNodes(captureRect); // Selects nodes within area
        }
    }

    public void moveRectangle(MouseEvent e) {
        if (!e.isMetaDown()) {

            captureRect.setLocation(captureRect.getLocation().x + e.getX() - point.x, captureRect.getLocation().y + e.getY() - point.y);
            panel.setRectSelection(captureRect);
            controller.moveNodeSelect(new Point(e.getX() - point.x, e.getY() - point.y));
            point.x = e.getX();
            point.y = e.getY();

            panel.repaint();
        }
    }
}
