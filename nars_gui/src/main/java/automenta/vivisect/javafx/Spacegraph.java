package automenta.vivisect.javafx;

import dejv.commons.jfx.input.GestureModifiers;
import dejv.commons.jfx.input.MouseButtons;
import dejv.commons.jfx.input.MouseModifiers;
import dejv.commons.jfx.input.handler.DragActionHandler;
import dejv.commons.jfx.input.handler.ScrollActionHandler;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.MouseEventProperties;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
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

    public void removeNodes(Node n) {
        verts.getChildren().remove(n);
    }

    public void addEdges(Node... n) {
        edges.getChildren().addAll(n);
    }

    public void removeEdges(Node n) {
        edges.getChildren().remove(n);
    }
    public void removeEdges(Collection<Node> n) {
        edges.getChildren().removeAll(n);
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

            double x = pan.getEntry(0), y = pan.getEntry(1);
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


    @Override
    protected void updateTransform() {
        super.updateTransform();
        if (grid!=null)
            grid.update();
    }

    final Group verts = new Group();
    final Group edges = new Group();
    final Pane layers = new AnchorPane(edges, verts);

    public Spacegraph() {
        super();

        //getChildren().add(0, grid = new GridCanvas(true));

        verts.maxWidth(Double.MAX_VALUE);
        verts.maxHeight(Double.MAX_VALUE);
        edges.maxWidth(Double.MAX_VALUE);
        edges.maxHeight(Double.MAX_VALUE);


        layers.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        layers.setPickOnBounds(false);
        edges.setPickOnBounds(false);
        verts.setPickOnBounds(false);


        content().add(layers);

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
                })
                .doOnDrag((event) -> pan(event.getSceneX(), event.getSceneY()))
                .doOnDragFinish((event) -> endPan())
                .register(getViewport());


        updateTransform();
    }

    public Scene newScene(double width, double height) {
        Scene s = new Scene(this, width, height);
        s.getStylesheets().add(Spacegraph.spacegraphCSS);
        return s;
    }
}
