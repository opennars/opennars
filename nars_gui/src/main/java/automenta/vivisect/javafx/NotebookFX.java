package automenta.vivisect.javafx;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.List;

/**
 * Console log with embedded interactive controls
 * https://github.com/TomasMikula/RichTextFX#codearea
 * https://github.com/TomasMikula/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/RichText.java
 */
public class NotebookFX extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane p = new BorderPane();
        Scene s = new Scene(p, 800, 600);
        s.getStylesheets().add(NotebookFX.class.getResource("codearea.css").toExternalForm());

        StyleClassedTextArea codeArea = new StyleClassedTextArea();
        codeArea.getStyleClass().add("code-area");

        List<String> ss = Lists.newArrayList("default");
        codeArea.setStyle(0, ss);


        p.setCenter(codeArea);


        primaryStage.setScene(s);
        primaryStage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
