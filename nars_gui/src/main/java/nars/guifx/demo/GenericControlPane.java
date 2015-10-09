package nars.guifx.demo;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Created by me on 10/8/15.
 */
public class GenericControlPane<X> extends BorderPane {

    public final X obj;

    public GenericControlPane(X obj) {
        super();
        this.obj = obj;

        TaggedParameters taggedParameters = new TaggedParameters();

        Region controls = POJONode.valueToNode(obj, taggedParameters, this); //new VBox();

        ToggleButton toggle = new ToggleButton("[X]");
        controls.visibleProperty().bind(toggle.selectedProperty());
        toggle.setSelected(true);

        setTop(toggle);
        setCenter(controls);

    }

}
