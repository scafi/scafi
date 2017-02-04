package it.unibo.scafi.simulation.gui.utility;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;

/**
 * This class handles the images update,
 * the component dimension
 * and the component position
 * Created by chiara on 14/11/16.
 */
public final class Utils {

    private static Dimension frameDimension = Toolkit.getDefaultToolkit().getScreenSize();

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";

    /*
    * Get the extension of a file.
    */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = (Utils.class.getResource("/"+ path));
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static ImageIcon getScaledImage(String srcImg, int w, int h){

        ImageIcon i = new ImageIcon(Utils.class.getResource("/"+srcImg).getPath());

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(i.getImage(), 0, 0, w, h, null);
        g2.dispose();
        ImageIcon res = new ImageIcon(resizedImg);
        res.setDescription(srcImg);
        return res;
    }

    /**
     * immagine "selezionata"
     * @param icon
     * @return
     */
    public static ImageIcon getSelectedIcon(final Icon icon){
        String nameIcon = Paths.get(((ImageIcon) icon).getDescription()).getFileName().toString();

        if (nameIcon.equals("source.png") || nameIcon.equals("sourceSelect.png")) {
            return getScaledImage("sourceSelect.png", icon.getIconWidth(), icon.getIconHeight());

        } else if (nameIcon.equals("sensorOk.png") || nameIcon.equals("sensorOkSelect.png")) {
            return getScaledImage("sensorOkSelect.png",  icon.getIconWidth(), icon.getIconHeight());

        } else {
            return getScaledImage("nodeSelect.png", icon.getIconWidth(), icon.getIconHeight());
        }
    }

    /**
     * immagine "non selezionata"
     * @param icon
     * @return
     */
    public static ImageIcon getNotSelectIcon(final Icon icon){
        String nameIcon = Paths.get(((ImageIcon) icon).getDescription()).getFileName().toString();

        if (nameIcon.equals("sourceSelect.png") || nameIcon.equals("source.png")) {
            return getScaledImage("source.png", icon.getIconWidth(), icon.getIconHeight());

        } else if (nameIcon.equals("sensorOkSelect.png") || nameIcon.equals("sensorOk.png")) {
            return getScaledImage("sensorOk.png",  icon.getIconWidth(), icon.getIconHeight());

        } else {
            return getScaledImage("node.png",  icon.getIconWidth(), icon.getIconHeight());
        }
    }

    /**
     * setta la dimensione del frame dell'applicazione
     * @param d
     */
    public static void setDimensionFrame(final Dimension d){frameDimension = d;}

    /**
     *
     * @return dimensione del frame dell'applicazione
     */
    public static Dimension getFrameDimension(){
        return frameDimension;
    }

    /**
     * @return GuiNode Dimension 5% FrameWidth and 10% FrameHeight
     */
    public static Dimension getSizeGuiNode() { return new Dimension((frameDimension.width*10/100),(frameDimension.height*10/100)); }

    /**
     * @return Configuration Panel Dimension 50% Fame Dimension
     */
    public static Dimension getConfPanelDim(){ return  new Dimension((Toolkit.getDefaultToolkit().getScreenSize().width/2),(Toolkit.getDefaultToolkit().getScreenSize().height/2));}

    /**
     * @return Menu panel of Simulation Panel Dimension
     */
    public static Dimension getMenuSimulationPanelDim(){return  new Dimension((frameDimension.width*10/100),(frameDimension.height*20/100));}

    /**
     * @return Menu panel of Simulation Panel Dimension
     */
    public static Dimension getGuiNodeInfoPanelDim(){return  new Dimension((frameDimension.width*10/100),(frameDimension.height*20/100));}

    /**
     * @return IconMenu Dimension
     */
    public  static Dimension getIconMenuDim(){return new Dimension((int)(frameDimension.width*1.5/100),(frameDimension.height*2/100));}

    /**
     *
     * @param position
     * @return calcola la posizione del GuiNode
     */
    public static Point calculatedGuiNodePosition(final Point2D position ){ // position.x : 1 = res.x : frame.getWidth();
        Point res = new Point();
        res.x = (int)(position.getX()*(frameDimension.getWidth()-getSizeGuiNode().getWidth()));  // Placing at the center of the frame
        res.y = (int) (position.getY()*(frameDimension.getHeight()-getSizeGuiNode().getHeight()));
        return res;
    }

    /**
     *
     * @param position
     * @return calcola la posizione del nodo del model
     */
    public static Point2D calculatedNodePosition(final Point position){
        return new Point2D.Double(position.getX()/ (getFrameDimension().getWidth()-getSizeGuiNode().getWidth()),
                position.getY()/ (getFrameDimension().getHeight()-getSizeGuiNode().getHeight()) );
    }
}