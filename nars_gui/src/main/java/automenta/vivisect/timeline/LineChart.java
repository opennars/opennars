package automenta.vivisect.timeline;

import automenta.vivisect.Video;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.AxisPlot.MultiChart;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nars.io.Texts;
import nars.io.meter.Metrics;
import nars.io.meter.Metrics.SignalData;

public class LineChart extends AxisPlot implements MultiChart {
    protected final List<SignalData> data;
    double min;
    double max;
    boolean showVerticalLines = false;
    boolean showPoints = true;
    float lineThickness = 0.1f;
    float borderThickness = 0.1f;

    private boolean specifiedRange;
    boolean xorOverlay = false;

  

    public LineChart(SignalData... series) {
        super();
        
        
        this.data = new ArrayList(series.length);
        for (SignalData s : series) {
            data.add(s);
        }
        
    }
    
    public LineChart(float min, float max, SignalData... series) {
        this(series);
        
        range(min, max);
    }

 
    public LineChart range(float min, float max) {
        this.specifiedRange = true;
        this.min = min;
        this.max = max; 
        return this;
    }

    public LineChart thickness(float thick) {
        this.lineThickness = thick;
        return this;
    }

    
    @Override
    public void draw(TimelineVis l) {
        
        float screenyHi = l.g.screenY(x, y);
        float screenyLo = l.g.screenY(x + width, y + height);
        
        int displayHeight = l.g.height;
        
        //TODO Horizontal clipping
        
        //Vertical Clipping:
        if ( ((screenyHi < 0) && (screenyLo < 0)) || ((screenyHi >= displayHeight) && (screenyLo >= displayHeight)) ) {
            return;
        }
                
        
        updateRange();
                
        
        l.g.stroke(127);
        l.g.strokeWeight(borderThickness);
        
        float range = (float)(xMax() - xMin());
        
        //bottom line
        l.g.line(0, y + height, width * range * width, y + height);
        //top line
        l.g.line(0, y, width * range * width, y);
        
        drawData(l, width, height, y);
        
        if (overlayEnable) {
            drawOverlay(l, screenyLo, screenyHi);
        }
    }

    protected void updateRange() {
        double[] bounds = getMetrics().getBounds(data);        
        min = bounds[0];
        max = bounds[1];        
    }
    
 
    protected void drawOverlay(TimelineVis l, float screenyLo, float screenyHi) {
        
        //draw overlay
        l.g.pushMatrix();
        l.g.resetMatrix();
                
        if (xorOverlay) {
            Graphics2D g2 = l.g2;
            g2.setXORMode(Color.white);
        }
    
        
        int dsy = (int) Math.abs(screenyLo - screenyHi);
        
        
        float ytspace = dsy * 0.75f / data.size() / 2;

        l.g.textFont(PCanvas.font9);
        l.g.fill(210);
        
        //TODO number precision formatting
        l.g.text(Texts.n4((float)min), 15, screenyLo - dsy / 10f);
        l.g.text(Texts.n4((float)max), 0, screenyHi + dsy / 10f);

        l.g.textFont(PCanvas.font12);
        float dsyt = screenyHi + 0.15f * dsy;
        int order = 0;
        for (SignalData chart : data) {
            l.g.fill(getColor(chart.getID(), order++) | 0x77777777);
            dsyt += ytspace;
            l.g.text(chart.getID(), 8, dsyt);
            dsyt += ytspace;
        }
        
        if (xorOverlay) {        
            Graphics2D g2 = l.g2;
            g2.setPaintMode();
        }
        
        l.g.popMatrix();
    }

    protected void drawData(TimelineVis l, float width1, float height1, float y) {

        int ccolor = 0;
        float pointWidth = lineThickness * 2.75f;
                
        int order = 0;
        for (SignalData chart : data) {
            ccolor = getColor(chart.getSignal(), order++);
            float lx = 0;
            float ly = 0;
            boolean firstPoint = false;
            
            l.g.stroke(ccolor);
            l.g.fill(ccolor);
            l.g.strokeWeight(lineThickness);
            
            float cs = (float)xMin();
            Iterator<Object[]> series = chart.iteratorWith(0);
            while (series.hasNext()) {
                Object[] o = series.next();
                Object ox = o[0]; //time
                Object oy = o[1]; //value
                if ((ox==null) || (oy==null))
                    continue;
                float t = ((Number)ox).floatValue();
                float v = ((Number)oy).floatValue();
                
                l.g.stroke = true;
                
                float x = (t-cs) * width1;
                if (Float.isNaN(v)) {
                    continue;
                }
                
                float p = (float)((max == min) ? 0 : (double) ((v - min) / (max - min)));
                float px = width * x;
                float h = p * height1;
                float py = y + height1 - h;
                                
                if (firstPoint) {
                    if (showVerticalLines) {
                        l.g.line(px, py, px, py + h);
                    }

                    if (t != xMin()) {
                        l.g.line(lx, ly, px, py);
                    }
                }
                
                lx = px;
                ly = py;
                
                firstPoint = true;
                
                if (showPoints) {
                    l.g.stroke = false;                    
                    
                    //TODO create separate size and opacity get/set parameter for the points
                    //l.g.fill(ccolor); //, 128f * (p * 0.5f + 0.5f));                    
                    l.g.rect(px - pointWidth / 2f, py - pointWidth / 2f, pointWidth, pointWidth);
                }
            }
        }
    }

    public double xMax() {
        return getMetrics().getSignal(0).getMax();
    }

    public double xMin() {
        return getMetrics().getSignal(0).getMin();
    }
    
    



    @Override
    public List<SignalData> getData() {
        return data;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }

    private int getColor(Object o, int order) {
        return Video.colorHSB(Video.hashFloat(o.hashCode())+ order*0.2f, 0.8f, 0.8f, 1.0f);
    }

    public Metrics getMetrics() {
        //TODO this assumes that all the charts are from the same Metrics
        //but this need not be the case in a future revision
        return getData().get(0).getMetric();
    }
       
    
}
