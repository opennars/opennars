package nars.guifx.util;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.apache.commons.math3.linear.ArrayRealVector;


/**
 * JavaFX container, that allows to freely zoom and scroll its content.
 * <p>
 *
 * @author dejv78 (dejv78.github.io)
 * @since 1.0.0
 */
public class ZoomFX extends AnchorPane {

    @Deprecated private static final double SCROLLING_DIVISOR = 200.0d;

//    private static final double SCROLL_MIN = 0.0;
//    private static final double SCROLL_MAX = 1.0;
//    private static final double SCROLL_UNIT_INC = 0.1;

    // Properties
    //must be private to avoid POJONode from bindign to it
    private final LerpDoubleProperty zoomFactor = new LerpDoubleProperty(1.0);
    private final LerpDoubleProperty panX = new LerpDoubleProperty(0);
    private final LerpDoubleProperty panY = new LerpDoubleProperty(0);

    //Sub-controls
    /*private final ScrollBar hscroll = new ScrollBar();
    private final ScrollBar vscroll = new ScrollBar();*/
    //private final Pane contentPane = new Pane();
    //private final Rectangle clip = new Rectangle();

    public final Group content = new Group();



    private ArrayRealVector panStart = null;


    public final Scale scale = new Scale();
    public final Translate translate = new Translate();
    private Animate positionAnimation;

    public ZoomFX() {


        //setupScrollbar(hscroll, Orientation.HORIZONTAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);
        //setupScrollbar(vscroll, Orientation.VERTICAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);

        //setupConstraints();
        setupStyle();
        //setupClipping();
        setupBindings();


        //contentPane.getChildren().add(contentGroup);
        getChildren().addAll(content/*, hscroll, vscroll*/);


        content.setAutoSizeChildren(false);
        //setCenterShape(false);


        //hscroll.setValue(0.5);
        //vscroll.setValue(0.5);

        visibleProperty().addListener(c -> {
            if (isVisible()) {
                start();
            }
            else {
                stop();
            }
        });
        checkVisibility();

    }

    private void checkVisibility() {
        if (isVisible()) start();
        else stop();
    }

    public double getPanX() {
        return panX.getValue();
    }
    public double getPanY() {
        return panY.getValue();
    }


    private void start() {
        synchronized (content) {
            if (positionAnimation == null) {
                positionAnimation = new Animate(0, a -> {
                    zoomFactor.update();

                    panX.update();

                    panY.update();
                });
                positionAnimation.start();
            }
        }
    }
    private void stop() {
        synchronized (content) {
            if (positionAnimation!=null) {
                positionAnimation.stop();
                positionAnimation = null;
            }
        }
    }


    /**
     * @return The list of contained nodes.
     */
    public ObservableList<Node> content() {
        return content.getChildren();
    }



    /**
     * @return The container (Pane), that actually holds the content.
     * Install the zooming and panning events on this control.
     */
    public Node getViewport() {
        //return contentPane;
        return this;
    }


    /**
     * @return The zoom factor of the content. Value of 1.0 means 1:1 content size, 0.5 means 1:2, etc.
     */
    public double getZoomFactor() {
        return zoomFactor.get();
    }


    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }


    public DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }


    public void zoom(double delta) {
        double mult = 1.0d + (delta / SCROLLING_DIVISOR);
        setZoomFactor(zoomFactor.get() * mult);
    }


    /**
     * Call to indicate the start of the panning.
     *
     * @param sceneX Scene X coordinate
     * @param sceneY Scene Y coordinate
     */
    public void startPan(double sceneX, double sceneY) {
        panStart = new ArrayRealVector(new double[] { sceneX, sceneY }, false);
        setCursor(Cursor.HAND);
    }


    /**
     * Call to update panning in-progress.
     *
     * @param sceneX Scene X coordinate
     * @param sceneY Scene Y coordinate
     */
    public void pan(double sceneX, double sceneY) {
        if (panStart == null) {
            startPan(panX.target, panY.target);
        } else {
            ArrayRealVector panStart = this.panStart;

            double dX = (sceneX - panStart.getEntry(0)) / 1.0f; //(pivotLogicalExtent.widthProperty().get() * zoomFactor.get());
            double dY = (sceneY - panStart.getEntry(1)) / 1.0f; //(pivotLogicalExtent.heightProperty().get() * zoomFactor.get());


            panStart.setEntry(0, sceneX);
            panStart.setEntry(1, sceneY);


            panX.setTargetPlus(-dX);
            panY.setTargetPlus(-dY);

        }
    }



    /**
     * Call to indicate the end of the panning.
     */
    public void endPan() {
        panStart = null;
        setCursor(Cursor.DEFAULT);
    }


//    @Deprecated private void setupScrollbar(final ScrollBar scroll, final Orientation orientation, final double min, final double max, final double unitIncrement) {
//        scroll.setOrientation(orientation);
//        scroll.setMin(min);
//        scroll.setMax(max);
//        scroll.setUnitIncrement(unitIncrement);
//    }
//

//    @Deprecated private void setupConstraints() {
//        final ColumnConstraints c1 = new ColumnConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
//        final ColumnConstraints c2 = new ColumnConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, false);
//        final RowConstraints r1 = new RowConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true);
//        final RowConstraints r2 = new RowConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, false);
//
////        getColumnConstraints().addAll(c1, c2);
////        getRowConstraints().addAll(r1, r2);
////
////        GridPane.setConstraints(contentPane, 0, 0);
////        GridPane.setConstraints(hscroll, 0, 1);
////        GridPane.setConstraints(vscroll, 1, 0);
//    }


//    private void setupClipping() {
//        clip.widthProperty().bind(widthProperty());
//        clip.heightProperty().bind(heightProperty());
//        //setClip(clip);
//        setNeedsLayout(false);
//    }


    private void setupStyle() {
        //contentPane.setStyle("-fx-background-color: GREY");
        setStyle("-fx-border-width: 0px");
    }


    private void setupBindings() {
        //final ObservableDimension2D viewportPhysicalSize = new ObservableDimension2D();
        //final ObservableBounds contentLogicalBounds = new ObservableBounds();

        setOnScroll((event) -> {
            double mult = 1.0d + (event.getDeltaY() / SCROLLING_DIVISOR);
            zoomFactor.set(zoomFactor.get() * mult);
        });

//        layoutBoundsProperty().addListener((sender, oldValue, newValue) -> {
//            viewportPhysicalSize.setWidth(newValue.getWidth());
//            viewportPhysicalSize.setHeight(newValue.getHeight());
//        });

//        content.boundsInLocalProperty().addListener((sender, oldValue, newValue) -> {
//            contentLogicalBounds.setMinX(newValue.getMinX());
//            contentLogicalBounds.setMinY(newValue.getMinY());
//            contentLogicalBounds.setMaxX(newValue.getMaxX());
//            contentLogicalBounds.setMaxY(newValue.getMaxY());
//        });


        scale.xProperty().bind(zoomFactor);
        scale.yProperty().bind(zoomFactor);


        translate.xProperty().bind(panX);
        translate.yProperty().bind(panY);

        content.getTransforms().setAll(scale, translate);
    }

//    protected void updateTransform() {
//        final ObservableDimension2D viewportPhysicalSize = new ObservableDimension2D();
//        final ObservableBounds contentLogicalBounds = new ObservableBounds();
//
//
//
//        final ObservableDimension2D viewportLogicalHalfSize = new ObservableDimension2D(
//                viewportPhysicalSize.widthProperty().divide(zoomFactor).multiply(0.5),
//                viewportPhysicalSize.heightProperty().divide(zoomFactor).multiply(0.5));
//
////        pivotLogicalExtent.minXProperty().bind(contentLogicalBounds.minXProperty().add(viewportLogicalHalfSize.widthProperty()));
////        pivotLogicalExtent.minYProperty().bind(contentLogicalBounds.minYProperty().add(viewportLogicalHalfSize.heightProperty()));
////        pivotLogicalExtent.maxXProperty().bind(contentLogicalBounds.maxXProperty().subtract(viewportLogicalHalfSize.widthProperty()));
////        pivotLogicalExtent.maxYProperty().bind(contentLogicalBounds.maxYProperty().subtract(viewportLogicalHalfSize.heightProperty()));
//
////        final ObservablePoint2D pivotLogicalCoords = new ObservablePoint2D(
////                pivotLogicalExtent.minXProperty().add(pivotLogicalExtent.widthProperty().multiply(pan.getX())), //hscroll.valueProperty())),
////                pivotLogicalExtent.minYProperty().add(pivotLogicalExtent.heightProperty().multiply(pan.getY()))); //vscroll.valueProperty())));
////
////        final ObservableBounds viewportLogicalBounds = new ObservableBounds(
////                pivotLogicalCoords.xProperty().subtract(viewportLogicalHalfSize.widthProperty()),
////                pivotLogicalCoords.yProperty().subtract(viewportLogicalHalfSize.heightProperty()),
////                pivotLogicalCoords.xProperty().add(viewportLogicalHalfSize.widthProperty()),
////                pivotLogicalCoords.yProperty().add(viewportLogicalHalfSize.heightProperty()));
//
//        //hscroll.visibleAmountProperty().bind(viewportLogicalBounds.widthProperty().divide(contentLogicalBounds.widthProperty()));
//        //vscroll.visibleAmountProperty().bind(viewportLogicalBounds.heightProperty().divide(contentLogicalBounds.heightProperty()));
//
//        //System.out.println(pan + " " + zoomFactor.floatValue());
//
//
//
////        scale.xProperty().set(zoomFactor.floatValue());
////        scale.yProperty().set(zoomFactor.floatValue());
//
//        //translate.xProperty().bind(viewportLogicalBounds.minXProperty().multiply(-1));
//        //translate.yProperty().bind(viewportLogicalBounds.minYProperty().multiply(-1));
//        //scale.xProperty().bind(zoomFactor);
//        //scale.yProperty().bind(zoomFactor);
//
//
//    }
}