package nars.guifx;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import nars.NAR;
import nars.io.out.Output;
import nars.task.Task;

/**
 * Created by me on 8/10/15.
 */
public class TaskPane extends BorderPane {

    public TaskPane(Output.Channel channel, NAR n, Task t) {
        super();

        Label l = new Label(channel.toString() + ": " + t.toString(n.memory).toString());
        l.setFont( new Font( 12d + 12d * t.getPriority()) );
        l.setAlignment(Pos.CENTER_LEFT);

        setCenter( l );
    }
}
