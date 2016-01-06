//package automenta.vivisect.timeline;
//
//import automenta.vivisect.Video;
//import automenta.vivisect.swing.PCanvas;
//import automenta.vivisect.timeline.AxisPlot.MultiChart;
//import nars.io.Texts;
//import nars.io.meter.Metrics;
//import nars.io.meter.SignalData;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class LineChart extends AxisPlot implements MultiChart {
//
//    protected final List<SignalData> data;
//    double min;
//    double max;
//    boolean showVerticalLines = false;
//    boolean showPoints = true;
//    float lineThickness = 0.1f;
//    final float pointWidthFactor = 2.75f; /* multiplied by linethickness */
//    float borderThickness = 0.1f;
//
//
//    boolean xorOverlay = false;
//    private float pwf;
//    private float lx;
//    private float ly;
//    private boolean firstPoint;
//    int ccolor;
//    private boolean specifiedRange;
//    private float screenyLo;
//    private float screenyHi;
//    private float pixelsTallNecessaryForLinePoints = 96;
//    private boolean allowLinePoints;
//    private float maxX;
//    private float minX;
//
//
//
//    public LineChart(SignalData... series) {
//        super();
//
//
//        this.data = new ArrayList(series.length);
//        for (SignalData s : series) {
//            data.add(s);
//        }
//
//    }
//
//    public LineChart(float min, float max, SignalData... series) {
//        this(series);
//
//        range(min, max);
//    }
//
//
//    public LineChart range(float min, float max) {
//        this.specifiedRange = true;
//        this.min = min;
//        this.max = max;
//        return this;
//    }
//
//    public LineChart thickness(float thick) {
//        this.lineThickness = thick;
//        return this;
//    }
//
//
//    @Override
//    public void render(TimelineVis l) {
//
//        screenyHi = l.g.screenY(x, y);
//        screenyLo = l.g.screenY(x + plotWidth, y + plotHeight);
//
//        allowLinePoints = ( (screenyLo - screenyHi) >= pixelsTallNecessaryForLinePoints);
//
//        int displayHeight = l.g.height;
//
//        //TODO Horizontal clipping
//
//        //Vertical Clipping:
//        if ( ((screenyHi < 0) && (screenyLo < 0)) || ((screenyHi >= displayHeight) && (screenyLo >= displayHeight)) ) {
//            return;
//        }
//
//        l.g.stroke(127);
//        l.g.strokeWeight(borderThickness);
//
//        minX = (float)xMin();
//        maxX = (float)xMax();
//
//        float range = Math.abs(maxX - minX);
//
//        //bottom line
//        l.g.line(0, y + plotHeight, plotWidth * range, y + plotHeight);
//        //top line
//        l.g.line(0, y, plotWidth * range, y);
//
//
//        drawData(l, plotWidth, plotHeight, y);
//
//        if (overlayEnable) {
//            drawOverlay(l, screenyLo, screenyHi);
//        }
//    }
//
//    @Override
//    public void update(TimelineVis l) {
//        for (SignalData s : data) {
//            s.getData();
//        }
//        updateRange();
//    }
//
//
//    protected void updateRange() {
//        if (specifiedRange) return;
//
//
//        double[] bounds = getMetrics().getBounds(data);
//        min = bounds[0];
//        max = bounds[1];
//    }
//
//
//    protected void drawOverlay(TimelineVis l, float screenyLo, float screenyHi) {
//
//        //render overlay
//        l.g.pushMatrix();
//        l.g.resetMatrix();
//
//        if (xorOverlay) {
//            Graphics2D g2 = l.g2;
//            g2.setXORMode(Color.white);
//        }
//
//
//        int dsy = (int) Math.abs(screenyLo - screenyHi);
//
//
//        float ytspace = dsy * 0.75f / data.size() / 2;
//
//        //l.g.textFont(PCanvas.font9);
//        l.g.fill(210);
//
//        //TODO number precision formatting
//        l.g.text(Texts.n4((float)min), 15, screenyLo - dsy / 10f);
//        l.g.text(Texts.n4((float)max), 0, screenyHi + dsy / 10f);
//
//        //l.g.textFont(PCanvas.font12);
//        float dsyt = screenyHi + 0.15f * dsy;
//        int order = 0;
//        for (SignalData chart : data) {
//            l.g.fill(getColor(chart.getID(), order++) | 0x77777777);
//            dsyt += ytspace;
//            l.g.text(chart.getID(), 8, dsyt);
//            dsyt += ytspace;
//        }
//
//        if (xorOverlay) {
//            Graphics2D g2 = l.g2;
//            g2.setPaintMode();
//        }
//
//        l.g.popMatrix();
//    }
//
//    protected void drawData(TimelineVis l, float w, float h, float y) {
//
//        Object[] time = l.getTimeAxis();
//
//        int order = 0;
//        for (SignalData chart : data) {
//            drawChart(time, chart, order, l, w, h, y);
//        }
//    }
//
//    void drawChart(Object[] time, SignalData chart, int order, TimelineVis l, float width, float height, float y1) {
//
//        pwf = pointWidthFactor*lineThickness;
//
//
//        ccolor = getColor(chart.getSignal(), order++);
//        lx = ly = 0;
//        firstPoint = false;
//
//        drawChartPre(l, ccolor);
//
//        float cs = minX;
//        Object[] series = chart.getDataCached();
//        if (series == null) return;
//        for (int t = 0; t< series.length; t++) {
//            Object ox = time[t]; //time
//            Object oy = series[t]; //value
//            if ((ox==null) || (oy==null))
//                continue;
//            float tx = ((Number)ox).floatValue();
//            float v = ((Number)oy).floatValue();
//            float x = (tx-cs) * width;
//            if (Float.isNaN(v)) {
//                continue;
//            }
//
//            drawPoint(l, v, width, x, height, y1, tx);
//        }
//    }
//
//    void drawPoint(TimelineVis l, float v, float width1, float x1, float height1, float y1, float t) {
//        l.g.stroke = true;
//        float p = (float)((max == min) ? 0 : (v - min) / (max - min));
//        float px = width1 * x1;
//        float h = p * height1;
//        float py = y1 + height1 - h;
//        if (firstPoint) {
//            if (showVerticalLines) {
//                l.g.line(px, py, px, py + h);
//            }
//
//            if (t != minX) {
//                l.g.line(lx, ly, px, py);
//            }
//        }
//        lx = px;
//        ly = py;
//        firstPoint = true;
//        if (showPoints && allowLinePoints) {
//            l.g.stroke = false;
//
//            //TODO create separate size and opacity get/set parameter for the points
//            //l.g.fill(ccolor); //, 128f * (p * 0.5f + 0.5f));
//            l.g.rect(px - pwf / 2f, py - pwf / 2f, pwf, pwf);
//        }
//    }
//
//    public double xMax() {
//        return getMetrics().getSignal(0).getMax();
//    }
//
//    public double xMin() {
//        return getMetrics().getSignal(0).getMin();
//    }
//
//
//
//
//
//    @Override
//    public List<SignalData> getData() {
//        return data;
//    }
//
//    public void setLineThickness(float lineThickness) {
//        this.lineThickness = lineThickness;
//    }
//
//    private int getColor(Object o, int order) {
//        return Video.colorHSB(Video.hashFloat(o.hashCode())+ order*0.2f, 0.8f, 0.8f, 1.0f);
//    }
//
//    public Metrics getMetrics() {
//        //TODO this assumes that all the charts are from the same Metrics
//        //but this need not be the case in a future revision
//        return getData().get(0).getMetric();
//    }
//
////    protected void drawChartPre(TimelineVis l, int ccolor) {
////        l.g.stroke(ccolor);
////        l.g.fill(ccolor);
////        l.g.strokeWeight(lineThickness);
////    }
//
//
//
//
// }
