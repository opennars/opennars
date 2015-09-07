package nars.guifx.console;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import nars.guifx.NARfx;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.StyleClassedTextArea;

import static javafx.application.Platform.runLater;


/**
 * https://github.com/mabe02/lanterna/blob/master/src/test/java/com/googlecode/lanterna/terminal/TelnetTerminalTest.java
 */
public class LanternaTerminal extends Application {

    public static class NTerminal extends StyleClassedTextArea {

        public class UltraCaret extends Rectangle {

            public UltraCaret() {
                super(16,16);
                setFill(Color.ORANGE);
            }

            public void run() {

            }
        }

        public NTerminal() {
            super();

            getStyleClass().addAll("code-area","monoconsole");

            setStyle(0, Lists.newArrayList("code-area-default"));

            Popup popup = new Popup();

            popup.setOpacity(0.5);
            popup.getContent().add(
                new UltraCaret()
            );
            setPopupAlignment(PopupAlignment.CARET_TOP);
            setPopupWindow(popup);

            runLater(()-> {
                popup.show(this, 0,0);
            });

            //area.setPopupAnchorOffset(new Point2D(4, 4));

        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {


        NTerminal area = new NTerminal();
        area.insertText(0,"abc");





//        Button toggle = new Button("Show/Hide popup");
//        toggle.setOnAction(ae -> {
//            if(popup.isShowing()) {
//                popup.hide();
//            } else {
//                popup.show(primaryStage);
//            }
//        });

        VBox.setVgrow(area, Priority.ALWAYS);



        primaryStage.setScene(new Scene(new VBox(area), 700, 700));
        primaryStage.getScene().getStylesheets().setAll(NARfx.css, "dark.css" );
        primaryStage.setTitle("Popup Demo");
        primaryStage.show();
        //popup.show(primaryStage);
    }}
