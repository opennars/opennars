/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.reinforcementlearning;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.DiffableFunctionGenerator;
import jurls.core.approximation.DiffableFunctionMarshaller;
import jurls.core.approximation.Generator;
import jurls.core.approximation.InputNormalizer;
import jurls.core.approximation.OutputNormalizer;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.reinforcementlearning.ActionSelector;
import jurls.core.reinforcementlearning.EpsilonGreedyActionSelector;
import jurls.core.reinforcementlearning.QUpdateProcedure;
import jurls.core.reinforcementlearning.RLAgentMarshaller;
import jurls.core.reinforcementlearning.RLParameters;
import jurls.core.reinforcementlearning.UpdateProcedure;

/**
 *
 * @author me
 */
public class Follow1D extends Application implements Runnable {

    public static double dist(double a, double b) {
        double d = 1.0 / (1.0 + Math.abs(a - b));
        return d * d; //sharper than just 'd'
    }

    private final int history = 128;
    private final ArrayDeque<Double> positions = new ArrayDeque(history);
    private final ArrayDeque<Double> targets = new ArrayDeque(history);
    private XYChart.Series positonSeries;
    private final long updatePeriodMS = 20;
    private XYChart.Series targetSeries;
    private final ApproxParameters approxParameters = new ApproxParameters(0.0001, 0.1);
    private final RLParameters rLParameters = new RLParameters(0.9, 0.9);
    private final EpsilonGreedyActionSelector.Parameters asParameters = new EpsilonGreedyActionSelector.Parameters(0.1);

    public void run() {
        double CHANGE_PROBABILITY = 0.01;
        int size = 4;
        float velocity = 0.1f;
        float target = 0;
        float position = 0;
        int cycles = 15000;
        boolean wrap = false;

        double maxQ = -Double.MAX_VALUE;
        double minQ = Double.MAX_VALUE;

        double[] vision = new double[size * 2];

        DiffableFunctionGenerator dfg = Generator.generateFourierBasis();
        //DiffableFunction df = Generator.generateRBFNet(xs, ps, 16);
        //df = Generator.generateFFNN(xs, ps, 5);

        UpdateProcedure up = new QUpdateProcedure();
        //UpdateProcedure up = new SARSAUpdateProcedure();

        ActionSelector as = new EpsilonGreedyActionSelector(asParameters);

        ParameterizedFunction f
                = new OutputNormalizer(
                        new InputNormalizer(
                                new DiffableFunctionMarshaller(dfg, vision.length + 1, 5)
                        )
                );

        RLAgentMarshaller h = new RLAgentMarshaller();
        h.reset(f, up, as, vision, approxParameters, rLParameters, 3);

        //RLAgent h = new RlAgent(FunctionMarshaller functionMarshaller, UpdateProcedure updateProcedure, int numActions, double lowerAction, double upperAction, double alpha, double gamma, double lambda, double epsilon, double momentum,double[] s0);
        int nextAction = -1;

        for (int i = 0; i < cycles; i++) {

            double reward = dist(target, position);

            Arrays.fill(vision, 0);
            for (int j = 0; j < size; j++) {
                vision[j] = dist(target, j); //target position in 1st half of vision
                vision[j + size] = dist(position, j); //current position in 2nd half of vision
            }

            //System.out.println(Arrays.toString(vision) + " = " + reward + " <- " + nextAction);
            h.getRLAgent().learn(vision, reward);

            double chooseAction = h.getRLAgent().chooseAction();

            nextAction = (int) chooseAction;

            if (nextAction == 0) {
                position += velocity;
            } else if (nextAction == 1) {
                position -= velocity;
            } else {
                //nothing
            }

            if (position < 0) {
                position = wrap ? size - 1 : 0;
            }
            if (position >= size) {
                position = wrap ? 0 : size - 1;
            }

            if (Math.random() < CHANGE_PROBABILITY) {
                target += Math.random() > 0.5 ? -velocity : +velocity;
                if (target < 0) {
                    target = wrap ? size - 1 : 0;
                }
                if (target >= size) {
                    target = wrap ? 0 : size - 1;
                }
            }

            final boolean overflow;
            if (positions.size() == history) {
                positions.removeFirst();
                targets.removeFirst();
                overflow = true;
            } else {
                overflow = false;
            }

            positions.add((double) position);
            targets.add((double) target);

            Platform.runLater(new Runnable() {

                List pss = new ArrayList(history);
                List tss = new ArrayList(history);

                @Override
                public void run() {

                    positonSeries.getData().clear();
                    targetSeries.getData().clear();
                    pss.clear();
                    tss.clear();

                    Iterator<Double> pdi = positions.descendingIterator();
                    Iterator<Double> tdi = targets.descendingIterator();

                    for (int i = 0; i < history && pdi.hasNext(); i++) {
                        double pv = pdi.next();
                        double tv = tdi.next();
                        pss.add(new XYChart.Data(i, pv));
                        tss.add(new XYChart.Data(i, tv));
                    }

                    positonSeries.getData().addAll(pss);
                    targetSeries.getData().addAll(tss);

                }

            });

            try {
                Thread.sleep(updatePeriodMS);
            } catch (InterruptedException ex) {
                Logger.getLogger(Follow1D.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("QLearning Test 1D");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        //creating the chart
        final LineChart<Number, Number> lineChart
                = new LineChart<Number, Number>(xAxis, yAxis);

        lineChart.setAnimated(false);
        lineChart.getXAxis().setAutoRanging(true);
        lineChart.setCreateSymbols(false);
        lineChart.setShape(null);

        //defining a series
        positonSeries = new XYChart.Series();
        positonSeries.setName("Position");
        targetSeries = new XYChart.Series();
        targetSeries.setName("Target");

        //populating the series with data
//        series.getData().add(new XYChart.Data(1, 23));
//        series.getData().add(new XYChart.Data(2, 14));
//        series.getData().add(new XYChart.Data(3, 15));
//        series.getData().add(new XYChart.Data(4, 24));
//        series.getData().add(new XYChart.Data(5, 34));
//        series.getData().add(new XYChart.Data(6, 36));
//        series.getData().add(new XYChart.Data(7, 22));
//        series.getData().add(new XYChart.Data(8, 45));
//        series.getData().add(new XYChart.Data(9, 43));
//        series.getData().add(new XYChart.Data(10, 17));
//        series.getData().add(new XYChart.Data(11, 29));
//        series.getData().add(new XYChart.Data(12, 25));
        Scene scene = new Scene(lineChart, 800, 600);
        lineChart.getData().add(positonSeries);
        lineChart.getData().add(targetSeries);

        stage.setScene(scene);
        stage.show();

        new Thread(this).start();
    }

}
