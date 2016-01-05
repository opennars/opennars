package za.co.knonchalant;


import javafx.scene.control.TextField;

/**
 * Helper to provide features not bundled with JavaFX.
 */
public enum JavaFXHelper {
    ;

    /**
     * Require the text field to contain numeric digits only.
     *
     * @param field the text field to restrict
     */
    public static void numericOnly(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
}
