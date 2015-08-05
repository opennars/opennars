package automenta.vivisect.javafx;

import com.google.common.collect.Lists;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Created by me on 8/2/15.
 */
public class CodeInput extends BorderPane {

    public CodeInput() {
        this("");
    }

    public CodeInput(String text) {
        super();

        StyleClassedTextArea codeArea = new StyleClassedTextArea();
        codeArea.getStyleClass().add("code-area");

        codeArea.setStyle(0, Lists.newArrayList("code-area-default"));

        codeArea.appendText(text);

        setCenter(codeArea);

        codeArea.setMinSize(100,100);

        //codeArea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        codeArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //codeArea.autosize();



        codeArea.setOnKeyPressed(k -> {
            //ctrl-enter
            if (k.isControlDown() && k.getCode() == KeyCode.ENTER) {
                if (onInput(codeArea.getText())) {
                    codeArea.clear();
                }
            }
        });
    }

    /** return false to indicate input was not accepted, leaving it as-is.
     * otherwise, return true that it was accepted and the buffer will be cleared. */
    public boolean onInput(String s) {
        return true;
    }
}
