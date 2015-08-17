package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import nars.budget.Itemized;
import nars.link.TaskLink;
import nars.task.Task;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by me on 8/14/15.
 */
public class ItemButton<I extends Itemized> extends Label implements Runnable {

    private final I item;
    private final Function<I, String> labeler;

    public ItemButton(I i, Function<I,String> labeler, Consumer<I> clicked) {
        super();

        this.item = i;
        this.labeler = labeler;

        Task t = null;
        if (i instanceof Task)
            t = (Task)i;
        else if (i instanceof TaskLink)
            t = ((TaskLink)i).getTask();

        if (clicked!=null) {
            setOnMouseClicked(e -> {
                clicked.accept(i);
            });
        }
        if (t!=null)
            setGraphic(new TaskSummaryIcon( t, this));

        run();

        setText(labeler.apply(item));
    }

    @Override
    public void run() {
        //if (!constantLabel() || getText().isEmpty())


        //setFont(NARfx.mono(10 + 8 * item.getPriority()));
        float p = item.getPriority() * 0.5f + 0.5f;
        setTextFill(Color.rgb(255,255,255,p));

        setStyle("-fx-font-size: " + 1 + 2 * item.getPriority());

        Node g = getGraphic();
        if (g instanceof Runnable)
            ((Runnable)g).run();

    }
}
