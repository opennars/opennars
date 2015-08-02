package nars.guifx;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import nars.NAR;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends VBox {

    public LogPane(NAR nar) {
        super();
        getChildren().add(new Button("<x --> y>. %1.00;0.90%"));

        Text t = new Text("Echo: WTF");
        t.setFill(Color.ORANGE);

        getChildren().add(t);
    }
}
