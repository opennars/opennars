package nars.guifx.chart;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.util.meter.TemporalMetrics;
import nars.util.meter.event.ObjectMeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * Created by me on 8/12/15.
 */
public class NARui {

    final List<TemporalMetrics<Double>> metrics = new ArrayList();
    private static final int DEFAULT_HISTORY_SIZE = 2048;
    public final NAR nar;


    public NARui(NAR s) {
        nar = s;

    }

    public NARui then(Consumer<NAR> e) {
        e.accept(nar);
        return this;
    }


    @FunctionalInterface public interface MetricsCollector<X> {
        MetricsCollector<X> set(String signal, X value);
    }
    public interface CollectNARMetrics<X> {
        void eachFrame(MetricsCollector<X> c, NAR n);
    }

    public <X> NARui meter(CollectNARMetrics<X> eachFrame) {
        TemporalMetrics meter = new TemporalMetrics(DEFAULT_HISTORY_SIZE);
        metrics.add(meter);

        MetricsCollector<X> mc = new MetricsCollector<X>() {

            Map<String, ObjectMeter<X>> m = new HashMap();

            @Override
            public MetricsCollector<X> set(String signal, X value) {
                ObjectMeter<X> s = m.get(signal);
                if (s == null) {
                    s = new ObjectMeter<>(signal);
                    meter.add(s);
                    m.put(signal, s);
                }
                s.set(value);
                return this;
            }
        };
        nar.onEachFrame((n) -> {
            eachFrame.eachFrame(mc, nar);
            meter.update(nar.time());
        });
        return this;
    }

    public NARui viewAll(Consumer<Pane> result) {

        /*NARfx.run((a, s) -> {
*/

            VBox v = new VBox();

            metrics.forEach(meter -> v.getChildren().add(linePlot(meter)));

            v.layout();

        result.accept(v);
        return this;

            //v.setMaxSize(MAX_VALUE, MAX_VALUE);

  /*          s.setScene(new Scene(NARfx.scrolled(v), 800, 600));

            //s.sizeToScene();

            s.show();

        });

        return this;*/
    }

    private static Node linePlot(TemporalMetrics<Double> meter) {
        //BorderPane b = new BorderPane();

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

        String[] signals = meter.getSignals().subList(1, meter.getSignals().size())
                .stream().map(s -> s.id).toArray(String[]::new);


        String[] _signals = signals;

        LineChart<Double, Double> bc = new LineChart(xAxis, yAxis);


        bc.setData( observableArrayList(
                Stream.of(_signals).map(s -> series(meter, s)).collect(Collectors.toList())
        ) );

        yAxis.setAutoRanging(true);
        bc.setCreateSymbols(false);
        bc.setHorizontalGridLinesVisible(true);
        bc.setVerticalGridLinesVisible(true);


        bc.setMinHeight(450);
        bc.setMinWidth(bc.getData().size());

        //bc.setCenter(bc);

        bc.autosize();

        return bc;

    }

    //http://tiwulfx.panemu.com/2013/01/07/provide-more-colors-for-chart-series/

    private static XYChart.Series<Double,Double> series(TemporalMetrics<Double> meter, String s) {

        ObservableList<XYChart.Data<Double,Double>> data = observableArrayList();
        int i = 0;
        for(Double x : meter.doubleArray(s)) {
            data.add(new XYChart.Data(i++, x));
        }
        XYChart.Series<Double, Double> series = new XYChart.Series<>(s, data);
        series.setName(s);

        series.setNode(null);

        return series;
    }

    public NARui run(int frames) {

        //enable charting immediately before (to be called after all other chart handlers)
        // and disable immediately after the run

        nar.frame(frames);

        return this;
    }

}
