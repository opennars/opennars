package nars.guifx.chart;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.NARSeed;
import nars.NARStream;
import nars.event.CycleReaction;
import nars.guifx.NARfx;
import nars.nar.Default;
import nars.util.meter.TemporalMetrics;
import nars.util.meter.event.DoubleMeter;

import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * Created by me on 8/12/15.
 */
public class NARui extends NARStream {

    final TemporalMetrics<Double> meter;
    private static final int DEFAULT_HISTORY_SIZE = 2048;

    public NARui(NAR n) {
        super(n);

        meter = new TemporalMetrics(DEFAULT_HISTORY_SIZE);



    }

    public NARui(NARSeed s) {
        this(new NAR(s));

    }

    public NARui then(Consumer<NARStream> e) {
        e.accept(this);
        return this;
    }


    public NARui meter(String id, ToDoubleFunction<NAR> nextCycleValue) {
        DoubleMeter dm = meter.add(new DoubleMeter(id));
        forEachCycle(() -> {
            dm.accept(nextCycleValue.applyAsDouble(nar));
        });
        return this;
    }

    public NARui view(String... signals) {
        if (signals.length == 0) {

            //add all signals, except the first 'key' signal
            signals = meter.getSignals().subList(1, meter.getSignals().size())
                    .stream().map( s -> s.id ).toArray(n -> new String[n]);
        }

        final String[] _signals = signals;

        NARfx.run((a,s) -> {

            s.setScene(new Scene(
                    //new ScrollPane(
                            new VBox( Stream.of(_signals).map(signal ->
                                    lineplot(signal) ).toArray( (n) -> new Pane[n] ) )
                    //)
            ));

            //s.getScene().getStylesheets().setAll(/*NARfx.css,*/ "dark.css");

            s.sizeToScene();

            s.show();

        });
        return this;
    }

    private Pane lineplot(String s) {
        BorderPane b = new BorderPane();

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("t");
        //yAxis.setLabel(s);


        /*
        .chart-series-line {
            -fx-stroke-width: 2px;
            -fx-effect: null;
        }

        .default-color0.chart-series-line { -fx-stroke: #e9967a; }
        .default-color1.chart-series-line { -fx-stroke: #f0e68c; }
        .default-color2.chart-series-line { -fx-stroke: #dda0dd; }
         */
        ObservableList<XYChart.Data<Double,Double>> data = observableArrayList();
        int i = 0;
        for(Double x : meter.doubleArray(s)) {
            data.add(new XYChart.Data(i++, x));
        }

        AreaChart<Double, Double> bc = new AreaChart(xAxis, yAxis);

        XYChart.Series<Double, Double> series = new XYChart.Series<>(s, data);

        bc.getData().setAll( series );
        yAxis.setAutoRanging(true);


        b.setCenter(bc);

        b.autosize();

        return b;
    }

    @Override
    public NARui run(int frames) {

        CycleReaction mc = new CycleReaction(nar) {
            @Override
            public void onCycle() {
                meter.update(nar.time());
            }
        };

        //enable charting immediately before (to be called after all other chart handlers)
        // and disable immediately after the run
        {
            super.run(frames);
        }

        mc.off();

        return this;
    }

    public static void main(String[] args) {

        new NARui(new Default())
                .meter("ConceptPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(true,false,false);
                })
                .meter("TermLinkPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(false,true,false);
                })
                .meter("TaskLinkPriorityMean", (nar) -> {
                    return nar.memory.getActivePriorityPerConcept(false,false,true);
                })
                .then(n -> {
                    n.input("<a-->b>.", "<b-->a>.").run(500);
                })
                .view();

    }
}
