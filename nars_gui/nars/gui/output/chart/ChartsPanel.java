package nars.gui.output.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.SwingUtilities;
import nars.gui.NARSwing;
import nars.gui.NCanvas;
import nars.util.meter.data.DataSet;

public class ChartsPanel extends NCanvas {

    
    //float motionBlur = 0.9f;
    private final int historySize;
    //private final int lineColor;
    //private final int chartType;
    private boolean autoRanging = true;
    private final DataSet data;
    final Map<String, TimeSeriesChart> charts = new TreeMap();
    
    public static final Font monofontLarge = NARSwing.monofont.deriveFont(Font.PLAIN, 18f);
    public static final Font monofontSmall = NARSwing.monofont.deriveFont(Font.PLAIN, 13f);
    
    float yScale = 1.0f;
    private int yOffset = 0;
    private Polygon p;

    /**
     *
     * @param title
     * @param historySize
     * @param chartType use Chart.BAR, Chart.LINE, Chart.PIE, Chart.AREA,
     * Chart.BAR_CENTERED
     */
    public ChartsPanel(DataSet data, int historySize) {
        super();
        this.data = data;
        this.historySize = historySize;
        //this.lineColor = papplet.getColor(title);
        //this.chartType = chartType;

        for (String f : data.keySet()) {
            addChart(f);
        }

        addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double r = e.getPreciseWheelRotation();
                
                if (r < 0) {
                    yScale *= 0.9f;
                }
                else if (r > 0) {
                    yScale *= 1.1f;
                }
                
                repaint();
            }
        });
        final MouseAdapter c;
        addMouseMotionListener(c = new MouseAdapter() {
            private Point startLocation;
            private int startYOffset;

            @Override
            public void mousePressed(MouseEvent e) {
                startLocation = e.getPoint();
                startYOffset = yOffset;
            }

            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (startLocation==null) {
                    startLocation = e.getPoint();
                    return;
                }
                Point currentLocation = e.getPoint();
                int deltaY  = currentLocation.y - startLocation.y;
                yOffset = startYOffset + deltaY;
                repaint();
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
                repaint();
                
                addComponentListener(new ComponentAdapter() {
                    @Override public void componentResized(ComponentEvent e) {
                        repaint();
                    }
                });
            }
            
        });
    }

    @Override
    public void paint(Graphics g) {
        update(false);
        super.paint(g);
    }
    
    protected TimeSeriesChart addChart(final String f) {
        //int chartType = TimeSeriesChart.AREA;

        TimeSeriesChart ch = new TimeSeriesChart(f, NARSwing.getColor(f, 0.7f, 0.7f), historySize);
        charts.put(f, ch);
        
        
        return ch;
    }
    
    
    
    public void update(final boolean addNextPoint) {
	
        //TODO allow buffering input data points while not visible
        Graphics2D g = getBufferGraphics();
        if (g == null) return;
        
        if (renderHints == null) {
            renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g.setRenderingHints(renderHints);

        int width = getWidth();
        int height = getHeight();
        
        g.clearRect(0,0,width,height);
        //g.setColor(backgroundClearColor);
        //g.fillRect(0,0,width,height);
 
            

        int numCharts = data.size();
        int y = yOffset;
        
        int verticalPadding = 2;
        int h = (int)(yScale * ((float)height) / ((float)numCharts));
        
        
        if (p == null) {
            p = new Polygon(new int[historySize+2], new int[historySize+2], historySize+2);
        }
        final int[] xPoints = p.xpoints;
        final int[] yPoints = p.ypoints;
        
        
        float xp = getWidth();
        float dx = (float)Math.ceil(((float)getWidth()) / ((float)historySize));
        for (int i = 0; i < historySize; i++) {            
            xPoints[i+1] = (int)Math.round(xp);
            xp -= dx;
        }
        xPoints[0] = xPoints[1];
        xPoints[xPoints.length-1] = xPoints[xPoints.length-2] = 0;
        
        for (Map.Entry<String, TimeSeriesChart> e : charts.entrySet()) {
            
            if (y + h < 0) {
                y+=h;
                continue;
            }

            String f = e.getKey();            
            TimeSeriesChart ch = e.getValue();
                        
            if (ch==null) {
                ch = addChart(f);            
            }
            
            if (addNextPoint) {
                Object value = data.get(f);

                if (value instanceof Double) {                    
                    ch.push(((Number) value).floatValue());
                }
                else if (value instanceof Float) {
                    ch.push(((Number) value).floatValue());
                }
                else if (value instanceof Integer) {
                    ch.push(((Number) value).floatValue());
                }
                else if (value instanceof Long) {
                    ch.push(((Number) value).floatValue());
                }
            }            
        
            g.setPaint(ch.getColor());            
            
            float min = ch.min;
            float max = ch.max;            
                    
            float range = max - min;
            
            double firstValue;
            if (range != 0) {
                //Draw Area chart
                
                int n = 0;
                firstValue = 0;
                for (float d : ch.values) {
                    if (n == 0)
                        firstValue = d;

                    d = (d - min) / (range);

                    int p = h - (int)(d*(h - verticalPadding));
                    if (p < 0) p = 0;
                    if (p > h) p = h;
                    yPoints[(n++)+1] = y + p;
                }
                Arrays.fill(yPoints, n, xPoints.length-1, y+h);
                yPoints[0] = yPoints[xPoints.length-1] = y + h;

                p.invalidate();                
                g.fillPolygon(p);                
            }
            else {                
                firstValue = ch.values[0];
            }
            
            g.setPaint(Color.WHITE);
            
            if (h < 14) {
                g.setFont(monofontSmall);                
            }
            else {
                g.setFont(monofontLarge);
            }
            g.drawString(f, 5, y+16);
            if (h > 25) {
                g.setFont(monofontSmall);
                g.drawString("  current=" + firstValue + ", min=" + min + ", max=" + max, 5, y+16+12);
            }
            
            y += h;
        
            if (y >= height) break;
             
        }

        showBuffer(g);
    }

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
