package automenta.vivisect.javafx;

import com.google.common.collect.Lists;
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

        setMinSize(100,100);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        autosize();

    }

}
