package it.unibo.scafi.simulation.gui.view;

import it.unibo.scafi.simulation.gui.utility.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * This is the general frame that contains all panel
 * Created by chiara on 19/10/16.
 */
public class SimulatorUI extends JFrame{

    private SimulationPanel center = new SimulationPanel();   //JDesktopPane per visualizzare le simulazioni
    private final JMenuBar menuBarNorth = new MenuBarNorth();       //barra del menù in alto
    private Dimension oldDim;   //utilizzato per la riposizione dei nodi quando il frame viene rimpicciolito

    public SimulatorUI(){

        super("SCAFI Simulator");

        setSize(Utils.getFrameDimension());
        oldDim = Utils.getFrameDimension();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(this.center, BorderLayout.CENTER);

        setContentPane(panel);
        this.setJMenuBar(menuBarNorth);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Utils.setDimensionFrame(getSize());

                for (JInternalFrame i : center.getAllFrames()) {
                    i.setSize(Utils.getSizeGuiNode());    //ridimensionamento

                    i.setLocation((int) Math.round(i.getLocation().getX() * getWidth()/ oldDim.getWidth()),
                            (int) Math.round(i.getLocation().getY() * getHeight() /oldDim.getHeight())); //riposizionamento
                }
                center.getCaptureRect().setSize((int) Math.round( center.getCaptureRect().getWidth() * getWidth()/ oldDim.getWidth()),
                        (int) Math.round( center.getCaptureRect().getHeight() * getHeight() /oldDim.getHeight()));

                center.getCaptureRect().setLocation((int) Math.round( center.getCaptureRect().getLocation().getX() * getWidth()/ oldDim.getWidth()),
                        (int) Math.round( center.getCaptureRect().getLocation().getY() * getHeight() /oldDim.getHeight())); //riposizionamento
                oldDim = getSize();
            }
        });
        setVisible(true);
    }

    /**
     * @return center panel
     */
    public SimulationPanel getSimulationPanel(){
        return center;
    }

    public void setSimulationPanel(final SimulationPanel simPanel){
        this.remove(center);
        this.add(simPanel,BorderLayout.CENTER);
        center = simPanel;
        this.revalidate();
        this.repaint();
    }

    /**
     * @return application menu
     */
    public JMenuBar getMenuBarNorth(){ return menuBarNorth; }

}
