package nars.guifx.demo;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Created by me on 10/8/15.
 */
public class GenericControlPane<X> extends VBox {

    public final X obj;

    public GenericControlPane(X obj) {
        super();
        this.obj = obj;

        TaggedParameters taggedParameters = new TaggedParameters();

        Region controls = POJONode.valueToNode(obj, taggedParameters, this); //new VBox();

        ToggleButton toggle = new ToggleButton("[X]");
        toggle.selectedProperty().addListener(e->{
            if (toggle.isSelected()) {
                getChildren().add(controls);
            }
            else {
                getChildren().remove(controls);
            }
        });


        toggle.setSelected(true);

        getChildren().setAll(toggle, controls);

    }

}
