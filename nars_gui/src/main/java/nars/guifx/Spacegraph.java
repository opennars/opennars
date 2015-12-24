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
import javafx.scene.paint.Color;
import nars.guifx.util.ZoomFX;

import java.util.Collection;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/2/15.
 */
public class Spacegraph extends ZoomFX {

    static final String spacegraphCSS = Spacegraph.class.getResource("spacegraph.css").toExternalForm();


    //private final GridCanvas grid = null;

    public void addNodes(Function<Node,Node> wrap, Node... n) {
        for (Node a : n) {
            addNodes( wrap.apply(a) );
        }
    }

    public void addNodes(Node... n) {
        verts.getChildren().addAll(n);
    }
    @Deprecated public void setNodes(Node... n) {
        //gets converted into a list in the setAll call anyway
        verts.getChildren().setAll(n);
    }
    public void addNodes(Collection<Node> n) {
        verts.getChildren().addAll(n);
    }
    public void setNodes(Collection<Node> n) {
        verts.getChildren().setAll(n);
    }

//    public void addEdges(Node... n) {
//        edges.getChildren().addAll(n);
//    }
//    public void setEdges(Node... n) {
//        edges.getChildren().setAll(n);
//    }

    public void removeNodes(Node... n) {
        verts.getChildren().removeAll(n);
    }
    public void removeNodes(Collection<Node> n) {
        verts.getChildren().removeAll(n);
    }
//    public void removeEdges(Collection<Node> n) {
//        edges.getChildren().removeAll(n);
//    }


    public ObservableList<Node> getVertices() {
        return verts.getChildren();
    }

    public class GridCanvas extends Canvas {

        private final boolean drawSharpLines;
        final Color c = new Color(0.4, 0.4, 0.4, 1);

        public GridCanvas(boolean drawSharpLines) {
            super(3000,3000);
            this.drawSharpLines = drawSharpLines;
            setMouseTransparent(true);
            runLater(this::update);
        }

        final double minScale = 30;

        public void update() {


            //setWidth(Spacegraph.this.getWidth());
            //setHeight(Spacegraph.this.getHeight());

            double w = getWidth();
            double h = getHeight();

            GraphicsContext gc = getGraphicsContext2D();

            gc.clearRect(0, 0, w, h);



            gc.setLineWidth(2);
            gc.setStroke(c);
            gc.strokeRect(0, 0, w, h);

            double x = getPanX(), y = getPanY();
            double scale = getZoomFactor() * 10.0f;

            while (scale < minScale)
                scale *= 2;

            while (x < 0) x+=scale;
            while (y < 0) y+=scale;

            while (x > scale) x-=scale;
            while (y > scale) y-=scale;

            boolean drawSharpLines = this.drawSharpLines;

            for (; y <= h; y += scale) {
                double Y;
                Y = drawSharpLines ? snap(y) : y;

                gc.strokeLine(0, Y, w, Y);
            }
            for (; x <= w; x += scale) {
                double sx;
                sx = drawSharpLines ? snap(x) : x;

                gc.strokeLine(sx, 0, sx, h);
            }

        }


    }


    public static Spacegraph getSpace(Node n) {
        Node p = n;
        while ((p = p.getParent())!=null) {
            if (p instanceof Spacegraph) return ((Spacegraph)p);
        }
        return null;
    }


    static double snap(double y) {
        return ((int) y) + 0.5;
    }

    public final Group bg = new Group();

    public final Group ground = new Group();

    //public final Group edges = new Group();
    public final Group verts = new Group();

    public final Group sky = new Group();

    public final Group hud = new Group();



    public Spacegraph() {
        super();

        //setCacheShape(false);
        //getChildren().add(0, new GridCanvas(true));

//        verts.maxWidth(Double.MAX_VALUE);
//        verts.maxHeight(Double.MAX_VALUE);
//        edges.maxWidth(Double.MAX_VALUE);
//        edges.maxHeight(Double.MAX_VALUE);

        verts.setPickOnBounds(false);
        //edges.setPickOnBounds(false);

        //content().add(layers);



        getChildren().add( 0, bg );
        content().addAll( ground, /*edges,*/verts, sky);
        getChildren().add( hud );



        //verts.setAutoSizeChildren(false);
        //edges.setAutoSizeChildren(false);


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
                     if (event.getButton() == MouseButton.SECONDARY) {
                         startPan(event.getSceneX(), event.getSceneY());
                         event.consume();
                     }
                })
                .doOnDrag((event) -> {

                    if (event.getButton() == MouseButton.SECONDARY) {
                        pan(event.getSceneX(), event.getSceneY());
                        event.consume();
                    }

                })
                .doOnDragFinish((event) -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        endPan();
                        event.consume();
                    }
                })
                .register(getViewport());



    }

    public Scene newScene(double width, double height) {
        Scene s = new Scene(this, width, height);
        s.getStylesheets().add(Spacegraph.spacegraphCSS);
        s.getStylesheets().add(NARfx.css);
        return s;
    }
    public SubScene newSubScene(double width, double height) {
        return new SubScene(this, width, height);
    }
}
