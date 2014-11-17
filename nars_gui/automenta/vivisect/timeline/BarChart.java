package automenta.vivisect.timeline;

import automenta.vivisect.TreeMLData;


public class BarChart extends LineChart {

    float barWidth = 0.9f;

    public BarChart(TreeMLData t) {
        super(t);
    }


    @Override
    protected void drawData(TimelineVis l, float timeScale, float yScale1, float y) {
        
        
        if (data.size()!=1) 
            throw new RuntimeException("BarChart only supports one data set");
        
        TreeMLData chart = data.get(0);
        
        int ccolor = chart.getColor();
        l.g.noStroke();
        for (int t = l.cycleStart; t < l.cycleEnd; t++) {
            float x = (t-l.cycleStart) * timeScale;
            float v = (float)chart.getData(t);
            
            if (Float.isNaN(v)) {
                continue;
            }
            
            float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
            float px = width * x;
            float h = p * yScale1;
            float py = y + yScale1 - h;
            l.g.fill(ccolor, 255f * (0.5f + 0.5f * p));
            l.g.rect(px, py, width * timeScale * barWidth, h);
        }
    }
}
