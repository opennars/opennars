package nars.gui.output;

import com.hipposretribution.controlP5.ControlP5;
import com.hipposretribution.controlP5.drawable.controller.single.chart.Chart;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import nars.util.NARState;
import processing.core.PApplet;


public class PLineChart extends PApplet {
    private final String title;
    private final int historySize;
    private final int lineColor;
    private final int chartType;
    private float minValue = 0;
    private float maxValue = 1;
    private boolean autoRanging = true;

    /**
     * 
     * @param title
     * @param historySize
     * @param chartType  use Chart.BAR, Chart.LINE, Chart.PIE, Chart.AREA, Chart.BAR_CENTERED
     */
    public PLineChart(String title, int historySize, int chartType) {
        super();
        this.title = title;
        this.historySize = historySize;
        this.lineColor = papplet.getColor(title);
        this.chartType = chartType;
    }


	private static final long serialVersionUID = -1260688258272923431L;

	ControlP5 cp5;

	Chart chart;

	public void setup() {
	  noLoop();
	  //smooth();
	  cp5 = new ControlP5(this);
	  chart = cp5.addChart("_a")
	               //.setPosition(50, 50)
	               //.setSize(200, 200)
	               //.setRange(-20, 20)
	               .setView(chartType);

	  chart.getColour().setBackground(0);
          chart.setCaptionLabel(title);
          chart.setLabelVisible(true);


	  chart.addDataSet("a");
          
          
          if (chartType == Chart.LINE) {
              chart.setStrokeWeight(2f);
              chart.setColors("a", lineColor);
          }
          else if (chartType == Chart.BAR) {              
              chart.setColors("a", color(0.75f), lineColor);
          }
          else {
              chart.setColors("a", lineColor);              
          }
	  chart.setData("a", new float[historySize]);
          
          
          background(0);
          
	}


	public void draw() {
            chart.setSize(getWidth(), getHeight());
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

    public void addPoint(float x, float y) {
        

        // push: add data from right to left (last in)
	chart.push("a", y);
        
        if (autoRanging) {
            if (y < minValue) minValue = y;
            if (y > maxValue) maxValue = y;
            chart.setRange(minValue, maxValue);
        }
        
        //if (lineChart == null) return;
        
        //lineChart.setData(data);
        
        redraw();
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
    

    public void update(NARState state) {
        try {
        Map.Entry<Long, HashMap<String, Object>> entry = state.lastEntry();
        long when = entry.getKey();
        HashMap<String, Object> data = entry.getValue();
        
        Object value = data.get(title);
        if (value instanceof Double)
           addPoint(when, ((Number)value).floatValue());
        if (value instanceof Float)
           addPoint(when, ((Number)value).floatValue());
        if (value instanceof Integer)
           addPoint(when, ((Number)value).floatValue());
        }
        catch(Exception ex) {}
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
