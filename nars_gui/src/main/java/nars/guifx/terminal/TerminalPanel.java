package nars.guifx.terminal;

import javafx.event.Event;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author sergei.malafeev
 */
public class TerminalPanel extends StackPane {
    private static final Logger logger = LoggerFactory.getLogger(TerminalPanel.class);
    private TextArea textArea;


    public TerminalPanel() {
        super();
        textArea = new TextArea();
        textArea.setFont(Font.font("Monospaced", 14));
        getChildren().add(textArea);
    }

    public void setPrintStream(PrintStream printStream) {

        textArea.addEventHandler(KeyEvent.KEY_RELEASED, Event::consume);

        textArea.addEventHandler(KeyEvent.KEY_TYPED, Event::consume);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED,
                event -> {
                    byte[] code = TerminalUtils.getCode(event, TerminalPanel.this);


                    //why is this necessary
                    if (event.getCode()== KeyCode.ENTER) {
                        textArea.appendText("\n");
                    }
                    textArea.appendText(event.getText());

                    if (code != null) {
                        try {
                            printStream.write(code);
                        } catch (IOException e) {
                            logger.warn("failed to write to stream", e);
                        }
                    } else {
                        printStream.print(event.getText());
                    }

                    event.consume();
                }
        );
    }

    public TextArea getTextArea() {
        return textArea;
    }
}
