package nars.timeline;

import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Item;
import nars.gui.NARSwing;
import nars.gui.NWindow;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.util.InferenceTrace;
import nars.util.InferenceTrace.InferenceEvent;
import nars.util.InferenceTrace.OutputEvent;
import nars.util.InferenceTrace.TaskEvent;
import org.jbox2d.common.MathUtils;
import processing.core.PApplet;

/** Timeline view of an inference trace.  Focuses on a specific window and certain features which
 can be adjusted dynamically. Can either analyze a trace while a NAR runs, or after it finishes. */
public class Timeline3DCanvas extends PApplet {

    float frameRate = 20f;
    private final int initialWidth;
    private final int initialHeight;

    long cycleStart = 0;
    long cycleEnd = 250;
    
    float camX = 0f;
    float camY = 0f;
    float camZ = -100f;
    float camScale = 8f;
    float rotX = 0;
    float rotY = 0;
    float rotZ = 0;
    float rotSpeed = 0.1f/frameRate;
    private float lastMousePressY = Float.NaN;
    private float lastMousePressX = Float.NaN;

    private final InferenceTrace trace;

    //private UndirectedGraph<EventPoint,Integer> influence = null;
    //private int nextEdgeID;
    
    //stores the previous "representative event" for an object as the visualization is updated each time step
    public Map<Object,EventPoint> lastSubjectEvent = new HashMap();
    
    //all events mapped to their visualized feature
    public Map<Object,EventPoint> events = new HashMap();
    
    private boolean updated = false;

    public static class EventPoint<X> {
        public float x, y, z;
        public X value;
        public List<EventPoint<X>> incoming = new ArrayList<>();

        public EventPoint(X value, float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
        }

        private void set(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
    }
    
    
    public static void main(String[] args) {
        NAR nar = new DefaultNARBuilder().build();
        InferenceTrace it = new InferenceTrace(nar);
        nar.addInput("<a --> b>.");
        nar.addInput("<b --> c>.");
        nar.addInput("<(^pick,x) =\\> a>.");
        nar.addInput("<(*, b, c) <-> x>.");
        nar.finish(250);
        
        
        
        NWindow n = new NWindow("Timeline Test", new Timeline3DCanvas(it, 1000, 800));
        n.pack();
        n.setVisible(true);        
        n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public Timeline3DCanvas(InferenceTrace trace, int w, int h) {
        super();
        this.trace = trace;
        this.initialWidth = w;
        this.initialHeight = h;
        init();
  
    }

    public void setup() {
        size(initialWidth, initialHeight, P3D);
        
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
                float dx = (mouseX - lastMousePressX);
                float dy = (mouseY - lastMousePressY);
                
                if (mouseButton == 37) {
                    //left mouse button
                    camX -= dx;
                    camY -= dy;
                }
                else if (mouseButton == 39) {
                    //right mouse button
                    rotY -= dx * rotSpeed;
                    rotX -= dy * rotSpeed;
                }
                else if (mouseButton == 3) {
                    //middle mouse button (wheel)
                    rotZ -= dx * rotSpeed;
                }
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
        
        rotateX(PI+rotX);
        rotateY(PI+rotY);
        rotateZ(-PI+rotZ);
    }
    
    
    public void draw() {            
        
        lights();
        background(0);
        
        updateCamera();


        if (!updated) {
            lastSubjectEvent.clear();
            events.clear();            
        }
        
        noStroke();
        
        TreeMap<Long, List<InferenceEvent>> time = trace.time;
        for (Map.Entry<Long, List<InferenceEvent>> e : time.subMap(cycleStart, cycleEnd).entrySet()) {
            long t = e.getKey();
            List<InferenceEvent> v = e.getValue();
            
            plot(t, v);
        }

        stroke(190f, 120f);
        strokeWeight(0.4f);
        for (EventPoint<Object> to : events.values()) {
            for (EventPoint<Object> from : to.incoming) {
                line(from.x, from.y, from.z, to.x, to.y, to.z);    
            }                
        }
        
        if (!updated) {
            
            /*
            for (Map.Entry<Object, EventPoint> e : lastSubjectEvent.entrySet()) {
                System.out.println(e);
            }
            System.out.println("Edges: "+ influence.edgeSet().size() + ", Vertices: " + influence.vertexSet().size());
            */
            
            
            
            updated = true;
        }
        
        
    }

    
    private void plot(long t, List<InferenceEvent> v) {
        
        float timeScale = 4f;
        
        float itemScale = timeScale*0.95f;
        
        float yScale = itemScale;
        
        float x = t * timeScale, y = 0;
        
        for (InferenceEvent i : v) {            
            
            
            
            //box(2);
            //quad(-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0, -0.5f, 0.5f, 0);
            
            if (i instanceof TaskEvent) {
                TaskEvent te = (TaskEvent)i;
                float p = te.priority;                
                
                {
                    fill(256f * NARSwing.hashFloat(i.getClass().hashCode()), 200f, 200f);
                    float z = p*10f;
                    
                    switch (te.type) {
                        case Added:
                            //forward
                            triangleHorizontal(i, te.task, p*itemScale, x, y, z, 1.0f);
                            break;
                        case Removed:
                            //backwards
                            triangleHorizontal(i, te.task, p*itemScale, x, y, z, -1.0f);
                            break;
                                
                    }
                    
                }                
            }
            else if (i instanceof OutputEvent) {
                OutputEvent te = (OutputEvent)i;
                
                float p = 0.5f;
                if (te.signal instanceof Item) {
                    p = ((Item)te.signal).getPriority();
                }
                float ph = 0.5f + 0.5f * p; //so that priority 0 will still be visible

                fill(256f * NARSwing.hashFloat(te.channel.hashCode()), 100f + 100f * ph, 255f * ph);

                
                if (te.channel.equals(IN.class)) {
                    pushMatrix();
                    translate(x, y, 0);
                    rotateZ(0.65f); //angled diagonally down and to the right                    
                    triangleHorizontal(i, te.signal, ph*itemScale, 0, 0, 0, 1.0f);                    
                    popMatrix();
                }
                else if (te.channel.equals(OUT.class)) {
                    //TODO use faster triangleVertical function instead of push and rotate
                    pushMatrix();
                    translate(x, y, 0);
                    rotateZ(MathUtils.HALF_PI); //angled diagonally down and to the right                   
                    triangleHorizontal(i, te.signal, ph*itemScale, 0, 0, 0, 1.0f);                    
                    popMatrix();
                }
                /*else if exe... {
                    
                }*/
                else {                    
                    rect(i, te.signal, ph*itemScale, x, y);
                }
            }
            else {
                fill(256f * NARSwing.hashFloat(i.getClass().hashCode()), 200f, 200f);
                rect(i, null, 0.75f * itemScale, x, y);
            }
            
            y += yScale;
        }
        
            
    }

    protected void rect(Object event, Object subject, float r, float x, float y/*, float z*/) {
        rect(
                x + -r/2f,  y + -r/2f, 
                r/2f,   r/2f
        );
        if (!updated) {
            setEventPoint(event, subject, x, y, 0);
        }
    }
    
    protected void triangleHorizontal(Object event, Object subject, float r, float x, float y, float z, float direction) {
        translate(0,0,z);
        triangle(
                x + direction * -r/2,   y + direction * -r/2, 
                x + direction * r/2,    y + 0, 
                x + direction * -r/2,   y + direction * r/2
        );
        translate(0,0,-z); //should be cheaper than a push/pop
        
        if (!updated) {
            setEventPoint(event, subject, x, y, z);
        }
    }
    
    protected void setEventPoint(Object event, Object subject, float x, float y, float z) {
        EventPoint f = new EventPoint(event, x, y, z);        
        events.put(event, f);
        
        if (subject!=null) {
            EventPoint e = lastSubjectEvent.put(subject, f);
            if (e != null) {
                f.incoming.add(e);
            }
        }
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
