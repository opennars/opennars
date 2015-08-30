package nars.guifx;

import dejv.commons.jfx.input.GestureModifiers;
import dejv.commons.jfx.input.MouseButtons;
import dejv.commons.jfx.input.MouseModifiers;
import dejv.commons.jfx.input.handler.DragActionHandler;
import dejv.commons.jfx.input.handler.ScrollActionHandler;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.MouseEventProperties;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * Created by me on 8/2/15.
 */
public class Spacegraph extends ZoomFX {

    static final String spacegraphCSS = Spacegraph.class.getResource("spacegraph.css").toExternalForm();
    private final GridCanvas grid = null;

    public void addNodes(Node... n) {
        verts.getChildren().addAll(n);
    }

    public void removeNodes(Node... n) {
        verts.getChildren().removeAll(n);
    }
    public void removeNodes(Collection<Node> n) {
        verts.getChildren().removeAll(n);
    }


    public ObservableList<Node> getVertices() {
        return verts.getChildren();
    }

    public class GridCanvas extends Canvas {

        private final boolean drawSharpLines;
        final Color c = new Color(0.3f, 0.3f, 0.3f, 0.5f);

        public GridCanvas(boolean drawSharpLines) {
            super();
            this.drawSharpLines = drawSharpLines;
            setMouseTransparent(true);
        }

        final double minScale = 30;

        public void update() {


            setWidth(Spacegraph.this.getWidth());
            setHeight(Spacegraph.this.getHeight());

            double w = getWidth();
            double h = getHeight();

            GraphicsContext gc = getGraphicsContext2D();

            gc.clearRect(0, 0, w, h);



            gc.setLineWidth(2);
            gc.setStroke(c);
            gc.strokeRect(0, 0, w, h);

            double x = panX.doubleValue(), y = panY.doubleValue();
            double scale = getZoomFactor() * 10f;

            while (scale < minScale)
                scale *= 2;

            while (x < 0) x+=scale;
            while (y < 0) y+=scale;

            while (x > scale) x-=scale;
            while (y > scale) y-=scale;

            final boolean drawSharpLines = this.drawSharpLines;

            for (; y <= h; y += scale) {
                final double Y;
                if (drawSharpLines)
                    Y = snap(y);
                else
                    Y = y;

                gc.strokeLine(0, Y, w, Y);
            }
            for (; x <= w; x += scale) {
                final double X;
                if (drawSharpLines)
                    X = snap(x);
                else
                    X = x;

                gc.strokeLine(X, 0, X, h);
            }

        }


    }

    public static Spacegraph getSpace(final Node n) {
        //TODO cache this?

        Node p = n;
        while ((p = p.getParent())!=null) {
            if (p instanceof Spacegraph) return ((Spacegraph)p);
        }
        return null;
    }


    static final double snap(final double y) {
        return ((int) y) + .5;
    }

    public final Group verts = new Group();

    public final Canvas floorCanvas = new Canvas();


    public final Pane layers = new AnchorPane(verts);

    public void print() {
        System.out.println(this + " " + verts.getChildren().size() + " verts");
    }
    public Spacegraph() {
        super();

        //getChildren().add(0, grid = new GridCanvas(true));

        verts.maxWidth(Double.MAX_VALUE);
        verts.maxHeight(Double.MAX_VALUE);


        layers.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        layers.setPickOnBounds(false);
        verts.setPickOnBounds(false);


        content().add(layers);


        floorCanvas.setManaged(false);
        floorCanvas.setPickOnBounds(false);
        floorCanvas.setMouseTransparent(true);


        floorCanvas.widthProperty().bind(widthProperty());
        floorCanvas.heightProperty().bind(heightProperty());
        getChildren().add(0, floorCanvas); //untransformed


        layers.setCenterShape(false);
        verts.setAutoSizeChildren(false);





        getStyleClass().setAll("spacegraph","dark");


//        space.zoomFactorProperty().addListener((prop, oldVal, newVal) -> zoomFactor.setText(String.format("%d%%", Math.round(newVal.doubleValue() * 100))));
//        bOne.setOnAction((event) -> space.zoomFactorProperty().set(1.0));
//        bMinus.setOnAction((event) -> space.zoomFactorProperty().set(space.zoomFactorProperty().get() * 0.75));
//        bPlus.setOnAction((event) -> space.zoomFactorProperty().set(space.zoomFactorProperty().get() * 1.25));

        GestureEventProperties zoomFXZoom = new GestureEventProperties(GestureModifiers.none());
        MouseEventProperties zoomFXPan = new MouseEventProperties(MouseModifiers.none(), MouseButtons.middle());

        ScrollActionHandler.with(zoomFXZoom)
                .doOnScroll((event) -> zoom(event.getDeltaY()))
                .register(getViewport());

        DragActionHandler.with(zoomFXPan)

                .doOnDragStart((event) -> {
                     if (event.getButton() == MouseButton.SECONDARY)
                        startPan(event.getSceneX(), event.getSceneY());
                    //event.consume();
                })
                .doOnDrag((event) -> {
                    pan(event.getSceneX(), event.getSceneY());
                    //event.consume();
                })
                .doOnDragFinish((event) -> {
                    endPan();
                    //event.consume();
                })
                .register(getViewport());



    }

    public Scene newScene(double width, double height) {
        Scene s = new Scene(this, width, height);
        s.getStylesheets().add(Spacegraph.spacegraphCSS);
        return s;
    }
    public SubScene newSubScene(double width, double height) {
        SubScene s = new SubScene(this, width, height);
        return s;
    }
}
