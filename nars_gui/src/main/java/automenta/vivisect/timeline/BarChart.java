package automenta.vivisect.timeline;

import nars.io.meter.Metrics.SignalData;


public class BarChart extends LineChart {

    float barWidth = 0.9f;

    public BarChart(SignalData... series) {
        super(series);
    }


//    @Override
//    protected void drawData(TimelineVis l, float timeScale, float yScale1, float y) {
//        
//        
//        if (data.size()!=1) 
//            throw new RuntimeException("BarChart only supports one data set");
//        
//        SignalData chart = data.get(0);
//        
//        int ccolor = Color.WHITE.getRGB(); //chart.getColor();
//        l.g.noStroke();
//        l.g.fill(ccolor);
//        for (int t = l.xMin; t < l.xMax; t++) {
//            float x = (t-l.xMin) * timeScale;
//            float v = (float)chart.getData(t);
//            
//            if (Float.isNaN(v)) {
//                continue;
//            }
//            
//            float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
//            float px = width * x;
//            float h = p * yScale1;
//            float py = y + yScale1 - h;
//            //l.g.fill(ccolor, 255f * (0.5f + 0.5f * p));
//            l.g.rect(px, py, width * timeScale * barWidth, h);
//        }
//    }


    @Override
     void drawPoint(TimelineVis l, float v, float width1, float x, float height1, float y1, float t) {

            
            float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
            float px = width * x;
            float h = p * height1;
            float py = y + height1 - h;
            //l.g.fill(ccolor, 255f * (0.5f + 0.5f * p));
            l.g.rect(px, py, width * width1 * barWidth, h);
    }

    public BarChart setBarWidth(float f) {
        this.barWidth = f;
        return this;
    }
}
