package nars.guifx.demo;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import za.co.knonchalant.builder.POJONode;

import java.util.List;

/**
 * Created by me on 10/8/15.
 */
public class GenericControlPane<X> extends BorderPane {

    public final X obj;

    public GenericControlPane(X obj) {
        super();
        this.obj = obj;

        List<Node> pn = POJONode.propertyNodes(obj);

        if (pn!=null) {
            VBox controls = new VBox();
            controls.getChildren().addAll(pn);
            ToggleButton toggle = new ToggleButton("[X]");
            toggle.selectedProperty().addListener(e->{
                if (toggle.isSelected()) {
                    setCenter(controls);
                }
                else {
                    setCenter(null);
                }
                layout();
            });

            toggle.setSelected(true);

            setTop(toggle);
        }
        else {
            setCenter(new Label(obj.toString()));
        }


    }

}
