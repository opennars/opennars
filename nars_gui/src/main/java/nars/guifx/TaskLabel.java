package nars.guifx;

import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import nars.NAR;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Term;

/**
 * Created by me on 8/10/15.
 */
public class TaskLabel extends Label {

    private final Task task;
    private final NAR nar;

    public TaskLabel(String prefix, Task task, NAR n) {
        super( );

        this.nar = n;
        this.task = task;

        String s = prefix + task.toString(n.memory).toString();
        setText(s);

        float pri = task.getPriority();
        this.setTextFill(Color.hsb(360.0 * pri, 0.75f, 0.85f));


        setAlignment(Pos.CENTER_LEFT);

        //Font ff = NARfx.mono(16.0 + 16.0 * pri);
        /*Font ff = NARfx.mono(16);
        setTranslateX(getWidth());
        setScaleX(1.0 + 0.5 * pri);
        setScaleY(1.0 + 0.5 * pri);
        this.setFont(ff);*/

        autosize();


        setCacheHint(CacheHint.SCALE);
        setCache(true);

    }

    public void enablePopupClickHandler() {

        setOnMouseClicked(e -> {
            Term t = task.getTerm();
            if (t!=null) {
                Concept c = nar.concept(t);
                if (c != null) {
                    NARfx.window(nar, c);
                }
            }
        });

    }

    public TaskLabel(Task task, NAR nar) {
        this("", task, nar);
    }

}
