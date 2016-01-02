package za.co.knonchalant.builder.converters;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import za.co.knonchalant.JavaFXHelper;
import za.co.knonchalant.builder.TaggedParameters;

/**
 * Converter for integers that produces numeric-only text fields.
 */
public class IntegerConverter extends BaseConverter<Integer> {

    @Override
    public Node toNode(Integer object, boolean readOnly, TaggedParameters parameters) {
        Node returned;
        String stringVersion = object == null ? "0" : object.toString();
        if (!readOnly) {
            TextField build = new TextField(); build.setText(stringVersion); //TextFieldBuilder.create().text(stringVersion).build();
            JavaFXHelper.numericOnly(build);

            returned = build;
            if (isTagSet()) {
                returned.setOnKeyReleased((EventHandler<? super KeyEvent>) parameters.get(getTag()));
            }
        } else {
            returned = new Label(stringVersion);
        }
        return returned;
    }

    @Override
    public Integer toValue(Node node, boolean readOnly) {
        return Integer.parseInt(parseString(node));
    }

    private String parseString(Node node) {
        if (TextInputControl.class.isAssignableFrom(node.getClass())) {
            TextInputControl field = (TextInputControl) node;
            return field.getText();
        }

        if (Label.class.isAssignableFrom(node.getClass())) {
            Label field = (Label) node;
            return field.getText();
        }
        return "";
    }
}
