package nars.gui.output.timeline;

import com.google.common.collect.Lists;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import processing.core.PApplet;
import processing.event.KeyEvent;

/**
 * Timeline view of an inference trace. Focuses on a specific window and certain
 * features which can be adjusted dynamically. Can either analyze a trace while
 * a NAR runs, or after it finishes.
 */
public class Timeline2DCanvas extends PApplet {

    float camScale = 1f;
    float scaleSpeed = 4f;
    private float lastMousePressY = Float.NaN;
    private float lastMousePressX = Float.NaN;

    final int defaultFrameRate = 25;
    
    boolean updating = true;

    float minLabelScale = 10f;
    float minYScale = 0.5f;
    float minTimeScale = 0.5f;
    float maxYScale = 1000f;
    float maxTimeScale = 1000f;
    float drawnTextScale = 0;

    //display options to extract to a parameter class ----------------------------------------
    boolean showItemLabels = true;
    float textScale = 0.1f;
    long cycleStart = 0;
    long cycleEnd = 45;

    public static class Camera {
        public float camX = 0f;
        public float camY = 0f;
        public float timeScale = 32f;
        public float yScale = 32f;   
        public long lastUpdate = 0;
    }
    
    public final Camera camera;
    
    long lastUpdate = System.nanoTime();
    long lastCameraUpdate = 0;

    public final List<Chart> charts;
    //display options ----------------------------------------

    public Timeline2DCanvas(List<Chart> charts) {
        this(new Camera(), charts);
    }
    
    public Timeline2DCanvas(Camera camera, List<Chart> charts) {
        super();
        this.camera = camera;
        this.charts = charts;
        init();
    }
    
    public Timeline2DCanvas(Chart... charts) {        
        this(Lists.newArrayList(charts));
    }
    
    public Timeline2DCanvas(Camera camera, Chart... charts) {        
        this(camera, Lists.newArrayList(charts));
    }
    
    public void view(long start, long end) {

        //TODO calculate timeScale and camX to view the cycle range
        
    }
    

    @Override
    public void setup() {
        colorMode(HSB);
        frameRate(defaultFrameRate);        
    }

    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        if ((newWidth <= 0)  || (newHeight <= 0))
            return;
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override public void run() {
                
                Timeline2DCanvas.super.resizeRenderer(newWidth, newHeight);
                updateNext();
            }
            
        });
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        /*int wr = e.getWheelRotation();
         camScale += wr * dScale;
         if (wr != 0)
         updateNext();*/
    }

    @Override
    public void keyPressed(KeyEvent event) {
        super.keyPressed(event);
        if (event.getKey() == 'l') {
            showItemLabels = !showItemLabels;
            updateNext();
        }

    }

    @Override
    public void mouseMoved() {
        updateMouse();
    }

    
    @Override
    public void mouseReleased() {
        updateMouse();
    }
    
    
    

    protected void updateMouse() {
        
        boolean changed = false;

        
        //scale limits
        if (mouseButton > 0) {
            long now = System.nanoTime();
            
            if (Float.isFinite(lastMousePressX)) {
                float dt = (now - lastUpdate)/1e9f;
         
                
                //float dx = (mouseX - lastMousePressX);
                //float dy = (mouseY - lastMousePressY);
                float dx = mouseX - pmouseX;
                float dy = mouseY - pmouseY;
                
                if (mouseButton == 37) {
                    //left mouse button
                    if ((dx != 0) || (dy != 0)) {
                        camera.camX -= dx;
                        camera.camY -= dy;
                        changed = true;
                    }
                } else if (mouseButton == 39) {
                    //right mouse button
                    
                    
                    float sx = dx * scaleSpeed * dt;
                    float sy = dy * scaleSpeed * dt;         
                    
                    camera.camX += sx / camera.timeScale;
                    camera.camY += sy / camera.yScale;
                    
                    camera.timeScale += sx;
                    camera.yScale += sy;

                    changed = true;
                    //System.out.println(camX +  " " + camY + " " + sx + " "  + sy);
                }
                else {
                    lastMousePressX = Float.NaN;
                }
//                else if (mouseButton == 3) {
//                    //middle mouse button (wheel)
//                    rotZ -= dx * rotSpeed;
//                }
            }

            lastMousePressX = mouseX;
            lastMousePressY = mouseY;

            lastUpdate = now;
            
        } else {
            lastMousePressX = Float.NaN;
        }
        
               
        if (changed) {            
            camera.lastUpdate = System.currentTimeMillis();
            updateNext();
        }

    }

    protected void updateCamera() {
            
        if (camera.yScale < minYScale) camera.yScale = minYScale;
        if (camera.yScale > maxYScale) camera.yScale = maxYScale;
        if (camera.timeScale < minTimeScale)  camera.timeScale = minTimeScale;
        if (camera.timeScale > maxTimeScale) camera.timeScale = maxTimeScale;

        translate(-camera.camX + width / 2, -camera.camY + height / 2);

        cycleStart = (int) (Math.floor((camera.camX - width / 2) / camera.timeScale) - 1);
        cycleStart = Math.max(0, cycleStart);
        cycleEnd = (int) (Math.ceil((camera.camX + width / 2) / camera.timeScale) + 1);

        if (cycleEnd < cycleStart) cycleEnd = cycleStart;

        drawnTextScale = Math.min(camera.yScale, camera.timeScale) * textScale;
        
        if (camera.lastUpdate > lastCameraUpdate) {
            updating = true;
        }

        
        
    }
    
    public void updateNext() {
        if (!updating) {
            for (Chart c : charts) {
                float h = c.height * camera.yScale;
                c.update(this, camera.timeScale, camera.yScale);
            }            
        }
        updating = true;        
    }

    @Override
    public void draw() {

        updateMouse();
        
        if (!isDisplayable() || !isVisible())
            return;
            
        updateCamera();

        if (!updating) {
            return;
        }

        updating = false;
        lastCameraUpdate = camera.lastUpdate;

        background(0);

        float y = 0;
        float yMargin = camera.yScale * 0.1f;
        for (Chart c : charts) {
            float h = c.height * camera.yScale;
            try {
                c.draw(this, y, camera.timeScale, camera.yScale);
            }
            catch (Exception e) { 
                System.out.println("Timeline draw: " + e);
            }
            y += (h + yMargin);
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
