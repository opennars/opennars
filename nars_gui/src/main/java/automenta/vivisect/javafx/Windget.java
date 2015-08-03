package automenta.vivisect.javafx;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
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
            super(rx * win.getWidth(), ry * win.getHeight(), w, h);
            this.win = win;

            setFill(Color.ORANGE);


            Bounds lb = win.getLayoutBounds();
            //setTranslateX((rx-0.5) * win.getWidth());
            //setTranslateY((ry-0.5) * win.getHeight());
            setTranslateZ(1);

            //setScaleX(w);
            //setScaleY(h);

            setOpacity(0.75f);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    setFill(Color.GREEN);
                    event.consume();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    setFill(Color.ORANGE);
                    event.consume();
                }
            });
            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                }
            });
            setOnMouseDragged(new EventHandler<MouseEvent>() {

                TriangleEdge dragging = null;

                @Override
                public void handle(MouseEvent e) {
                    if (dragging == null) {
                        win.addOverlay(dragging = new TriangleEdge(RectPort.this, e));

                        //System.out.println(e);
                    }
                    else {
                        dragging.update(e);
                    }

                    e.consume();
                }
            });
        }

    }

    public static class TriangleEdge extends Polygon {

        private final RectPort source;
        private RectPort target;

        public TriangleEdge(RectPort source, MouseEvent event) {
            this(source, (RectPort)null);
            update(event);
        }

        public TriangleEdge(RectPort source, RectPort target) {
            super();

            this.source = source;
            this.target = target;

            update();
        }

        protected void update() {

        }
        protected void update(MouseEvent event) {
            ObservableList<Double> pp = getPoints();
            pp.clear();
            pp.addAll(source.getX()-50, source.getY());
            pp.addAll(event.getX(), event.getY()-50);
            pp.addAll(event.getX(), event.getY());
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
