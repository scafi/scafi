package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.model.Node;
import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.nio.file.Paths;

/**
 * This class represents a
 * graphics representation of Node
 *
 * Created by chiara on 01/06/16.
 */
public class GuiNode extends JInternalFrame {

    //costante per il font del valore visualizzato (valueShow)
    private final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 14);

    private final JLabel valueShow;
    private final JButton button;
    private NodeInfoPanel infoPanel;    //pannello delle informazioni del nodo
    private boolean isSelected;
    private Node node = null;

    public GuiNode(Node node) {
        //initial settings
        super();
        this.node = node;
        this.valueShow = new JLabel("");
        this.button = new JButton(Utils.createImageIcon("node.png"));
        setSize(Utils.getSizeGuiNode());
        setBorder(null);

        JPanel background = new JPanel(new BorderLayout());
        background.setOpaque(false);    //trasparenza
        setContentPane(background);
        valueShow.setFont(DEFAULT_FONT);

        //pannello in cui visualizzo una singola info (id, export,...)
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.add(valueShow);
        background.add(north, BorderLayout.NORTH);

        //pannello con il bottone con l'icona del nodo
        JPanel pBotton = new JPanel();
        pBotton.setOpaque(false);
        pBotton.add(button);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setOpaque(false);
        button.addMouseListener(new GuiNodeListeners(this)); //listener dei movimentis
        button.addMouseMotionListener(new GuiNodeListeners(this));
        background.add(pBotton, BorderLayout.SOUTH);

        revalidate();
        repaint();
        setVisible(true);
        try {
            this.setSelected(false);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public Node getNode(){ return this.node; }

    //gestione dimensione nodo
    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        String nameImg = Paths.get(((ImageIcon) button.getIcon()).getDescription()).getFileName().toString(); //path icona presente sul bottone
        int dividendo = getWidth() < getHeight() ? getWidth() : getHeight();
        //ridimensionamento icone
        button.setIcon(Utils.getScaledImage(nameImg, dividendo / 2, dividendo / 2));
        //ridimensionamento font
        if (d.getHeight() < (Utils.getFrameDimension().getHeight() / 2)){
            this.valueShow.setFont(DEFAULT_FONT.deriveFont(DEFAULT_FONT.getSize()/2));
        } else {
            this.valueShow.setFont(DEFAULT_FONT);
        }
        //ridimensionamento infoPanel
        if (infoPanel != null) {
            infoPanel.setSize(Utils.getMenuSimulationPanelDim());
            infoPanel.setLocation(getLocation().x + getWidth(), getLocation().y);
        }
    }

    //gestione posizionamento nodo e pannello informazioni
    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x,y);
        if(this.infoPanel!=null){
            infoPanel.setLocation(x+getWidth(),y);
        }
    }

    //gestione selezione nodi
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) throws PropertyVetoException {
//non faccio la super perchè se no non riesco a selezionarne più di uno
        if (selected) {
            this.button.setIcon(Utils.getSelectedIcon( button.getIcon()));
            this.valueShow.setForeground(Color.lightGray);
        } else {
            this.button.setIcon(Utils.getNotSelectIcon(button.getIcon()));
            this.valueShow.setForeground(Color.black);
        }
        isSelected = selected;
    }

    //gestione visualizzazione valori
    public void setValueToShow(final String text) {
        this.valueShow.setText("<html>" + text.replaceAll("\n", "<br>"));
    }

    public void setLabelFont(final Font font) {
        this.valueShow.setFont(font);
    }

    public void setLabelColor(final Color color) {
        this.valueShow.setForeground(color);
    }

    public void setInfoPanel(final NodeInfoPanel p) {
        this.infoPanel = p;
    }

    public void setImageButton(final String res) {
        int dividendo = getWidth() < getHeight() ? getWidth() : getHeight();
        button.setIcon(Utils.getScaledImage(res, dividendo / 2, dividendo / 2));
    }

    public void showInfo(final boolean show) {
        infoPanel.setVisible(show);
    }

    public NodeInfoPanel getInfoPanel() {
        return this.infoPanel;
    }
}


