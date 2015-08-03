package automenta.vivisect.javafx;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jfxtras.scene.control.window.Window;

/** window widget */
public class Windget extends Window {

    private final StackPane wrap;
    private final AnchorPane overlay;

    public void addOverlay(Node n) {
        overlay.getChildren().add(n);
    }

    public static class RectPort extends Rectangle {

        private final Windget win;

        public RectPort(Windget win, boolean incoming, double rx, double ry, double w, double h) {
            super(1, 1);
            this.win = win;

            setFill(Color.ORANGE);

            setScaleX(w);
            setScaleY(h);
            setTranslateX(rx * win.getWidth());
            setTranslateY(ry * win.getHeight());
            setTranslateZ(1);
            setOpacity(0.75f);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    setFill(Color.GREEN);
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    setFill(Color.ORANGE);
                }
            });
            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                }
            });
        }
    }

    public Windget(String title, Node content) {
        super(title);

        wrap = new StackPane();
        setContentPane(wrap);

        overlay = new AnchorPane();

        getContentPane().getChildren().setAll(content, overlay);
        autosize();
    }

    public Windget(String title, Region content, double width, double height) {
        this(title, content);




        //content.setAutoSizeChildren(true);
        content.resize(width,height);
        content.setPrefSize(width,height);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        layout();
        autosize();
    }

    public Windget move(double x, double y) {
        setTranslateX(x);
        setTranslateY(y);
        return this;
    }

//        public Windget size(final double width, final double height) {
//            getContentPane().setPrefSize(width, height);
//            getContentPane().setMinSize(width, height);
//
//            return this;
//        }
}
