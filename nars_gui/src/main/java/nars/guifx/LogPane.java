package nars.guifx;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import nars.NAR;
import nars.io.out.Output;
import nars.io.out.TextOutput;

import java.util.Arrays;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends VBox {

    private final Output incoming;
    private final NAR nar;

    public LogPane(NAR nar) {
        super();

        this.nar = nar;

        incoming = new Output(nar) {

            @Override
            protected boolean output(Channel channel, Class event, Object... args) {
                Node n = getNode(channel, event, args);
                if (n!=null) {
                    getChildren().add(n);
                    //TODO remove oldest if length exceeds history limit
                }
                return false;
            }

        };
    }

    public Node getNode(Output.Channel channel, Class event, Object[] args) {
        StringBuilder sb = TextOutput.append(event, args, true, nar, new StringBuilder());
        final String s;
        if (sb != null)
            s = sb.toString();
        else
            s = "null: " + channel + " " + event + " " + Arrays.toString(args);

        Text t = new Text(s.toString());
        t.setFill(Color.ORANGE);
        return t;
    }

}
