/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.utils;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 *
 * @author me
 */
public class LineCharts extends JFXPanel {

    int historyLength;
    private LineChart<Number, Number> lineChart;
    private NumberAxis yAxis;
    private NumberAxis xAxis;
    private XYChart.Series[] series;
    private int numSeries;
    private final String yAxisLabel;

    public LineCharts(String yAxis, int numSeries, int historyLength) {
        this.numSeries = numSeries;
        this.historyLength = historyLength;
        yAxisLabel = yAxis;

        if (numSeries != -1) {
            update();
        }
    }

    protected void update() {
        Platform.runLater(() -> setScene(scene()));
    }

    public void setSeries(int numSeries) {
        this.numSeries = numSeries;
        update();
    }

    public void updateReward(double t, double[] r) {
        if (series == null || series.length!=r.length) {            
            setSeries(r.length);
            return;
        }
        
        for (int l = 0; l < numSeries; l++) {
            if (series[l] == null || series[l].getData() == null)
                return;
            series[l].getData().add(new XYChart.Data(t, r[l]));

            while (series[l].getData().size() >= historyLength) {
                series[l].getData().remove(0);
            }
        }

    }

    protected Scene scene() {

        //defining the axes
        xAxis = new NumberAxis();
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel(yAxisLabel);
        yAxis.setForceZeroInRange(false);
        lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setAnimated(false);

        lineChart.getXAxis().setAutoRanging(true);
        lineChart.getYAxis().setAutoRanging(true);
        lineChart.setCreateSymbols(false);
        lineChart.setShape(null);
        lineChart.setLegendVisible(false);

        series = new XYChart.Series[numSeries];
        for (int i = 0; i < numSeries; i++) {            
            lineChart.getData().add(series[i] = new XYChart.Series());
        }

        Scene scene = new Scene(lineChart);

        return (scene);
    }

}
