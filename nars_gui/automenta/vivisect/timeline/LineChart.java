package automenta.vivisect.timeline;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.timeline.Chart.MultiChart;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class LineChart extends Chart implements MultiChart {
    protected final List<TreeMLData> data;
    float min;
    float max;
    boolean showVerticalLines = false;
    boolean showPoints = true;
    float lineThickness = 1f;
    float borderThickness = 0.5f;
    private int end;
    private int start;
    private boolean specifiedRange;
    boolean xorOverlay = false;

  

    public LineChart(TreeMLData... series) {
        super();
        
        this.data = new ArrayList(series.length);
        for (TreeMLData s : series)
            data.add(s);
        
    }
    
    public LineChart(float min, float max, TreeMLData... series) {
        this(series);
        
        range(min, max);
    }

 
    public LineChart range(float min, float max) {
        this.specifiedRange = true;
        this.min = min;
        this.max = max; 
        return this;
    }

    


    @Override
    public void draw(TimelineVis l, float y, float timeScale, float yScale) {
        yScale *= height;
        float screenyHi = l.g.screenY(l.cycleStart * timeScale, y);
        float screenyLo = l.g.screenY(l.cycleStart * timeScale, y + yScale);
        
        int displayHeight = l.g.height;
        
        //TODO Horizontal clipping
        
        //Vertical Clipping:
        if ( ((screenyHi < 0) && (screenyLo < 0)) || ((screenyHi >= displayHeight) && (screenyLo >= displayHeight)) ) {
            return;
        }
                
        if (!specifiedRange)
            updateRange(l);
        
        l.g.stroke(127);
        l.g.strokeWeight(borderThickness);
        //bottom line
        l.g.line(0, y + yScale, width * (l.cycleEnd-l.cycleStart) * timeScale, y + yScale);
        //top line
        l.g.line(0, y, width * (l.cycleEnd-l.cycleStart) * timeScale, y);
        drawData(l, timeScale, yScale, y);
        
        if (overlayEnable) {
            drawOverlay(l, screenyLo, screenyHi);
        }
    }

    protected void updateRange(TimelineVis l) {
        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;
        for (TreeMLData chart : data) {
            double[] mm = chart.getMinMax(l.cycleStart, l.cycleEnd);
            min = (float)Math.min(min,mm[0]);
            max = (float)Math.max(max,mm[1]);
        }
        
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

        l.g.textSize(11f);
        l.g.fill(210);
        
        //TODO number precision formatting
        l.g.text("" + ((double) min), 0, screenyLo - dsy / 10f);
        l.g.text("" + ((double) max), 0, screenyHi + dsy / 10f);

        l.g.textSize(15f);
        float dsyt = screenyHi + 0.15f * dsy;
        for (TreeMLData chart : data) {
            l.g.fill(chart.getColor());
            dsyt += ytspace;
            l.g.text(chart.label, 0, dsyt);
            dsyt += ytspace;
        }
        
        if (xorOverlay) {        
            Graphics2D g2 = l.g2;
            g2.setPaintMode();
        }
        
        l.g.popMatrix();
    }

    protected void drawData(TimelineVis l, float timeScale1, float yScale1, float y) {
        int ccolor = 0;
        for (TreeMLData chart : data) {
            ccolor = chart.getColor();
            float lx = 0;
            float ly = 0;
            l.g.fill(255f);
            boolean firstPoint = false;
            int cs = l.cycleStart;
            for (int t = cs; t < l.cycleEnd; t++) {
                float x = (t-cs) * timeScale1;
                float v = (float)chart.getData(t);
                if (Float.isNaN(v)) {
                    continue;
                }
                
                float p = (float)((max == min) ? 0 : (double) ((v - min) / (max - min)));
                float px = width * x;
                float h = p * yScale1;
                float py = y + yScale1 - h;
                                
                if (firstPoint) {
                    l.g.strokeWeight(lineThickness);
                    if (showVerticalLines) {
                        l.g.stroke(ccolor, 127f);
                        l.g.line(px, py, px, py + h);
                    }
                    l.g.stroke(ccolor);

                    if (t != l.cycleStart) {
                        l.g.line(lx, ly, px, py);
                    }
                }
                
                lx = px;
                ly = py;
                
                firstPoint = true;
                
                if (showPoints) {
                    l.g.noStroke();
                    //TODO create separate size and opacity get/set parameter for the points
                    l.g.fill(ccolor, 128f * (p * 0.5f + 0.5f));
                    float w = lineThickness * 2.75f;
                    l.g.rect(px - w / 2f, py - w / 2f, w, w);
                }
            }
        }
    }

    @Override
    public int getStart() {
        start = Integer.MAX_VALUE;
        end = 0;
        for (TreeMLData s  : data) {
            int ss = s.getStart();
            int se = s.getEnd();
                    
            if (start > ss) start = ss;
            if (end < se) end = se;
        }
        return start;
    }

    @Override
    public int getEnd() {
        //call getStart() prior to this
        return end;
    }

    @Override
    public List<TreeMLData> getData() {
        return data;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }
    
    
    
    
    
}
