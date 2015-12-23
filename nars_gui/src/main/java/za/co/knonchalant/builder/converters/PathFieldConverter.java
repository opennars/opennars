package za.co.knonchalant.builder.converters;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import za.co.knonchalant.builder.TaggedParameters;

import java.io.File;

/**
 * Generate a Browse button if the field is not read-only.
 */
public class PathFieldConverter extends StandardTextFieldConverter {
    @Override
    public String toValue(Node node, boolean readOnly) {
        Node actualPath = node.lookup(".actual-path");

        if (actualPath != null) {
            return super.toValue(actualPath, false);
        }

        return super.toValue(node, true);
    }

    @Override
    public Node toNode(String object, boolean readOnly, TaggedParameters parameters) {
        if (readOnly) {
            return super.toNode(object, true, parameters);
        }

        VBox vbox = new VBox();
        TextField path = new TextField(); path.setText(object); //TextFieldBuilder.create().text(object).build();
        path.getStyleClass().add("actual-path");
        Button button = new Button("Browse");
        button.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(null);

            if (selectedDirectory != null) {
                path.setText(selectedDirectory.getAbsolutePath());
            }
        });
        vbox.getChildren().addAll(path, button);
        return vbox;
    }
}
