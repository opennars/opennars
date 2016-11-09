package nars.lab.nario.mapedit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JComponent;
import nars.lab.nario.Art;
import nars.lab.nario.LevelRenderer;
import nars.lab.nario.level.Level;


public class LevelEditView extends JComponent implements MouseListener, MouseMotionListener
{
    private static final long serialVersionUID = -7696446733303717142L;

    private LevelRenderer levelRenderer;
    private Level level;

    private int xTile = -1;
    private int yTile = -1;
    private TilePicker tilePicker;

    public LevelEditView(TilePicker tilePicker)
    {
        this.tilePicker = tilePicker;
        level = new Level(256, 15);
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void setLevel(Level level)
    {
        this.level = level;
        Dimension size = new Dimension(level.width * 16, level.height * 16);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        repaint();
        levelRenderer.setLevel(level);
    }
    
    public Level getLevel()
    {
        return level;
    }

    public void addNotify()
    {
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
        levelRenderer = new LevelRenderer(level, getGraphicsConfiguration(), level.width * 16, level.height * 16);
        levelRenderer.renderBehaviors = true;
    }

    public void paintComponent(Graphics g)
    {
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, level.width * 16, level.height * 16);
        levelRenderer.render(g, 0, 0);
        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
        xTile = -1;
        yTile = -1;
        repaint();
    }

    public void mousePressed(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        if (e.getButton() == 3)
        {
            tilePicker.setPickedTile(level.getBlock(xTile, yTile));
        }
        else
        {
            level.setBlock(xTile, yTile, tilePicker.pickedTile);
            levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

            repaint();
        }
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseDragged(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        level.setBlock(xTile, yTile, tilePicker.pickedTile);
        levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

        repaint();
    }

    public void mouseMoved(MouseEvent e)
    {
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }
}