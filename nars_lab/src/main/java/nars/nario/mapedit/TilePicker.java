package nars.nario.mapedit;

import nars.nario.Art;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


public class TilePicker extends JComponent implements MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = -7696446733303717142L;

    private int xTile = -1;
    private int yTile = -1;
    
    public byte pickedTile;

    private final byte paint = 0;
    private LevelEditor tilePickChangedListener;

    public TilePicker()
    {
        Dimension size = new Dimension(256, 256);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        Art.init(getGraphicsConfiguration());
    }

    @Override
    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, 256, 256);
        
        for (int x=0; x<16; x++)
            for (int y=0; y<16; y++)
            {
                g.drawImage(Art.level[x][y], (x << 4), (y << 4), null);
            }

        g.setColor(Color.WHITE);
        int xPickedTile = (pickedTile&0xff)%16;
        int yPickedTile = (pickedTile&0xff)/16;
        g.drawRect(xPickedTile * 16, yPickedTile * 16, 15, 15);

        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        xTile = -1;
        yTile = -1;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        
        setPickedTile((byte)(xTile+yTile*16));
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }

    public void setPickedTile(byte block)
    {
        pickedTile = block;
        repaint();
        if (tilePickChangedListener!=null)
            tilePickChangedListener.setPickedTile(pickedTile);
    }

    public void addTilePickChangedListener(LevelEditor editor)
    {
        tilePickChangedListener = editor;
        if (tilePickChangedListener!=null)
            tilePickChangedListener.setPickedTile(pickedTile);
    }
}