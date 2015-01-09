package nars.gui.output.chart;


import automenta.vivisect.Video;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.AxisPlot;
import automenta.vivisect.timeline.AxisPlot.MultiChart;
import automenta.vivisect.timeline.TimelineVis;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.io.meter.Metrics;
import nars.io.meter.SignalData;
import nars.io.meter.Signal;

public class MeterVis extends TimelineVis {
    private final NAR nar;

    
    public class DataChart {
        
        public final SignalData data;
        public AxisPlot chart;

        public DataChart(String id) {
            this.data = meters.newSignalData(id);
            this.chart = displayedCharts.get(id);
            
            if (chart!=null)
                chart.setOverlayEnable(true);
        }        

        private DataChart(Signal s) {
            this(s.id);
        }
    }
    
    final Map<String, AxisPlot> displayedCharts = new HashMap();
    final Metrics meters;
    final Map<String, DataChart> charts;
    
    private final int historySize;
    
    public static final Font monofontLarge = Video.monofont.deriveFont(Font.PLAIN, 18f);
    public static final Font monofontSmall = Video.monofont.deriveFont(Font.PLAIN, 13f);
    int nextChartTime = 0;
    

    
    public MeterVisPanel newPanel() {
        return new MeterVisPanel();
    }
    
    public MeterVis(NAR nar, Metrics meters, int historySize) {
        this(nar, meters, historySize, null);
    }
    
    /**
     *
     * @param title
     * @param historySize
     * @param chartType use Chart.BAR, Chart.LINE, Chart.PIE, Chart.AREA,
     * Chart.BAR_CENTERED
     */
    public MeterVis(NAR nar, Metrics<?,?> meters, int historySize, String[] enabled) {
        super();

        this.nar = nar;        
        this.meters = meters;
        this.historySize = historySize;

        charts = new TreeMap();
        
        if (enabled == null) {
            for (Signal s : meters.getSignals()) {
                charts.put(s.id, new DataChart(s));
            }
        }
        else {
            for (String f : enabled) {
                charts.put(f, new DataChart(f));            
            }
        }
        
    }
    


    public DataChart getDisplayedChart(String id) {
        if (id.contains("#")) {
            String baseName = id.split("#")[0];
            return getDisplayedChart(baseName);
        }
        DataChart c = charts.get(id);
        if (c!=null) {
            if (c instanceof MultiChart)
                ((MultiChart)c).getData().add(c.data);
            else
                throw new RuntimeException(c + " does not support multiple datas");
        }
        else {
            /*c = meters.newDefaultChart(id, data);
            
            if (c == null)
                c = new LineChart(data);
            
            displayedCharts.put(id, c);
            addChart(c);            */
        }
        return c;
    }    
    /** sample the next value from each meter into the history */
    public void updateData(long t) {
        int ct = nextChartTime++; 
        meters.update(t);
    }

    
    
    public class MeterVisPanel extends PCanvas implements EventObserver {

        float scaleSpeed = 1f / 1000.0f;
        
        public MeterVisPanel() {
            super(MeterVis.this);
            noLoop();
            
            
            
            
            //TODO disable event when window hiden
            nar.on(FrameEnd.class, this);

            /*
            addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    double r = e.getPreciseWheelRotation();

                    if (r < 0) {
                        camera.yScale *= 0.9f;
                    }
                    else if (r > 0) {
                        camera.yScale *= 1.1f;
                    }

                    repaint();
                }
            });
            */
            
            final MouseAdapter c;
            addMouseMotionListener(c = new MouseAdapter() {
                private Point startLocation;
                //private int startYOffset;
                float oy, ox;

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        e.consume();
                        startLocation = e.getPoint();
                        oy = camera.yScale;
                        ox = camera.xScale;                        
                    }
                    
                }


                @Override
                public void mouseDragged(MouseEvent e) {
                    if (startLocation==null) {
                        return;
                    }
                    
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        e.consume();
                        Point currentLocation = e.getPoint();
                        int deltaX  = currentLocation.x - startLocation.x;
                        int deltaY  = currentLocation.y - startLocation.y;
                        camera.yScale = oy * (1.0f + (deltaY* scaleSpeed));
                        camera.xScale = ox * (1.0f + (deltaX * scaleSpeed));
                        redraw();
                        
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    startLocation = null;
                }

            });
            addMouseListener(c);

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setZoom(0.2f);
                    setPanY(getHeight()+100.0f);
                    setPanX(getWidth()/2); //setPanX(getWidth()+35.0f);
                    camera.xScale = 200;
                    camera.yScale = 200;
                    repaint();

                    /*addComponentListener(new ComponentAdapter() {
                        @Override public void componentResized(ComponentEvent e) {
                            repaint();
                        }
                    });*/
                }

            });
        
        }
        
        @Override
        public void event(Class event, Object[] args) {
            if (event == FrameEnd.class) {
                updateData(nar.time());
                redraw();
            }
        }
        
    }

    
//    @Override
//    public boolean draw(PGraphics pg) {
//        for (Map.Entry<String, DataChart> e : charts.entrySet()) {
//            String f = e.getKey();            
//            DataChart dc = e.getValue();
//            Chart chart = dc.chart;
//            
//            
//        }
//        return true;
//    }
    
    
//    public void redraw() {
//	
//        //TODO allow buffering input data points while not visible
//        Graphics2D g = getBufferGraphics();
//        if (g == null) return;
//        
//        if (renderHints == null) {
//            renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        }
//        g.setRenderingHints(renderHints);
//
//        int width = getWidth();
//        int height = getHeight();
//        
//        g.clearRect(0,0,width,height);
//        //g.setColor(backgroundClearColor);
//        //g.fillRect(0,0,width,height);
// 
//            
//
//        int numCharts = data.size();
//        int y = yOffset;
//        
//        int verticalPadding = 2;
//        int h = (int)(yScale * ((float)height) / ((float)numCharts));
//        
//        
//        if (p == null) {
//            p = new Polygon(new int[historySize+2], new int[historySize+2], historySize+2);
//        }
//        final int[] xPoints = p.xpoints;
//        final int[] yPoints = p.ypoints;
//        
//        
//        float xp = getWidth();
//        float dx = (float)Math.ceil(((float)getWidth()) / ((float)historySize));
//        for (int i = 0; i < historySize; i++) {            
//            xPoints[i+1] = (int)Math.round(xp);
//            xp -= dx;
//        }
//        xPoints[0] = xPoints[1];
//        xPoints[xPoints.length-1] = xPoints[xPoints.length-2] = 0;
//        
//        for (Map.Entry<String, TimeSeries> e : charts.entrySet()) {
//            
//            if (y + h < 0) {
//                y+=h;
//                continue;
//            }
//
//            String f = e.getKey();            
//            TimeSeries ch = e.getValue();
//                        
//            if (ch==null) {
//                ch = addChart(f);            
//            }
//            
//        
//            g.setPaint(ch.getColor());            
//        
//            if (ch.values.size() == 0)
//                continue;
//            long end = ch.getEnd();
//            long start = end - historySize;
//            float[] mm = ch.getMinMax(start, end);
//            float min = mm[0];
//            float max = mm[1];
//                    
//            float range = max - min;
//            
//            double firstValue;
//            if (range != 0) {
//                //Draw Area chart
//                
//                int n = 0;
//                firstValue = 0;
//                
//                for (long pp = start; pp < end; pp++) {
//                    float d = ch.getValue(pp);
//                    if (n == 0)
//                        firstValue = d;
//
//                    d = (d - min) / (range);
//
//                    int p = h - (int)(d*(h - verticalPadding));
//                    if (p < 0) p = 0;
//                    if (p > h) p = h;
//                    yPoints[(n++)+1] = y + p;
//                }
//                Arrays.fill(yPoints, n, xPoints.length-1, y+h);
//                yPoints[0] = yPoints[xPoints.length-1] = y + h;
//
//                p.invalidate();                
//                g.fillPolygon(p);                
//            }
//            else {                
//                firstValue = ch.getValue(0);
//            }
//            
//            g.setPaint(Color.WHITE);
//            
//            if (h < 14) {
//                g.setFont(monofontSmall);                
//            }
//            else {
//                g.setFont(monofontLarge);
//            }
//            g.drawString(f, 5, y+16);
//            if (h > 25) {
//                g.setFont(monofontSmall);
//                g.drawString("  current=" + 
//                        Texts.n4((float)firstValue) + ", min=" + 
//                        Texts.n4(min) + ", max=" + 
//                        Texts.n4(max), 5, y+16+12);
//            }
//            
//            y += h;
//        
//            if (y >= height) break;
//             
//        }
//
//        showBuffer(g);
//    }

    /*
     public static void main(String[] args) {
     PLineChart p = new PLineChart("Average",10);
     p.addPoint(2,2);
     p.addPoint(4,4);
        
     Window w = new Window("", p.newPanel());
        
        
        
     w.setSize(400,400);
     w.setVisible(true);
     }
     */
}
