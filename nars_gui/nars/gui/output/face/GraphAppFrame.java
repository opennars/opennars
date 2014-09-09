package nars.gui.output.face;

import javax.swing.JFrame;

/**
 *
 * @author me
 */


   
public class GraphAppFrame extends JFrame   
{   
    private static final long serialVersionUID = 1L;   
    GraphApp g;   
   
    public GraphAppFrame(String s, FaceGUI face, String s1, String s2)   
    {   
        this(s, face, s1, s2, false);   
    }   
   
    public GraphAppFrame(String s, FaceGUI face2bapplet, String s1, String s2, boolean flag)   
    {   
        super(s);   
        setSize(800,600);
        
        
        g = new GraphApp(face2bapplet, s1, s2, flag);   
        //getContentPane().add(g);
        
        getContentPane().add(face2bapplet);
        
        addKeyListener(face2bapplet);
        addMouseListener(face2bapplet);
        addMouseMotionListener(face2bapplet);
        
        face2bapplet.start();
        
        
        setVisible(true);
        

    }   
   
    public GraphApp getGraphanim()   
    {   
        return g;   
    }   
   
    
    public static void main(String[] arg) {
        GraphAppFrame f = new GraphAppFrame("x", new FaceGUI(), "", "", true);
    }
}  