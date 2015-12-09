/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.guifx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

abstract public class LinePlotJFX extends Application {




    @Override public void start(Stage stage) {
        stage.setTitle("?");

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("X");
        final LineChart<Number,Number> lineChart = new LineChart(xAxis,yAxis);

        Scene scene  = new Scene(lineChart, 800, 600);
        XYChart.Series[] series = initChart();
        for (XYChart.Series s : series)
            lineChart.getData().add(s);
       
        stage.setScene(scene);
        stage.show();
    }

    abstract public XYChart.Series[] initChart();


    public static void main(String[] args) {

        launch(args);
    }

}