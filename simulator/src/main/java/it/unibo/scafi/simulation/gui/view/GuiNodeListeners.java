package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.controller.Controller;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class represent an ActionListener
 * for a GuiNode
 * Created by chiara on 03/06/16.
 */

public class GuiNodeListeners extends MouseAdapter{

    private final GuiNode node;
    private final Controller controller = Controller.getIstance();
    private final Point p = new Point();

    GuiNodeListeners(final GuiNode node){
        this.node = node;
    }

    //apre il pannello delle informazioni
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if(e.getButton()==MouseEvent.BUTTON3) {
            repositionsInfoPanel();
        }
    }

    //cattura il punto da cui parte il GuiNode
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (!e.isMetaDown()) {
            p.x = e.getX();
            p.y = e.getY();
        }
    }

    //sposta il GuiNode
    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);

        if (!e.isMetaDown()) {
            Point pos = node.getLocation();
            node.setLocation(pos.x + e.getX() - p.x, pos.y + e.getY() - p.y);
            if(node.getInfoPanel()!=null) {
                repositionsInfoPanel();
            }
            controller.moveNode(node, pos);
        }
    }

    /*riposizione il pannello delle informazioni
   basandosi sulla posizione del GuiNode nell schermo*/
    private void repositionsInfoPanel(){
        controller.showInfoPanel(node, true);
        NodeInfoPanel infoP = node.getInfoPanel();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (node.getX() > screen.getWidth() / 2) {
            infoP.setLocation(node.getX() - infoP.getSize().width, node.getY());  //se è nella parte destra del monitor apro le info a sinistra
        } else {
            infoP.setLocation(node.getX() + node.getSize().width, node.getY());   //altrimenti a destra
        }
        if (node.getY() > (screen.getHeight() / 1.5)) {
            infoP.setLocation(node.getX(), node.getY() - infoP.getHeight());    //se è nella parte bassa lo apro sopra
        }
    }
}




