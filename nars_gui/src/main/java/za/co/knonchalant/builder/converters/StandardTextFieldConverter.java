package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.control.TextInputControl;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Standard converter for a String or String-like object.
 */
public class StandardTextFieldConverter extends BaseConverter<String> {
    @Override
    public Node convert(String object, boolean readOnly, TaggedParameters parameters) {
        Node returned;
        if (!readOnly) {
            String prompt = (String) parameters.get("prompt");
            if (prompt == null) {
                prompt = "";
            }

            returned = TextFieldBuilder.create().text(object).promptText(prompt).build();
            if (isTagSet()) {
                returned.setOnKeyReleased((javafx.event.EventHandler<? super javafx.scene.input.KeyEvent>) parameters.get(getTag()));
            }
        } else {
            returned = new Label(object);
        }
        return returned;
    }

    @Override
    public String parse(Node node, boolean readOnly) {
        if (TextInputControl.class.isAssignableFrom(node.getClass())) {
            TextInputControl field = (TextInputControl) node;
            return field.getText();
        }

        if (Label.class.isAssignableFrom(node.getClass())) {
            Label field = (Label) node;
            return field.getText();
        }

        return null;
    }
}
