package automenta.vivisect.face;

import automenta.vivisect.swing.NPanel;
import automenta.vivisect.swing.NWindow;
import java.awt.BorderLayout;
import java.awt.Graphics;


   
public class HumanoidFacePanel extends NPanel {   
    private static final long serialVersionUID = 1L;   
    GraphApp g;   
    protected final FaceGUI face;

    double nextSpin = 0;
    double nextNod = 0;
    double momentum = 0.95;
    
    public boolean nod = false;
    public boolean shake = false;
    public boolean busy = false;
    public boolean happy = false;
    public int talk=-1;

    
    public HumanoidFacePanel()   {   
        super(new BorderLayout());   
        setSize(800,600);
        
        face = new FaceGUI() {           
            
            long start = System.currentTimeMillis();
            int cycle = 0;
            @Override
            public void render(Graphics g) {
                if (cycle++ > 0)  {
                    long now = System.currentTimeMillis();

                    HumanoidFacePanel.this.update(((double)now - start) / 1000.0);

                    spin = (spin * momentum) + (nextSpin * (1.0 - momentum));

                }
                super.render(g);                
            }
            
        };
        g = new GraphApp(face, "", "", true);
        
        add(face, BorderLayout.CENTER);
        
        addKeyListener(face);
        addMouseListener(face);
        addMouseMotionListener(face);
        
        face.start();
        
    }   
    
    public void update(double t) {
        
        
        if(talk==0 || talk==-1) {
            face.setFlex('m');
            talk=-1;
        }
        if(talk!=-1) {
            talk--;
            face.setFlex('o');
        }
        
        face.setFlex('_'); //neutral brows
        face.setFlex('a');
        
        if (nod && shake) {
            //confused
            face.setFlex('\'');
            face.setFlex('~');
            face.setFlex('P');
            nextSpin = 0;
        }
        else if (shake) {
            nextSpin = Math.sin(t*4f)*6f;
        }
        else if (nod) {
            if (Math.sin(t*6f) < 0)
                face.setFlex('v');
            else
                face.setFlex('^');
        }
        else {
            face.setFlex('~');
            nextSpin = 0;
        }
        
        if (busy) {
            face.setFlex('z');
            face.setFlex('`');
            face.setFlex('b');
            face.setFlex('u');
        }
        else {
            face.setFlex('x');
            face.setFlex('t');
            face.setFlex(':');
            face.setFlex('_');
            face.setFlex('c');
        }
        
        if (happy) {
            face.setFlex('S');
        }
        else {
            face.setFlex('n');
        }
    }
    
    @Override
    protected void onShowing(boolean showing) {
        
        if (showing) {
            face.start();
        }
        else {
            face.stop();
        }
    }
    
    
   
   
    
    public static void main(String[] arg) {
        HumanoidFacePanel f = new HumanoidFacePanel();
        NWindow w = new NWindow("Face", f);
        w.setSize(250,400);
        w.setVisible(true);
    }

}  