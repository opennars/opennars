package nars.gui.output;

import com.hipposretribution.controlP5.ControlP5;
import com.hipposretribution.controlP5.drawable.controller.single.chart.Chart;
import com.hipposretribution.controlP5.drawable.controller.single.text.TextLabel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import nars.gui.NARSwing;
import nars.util.meter.data.DataSet;
import processing.core.PApplet;
import processing.core.PFont;

public class PLineChart extends PApplet {

    //float motionBlur = 0.9f;
    private final int historySize;
    //private final int lineColor;
    //private final int chartType;
    private boolean autoRanging = true;
    private final DataSet data;
    final Map<String, Chart> charts = new HashMap();
    final Map<String, TextLabel> labels = new HashMap();
    //private final Map<String, JToggleButton> enable = new HashMap();
    private final PFont monofont = new PFont(NARSwing.monofont.deriveFont(Font.PLAIN, 16f), true);
    float yScale = 1.0f;
    private int yOffset = 0;
    
    /**
     *
     * @param title
     * @param historySize
     * @param chartType use Chart.BAR, Chart.LINE, Chart.PIE, Chart.AREA,
     * Chart.BAR_CENTERED
     */
    public PLineChart(DataSet data, int historySize) {
        super();
        this.data = data;
        this.historySize = historySize;
        //this.lineColor = papplet.getColor(title);
        //this.chartType = chartType;
    }

    private static final long serialVersionUID = -1260688258272923431L;

    ControlP5 cp5;

    public void setup() {
        noLoop();
        //smooth();
        
        cp5 = new ControlP5(this);
        

        for (String f : data.keySet()) {
            addChart(f);
        }

        background(0);

    }

    protected Chart addChart(String f) {
        int chartType = Chart.AREA;

        Chart ch = cp5.addChart(f+".chart")
                //.setPosition(50, 50)
                //.setSize(200, 200)
                //.setRange(-20, 20)      
                
                .setView(chartType);

        
        TextLabel lh = cp5.addTextlabel(f+".label", f).setFont(monofont).setColourCaption(color(255f, 230f));
        
        ch.getColour().setBackground(0);
        //ch.setCaptionLabel(f);
        //ch.setLabelVisible(true);
        ch.addDataSet(f);
        

        int lineColor = papplet.getColor(f);

        if (chartType == Chart.LINE) {
            ch.setStrokeWeight(2f);
            ch.setColors(f, lineColor);
        } else if (chartType == Chart.BAR) {
            ch.setColors(f, color(0.75f), lineColor);
        } else {
            ch.setColors(f, lineColor);
        }

        ch.setData(f, new float[historySize]);

        charts.put(f, ch);
        labels.put(f, lh);
        
        return ch;
    }
    
    int frame = 0;

    @Override
    public void draw() {
        //fill(0, (1f - motionBlur)*255f  );
        //rect(0,0,getWidth(),getHeight());
        
        background(0);
        
        //dont call super.draw() otherwise p5 will not draw
    }

    
    
    

    protected void addPoint(String f, Chart ch, float y) {

        if (ch.size() >= historySize)
            ch.removeData(0);

        // push: add data from right to left (last in)
        ch.push(f, y);

        if (autoRanging) {
            float[] a = ch.getValuesFrom(f);
            
            float min = a[0];
            float max = a[0];
            for (int i = 1; i < a.length; i++) {
                float x = a[i];
                if (x < min) {
                    min = ( x );
                }
                if (x > max) {
                    max = ( x );
                }                   
            }
            
            if (min == max) { max += 1f; }
            
            ch.setRange(min, max);
            
   
            TextLabel lh = labels.get(f);
            if (lh!=null) {
                lh.setText(f + "\n current=" + y + ", min=" + min + ", max=" + max);
            }
                        
        }

    }

    public JPanel newPanel() {
        JPanel p = new JPanel(new BorderLayout());
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
        
        p.add(this, BorderLayout.CENTER);
        init();
        return p;
    }

    public void update(boolean addNextPoint) {

        int numCharts = data.size();
        int y = yOffset;
        
        int h = (int)(yScale * ((float)getHeight()) / ((float)numCharts));
        
        for (String f : data.keySet()) {
            Chart ch = charts.get(f);            
            TextLabel lh = labels.get(f);
            
            if (ch==null) {
                ch = addChart(f);                
            }
            
            if (addNextPoint) {
                Object value = data.get(f);

                if (value instanceof Double) {
                    addPoint(f, ch, ((Number) value).floatValue());
                }
                if (value instanceof Float) {
                    addPoint(f, ch, ((Number) value).floatValue());
                }
                if (value instanceof Integer) {
                    addPoint(f, ch, ((Number) value).floatValue());
                }
            }            
            
            ch.setSize(getWidth(), h);
            ch.setPosition(0, y);            
         
            if (lh!=null) {
                lh.setPosition(0, y);
            }
            
            y += h;
        
             
        }

        redraw();
        frame++;

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
