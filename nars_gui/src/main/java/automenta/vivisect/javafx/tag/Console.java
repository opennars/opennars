package jnetention.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.UP;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

/**
 *
 * @author http://codereview.stackexchange.com/questions/52197/console-component-in-javafx
 */
public class Console extends BorderPane {
    protected final TextArea textArea = new TextArea();
    protected final TextField textField = new TextField();

    protected final List<String> history = new ArrayList<>();
    protected int historyPointer = 0;

    public static interface ConsoleModel {
        public void parse(Console c, String input);
    }

    final ConsoleModel model;
    
    public Console(ConsoleModel model) {
        
        this.model = model;
        
        textArea.setFont(Font.font("Monospace"));
        textArea.setEditable(false);
        setCenter(textArea);

        textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER:
                    String text = textField.getText();
                    //textArea.appendText(text + System.lineSeparator());
                    history.add(text);
                    historyPointer++;
                    model.parse(this, text);
                    
                    textField.clear();
                    break;
                case UP:
                    if (historyPointer == 0) {
                        break;
                    }
                    historyPointer--;
                    runSafe(() -> {
                        textField.setText(history.get(historyPointer));
                        textField.selectAll();
                    });
                    break;
                case DOWN:
                    if (historyPointer == history.size() - 1) {
                        break;
                    }
                    historyPointer++;
                    runSafe(() -> {
                        textField.setText(history.get(historyPointer));
                        textField.selectAll();
                    });
                    break;
                default:
                    break;
            }
        });
        setBottom(textField);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        textField.requestFocus();
    }


    public void clear() {
        runSafe(() -> textArea.clear());
    }

    public void print(final String text) {
        Objects.requireNonNull(text, "text");
        runSafe(() -> textArea.appendText(text));
    }

    public void println(final String text) {
        Objects.requireNonNull(text, "text");
        runSafe(() -> textArea.appendText(text + System.lineSeparator()));
    }

    public void println() {
        runSafe(() -> textArea.appendText(System.lineSeparator()));
    }  
    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }    
}

class WordWrapConsole extends Console {
    public WordWrapConsole(ConsoleModel model) {
        super(model);
        textArea.setWrapText(true);
    }
}