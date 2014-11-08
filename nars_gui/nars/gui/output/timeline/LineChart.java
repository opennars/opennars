package nars.gui.output.timeline;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nars.gui.output.chart.TimeSeries;
import nars.io.Texts;
import nars.util.NARTrace;

public class LineChart extends Chart {
    public final List<TimeSeries> sensors;
    double min;
    double max;
    boolean showVerticalLines = false;
    boolean showPoints = true;
    float lineThickness = 1.5f;
    float borderThickness = 0.5f;

  

    public LineChart(TimeSeries... series) {
        super();
        this.sensors = Arrays.asList(series);
    }

    public LineChart(NARTrace t, String... sensors) {
        super();
        this.sensors = Stream.of(sensors).map((x) -> t.charts.get(x)).collect(Collectors.toList());
    }

    @Override
    public void draw(Timeline2DCanvas l, float y, float timeScale, float yScale) {
        yScale *= height;
        float screenyHi = l.screenY(l.cycleStart * timeScale, y);
        float screenyLo = l.screenY(l.cycleStart * timeScale, y + yScale);
        updateRange(l);
        l.stroke(127);
        l.strokeWeight(borderThickness);
        //bottom line
        l.line(l.cycleStart * timeScale, y + yScale, l.cycleEnd * timeScale, y + yScale);
        //top line
        l.line(l.cycleStart * timeScale, y, l.cycleEnd * timeScale, y);
        drawData(l, timeScale, yScale, y);
        drawOverlay(l, screenyLo, screenyHi);
    }

    protected void updateRange(Timeline2DCanvas l) {
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        for (TimeSeries chart : sensors) {
            float[] mm = chart.getMinMax(l.cycleStart, l.cycleEnd);
            min = Math.min(min,mm[0]);
            max = Math.max(max,mm[1]);
        }
        
    }

    protected void drawOverlay(Timeline2DCanvas l, float screenyLo, float screenyHi) {
        //draw overlay
        l.pushMatrix();
        l.resetMatrix();
        l.textSize(15f);
        int dsy = (int) Math.abs(screenyLo - screenyHi);
        float dsyt = screenyHi + 0.15f * dsy;
        float ytspace = dsy * 0.75f / sensors.size() / 2;
        for (TimeSeries chart : sensors) {
            l.fill(chart.getColor().getRGB());
            dsyt += ytspace;
            l.text(chart.label, 0, dsyt);
            dsyt += ytspace;
        }
        l.textSize(11f);
        l.fill(200, 195f);
        l.text(Texts.n4((float) min), 0, screenyLo - dsy / 10f);
        l.text(Texts.n4((float) max), 0, screenyHi + dsy / 10f);
        l.popMatrix();
    }

    protected void drawData(Timeline2DCanvas l, float timeScale1, float yScale1, float y) {
        int ccolor = 0;
        for (TimeSeries chart : sensors) {
            ccolor = chart.getColor().getRGB();
            float lx = 0;
            float ly = 0;
            l.fill(255f);
            boolean firstPoint = false;
            for (long t = l.cycleStart; t < l.cycleEnd; t++) {
                float x = t * timeScale1;
                float v = chart.getValue(t);
                if (Float.isNaN(v)) {
                    continue;
                }
                
                float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
                float px = x;
                float h = p * yScale1;
                float py = y + yScale1 - h;
                                
                if (firstPoint) {
                    l.strokeWeight(lineThickness);
                    if (showVerticalLines) {
                        l.stroke(ccolor, 127f);
                        l.line(px, py, px, py + h);
                    }
                    l.stroke(ccolor);

                    if (t != l.cycleStart) {
                        l.line(lx, ly, px, py);
                    }
                }
                
                lx = px;
                ly = py;
                
                firstPoint = true;
                
                if (showPoints) {
                    l.noStroke();
                    l.fill(ccolor);
                    float w = Math.min(timeScale1, yScale1) / 12f;
                    l.rect(px - w / 2f, py - w / 2f, w, w);
                }
            }
        }
    }
    
}
