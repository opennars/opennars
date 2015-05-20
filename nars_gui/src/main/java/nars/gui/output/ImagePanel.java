package nars.gui.output;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by me on 4/19/15.
 */
public class ImagePanel extends JComponent {

    final int w, h;
    public BufferedImage image;

    public ImagePanel(int width, int height) {
        super();

        this.w = width;
        this.h = height;

        setDoubleBuffered(true);
        setIgnoreRepaint(true);

        setSize(width, height);
        Dimension minimumSize = new Dimension(width, height);
        setBorder(LineBorder.createGrayLineBorder());
        setMinimumSize(minimumSize);
        setPreferredSize(minimumSize);
    }

    public synchronized Graphics2D g() {
        if (image == null) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        if (image != null) {
            return image.createGraphics();
        }
        return null;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        //super.paintComponent(g);
        if (image!=null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
        else {
            super.paintComponent(g);
        }
        super.paintBorder(g);
    }


}
