package nars.guifx;

import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import nars.NAR;
import nars.task.Task;


public class TaskLabel extends HBox {

    private final Text label;
    private final Task task;
    private final TaskSummaryIcon summary;
    //private final NSliderFX slider;

    public TaskLabel(String prefix, Task task, NAR n) {
        super( 1 );

        this.task = task;

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        task.toString(sb, n.memory, true, false, false);

        label = new Text(sb.toString());

        label.setMouseTransparent(true);
        label.setCacheHint(CacheHint.SCALE);


        getChildren().setAll(
                summary = new TaskSummaryIcon(task, this).width(40),
                //slider = new NSliderFX(40, 20).set(0, 0, 1),
                label
        );




        //setAlignment(Pos.CENTER_LEFT);

        //Font ff = NARfx.mono(16.0 + 16.0 * pri);
        /*Font ff = NARfx.mono(16);
        setTranslateX(getWidth());
        setScaleX(1.0 + 0.5 * pri);
        setScaleY(1.0 + 0.5 * pri);
        this.setFont(ff);*/

        //autosize();


        //setCacheHint(CacheHint.SCALE);
        //setCache(true);

        update();

        label.setCache(true);
    }

    public void enablePopupClickHandler(NAR nar) {

        setOnMouseClicked(e -> {
            NARfx.newWindow(nar, task);
//            Term t = task.getTerm();
//            if (t!=null) {
////                Concept c = nar.concept(t);
////                if (c != null) {
////                    NARfx.window(nar, c);
////                }
//
//            }
        });

    }

    public TaskLabel(Task task, NAR nar) {
        this("", task, nar);
    }

    public void update() {

        summary.run();

        float pri = task.getBudget().getPriorityIfNaNThenZero();

        //slider.value.set(pri);


        setAlignment(Pos.BASELINE_LEFT);

        double sc = 0.5 + 2.5 * ( 1 -  pri);
        label.setScaleX(sc);
        label.setScaleY(sc);

        layout();

        label.setFill(JFX.grayscale.get(pri*0.5+0.5));

    }
}
