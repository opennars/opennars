package nars.guifx;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import nars.NAR;
import nars.guifx.util.NSliderFX;
import nars.task.Task;
import org.apache.commons.math3.util.Precision;


public class TaskLabel extends BorderPane {

    private final Text label;
    private final Task task;
    private final TaskSummaryIcon summary;
    private final NSliderFX slider;
    private float lastPri = -1;

    public TaskLabel(String prefix, Task task, NAR n) {
        super( );

        this.task = task;

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        task.toString(sb, n.memory, true, false, false);

        label = new Text(sb.toString());

        label.getStyleClass().add("tasklabel_text");
        label.setMouseTransparent(true);
        //label.setCacheHint(CacheHint.SCALE);
        label.setPickOnBounds(false);
        label.setSmooth(false);
        label.setTextAlignment(TextAlignment.LEFT);


        int iconWidth = 50;
        int iconSpacing = 2;

        StackPane icon = new StackPane(
                summary = new TaskSummaryIcon(task, this).width(iconWidth),
                slider = new NSliderFX(iconWidth, 20).set(0, 0, 1)
        );
        icon.autosize();

        setLeft(icon);
        setCenter(label);


        slider.setOpacity(0.5);
        summary.setMouseTransparent(false);

        layout();

        update();

        //label.setCache(true);
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


        float pri = task.getBudget().getPriorityIfNaNThenZero();
        if (Precision.equals(lastPri, pri, 0.025)) {
            return;
        }
        lastPri = pri;

        summary.run();

        double sc = 0.25 + 1 * ( 1 -  pri);
        label.setScaleX(sc);
        label.setScaleY(sc);
        label.setFill(JFX.grayscale.get(pri*0.5+0.5));

        layout();


    }
}
