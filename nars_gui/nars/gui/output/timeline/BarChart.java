package nars.gui.output.timeline;

import nars.gui.output.chart.TimeSeries;
import nars.util.NARTrace;


public class BarChart extends LineChart {

    float barWidth = 0.9f;

    public BarChart(TimeSeries t) {
        super(t);
    }

    public BarChart(NARTrace t, String... sensors) {
        super(t, sensors);
    }

    @Override
    protected void drawData(Timeline2DCanvas l, float timeScale, float yScale1, float y) {
        int ccolor = 0;
        TimeSeries chart = sensors.get(0);
        ccolor = chart.getColor().getRGB();
        l.noStroke();
        for (long t = l.cycleStart; t < l.cycleEnd; t++) {
            float x = t * timeScale;
            float v = chart.getValue(t);
            
            if (Float.isNaN(v)) {
                continue;
            }
            
            float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
            float px = x;
            float h = p * yScale1;
            float py = y + yScale1 - h;
            l.fill(ccolor, 255f * (0.5f + 0.5f * p));
            l.rect(px, py, timeScale * barWidth, h);
        }
    }
}
