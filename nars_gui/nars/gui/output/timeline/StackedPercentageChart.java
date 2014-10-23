/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.timeline;

import nars.gui.output.chart.TimeSeries;
import nars.util.NARTrace;

/**
 *
 * @author me
 */
public class StackedPercentageChart extends LineChart {
    float barWidth = 0.9f;
    float barHeight = 0.9f;

    public StackedPercentageChart(TimeSeries... series) {
        super(series);
    }

    public StackedPercentageChart(NARTrace t, String... sensors) {
        super(t, sensors);
    }

    @Override
    protected void updateRange(Timeline2DCanvas l) {
        super.updateRange(l);
        min = 0;
        max = 1.0f;
    }

    @Override
    protected void drawData(Timeline2DCanvas l, float timeScale, float yScale, float y) {
        l.noStroke();
        for (long t = l.cycleStart; t < l.cycleEnd; t++) {
            float total = 0;
            for (TimeSeries chart : sensors) {
                float v = chart.getValue(t);
                if (Float.isNaN(v)) {
                    continue;
                }
                total += v;
            }
            if (total == 0) {
                continue;
            }
            float sy = y;
            float gap = yScale * (1.0f - barHeight) / sensors.size();
            for (TimeSeries chart : sensors) {
                int ccolor = chart.getColor().getRGB();
                float lx = 0;
                float ly = 0;
                l.strokeWeight(1f);
                l.fill(255f);
                float x = t * timeScale;
                float v = chart.getValue(t);
                if (Float.isNaN(v)) {
                    continue;
                }
                float p = v / total;
                float px = x;
                float h = p * yScale;
                l.fill(ccolor, 255f * (0.5f + 0.5f * p));
                l.rect(px, sy + gap / 2, timeScale * barWidth, h - gap / 2);
                sy += h;
            }
        }
    }
    
}
