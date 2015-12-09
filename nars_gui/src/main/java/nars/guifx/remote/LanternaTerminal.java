package nars.guifx.remote;

import com.google.common.collect.Lists;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
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

        private final UltraCaret uc = new UltraCaret();
        private float charWidth;
        private float charHeight;

        public class UltraCaret extends Rectangle {

            public UltraCaret() {
                super(0,0,16,16);
                setFill(new Color(0,0,1.0,0.8));


                setManaged(false);
                setCenterShape(false);
                setMouseTransparent(true);
            }

            public void run() {

            }
        }

        public NTerminal() {


            getStyleClass().addAll("code-area","monoconsole");

            setStyle(0, Lists.newArrayList("code-area-default"));

            setFont(NARfx.mono(46.0));


            getChildren().add(uc);

            Popup popup = new Popup();
            popup.setWidth(0);
            popup.setHeight(0);

            InvalidationListener caretChanged = (c) -> {

                //System.out.println( sceneToLocal(0,0) + " " + sceneToLocal(charWidth,charHeight));

                double sw = getScene().getWidth();
                double sh = getScene().getHeight();
                uc.setX(popup.getX() - sw / 2.0 + charWidth*2);
                uc.setY(popup.getY() - sh / 2.0 + charHeight*2);
                uc.setWidth(charWidth);
                uc.setHeight(charHeight);
            };
            popup.xProperty().addListener(caretChanged);
            popup.yProperty().addListener(caretChanged);


//                int p = getCaretPosition();
//
//                Position pos2D = offsetToPosition(p, Forward);
//
//                int paragraph = pos2D.getMajor();
//                int col = pos2D.getMinor();
//                System.out.println(paragraph + " " +col + " " + getPopupWindow().getX() + " "  +getPopupWindow().getAnchorX());
//                //uc.setTranslateX( col * charWidth );
//                uc.setTranslateY( paragraph * charHeight );
//
//                double minx = getLayoutBounds().getMinX();
//                System.out.println(getLayoutBounds());
//                uc.setTranslateX(getPopupWindow().getAnchorX());
//            });

            popup.getContent().setAll(new Rectangle(1,1));
            //popup.setHeight(0);
            //popup.setWidth(0);
//
            //popup.setAutoHide(true);
            //popup.setOpacity(0.5);
//            popup.setAutoFix(true);

            ChangeListener cc = ((a, b, c) -> {
                popup.hide();
                popup.show(this, 0, 0);
            });



            setPopupAlignment(PopupAlignment.CARET_CENTER);
            setPopupWindow(popup);



            runLater(this::updateFontSize);

            runLater(()-> {
                getScene().getWindow().xProperty().addListener(cc);
                getScene().getWindow().yProperty().addListener(cc);
                getScene().getWindow().widthProperty().addListener(cc);
                getScene().getWindow().heightProperty().addListener(cc);


                popup.show(this, 0,0);
            });

            //area.setPopupAnchorOffset(new Point2D(4, 4));


        }

        private void updateCaret(int x, int y) {
            updateFontSize();
            System.out.println(x + " " + y);

        }

        protected void updateFontSize() {

            if (getFont()!=null) {
                FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(getFont());
                charHeight = fm.getAscent();
                charWidth = fm.computeStringWidth("X");
                System.out.println(charHeight + " " + charWidth);
            }
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
        primaryStage.getScene().getStylesheets().setAll(NARfx.css );
        primaryStage.setTitle("Popup Demo");
        primaryStage.show();
        //popup.show(primaryStage);
    }}
