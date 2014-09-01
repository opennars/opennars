package nars.gui.output.chart;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import nars.gui.NARSwing;
import nars.util.meter.data.DataSet;

public class ChartsPanel extends Canvas {

    
    //float motionBlur = 0.9f;
    private final int historySize;
    //private final int lineColor;
    //private final int chartType;
    private boolean autoRanging = true;
    private final DataSet data;
    final Map<String, TimeSeriesChart> charts = new HashMap();
    
    //private final Map<String, JToggleButton> enable = new HashMap();
    private final Font monofontLarge = NARSwing.monofont.deriveFont(Font.PLAIN, 18f);
    private final Font monofontSmall = NARSwing.monofont.deriveFont(Font.PLAIN, 14f);
    
    float yScale = 1.0f;
    private int yOffset = 0;
    private int[] xPoints;
    private int[] yPoints;
    private RenderingHints renderHints;
    
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
                
                update(false);
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
                update(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startLocation = null;
            }
            
        });
        addMouseListener(c);
        
    
    }
    

    
    protected TimeSeriesChart addChart(String f) {
        //int chartType = TimeSeriesChart.AREA;

        TimeSeriesChart ch = new TimeSeriesChart(f, NARSwing.getColor(f), historySize);
        charts.put(f, ch);
        
        
        return ch;
    }
    
    BufferedImage image = null;
    
    private boolean updateDoubleBuffer()     {
        int w = getWidth();
        int h = getHeight();
        if ((w == 0) || (h == 0))
            return false;
        
            // obtain the current system graphical settings
            GraphicsConfiguration gfx_config = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().getDefaultScreenDevice().
                    getDefaultConfiguration();
            
            /*
             * if image is already compatible and optimized for current system 
             * settings, simply return it
             */
            if ((image!=null) && (image.getColorModel().equals(gfx_config.getColorModel())) && (image.getWidth()==w) && (image.getHeight()==h)) {
                    //use existing image
            }
            else {
                image = gfx_config.createCompatibleImage(w, h);
            }
            
            return true;
    }

    Color backgroundClearColor = new Color(0,0,0,0.1f);
    
    public void update(final boolean addNextPoint) {
	
        //TODO allow buffering input data points while not visible
        
        if (!updateDoubleBuffer()) 
            return;
        
	Graphics2D g = (Graphics2D)image.getGraphics();
        
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
        
        if (xPoints == null) {
            xPoints = new int[historySize+2];
            yPoints = new int[historySize+2];
        }
        
        float xp = getWidth();
        float dx = (float)Math.ceil(((float)getWidth()) / ((float)historySize));
        for (int i = 0; i < historySize; i++) {            
            xPoints[i+1] = (int)Math.round(xp);
            xp -= dx;
        }
        xPoints[0] = xPoints[1];
        xPoints[xPoints.length-1] = xPoints[xPoints.length-2] = 0;
        
        for (final String f : data.keySet()) {
            if (y + h < 0) {
                y+=h;
                continue;
            }
            
            TimeSeriesChart ch = charts.get(f);            
                        
            if (ch==null) {
                ch = addChart(f);            
            }
            
            if (addNextPoint) {
                Object value = data.get(f);

                if (value instanceof Double) {                    
                    ch.push(((Number) value).doubleValue());
                }
                if (value instanceof Float) {
                    ch.push(((Number) value).doubleValue());
                }
                if (value instanceof Integer) {
                    ch.push(((Number) value).doubleValue());
                }
            }            
        
            g.setPaint(ch.getColor());
            
            
            double min = ch.min;
            double max = ch.max;
            if (!Double.isFinite(min)) min = 0;
            if (!Double.isFinite(max)) max = min;
                    
            double range = max - min;
            
            if (range == 0) {
                range = 1;
                max += 0.5;
                min -= 0.5;
            }
            
            int n = 0;
            double firstValue = 0;
            for (Double d : ch) {
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

            
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            
            g.setPaint(Color.WHITE);
            
            g.setFont(monofontLarge);
            g.drawString(f, 0, y+25);
            if (h > 25) {
                g.setFont(monofontSmall);
                g.drawString("  current=" + firstValue + ", min=" + min + ", max=" + max, 0, y+20+18);
            }
            
            y += h;
        
            if (y >= height) break;
             
        }

        Graphics localGraphics = getGraphics();
        localGraphics.drawImage(image, 0, 0, null);
        
        g.dispose();
        localGraphics.dispose();

 
        //Tell the System to do the Drawing now, otherwise it can take a few extra ms until 
        //Drawing is done which looks very jerky
        //Toolkit.getDefaultToolkit().sync();	
        
        //repaint();
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
