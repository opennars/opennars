//package automenta.vivisect.timeline;
//
//import nars.io.meter.SignalData;
//
//
//public class BarChart extends LineChart {
//
//    float barWidth = 0.9f;
//
//    public BarChart(SignalData... series) {
//        super(series);
//    }
//
//
//    @Override
//    protected void drawChartPre(TimelineVis l, int ccolor) {
//        l.g.noStroke();
//
//    }
//
//
//
//
//
//    @Override
//     void drawPoint(TimelineVis l, float v, float width1, float x, float height1, float y1, float t) {
//
//
//            float p = (max == min) ? 0 : (float) ((v - min) / (max - min));
//            float px = plotWidth * x;
//            float h = p * height1;
//            float py = y + height1 - h;
//            l.g.fill(ccolor, 255f * (0.5f + 0.5f * p));
//            l.g.rect(px, py, plotWidth * width1 * barWidth, h);
//    }
//
//    public BarChart setBarWidth(float f) {
//        this.barWidth = f;
//        return this;
//    }
// }
