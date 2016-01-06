//package automenta.vivisect.timeline;
//
//import automenta.vivisect.Vis;
//import automenta.vivisect.swing.PCanvas;
//import com.google.common.collect.Lists;
//import nars.io.meter.SignalData;
//import processing.core.PGraphics;
//import processing.core.PGraphicsJava2D;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.Collection;
//import java.util.List;
//
//import static processing.core.PConstants.HSB;
//
///**
// * ConceptComet view of an logic trace. Focuses on a specific window and certain
// * features which can be adjusted dynamically. Can either analyze a trace while
// * a NAR runs, or after it finishes.
// */
//public class TimelineVis implements Vis {
//
//    float camScale = 1f;
//    float scaleSpeed = 4f;
//    private float lastMousePressY = Float.NaN;
//    private float lastMousePressX = Float.NaN;
//
//    final int defaultFrameRate = 25;
//
//    boolean updating = true;
//
//    float minLabelScale = 10f;
//    float minYScale = 0.5f;
//    float minTimeScale = 0.5f;
//    float maxYScale = 1000f;
//    float maxTimeScale = 1000f;
//    float drawnTextScale = 0;
//
//    //display options to extract to a parameter class ----------------------------------------
//    boolean showItemLabels = true;
//    float textScale = 0.1f;
//
//
//    public PGraphics g;
//    public Graphics2D g2; //for direct Swing control
//    private float chartMargin = 1;
//
//    final SignalData timeAxis;
//
//    public Object[] getTimeAxis() {
//        Object[] r = timeAxis.getDataCached();
//        return r;
//    }
//
//    public static class Camera {
//        public float camX = 0f;
//        public float camY = 0f;
//        public float xScale = 100f;
//        public float yScale = 100f;
//        public long lastUpdate = 0;
//    }
//
//    public final Camera camera;
//
//    long lastUpdate = System.nanoTime();
//    long lastCameraUpdate = 0;
//
//    public final List<AxisPlot> charts;
//    //display options ----------------------------------------
//
//    public TimelineVis(SignalData timeAxis, List<AxisPlot> charts) {
//        this(new Camera(), timeAxis, charts);
//    }
//
//    public TimelineVis(Camera camera, SignalData timeAxis, List<AxisPlot> charts) {
//        super();
//        this.camera = camera;
//        this.charts = charts;
//        this.timeAxis = timeAxis;
//
//    }
//
//    public TimelineVis(SignalData timeAxis, AxisPlot... charts) {
//        this(new Camera(), timeAxis, charts);
//    }
//
//    public TimelineVis(Camera camera, SignalData timeAxis, AxisPlot... charts) {
//        this(camera, timeAxis, Lists.newCopyOnWriteArrayList(Lists.newArrayList(charts)));
//    }
//
//    public void view(long start, long end) {
//
//        //TODO calculate timeScale and camX to view the cycle range
//
//    }
//
//    public void clearCharts() {
//        charts.clear();
//    }
//
//    public void setCharts(Collection<AxisPlot> newChartList) {
//        clearCharts();
//        charts.addAll(newChartList);
//        updateNext();
//    }
//
//    public void addChart(AxisPlot c) {
//        charts.add(c);
//    }
//
//
//
//
//    public void updateNext() {
//
//        timeAxis.getData();
//            for (AxisPlot c : charts) {
//                c.update(this);
//            }
//
//    }
//
//    @Override
//    public boolean render(PGraphics g) {
//
//        if (getTimeAxis() == null)
//            return true;
//
//        this.g = g;
//        this.g2 = ((PGraphicsJava2D)g).g2;
//
//        int originalColorMode = g.colorMode;
//        g.colorMode(HSB);
//
//        lastCameraUpdate = camera.lastUpdate;
//
//        //background(0);
//
//        g.pushMatrix();
//        g.translate(camera.camX, camera.camY);
//        g.scale(camera.xScale, camera.yScale);
//
//        g.pushMatrix();
//        float pos = chartMargin/2.0f;
//        for (AxisPlot c : charts) {
//            g.translate(0, pos);
//            c.render(this);
//            pos = c.plotHeight+chartMargin;
//        }
//        g.popMatrix();
//
//
//        g.colorMode(originalColorMode);
//
//        g.popMatrix();
//
//        return true;
//    }
//
//    public MouseAdapter newMouseDragPanScale(PCanvas p) {
//        final float scaleSpeed = 0.01f;
//
//        return new MouseAdapter() {
//            private Point startLocation;
//            //private int startYOffset;
//            float oy, ox;
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                if (SwingUtilities.isLeftMouseButton(e)) {
//                    e.consume();
//                    startLocation = e.getPoint();
//                    oy = camera.yScale;
//                    ox = camera.xScale;
//                }
//
//            }
//
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (startLocation == null) {
//                    return;
//                }
//
//                if (SwingUtilities.isLeftMouseButton(e)) {
//                    e.consume();
//                    Point currentLocation = e.getPoint();
//                    int deltaX = currentLocation.x - startLocation.x;
//                    int deltaY = currentLocation.y - startLocation.y;
//                    camera.yScale = oy * (1.0f + (deltaY * scaleSpeed));
//                    camera.xScale = ox * (1.0f + (deltaX * scaleSpeed));
//
//                    SwingUtilities.invokeLater(p::predraw);
//
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                startLocation = null;
//            }
//
//        };
//    }
//
//
//    public boolean isShowingItemLabels() {
//        return showItemLabels;
//    }
//
//    public float getMinLabelScale() {
//        return minLabelScale;
//    }
//
//
//
//    /*
//
//
//     import picking.*;
//
//     Picker picker;
//     float a = 0.0;
//
//     void setup() {
//     size(200, 150, P3D);
//     picker = new Picker(this);
//     }
//
//     void render() {
//     a += 0.01;
//
//     background(255);
//
//     picker.start(0);
//     drawBox(80, 75, 50, #ff8800);
//
//     picker.start(1);
//     drawBox(140, 75, 20, #eeee00);
//
//     picker.stop();
//
//     color c = 0;
//     int id = picker.get(mouseX, mouseY);
//     switch (id) {
//     case 0:
//     c = #ff8800;
//     break;
//     case 1:
//     c = #eeee00;
//     break;
//     }
//     drawBorder(10, c);
//     }
//
//     void drawBox(int x, int y, int w, color c) {
//     stroke(0);
//     fill(c);
//     pushMatrix();
//     translate(x, y);
//     rotateX(a); rotateY(a);
//     box(w);
//     popMatrix();
//     }
//
//     void drawBorder(int w, color c) {
//     noStroke();
//     fill(c);
//     rect(0,   0, width, w);
//     rect(0, height - w, width, w);
//     rect(0,   0, w, height);
//     rect(width - w, 0, w, height);
//     }
//     */
//
//    public float getDrawnTextScale() {
//        return drawnTextScale;
//    }
//
//
//}
//
////    @Override
////    public void mouseWheelMoved(MouseWheelEvent e) {
////        super.mouseWheelMoved(e);
////        /*int wr = e.getWheelRotation();
////         camScale += wr * dScale;
////         if (wr != 0)
////         updateNext();*/
////    }
////
////    @Override
////    public void keyPressed(KeyEvent event) {
////        super.keyPressed(event);
////        if (event.getKey() == 'l') {
////            showItemLabels = !showItemLabels;
////            updateNext();
////        }
////
////    }
//
////    @Override
////    public void mouseMoved() {
////        updateMouse();
////    }
////
////
////    @Override
////    public void mouseReleased() {
////        updateMouse();
////    }
//
//
//
////    protected void updateMouse() {
////
////        boolean changed = false;
////
////
////        //scale limits
////        if (mouseButton > 0) {
////            long now = System.nanoTime();
////
////            if (Float.isFinite(lastMousePressX)) {
////                float dt = (now - lastUpdate)/1e9f;
////
////
////                //float dx = (mouseX - lastMousePressX);
////                //float dy = (mouseY - lastMousePressY);
////                float dx = mouseX - pmouseX;
////                float dy = mouseY - pmouseY;
////
////                if (mouseButton == 37) {
////                    //left mouse button
////                    if ((dx != 0) || (dy != 0)) {
////                        camera.camX -= dx;
////                        camera.camY -= dy;
////                        changed = true;
////                    }
////                } else if (mouseButton == 39) {
////                    //right mouse button
////
////
////                    float sx = dx * scaleSpeed * dt;
////                    float sy = dy * scaleSpeed * dt;
////
////                    camera.camX += sx / camera.timeScale;
////                    camera.camY += sy / camera.yScale;
////
////                    camera.timeScale += sx;
////                    camera.yScale += sy;
////
////                    changed = true;
////                    //System.out.println(camX +  " " + camY + " " + sx + " "  + sy);
////                }
////                else {
////                    lastMousePressX = Float.NaN;
////                }
//////                else if (mouseButton == 3) {
//////                    //middle mouse button (wheel)
//////                    rotZ -= dx * rotSpeed;
//////                }
////            }
////
////            lastMousePressX = mouseX;
////            lastMousePressY = mouseY;
////
////            lastUpdate = now;
////
////        } else {
////            lastMousePressX = Float.NaN;
////        }
////
////
////        if (changed) {
////            camera.lastUpdate = System.currentTimeMillis();
////            updateNext();
////        }
////
////    }
////
////    protected void updateCamera() {
////
////        if (camera.yScale < minYScale) camera.yScale = minYScale;
////        if (camera.yScale > maxYScale) camera.yScale = maxYScale;
////        if (camera.timeScale < minTimeScale)  camera.timeScale = minTimeScale;
////        if (camera.timeScale > maxTimeScale) camera.timeScale = maxTimeScale;
////
////        translate(-camera.camX + width / 2, -camera.camY + height / 2);
////
////        cycleStart = (int) (Math.floor((camera.camX - width / 2) / camera.timeScale) - 1);
////        cycleStart = Math.max(0, cycleStart);
////        cycleEnd = (int) (Math.ceil((camera.camX + width / 2) / camera.timeScale) + 1);
////
////        if (cycleEnd < cycleStart) cycleEnd = cycleStart;
////
////        drawnTextScale = Math.min(camera.yScale, camera.timeScale) * textScale;
////
////        if (camera.lastUpdate > lastCameraUpdate) {
////            updating = true;
////        }
////
////
////
// // }
