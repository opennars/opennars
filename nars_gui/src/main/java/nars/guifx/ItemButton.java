package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.Button;
import nars.budget.Item;
import nars.link.TaskLink;
import nars.task.Task;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by me on 8/14/15.
 */
public class ItemButton<I extends Item> extends Button implements Runnable {

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
    }

    @Override
    public void run() {
        Node g = getGraphic();
        if (g instanceof Runnable)
            ((Runnable)g).run();

        setText(labeler.apply(item));
        setFont(NARfx.mono(10 + 4 * item.getPriority()));
    }
}
