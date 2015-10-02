package nars.guifx;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import nars.NAR;

/**
 * Created by me on 8/2/15.
 */
public class IOPane extends BorderPane implements FXIconPaneBuilder {


    private final NAR nar;

    public IOPane(NAR nar) {
        super();


        this.nar = nar;

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);

        split.getItems().addAll(NARfx.scrolled(
                new LogPane(nar, "eventCycle", "eventFrame")),
                new InputPane(nar));

        split.setDividerPosition(0,0.85);


        setMinSize(400, 300);
        split.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        setCenter(split);
    }

    @Override
    public Node newIconPane() {
        return new StatusPane(nar);

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
