package nars.gui.output;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;



public class BitmapPanel extends JComponent {
    
    /** Holds the reference to the BuffedImage objec on which the drawing is done */
    private BufferedImage image;

    /** The value of the X coordinate of the pannel */
    private final int X = 150;
    /** The value of the Y coordinate of the pannel */
    private final int Y = 150;

    /**
     * Creates the new image pannel by colling the constructor of the JPanel
     */
    public BitmapPanel() {
    }
    public BitmapPanel(BufferedImage i) {
        setImage(i);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            //g.drawImage(resize(image, X, Y), (getWidth()-X)/2, (getHeight()-Y)/2, null);            
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
       //          g.drawImage(image, 0, 0, null);
        } catch (NullPointerException e) {}
    }

    /**
     * Sets the image to the given location
     *
     * @param imagePath the location of the image
     */
    public void setImage(String imagePath) {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(imagePath));
            image = img;
            repaint();
        } catch (IOException ex) { }
    }
    
    public void setImage(BufferedImage img) {
        image = img;
        repaint();
    }

    /**
     * Resizes the given image to the given size
     * 
     * @param img the image that needs to be resized
     * @param newW the number that representd the new width of the image
     * @param newH the number that representd the new hight of the image
     *
     * @return the resized image
     */
    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, 1);
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
    
}
