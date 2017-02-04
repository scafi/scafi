package it.unibo.scafi.simulation.gui.view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This is the most important panel in wich will be executed the simulation
 * Created by chiara on 19/10/16.
 */

public class SimulationPanel extends JDesktopPane {

    private final NeighborsPanel neighborsPanel = new NeighborsPanel(); //pannello visualizzazione vicini
    private Image bkgImage = null;  //TODO cerca di cambiarlo
    private final Rectangle captureRect = new Rectangle(); //rettangolo di selezione
    private final MyPopupMenu popup = new MyPopupMenu();   //menu tasto destro

    public SimulationPanel(){
        this.setBackground(Color.decode("#9EB3C2")); //azzurro
        setBorder(new LineBorder(Color.black));
        this.add(neighborsPanel, 1);

        SimulationPanelMouseListener motion =  new SimulationPanelMouseListener(this);
        this.addMouseListener(motion);  //gestisco quando appare il pannello delle opzioni
        this.addMouseMotionListener(motion);     //creo e gestisco l'era di selezione
    }

    @Override
    public void paintComponent(Graphics g)
    {
        if(bkgImage != null) {  //visualizzazione immagine di sfondo
            g.drawImage(bkgImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }
        if (captureRect!=null) {    //visualizzazione area di selezione
            g.setColor(Color.lightGray);
            g.drawRect((int)captureRect.getX(), (int)captureRect.getY(), (int)captureRect.getWidth(), (int)captureRect.getHeight());
            g.setColor(new Color(255,255,255,150));
            g.fillRect((int)captureRect.getX(), (int)captureRect.getY(), (int)captureRect.getWidth(), (int)captureRect.getHeight());;
        }
    }

    /**
     * Set the background image
     * @param bkgImage
     */
    public void setBackgroundImage(final Image bkgImage){
        this.bkgImage = bkgImage;
    }

    /**
     * Shows the panel representing the neighbourhood
     * @param show
     */
    public void showNeighbours(final boolean show){//mostro il pannello che visualizza i collegamenti con i vicini
        neighborsPanel.setVisible(show);
        this.revalidate();
        this.repaint();
    }

    public void setRectSelection(Rectangle r){
        this.captureRect.setRect(r);
    }

    public Rectangle getCaptureRect(){
        return captureRect;
    }

    public void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }

    public MyPopupMenu getPopUpMenu(){
        return this.popup;
    }
}
