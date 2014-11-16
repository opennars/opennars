package automenta.vivisect.face;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Date;
import javax.swing.JPanel;

/**
 *
 * @author me
 */


public abstract class BaseClass extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener
{   
    BufferedImage db;   
    Graphics buffer;   
    Thread t;   
    int mousex;   
    int mousey;   
    int mouselx;   
    int mousely;   
    int key;   
    int width;   
    int height;   
    public boolean mouseMove;   
    public boolean mouseDown;   
    public boolean mouseDrag;   
    public boolean mouseUp;   
    public boolean keyDown;   
    public boolean mouseIsDown;   
    boolean resizing;   
    boolean debugging;   
    boolean mouseLeft;   
    boolean mouseMiddle;   
    boolean mouseRight;   
    Date mouseTime;   
    MouseEvent mouseEvent;   
    Rectangle r;   
    Color bgcolor;   
    Color fgcolor;   
    int loX;   
    int loY;   
    int hiX;   
    int hiY;   
    private static final long serialVersionUID = 1L;   
    private BufferedImage db2;
    
    Kernel kernel = new Kernel(3, 3,
    new float[] {
  1f/9f, 1f/9f, 1f/9f,
  1f/9f, 1f/9f, 1f/9f,
  1f/9f, 1f/9f, 1f/9f});
    BufferedImageOp op = new ConvolveOp(kernel);
    
    public BaseClass()   
    {   
        super();
        db = null;   
        buffer = null;   
        mouseMove = false;   
        mouseDown = false;   
        mouseDrag = false;   
        mouseUp = false;   
        keyDown = false;   
        mouseIsDown = false;   
        resizing = true;   
        debugging = false;   
        mouseTime = null;   
        mouseEvent = null;   
        r = new Rectangle(0, 0, 0, 0);   
        bgcolor = Color.white;   
        fgcolor = Color.black;   
    }   
   
    public void damage(int i, int j, int k, int l)   
    {   
        loX = Math.min(loX, i);   
        loY = Math.min(loY, j);   
        hiX = Math.max(hiX, k);   
        hiY = Math.max(hiY, l);   
    }   
   
    public void drawArrow(Graphics g, int i, int j, int k, int l, int i1)   
    {   
        int j1 = k - i;   
        int k1 = l - j;   
        int l1 = (int)Math.sqrt((double)(j1 * j1 + k1 * k1) + 0.5D);   
        if(l1 > 0)   
        {   
            int i2 = (-k1 * i1) / l1 / 2;   
            int j2 = (j1 * i1) / l1 / 2;   
            int k2 = k - 3 * j2;   
            int l2 = l + 3 * i2;   
            int ai[] = {   
                i - i2, i + i2, k2 + i2, k2 + 3 * i2, k, k2 - 3 * i2, k2 - i2   
            };   
            int ai1[] = {   
                j - j2, j + j2, l2 + j2, l2 + 3 * j2, l, l2 - 3 * j2, l2 - j2   
            };   
            g.fillPolygon(ai, ai1, 7);   
        }   
    }   
   
    public void drawBar(Graphics g, int i, int j, int k, int l, int i1)   
    {   
        int j1 = k - i;   
        int k1 = l - j;   
        int l1 = (int)Math.sqrt((double)(j1 * j1 + k1 * k1) + 0.5D);   
        if(l1 > 0)   
        {   
            int i2 = (-k1 * i1) / l1 / 2;   
            int j2 = (j1 * i1) / l1 / 2;   
            if(i2 == 0 && j2 == 0)   
                if(j1 * j1 > k1 * k1)   
                    j2 = 1;   
                else   
                    i2 = 1;   
            int ai[] = {   
                i - i2, i + i2, k + i2, k - i2   
            };   
            int ai1[] = {   
                j - j2, j + j2, l + j2, l - j2   
            };   
            g.fillPolygon(ai, ai1, 4);   
        }   
    }   
   
    public void drawCircle(Graphics g, int i, int j, int k)   
    {   
        g.drawOval(i - k, j - k, 2 * k, 2 * k);   
    }   
   
    public void fillCircle(Graphics g, int i, int j, int k)   
    {   
        g.fillOval(i - k, j - k, 2 * k, 2 * k);   
    }   
      
    public boolean isDamage()   
    {   
        return hiX > loX && hiY > loY;   
    }   

    @Override
    public void keyPressed(KeyEvent e) {        
        keyDown = true;   
        key = e.getKeyChar();
    }   
    @Override
    public void keyReleased(KeyEvent e) {
        keyDown = false;   
        key = e.getKeyChar();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    
    }
    
    
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseDrag = mouseIsDown = true;   
        mouselx = mousex;   
        mousely = mousey;   
        mousex = e.getX();   
        mousey = e.getY();   
        mouseTime = new Date();   
        mouseEvent = e;   
    }   

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseMove = true;   
        mouseIsDown = false;   
        mouselx = mousex;   
        mousely = mousey;   
        mousex = e.getX();   
        mousey = e.getY();   
        mouseTime = new Date();   
        mouseEvent = e;   
    }   

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = mouseIsDown = true;   
        mouseMiddle = e.getModifiers() == 8;   
        mouseRight = e.getModifiers() == 4;   
        mouseLeft = !mouseMiddle && !mouseRight;   
        mouselx = mousex;   
        mousely = mousey;   
        mousex = e.getX();   
        mousey = e.getY();   
        mouseTime = new Date();   
        mouseEvent = e;   
    }

    @Override
    public void mouseExited(MouseEvent e) {
    
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    
    }

    @Override
    public void mouseClicked(MouseEvent e) {    
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseUp = true;   
        mouseIsDown = false;   
        mouselx = mousex;   
        mousely = mousey;   
        mousex = e.getX();   
        mousey = e.getY();   
        mouseTime = new Date();   
        mouseEvent = e;   
    }
    
    public void noDamage()   
    {   
        loX = 1000;   
        loY = 1000;   
        hiX = -1;   
        hiY = -1;   
    }   
   
    @Override
    public void paint(Graphics g)   
    {   
        if(db != null)    {
            
            /*if (db!=null) {
                db2 = op.filter(db, db2);
            }*/
            
            g.drawImage(db, 0, 0, this);   
        }
    }   
   
    public abstract void render(Graphics g);   
   
    public void run()   
    {   
        final long cycleDelay = 30L;
        try   
        {   
            double d = 0.0D;   
            do   
            {   
                width = getWidth();
                height = getHeight();
                if ((width == 0) || (height == 0)) {
                    Thread.sleep(cycleDelay);   
                    continue;
                }
                
                if(r.width != width || r.height != height)   
                {   
                    
                    db = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    buffer = db.getGraphics();   
                    /*((Graphics2D)buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
        RenderingHints.VALUE_ANTIALIAS_ON);*/
                    r = bounds();   
                    resizing = true;   
                    damage(0, 0, width, height);   
                }   
                if(isDamage())   
                {   
                    buffer.setColor(bgcolor);   
                    buffer.fillRect(loX, loY, hiX - loX, hiY - loY);   
                    buffer.setColor(fgcolor);   
                }   
                render(buffer);   
                //mouseDown = mouseDrag = mouseUp = mouseMove = resizing = false;   


  
                repaint();   
                Thread.sleep(cycleDelay);
                d += 0.10000000000000001D;   
            } while(true);   
        }   
        catch(InterruptedException _ex)   
        {   
            return;   
        }   
    }   
   
    public void sleep(int i)   
    {   
        try   
        {   
            Thread.sleep(i);   
        }   
        catch(InterruptedException _ex) { }   
    }   
   
    public void start()   
    {   
        
        if(t == null)   
        {   
            t = new Thread(this);   
            t.start();   
        }   
    }   
   
    public void stop()   
    {   
        if(t != null) {   
            t.stop();   
            t = null;   
        }   
    }   
   
}  