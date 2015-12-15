package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import nars.budget.Itemized;
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

        item = i;
        this.labeler = labeler;

        Task t = null;
        if (i instanceof Task)
            t = (Task)i;

        if (clicked!=null) {
            setOnMouseClicked(e -> clicked.accept(i));
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
        float p = item.getBudget().getPriorityIfNaNThenZero();
        float ph = p * 0.5f + 0.5f;
        setTextFill(JFX.grayscale.get(ph));

        setStyle(JFX.fontSize(8 + 12 * p));

        Node g = getGraphic();
        if (g instanceof Runnable)
            ((Runnable)g).run();

    }
}
