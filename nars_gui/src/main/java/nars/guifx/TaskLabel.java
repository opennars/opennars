package nars.guifx;

import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import nars.NAR;
import nars.task.Task;

/**
 * Created by me on 8/10/15.
 */
public class TaskLabel extends HBox {

    private final Label label;
    private final Task task;
    private final TaskSummaryIcon summary;
    private final NSliderFX slider;

    public TaskLabel(String prefix, Task task, NAR n) {
        super( );

        this.task = task;

        String s = prefix + task.toString(n.memory).toString();
        label = new Label(s);
        label.setMouseTransparent(true);
        label.getStylesheets().clear();
        label.setCacheShape(true);
        label.setCacheHint(CacheHint.SPEED);
        label.setCache(true);


        getChildren().setAll(
                summary = new TaskSummaryIcon(task, this).width(40),
                slider = new NSliderFX(40, 20).set(0, 0, 1),
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

    }

    public void enablePopupClickHandler(NAR nar) {

        setOnMouseClicked(e -> {
            NARfx.window(nar, task);
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

        slider.value.set(pri);

        label.setStyle(JFX.fontSize(8 + 16 * pri));
        label.setTextFill(JFX.grayscale.get(pri));




    }
}
