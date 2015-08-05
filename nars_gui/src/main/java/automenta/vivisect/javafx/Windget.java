package automenta.vivisect.javafx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
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

    public interface Port {
        public boolean acceptIn(Port from);

        public boolean acceptOut(Port to);

        public void addIncoming(Link l);

        public void addOutgoing(Link l);

        public double getX();
        public double getY();

        /** the port node */
        Node getNode();

        /** the window node containing this port */
        Windget getWindow();
    }

    public interface Link {

        //TODO move this directionality-specific functions to DirectedLink
        public Port getSource();
        public Port getTarget();
    }

    public interface DirectedLink extends Link {

    }

    public static class RectPort extends Rectangle implements Port {

        private final Windget win;
        TriangleEdge dragging = null;

        public RectPort(Windget win, boolean incoming, double rx, double ry, double w, double h) {
            super(rx * win.getWidth(), ry * win.getHeight(), w, h);
            this.win = win;

            setFill(Color.ORANGE);
            //setTranslateZ(1);


            //Bounds lb = win.getLayoutBounds();
            //setTranslateX((rx-0.5) * win.getWidth());
            //setTranslateY((ry-0.5) * win.getHeight());

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
            /*setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                }
            });*/

            setOnMouseReleased(e -> {
                if (dragging!=null) {
                    PickResult pr = e.getPickResult();

                    //System.out.println(pr);
                    //System.out.println(pr.getIntersectedNode());

                    Node target = pr.getIntersectedNode();
                    if (target instanceof Port) {
                        Port tp = (Port)target;
                        if (tp.acceptIn(this) && this.acceptOut(tp)) {
                            //System.out.println(this + " -> " + dragging + " -> " + tp);
                            addOutgoing(dragging);
                            tp.addIncoming(dragging);
                            dragging.setTarget(tp);
                        }
                        else {
                            dragging.delete();
                        }
                    }
                    else {
                        dragging.delete();
                    }

                    dragging = null;
                    e.consume();
                }

            });
            setOnMouseDragged(e -> {

                if (dragging == null) {
                    win.getSpace().addEdges(dragging = new TriangleEdge(RectPort.this, e));
                }
                else {
                    dragging.update(e);
                }

                e.consume();

            });

            /*setOnMouseDragReleased(e -> {
            });*/
        }

        @Override
        public Node getNode() {
            return this;
        }

        @Override
        public Windget getWindow() {
            return (Windget) getNode().getParent().getParent().getParent().getParent();
        }

        @Override
        public boolean acceptIn(Port from) {
            return true;
        }

        @Override
        public boolean acceptOut(Port to) {
            return true;
        }

        @Override
        public void addIncoming(Link l) {

        }

        @Override
        public void addOutgoing(Link l) {

        }

    }

    public Spacegraph getSpace() { return Spacegraph.getSpace(this); }

    public static class TriangleEdge extends Polygon implements DirectedLink, ChangeListener {

        /** TODO use softreference to allow GC */
        private final RectPort source;
        private Port target;

        public TriangleEdge(RectPort source, MouseEvent event) {
            this(source, (RectPort)null);
        }

        public TriangleEdge(RectPort source, RectPort target) {
            super();

            setPickOnBounds(false);

            this.target = this.source = source;
            setTarget(target);

            listen(source.getWindow(), true);

            update();
        }


        @Override
        public RectPort getSource() {
            return source;
        }

        @Override
        public Port getTarget() {
            return target;
        }

        protected void update() {
            if (source!=null && target!=null)
                update(b(source), b(target));
        }

        void update(Point2D  a, Point2D  b) {
            update(a.getX(), a.getY(), b.getX(), b.getY());
        }

        void update(Point2D a, double x2, double y2) {
            update(a.getX(), a.getY(), x2, y2);
        }

        private void update(double x1, double y1, double x2, double y2) {

            getPoints().setAll(
                      x1, y1,
                      x1+40, y1+20,
                      x2, y2);

        }

        protected void update(MouseEvent event) {
            Point2D bp = b(source);
            update(bp, event.getX()+bp.getX(), event.getY()+bp.getY());
        }

        private static Point2D b(Port p) {
            //System.out.println(p + " " + p.getNode() + " " +  p.getWindow());
            return b(p.getNode(), p.getWindow());
        }

        private static Point2D b(Node port, Node window) {
            //Bounds localBounds = port.getBoundsInLocal();
            Bounds portBounds = port.getBoundsInParent();

            //System.out.println(port + " @ " + port.getParent().getParent().getParent().getParent());
            Point2D p = c(portBounds);
            Bounds windowBounds = window.getBoundsInParent();

            int titleBarOffset = 20; //TODO compute accurately

            return p.add(windowBounds.getMinX(), windowBounds.getMinY() + titleBarOffset);
        }

        private static Point2D c(Bounds b) {
            return new Point2D(
                    0.5  * (b.getMinX() + b.getMaxX()),
                    0.5  * (b.getMinY() + b.getMaxY())
                    );
        }


        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            update();
        }

        public void setTarget(Port p) {
            if (this.target == p) return;


            if (this.target!=null) {
                listen(this.target.getWindow(), false);
            }

            this.target = p;

            if (p!=null) {
                listen(p.getWindow(), true);
                setMouseTransparent(false);
                setOpacity(1f);
            }
            else {
                setMouseTransparent(true);
                setOpacity(0.75f);
            }

            update();
        }

        public Spacegraph getSpace() {
            return Spacegraph.getSpace(this);
        }

        protected void listen(Node n, boolean enabled) {
            ReadOnlyObjectProperty<Parent> parentProp = n.parentProperty();

            BooleanProperty visProp = n.visibleProperty();

            DoubleProperty xProp = n.layoutXProperty();
            DoubleProperty yProp = n.layoutYProperty();

            if (enabled) {
                xProp.addListener(this);
                yProp.addListener(this);
                parentProp.addListener(this);
                visProp.addListener(this);
            }
            else {
                xProp.removeListener(this);
                yProp.removeListener(this);
                parentProp.removeListener(this);
                visProp.removeListener(this);
            }
        }

        public void delete() {

            listen(source, false);

            if (getTarget()!=null)
                setTarget(null);

            Spacegraph s = getSpace();
            if (s != null) {
                s.removeEdges(this);
            }

        }
    }



    public Windget(String title, Node content) {
        super(title);

        wrap = new StackPane();
        setContentPane(wrap);

        overlay = new AnchorPane();
        overlay.setPickOnBounds(false);

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
