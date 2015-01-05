package automenta.vivisect.face;


   
import java.awt.BorderLayout;
import java.awt.Color;   
import java.awt.Component;   
import java.awt.Container;   
import java.awt.Dimension;   
import java.awt.Graphics;   
import java.awt.GridBagConstraints;   
import java.awt.GridBagLayout;   
import java.awt.Image;   
import java.awt.Insets;   
import java.awt.Panel;   
import java.awt.Point;   
import java.awt.Polygon;   
import java.awt.image.MemoryImageSource;   
import java.awt.image.PixelGrabber;   
import java.util.Vector;   
import javax.swing.JPanel;
   


public class GraphApp extends JPanel   
{   
    private static final long serialVersionUID = 1L;   
    protected int snapwidth;   
    protected int snapcols;   
    protected int snaprows;   
    protected int maxsnaps;   
    protected int framewidth;   
    protected int animwidth;   
    protected double timemult;   
    protected double timediv;   
    protected int xOffset;   
    protected int yOffset;   
    protected boolean dragdraw;   
    protected int dragMode;   
    protected int oldx;   
    protected int oldy;   
    protected boolean oldinanim;   
    protected boolean wasinanim;   
    protected boolean deleteFrame;   
    protected int scrPos;   
    protected int fullAnimWidth;   
    protected FaceGUI faceApplet;   
    protected Vector frames;   
    protected int currentSnap;   
    protected int currentFrame;   
    protected FaceFrame currentFace;   
    protected boolean doRollOver;   
    protected int rollSnap;   
    protected int rollFrame;   
    protected double preRollTargets[][];   
    protected Vector seqnos;   
    protected String codeBase;   
    protected boolean isFrame;   
    protected boolean standalone;   
    
    Panel snapControlPan;   
    Panel animControlPan;   
    DrawGraphAppPanel snapFacePan;   
    Panel animPan;   
    GridBagLayout gb;   
    GridBagConstraints gbc;   
    Image playIm;   
    Image pauseIm;   
    Polygon timeArrow;   
    Polygon paletteArrow;   

   
    public GraphApp(FaceGUI face, String s, String s1)   
    {   
        this(face, s, s1, false);   
    }   
   
    public GraphApp(FaceGUI face, String s, String s1, boolean flag)   
    {   
        super(new BorderLayout());
        
        add(face, BorderLayout.CENTER);
        
        codeBase = s;   
        faceApplet = face;   
        standalone = flag;   
        

        standalone = true;   
        
        timemult = 0.5D;   
        timediv = 1.0D / timemult;   
        if(faceApplet != null)   
            snapwidth = (int)(((float)faceApplet.bounds().width * 40F) / (float)faceApplet.bounds().height);   
        else   
            snapwidth = 25;   
        scrPos = 0;   
        currentSnap = -1;   
        rollSnap = -1;   
        frames = new Vector();   
        rollFrame = -1;   
        preRollTargets = null;   
        doRollOver = false;   
        setLayout(null);   
        snapControlPan = new Panel();   
        snapFacePan = new DrawGraphAppPanel(this, 1, this);   
        animControlPan = new Panel();   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 0;   
        gbc.anchor = 13;   
        gbc.fill = 0;   
        gbc.weightx = 0.0D;   
        gbc.weighty = 0.0D;   
        gbc.insets = new Insets(3, 3, 3, 3);   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 1;   
        gbc.anchor = 12;   
        gbc.fill = 0;   
        gbc.weightx = 0.0D;   
        gbc.weighty = 1.0D;   
        gbc.insets = new Insets(3, 3, 3, 3);   
        gbc = new GridBagConstraints();   
        gbc.gridx = 1;   
        gbc.gridy = 0;   
        gbc.anchor = 17;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 1;   
        gbc.gridy = 1;   
        gbc.anchor = 17;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 1.0D;   
        gbc.insets = new Insets(3, 3, 6, 3);   
        gb = new GridBagLayout();   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 0;   
        gbc.anchor = 13;   
        gbc.fill = 0;   
        gbc.weightx = 0.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 1;   
        gbc.anchor = 12;   
        gbc.fill = 0;   
        gbc.weightx = 0.0D;   
        gbc.weighty = 1.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 1;   
        gbc.gridy = 0;   
        gbc.anchor = 17;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 1;   
        gbc.gridy = 1;   
        gbc.anchor = 17;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 1.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 2;   
        gbc.gridwidth = 2;   
        gbc.anchor = 10;   
        gbc.fill = 0;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 0;   
        gbc.gridwidth = 2;   
        gbc.anchor = 10;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 1.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 1;   
        gbc.anchor = 13;   
        gbc.fill = 0;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 1;   
        gbc.gridy = 1;   
        gbc.anchor = 17;   
        gbc.fill = 0;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 0;   
        gbc.anchor = 10;   
        gbc.fill = 1;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 1.0D;   
        gbc = new GridBagConstraints();   
        gbc.gridx = 0;   
        gbc.gridy = 1;   
        gbc.anchor = 10;   
        gbc.fill = 0;   
        gbc.weightx = 1.0D;   
        gbc.weighty = 0.0D;   
        gbc.insets = new Insets(3, 3, 3, 3);   
        
    }   
   
    public Image scaleSnap(Image image)   
    {   
        Image image1 = createImage(snapwidth, 40);   
        Graphics g = image1.getGraphics();   
        g.drawImage(image, 0, 0, snapwidth, 40, Color.white, null);   
        return image1;   
    }   
   
    public void clearSnaps()   
    {   
        currentSnap = -1;   
        draw(1);   
    }   
   
    public void clearFrames()   
    {   
        frames = new Vector();   
        draw(8);   
    }   
   
    protected String getFrameString()   
    {   
        String s = new String();   
        for(int i = 0; i < frames.size(); i++)   
        {   
            FaceFrame faceframe = (FaceFrame)frames.elementAt(i);   
            s += faceframe.toString();   
        }   
   
        return s.substring(0, s.length() - 1);   
    }   
   
    public void draw(int i)   
    {   
    }   
   
    public void paint(Graphics g)   
    {   
        draw(11);   
    }   
   
    public synchronized void reshape(int i, int j, int k, int l)   
    {   
        super.reshape(i, j, k, l);   
        timeArrow = paletteArrow = null;   
        snapControlPan.reshape(0, 0, 100, 32);   
    }   
   
    private Image scaleImage(Image image)   
    {   
        int i = image.getWidth(this);   
        int j = image.getHeight(this);   
        int k = snapwidth;   
        int l = 40;   
        int ai[] = new int[i * j];   
        int ai1[] = new int[k * l];   
        double d = (double)i / (double)k;   
        double d1 = (double)j / (double)l;   
        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, i, j, ai, 0, i);   
        try   
        {   
            pixelgrabber.grabPixels();   
        }   
        catch(InterruptedException _ex)   
        {   
            System.out.println("interrupted waiting for pixels!");   
            return null;   
        }   
        if((pixelgrabber.status() & 0x80) != 0)   
        {   
            System.out.println("image fetch aborted or errored");   
            return null;   
        }   
        for(int i1 = 0; i1 < l; i1++)   
        {   
            for(int j1 = 0; j1 < k; j1++)   
            {   
                double d2 = 0.0D;   
                double d3 = 0.0D;   
                double d4 = 0.0D;   
                for(int k1 = (int)Math.floor((double)i1 * d1); k1 < (int)Math.ceil((double)(i1 + 1) * d1); k1++)   
                {   
                    for(int l1 = (int)Math.floor((double)j1 * d); l1 < (int)Math.ceil((double)(j1 + 1) * d); l1++)   
                    {   
                        double d6 = Math.max(l1, (double)j1 * d);   
                        double d7 = Math.min(l1 + 1, (double)(j1 + 1) * d);   
                        double d8 = Math.max(k1, (double)i1 * d1);   
                        double d9 = Math.min(k1 + 1, (double)(i1 + 1) * d1);   
                        double d10 = (d7 - d6) * (d9 - d8);   
                        int i2 = k1 * i + l1;   
                        int j2 = (ai[i2] & 0xff0000) >> 16;   
                    int k2 = (ai[i2] & 0xff00) >> 8;   
            int l2 = ai[i2] & 0xff;   
            d2 += (double)j2 * d10;   
            d3 += (double)k2 * d10;   
            d4 += (double)l2 * d10;   
                    }   
   
                }   
   
                double d5 = d * d1;   
                ai1[i1 * k + j1] = 0xff000000 | (int)Math.round(d2 / d5) << 16 | (int)Math.round(d3 / d5) << 8 | (int)Math.round(d4 / d5);   
            }   
   
        }   
   
        Image image1 = createImage(new MemoryImageSource(k, l, ai1, 0, k));   
        System.out.println(image1 + "  " + image1.getWidth(this) + "," + image1.getHeight(this));   
        return image1;   
    }   
   
    public Point getLocationGlobal(Component component)   
    {   
        Point point = component.location();   
        for(Container container = component.getParent(); container != this; container = container.getParent())   
        {   
            Point point1 = container.location();   
            point.x += point1.x;   
            point.y += point1.y;   
        }   
   
        return point;   
    }   
   
    public int[] getLocalCoords(Component component, int i, int j)   
    {   
        Point point = getLocationGlobal(component);   
        int ai[] = new int[2];   
        ai[0] = i - point.x;   
        ai[1] = j - point.y;   
        return ai;   
    }   
   
    public int[] toSnapGrid(int i)   
    {   
        int ai[] = new int[2];   
        ai[0] = (i % snapcols) * snapwidth;   
        ai[1] = (i / snapcols) * 40;   
        return ai;   
    }   
   
    public boolean containsGlobal(Component component, int i, int j)   
    {   
        Point point = getLocationGlobal(component);   
        Dimension dimension = component.size();   
        return i >= point.x && i < point.x + dimension.width && j >= point.y && j < point.y + dimension.height;   
    }   
   
    protected void insertInOrder(Vector vector, FaceFrame faceframe)   
    {   
        int i;   
        for(i = 0; i < vector.size(); i++)   
            if(faceframe.getTime() <= ((FaceFrame)vector.elementAt(i)).getTime())   
                break;   
   
        vector.insertElementAt(faceframe, i);   
    }   
}  
