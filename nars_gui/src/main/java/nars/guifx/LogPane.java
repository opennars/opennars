package nars.guifx;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import nars.NAR;
import nars.io.out.Output;
import nars.io.out.TextOutput;
import nars.task.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends VBox implements Runnable {

    private final Output incoming;
    private final NAR nar;
    final int maxLines = 256;
    final ConcurrentLinkedDeque<Node> pendingAdds = new ConcurrentLinkedDeque();
    final AtomicBoolean pending = new AtomicBoolean(false);
    ScrollPane scrollParent = null;

    /* to be run in javafx thread */
    @Override public void run() {
        if (pending.compareAndSet(true, false)) {

            int toAdd = pendingAdds.size();
            int existing = getChildren().size();

            if (toAdd + existing > maxLines) {
                int toRemove = (toAdd + existing) - maxLines;
                int pendingsToRemove = toRemove - existing;
                while (pendingsToRemove > 0) {
                    pendingAdds.pop(); //old items we will not be displaying because too many were pushed
                    pendingsToRemove--;
                    toRemove--;
                }

                if (toRemove > 0) {
                    getChildren().remove(0, toRemove);
                }

            }

            List<Node> adding = new ArrayList(toAdd);
            for (int i = 0; i < toAdd; i++) {
                adding.add(pendingAdds.pop());
            }

            getChildren().addAll(adding);

            if (scrollParent!=null) {
                scrollParent.setVvalue(1f);
            }


        }
    }

    void updateParent() {
        if (getParent()!=null) {
            if (getParent().getParent() != null)
                if (getParent().getParent().getParent() != null) {
                    Node s = getParent().getParent().getParent();
                    if (s instanceof ScrollPane)
                        scrollParent = (ScrollPane) s;
                    else
                        scrollParent = null;
                }
        }

    }

    public LogPane(NAR nar) {
        super();

        this.nar = nar;

        sceneProperty().addListener((c) -> {
            updateParent();
        });

        incoming = new Output(nar) {

            @Override
            protected boolean output(Channel channel, Class event, Object... args) {
                Node n = getNode(channel, event, args);
                if (n!=null) {

                    pendingAdds.push(n);

                    if (!pending.getAndSet(true)) {
                        Platform.runLater(LogPane.this);
                    }
                }
                return false;
            }

        };
    }

    public Node getNode(Output.Channel channel, Class event, Object[] args) {

        if (args[0] instanceof Task) {
            TaskLabel tl = new TaskLabel(channel.getLinePrefix(event, args) + ' ',
                    (Task)args[0], nar);
            tl.enablePopupClickHandler(nar);
            return tl;
        }

        StringBuilder sb = TextOutput.append(event, args, false, nar, new StringBuilder());
        final String s;
        if (sb != null)
            s = sb.toString();
        else
            s = "null: " + channel.get(event, args) + " " + event + " " + Arrays.toString(args);

        Text t = new Text(s.toString());
        t.setFill(Color.ORANGE);
        return t;
    }

}
