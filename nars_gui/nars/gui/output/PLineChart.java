package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import nars.util.NARState;
import org.gicentre.utils.stat.XYChart;
import processing.core.PApplet;
import processing.core.PVector;


public class PLineChart extends PApplet {
    private final String title;
    private List<PVector> data;
    private final int historySize;
    private final int lineColor;

    public PLineChart(String title, int historySize) {
        super();
        this.title = title;
        this.historySize = historySize;
        this.lineColor = papplet.getColor(title);
        data = new ArrayList<PVector>();
    }


    // Displays a simple line chart representing a time series.
    XYChart lineChart;

// Loads data into the chart and customises its appearance.
    @Override
    public void setup() {
        noLoop();
        
        //size(500, 200);
        //textFont(createFont("Arial", 10), 10);

        // Both x and y data set here. 
        lineChart = new XYChart(this);
        
        
        lineChart.setData(data);
        
        // Axis formatting and labels.
        lineChart.showXAxis(true);
        lineChart.showYAxis(true);
        //lineChart.setMinY(0);

        //lineChart.setYFormat("$###,###");  // Monetary value in $US
        //lineChart.setXFormat("0000");      // Year

        // Symbol colours
        lineChart.setPointColour(color(120, 120, 120, 100));
        lineChart.setPointSize(0);
        lineChart.setLineWidth(3);
        lineChart.setAxisColour(Color.WHITE.getRGB());
        lineChart.setAxisValuesColour(Color.WHITE.getRGB());
        lineChart.setAxisLabelColour(Color.WHITE.getRGB());
        lineChart.setLineColour(lineColor);
    }

    public void addPoint(float x, float y) {
        
        data.add(new PVector(x, y));
        if (data.size() > historySize)
            data = data.subList(Math.max(0,data.size() - historySize), data.size()-1);
        
        if (lineChart == null) return;
        
        lineChart.setData(data);
        
        redraw();
    }
    
// Draws the chart and a title.
    @Override
    public void draw() {
        background(0);
        textSize(9);
        
        fill(240f);
        lineChart.draw(15, 15, width - 30, height - 30);

        // Draw a title over the top of the chart.
        
        textSize(16);        
        text(title, 40, 30);
        /*textSize(11);
        text("Gross domestic product measured in inflation-corrected $US",
                70, 45);*/
        
    }

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
