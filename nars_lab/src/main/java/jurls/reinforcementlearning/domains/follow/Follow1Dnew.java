///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.reinforcementlearning.domains;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.scene.Scene;
//import javafx.scene.chart.LineChart;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.XYChart;
//import javafx.stage.Stage;
//import jurls.core.approximation.ApproxParameters;
//import jurls.core.approximation.DiffableFunctionGenerator;
//import jurls.core.approximation.DiffableFunctionMarshaller;
//import jurls.core.approximation.Generator;
//import jurls.core.approximation.InputNormalizer;
//import jurls.core.approximation.OutputNormalizer;
//import jurls.core.approximation.ParameterizedFunction;
//import jurls.core.reinforcementlearning.ActionSelector;
//import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
//import jurls.core.reinforcementlearning.QUpdateProcedure;
//import jurls.core.reinforcementlearning.RLParameters;
//import jurls.core.reinforcementlearning.UpdateProcedure;
//
///**
// *
// * @author me
// */
//public class Follow1Dnew extends Application implements Runnable {
//
//    public static double dist(double a, double b) {
//        double d = 1.0 / (1.0 + Math.abs(a - b));
//        return d; // * d; //sharper than just 'd'
//    }
//
//    private final int history = 128;
//    private final ArrayDeque<Double> positions = new ArrayDeque(history);
//    private final ArrayDeque<Double> targets = new ArrayDeque(history);
//    private XYChart.Series positonSeries;
//    private final long updatePeriodMS = 3;
//    private XYChart.Series targetSeries;
//    protected final double maxPos = 10;
//    protected final double minPos = 0;
//    protected double myPos = 5;
//    protected double targetPos = 5;
//    private double myV = 0;
//    private double targetV = 0;
//    private int iterations = 0;
//    private final ApproxParameters approxParameters = new ApproxParameters(0.01, 0.1);
//    private final RLParameters rLParameters = new RLParameters(0.9, 0.9);
//    //private final EpsilonGreedyActionSelector.Parameters asParameters = new EpsilonGreedyActionSelector.Parameters(0.1);
//
//    private double reward() {
//        //return 10 - Math.abs(myPos - targetPos);
//        return dist(myPos, targetPos);
//    }
//
//    public double[] observe() {
//        return new double[]{myPos, targetPos};
//    }
//
//    @Override
//    public void run() {
//        int cycles = 15000;
//
//        int features = 16;
//
//        long t0 = System.currentTimeMillis();
//
//        DiffableFunctionGenerator dfg = Generator.generateFourierBasis(features);
//        UpdateProcedure up = new QUpdateProcedure();
//        ActionSelector as = new EpsilonGreedyActionSelector();
//
//        ParameterizedFunction f
//                = new OutputNormalizer(
//                        new InputNormalizer(
//                                new DiffableFunctionMarshaller(dfg, observe().length + 1)
//                        )
//                );
//
//        RLAgentMarshaller h = new RLAgentMarshaller();
//        h.reset(f, up, as, observe(), approxParameters, rLParameters, 2);
//
//        while (true) {
//            for (int i = 0; i < cycles; i++) {
//                iterations++;
//
//                h.getRLAgent().learn(observe(), reward());
//                double chooseAction = h.getRLAgent().chooseAction() - 0.5;
//
//                myV = chooseAction * 0.2;
//                targetV = getTarget(targetV, i);
//
//                myPos += myV;
//                if (myPos > maxPos) {
//                    myPos = maxPos;
//                    myV = 0;
//                }
//                if (myPos < minPos) {
//                    myPos = minPos;
//                    myV = 0;
//                }
//
//                targetPos = targetV;
//                if (targetPos > maxPos) {
//                    targetPos = maxPos;
//                    targetV = 0;
//                }
//                if (targetPos < minPos) {
//                    targetPos = minPos;
//                    targetV = 0;
//                }
//
//                positions.add(myPos);
//                targets.add(targetPos);
//
//                long t1 = System.currentTimeMillis();
//                if (t1 - t0 > 100) {
//                    t0 = t1;
//
//                    final ArrayDeque<Double> p2 = new ArrayDeque<>(positions);
//                    final ArrayDeque<Double> t2 = new ArrayDeque<>(targets);
//
//                    Platform.runLater(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            positonSeries.setName("Position (" + iterations + ")");
//
//                            positonSeries.getData().clear();
//                            targetSeries.getData().clear();
//                            List pss = new ArrayList(history);
//                            List tss = new ArrayList(history);
//                            pss.clear();
//                            tss.clear();
//
//                            Iterator<Double> pdi = p2.descendingIterator();
//                            Iterator<Double> tdi = t2.descendingIterator();
//
//                            for (int j = 0; j < history && pdi.hasNext(); j++) {
//                                double pv = pdi.next();
//                                double tv = tdi.next();
//                                pss.add(new XYChart.Data(j, pv));
//                                tss.add(new XYChart.Data(j, tv));
//                            }
//
//                            positonSeries.getData().addAll(pss);
//                            targetSeries.getData().addAll(tss);
//
//                        }
//
//                    });
//                }
//
//                try {
//                    Thread.sleep(updatePeriodMS);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(Follow1Dnew.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage stage) {
//        stage.setTitle("QLearning Test 1D");
//        //defining the axes
//        final NumberAxis xAxis = new NumberAxis();
//        final NumberAxis yAxis = new NumberAxis(minPos, maxPos, 1);
//        xAxis.setLabel("Time");
//        //creating the chart
//        final LineChart<Number, Number> lineChart
//                = new LineChart<Number, Number>(xAxis, yAxis);
//
//        lineChart.setAnimated(false);
//        lineChart.getXAxis().setAutoRanging(true);
//        lineChart.setCreateSymbols(false);
//        lineChart.setShape(null);
//
//        //defining a series
//        positonSeries = new XYChart.Series();
//        positonSeries.setName("Position");
//        targetSeries = new XYChart.Series();
//        targetSeries.setName("Target");
//
//        //populating the series with data
////        series.getData().add(new XYChart.Data(1, 23));
////        series.getData().add(new XYChart.Data(2, 14));
////        series.getData().add(new XYChart.Data(3, 15));
////        series.getData().add(new XYChart.Data(4, 24));
////        series.getData().add(new XYChart.Data(5, 34));
////        series.getData().add(new XYChart.Data(6, 36));
////        series.getData().add(new XYChart.Data(7, 22));
////        series.getData().add(new XYChart.Data(8, 45));
////        series.getData().add(new XYChart.Data(9, 43));
////        series.getData().add(new XYChart.Data(10, 17));
////        series.getData().add(new XYChart.Data(11, 29));
////        series.getData().add(new XYChart.Data(12, 25));
//        Scene scene = new Scene(lineChart, 800, 600);
//        lineChart.getData().add(positonSeries);
//        lineChart.getData().add(targetSeries);
//
//        stage.setScene(scene);
//        stage.show();
//
//        new Thread(this).start();
//    }
//
//    protected double getTarget(double currentValue, int cycle) {
//        return currentValue + (Math.random() - 0.5) * 0.5;
//    }
//
// }
