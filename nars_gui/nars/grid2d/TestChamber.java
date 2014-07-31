package nars.grid2d;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.gui.Window;
import processing.core.PApplet;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.PROJECT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.SQUARE;
import static processing.core.PConstants.UP;


class TestChamber_applet extends PApplet  //(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
{

///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    ProcessingJs processingjs = new ProcessingJs();
//Hnav 2D navigation system   
    Hnav hnav = new Hnav();
//Object
    float selection_distance = 10;
    public float maxNodeSize = 40f;
    float FrameRate = 30f;

    boolean drawn = false;
    
    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();


    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    NAR nar;
    
    float sx = 800;
    float sy = 800;


    long lasttime = -1;

    
    
    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        super.resizeRenderer(newWidth, newHeight); //To change body of generated methods, choose Tools | Templates.
        drawn = false;
    }
    
    public void mouseScrolled() {
        hamlib.mouseScrolled();
    }

    public void keyPressed() {
        hamlib.keyPressed();
    }

    public void mouseMoved() {
        hamlib.mouseMoved();
    }

    public void mouseReleased() {
        hamlib.mouseReleased();
    }

    public void mouseDragged() {
        hamlib.mouseDragged();
    }

    public void mousePressed() {
        hamlib.mousePressed();
    }

        
    
    Hauto A=new Hauto(10);
    public void automataclicked(float x,float y)
    {
        if(x<0 || y<0)
            return;
        float realx=x/rendersize;
        float realy=y/rendersize;
        if(realx>=A.n || realy>=A.n)
            return;
        A.readCells[(int)realx][(int)realy].state=1;
        A.writeCells[(int)realx][(int)realy].state=1;
    }
    
    @Override
    public void draw() {
      
        background(0,0,0, 0.001f);

        
        //pushMatrix();
        hnav.Transform();
        hrend_DrawBegin();
        drawit();
        hrend_DrawEnd();
        //popMatrix();
        
        hrend_DrawGUI();
        
        
    }


    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();
        /*if (lastclicked != null) {
            fill(255, 0, 0);
            ellipse(lastclicked.x, lastclicked.y, 10, 10);
        }*/
    }

    public void hrend_DrawGUI() {
    }

    @Override
    public void setup() {  
        frameRate(FrameRate);
        
    }


    int calcevery=100;
    int calci=0;
    float rendersize=100;
    public void drawit() {
        calci++;
        if(calci%100==0) {
           A.Exec();
        }
        //for speed:
        strokeCap(SQUARE);
        strokeJoin(PROJECT);
        fill(255);
        
        //Hauto h=A;
        
        for(int i=0;i<A.n;i++)
        {
            for(int j=0;j<A.n;j++)
            {
                fill(255);
                //rect(i,j,1,1);
                if(i==0 || i==A.n-1 || j==0 || j==A.n-1)
                {
                    fill(0,0,255);
                    rect(i*rendersize,j*rendersize,rendersize,rendersize);
                }
                else
                if(A.readCells[i][j].state==0)
                {
                    fill(255);
                    rect(i*100,j*rendersize,rendersize,rendersize);
                }
                else
                {
                    fill(128);
                    rect(i*rendersize,j*rendersize,rendersize,rendersize);
                }
            }
        }
    }


    class ProcessingJs {

        ProcessingJs() {
            addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                    mouseScroll = -evt.getWheelRotation();
                    mouseScrolled();
                }
            }
            );
        }
    }

    class Hnav {

        private float savepx = 0;
        private float savepy = 0;
        private int selID = 0;
        private float zoom = 1.0f;
        private float difx = 0;
        private float dify = 0;
        private int lastscr = 0;
        private boolean EnableZooming = true;
        private float scrollcamspeed = 1.1f;

        float MouseToWorldCoordX(int x) {
            return 1 / zoom * (x - difx - width / 2);
        }

        float MouseToWorldCoordY(int y) {
            return 1 / zoom * (y - dify - height / 2);
        }
        private boolean md = false;

        void mousePressed() {
            md = true;
            if (mouseButton == RIGHT) {
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }

        void mouseReleased() {
            md = false;
        }

        void mouseDragged() {
            if (mouseButton == RIGHT) {
                difx += (mouseX - savepx);
                dify += (mouseY - savepy);
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }
        private float camspeed = 20.0f;
        private float scrollcammult = 0.92f;
        boolean keyToo = true;

        void keyPressed() {
            if ((keyToo && key == 'w') || keyCode == UP) {
                dify += (camspeed);
            }
            if ((keyToo && key == 's') || keyCode == DOWN) {
                dify += (-camspeed);
            }
            if ((keyToo && key == 'a') || keyCode == LEFT) {
                difx += (camspeed);
            }
            if ((keyToo && key == 'd') || keyCode == RIGHT) {
                difx += (-camspeed);
            }
            if (!EnableZooming) {
                return;
            }
            if (key == '-' || key == '#') {
                float zoomBefore = zoom;
                zoom *= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            if (key == '+') {
                float zoomBefore = zoom;
                zoom /= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            drawn = false;
        }

        void Init() {
            difx = -width / 2;
            dify = -height / 2;
        }

        void mouseScrolled() {
            if (!EnableZooming) {
                return;
            }
            float zoomBefore = zoom;
            if (mouseScroll > 0) {
                zoom *= scrollcamspeed;
            } else {
                zoom /= scrollcamspeed;
            }
            difx = (difx) * (zoom / zoomBefore);
            dify = (dify) * (zoom / zoomBefore);
            drawn = false;
        }

        void Transform() {
            translate(difx + 0.5f * width, dify + 0.5f * height);
            scale(zoom, zoom);
        }
    }

    
////Object management - dragging etc.

    class Hsim {

        ArrayList obj = new ArrayList();

        void Init() {
            smooth();
        }

        void mousePressed() {
            if (mouseButton == LEFT) {
                checkSelect();
                float x=hnav.MouseToWorldCoordX(mouseX);
                float y=hnav.MouseToWorldCoordY(mouseY);
                automataclicked(x,y);
            }
        }
        boolean dragged = false;

        void mouseDragged() {
            if (mouseButton == LEFT) {
                dragged = true;
                dragElems();
            }
        }

        void mouseReleased() {
            dragged = false;
            //selected = null;
        }

        void dragElems() {
            /*
            if (dragged && selected != null) {
                selected.x = hnav.MouseToWorldCoordX(mouseX);
                selected.y = hnav.MouseToWorldCoordY(mouseY);
                hsim_ElemDragged(selected);
            }
                    */
        }
        

        void checkSelect() {
            /*
            double selection_distanceSq = selection_distance*selection_distance;
            if (selected == null) {
                for (int i = 0; i < obj.size(); i++) {
                    Vertex oi = (Vertex) obj.get(i);
                    float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
                    float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
                    float distanceSq = (dx * dx + dy * dy);
                    if (distanceSq < (selection_distanceSq)) {
                        selected = oi;
                        hsim_ElemClicked(oi);
                        return;
                    }
                }
            }
                    */
        }
    }

//Hamlib handlers
    class Hamlib {

        void Init() {
            noStroke();
            hnav.Init();
            hsim.Init();
        }

        void mousePressed() {
            hnav.mousePressed();
            hsim.mousePressed();
        }

        void mouseDragged() {
            hnav.mouseDragged();
            hsim.mouseDragged();
        }

        void mouseReleased() {
            hnav.mouseReleased();
            hsim.mouseReleased();
        }

        public void mouseMoved() {
        }

        void keyPressed() {
            hnav.keyPressed();
        }

        void mouseScrolled() {
            hnav.mouseScrolled();
        }

        void Camera() {

        }


    }
}

public class TestChamber extends Window {

    TestChamber_applet app = null;

    public TestChamber(NAR n) {
        super("TestChamber");


        app = new TestChamber_applet();
        app.init();
        app.nar = n;
        
        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        /*
        final JCheckBox syntaxEnable = new JCheckBox("Syntax");
        syntaxEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                app.showSyntax = (syntaxEnable.isSelected());        
                app.setUpdateNext();
            }
        });
        menu.add(syntaxEnable);        
        */
          
        
        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
        

    
    }

    @Override
    protected void close() {
        app.stop();
        app.destroy();
        getContentPane().removeAll();
        app = null;
    }
    
    public static void main(String[] arg) {
        NAR n = new NAR();
        new TestChamber(n);
    }
}