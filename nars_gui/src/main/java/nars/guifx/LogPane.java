package nars.guifx;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import nars.NAR;
import nars.io.out.Output;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends VBox {


    private final Output incoming;

    public LogPane(NAR nar) {
        super();
        getChildren().add(new Button("<x --> y>. %1.00;0.90%"));

        Text t = new Text("Echo: WTF");
        t.setFill(Color.ORANGE);

        getChildren().add(t);

        incoming = new Output(nar) {

            @Override
            protected boolean output(Channel channel, Class event, Object... args) {
                return false;
            }
        };
    }
}
