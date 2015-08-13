package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * Tab that manages the visibilty of its content node
 */
public class TabX extends Tab {
    public TabX(String name, Node content) {
        super(name, content);
        selectedProperty().addListener(s -> {
            update();
        });
        update();
    }

    protected void update() {
        Node c = getContent();
        if (c !=null)
            c.setVisible(isSelected());
    }
}
