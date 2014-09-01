package nars.gui.output;

import com.hipposretribution.controlP5.ControlP5;
import com.hipposretribution.controlP5.drawable.controller.single.chart.Chart;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import nars.util.meter.data.DataSet;
import processing.core.PApplet;

public class PLineChart extends PApplet {

    private final int historySize;
    //private final int lineColor;
    //private final int chartType;
    private boolean autoRanging = true;
    private final DataSet data;
    private final Map<String, Chart> charts = new HashMap();
    
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

        Chart ch = cp5.addChart(f)
                //.setPosition(50, 50)
                //.setSize(200, 200)
                //.setRange(-20, 20)
                .setView(chartType);

        ch.getColour().setBackground(0);
        ch.setCaptionLabel(f);
        ch.setLabelVisible(true);
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
        
        return ch;
    }
    
    int frame = 0;
    
    public void draw() {
        background(0);

    }

//    // Displays a simple line chart representing a time series.
//    XYChart lineChart;
//
//// Loads data into the chart and customises its appearance.
//    @Override
//    public void setup() {
//        noLoop();
//        
//        //size(500, 200);
//        //textFont(createFont("Arial", 10), 10);
//
//        // Both x and y data set here. 
//        lineChart = new XYChart(this);
//        
//        
//        lineChart.setData(data);
//        
//        // Axis formatting and labels.
//        lineChart.showXAxis(true);
//        lineChart.showYAxis(true);
//        //lineChart.setMinY(0);
//
//        //lineChart.setYFormat("$###,###");  // Monetary value in $US
//        //lineChart.setXFormat("0000");      // Year
//
//        // Symbol colours
//        lineChart.setPointColour(color(120, 120, 120, 100));
//        lineChart.setPointSize(0);
//        lineChart.setLineWidth(1);
//        lineChart.setDecorations(redraw);
//        lineChart.setAxisColour(Color.WHITE.getRGB());
//        lineChart.setAxisValuesColour(Color.WHITE.getRGB());
//        lineChart.setAxisLabelColour(Color.WHITE.getRGB());
//        lineChart.setLineColour(lineColor);
//    }
    protected void addPoint(String f, Chart ch, float y) {

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
        }

        //if (lineChart == null) return;
        //lineChart.setData(data);
    }
//    
//// Draws the chart and a title.
//    @Override
//    public void draw() {
//        background(0);
//        textSize(9);
//        
//        fill(240f);
//        lineChart.draw(15, 15, width - 30, height - 30);
//
//        // Draw a title over the top of the chart.
//        
//        textSize(16);        
//        text(title, 40, 30);
//        /*textSize(11);
//        text("Gross domestic product measured in inflation-corrected $US",
//                70, 45);*/
//        
//    }

    public JPanel newPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(this, BorderLayout.CENTER);
        init();
        return p;
    }

    public void update() {

        int numCharts = data.size();
        int y = 0;
        
        for (String f : data.keySet()) {
            Chart ch = charts.get(f);
            if (ch==null) {
                ch = addChart(f);                
            }
            
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
            
            int h = (int)(getHeight()/numCharts);
            ch.setSize(getWidth(), h);
            ch.setPosition(0, y);
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
