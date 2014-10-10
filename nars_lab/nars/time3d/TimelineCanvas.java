package nars.time3d;

import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.util.InferenceTrace;
import nars.util.InferenceTrace.InferenceEvent;
import nars.util.InferenceTrace.TaskEvent;
import processing.core.PApplet;

/** Timeline view of an inference trace.  Focuses on a specific window and certain features which
 can be adjusted dynamically. Can either analyze a trace while a NAR runs, or after it finishes. */
public class TimelineCanvas extends PApplet {

    float frameRate = 20f;
    private final int initialWidth;
    private final int initialHeight;

    long cycleStart = 0;
    long cycleEnd = 100;
    
    float camX = 0f;
    float camY = 0f;
    float camZ = -100f;
    float camScale = 8f;
    private float lastMousePressY = Float.NaN;
    private float lastMousePressX = Float.NaN;
    
    public static void main(String[] args) {
        NAR nar = new DefaultNARBuilder().build();
        InferenceTrace it = new InferenceTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<c --> a>.");
        nar.finish(64);
        
        
        
        NWindow n = new NWindow("Timeline Test", new TimelineCanvas(it, 1000, 800));
        n.pack();
        n.setVisible(true);        
        n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    private final InferenceTrace trace;
    
    public TimelineCanvas(InferenceTrace trace, int w, int h) {
        super();
        this.trace = trace;
        this.initialWidth = w;
        this.initialHeight = h;
        init();
  
    }

    public void setup() {
        size(initialWidth, initialHeight, P3D);
        noStroke();
        frameRate(frameRate);
        colorMode(HSB);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        int wr = e.getWheelRotation();
        camZ += wr * 20f;   
    }

    
    protected void updateCamera() {
        
        //scale limits
        if (camScale > 10f) camScale = 10f;
        if (camScale < 0.5f) camScale = 0.5f;

        
        if (mouseButton > 0) {
            if (Float.isFinite(lastMousePressX)) {
                camX -= (mouseX - lastMousePressX);
                camY -= (mouseY - lastMousePressY);
            }
            
            lastMousePressX = mouseX;
            lastMousePressY = mouseY;
        }
        else {
            lastMousePressX = Float.NaN;
        }
        
        float cameraY = height / 2.0f;
        float fov = /*mouseX / (float)(width) **/ PI / 2;
        float cameraZ = cameraY / tan(fov / 2.0f);
        float aspect = (float)(width)/height;

        perspective(fov, aspect, cameraZ / 10.0f, cameraZ * 10.0f);
        
        camera(camX, camY, (height/2) / tan(PI/6), camX, camY, 0, 0, 1, 0);

        translate(width/2, height/2, camZ);
        scale(camScale);
        
        rotateX(PI);
    }
    
    public void draw() {
        lights();
        background(0);
        
        updateCamera();


        
        TreeMap<Long, List<InferenceEvent>> time = trace.time;
        for (Map.Entry<Long, List<InferenceEvent>> e : time.subMap(cycleStart, cycleEnd).entrySet()) {
            long t = e.getKey();
            List<InferenceEvent> v = e.getValue();
            
            plot(t, v);
        }
        
        
        
    }

    private void plot(long t, List<InferenceEvent> v) {
        
        float timeScale = 4f;
        float yScale = 4f;

        pushMatrix();
        translate(t * timeScale, 0);
        
        for (InferenceEvent i : v) {
            translate(0, yScale);
            
            
            //box(2);
            //quad(-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0, -0.5f, 0.5f, 0);
            
            if (i instanceof InferenceTrace.TaskEvent) {
                TaskEvent te = (TaskEvent)i;
                float p = te.priority;
                float r = 4f + p;
                
                pushMatrix();
                {
                    fill(256f * NARSwing.hashFloat(i.getClass().hashCode()), 200f, 200f);
                    translate(0,0,p);
                    
                    switch (te.type) {
                        case Added:
                            //forward
                            triangle(-r/2, -r/2, r/2, 0, -r/2, r/2);
                            break;
                        case Removed:
                            //backwards
                            triangle(r/2, -r/2, -r/2, 0, r/2, r/2);
                            break;
                                
                    }
                    
                }                
                popMatrix();
            }
            else {
                float r = 4f;
                fill(256f * NARSwing.hashFloat(i.getClass().hashCode()), 200f, 200f);
                rect(-r/2f, -r/2f, r/2f, r/2f);
            }
        }
        popMatrix();
            
    }
    
    
    /*
    
    
import picking.*;

Picker picker;
float a = 0.0;

void setup() {
  size(200, 150, P3D);
  picker = new Picker(this);
}

void draw() {
  a += 0.01;

  background(255);

  picker.start(0);
  drawBox(80, 75, 50, #ff8800);

  picker.start(1);
  drawBox(140, 75, 20, #eeee00);

  picker.stop();

  color c = 0;
  int id = picker.get(mouseX, mouseY);
  switch (id) {
    case 0:
      c = #ff8800;
      break;
    case 1:
      c = #eeee00;
      break;
  }
  drawBorder(10, c);
}

void drawBox(int x, int y, int w, color c) {
  stroke(0);
  fill(c);
  pushMatrix();
    translate(x, y);
    rotateX(a); rotateY(a);
    box(w);
  popMatrix();
}

void drawBorder(int w, color c) {
  noStroke();
  fill(c);
  rect(0,   0, width, w);
  rect(0, height - w, width, w);
  rect(0,   0, w, height);
  rect(width - w, 0, w, height);
}
    */


}
