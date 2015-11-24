package nars.guifx;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import nars.NAR;
import nars.guifx.util.NSlider;

/**
 * Created by me on 8/2/15.
 */
public class IOPane extends BorderPane implements FXIconPaneBuilder {


    NSlider vs = new NSlider("Volume", 100, 45, NSlider.BarSlider, 0.0);
    DoubleProperty volume = vs.value[0];

    private final NAR nar;

    public IOPane(NAR nar) {
        super();


        this.nar = nar;

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);

        split.getItems().addAll(
                new TracePane(nar, volume),
                new InputPane(nar));

        split.setDividerPosition(0,0.85);


        setMinSize(400, 300);
        split.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        setCenter(split);
    }

    @Override
    public Node newIconPane() {


        BorderPane b = new BorderPane();
        b.setBottom(vs);
        b.setCenter(new StatusPane(nar, 384));
        return b;

        //return new NSlider(150, 60);

        /*return new HBox(
            new LinePlot(
                    "# Concepts",
                    () -> nar.concepts().size(),
                    300,
                    200,200
            ),
            new LinePlot(
                    "Memory",
                    () -> Runtime.getRuntime().freeMemory(),
                    300,
                    200,200
            )
        );*/
    }
}
