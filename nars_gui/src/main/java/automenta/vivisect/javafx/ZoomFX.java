package automenta.vivisect.javafx;

import dejv.commons.jfx.geometry.ObservableBounds;
import dejv.commons.jfx.geometry.ObservableDimension2D;
import dejv.commons.jfx.geometry.ObservablePoint2D;
import javafx.beans.DefaultProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;


/**
 * JavaFX container, that allows to freely zoom and scroll its content.
 * <p>
 *
 * @author dejv78 (dejv78.github.io)
 * @since 1.0.0
 */
@DefaultProperty("content")
public class ZoomFX
        extends Pane {

    private static final double SCROLLING_DIVISOR = 200.0d;
    private static final double SCROLL_MIN = 0.0;
    private static final double SCROLL_MAX = 1.0;
    private static final double SCROLL_UNIT_INC = 0.1;

    // Properties
    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);

    //Sub-controls
    /*private final ScrollBar hscroll = new ScrollBar();
    private final ScrollBar vscroll = new ScrollBar();*/
    //private final Pane contentPane = new Pane();
    private final Group contentGroup = new Group();
    private final Rectangle clip = new Rectangle();

    private final ObservableBounds pivotLogicalExtent = new ObservableBounds();

    private Point2D panStart = null;
    private Point2D pan = new Point2D(0.5, 0.5);



    public ZoomFX() {
        //setupScrollbar(hscroll, Orientation.HORIZONTAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);
        //setupScrollbar(vscroll, Orientation.VERTICAL, SCROLL_MIN, SCROLL_MAX, SCROLL_UNIT_INC);

        setupConstraints();
        setupStyle();
        setupClipping();
        setupBindings();

        //contentPane.getChildren().add(contentGroup);
        getChildren().addAll(contentGroup/*, hscroll, vscroll*/);


        //hscroll.setValue(0.5);
        //vscroll.setValue(0.5);
    }


    /**
     * @return The list of contained nodes.
     */
    public ObservableList<Node> getContent() {
        return contentGroup.getChildren();
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
        zoomFactor.set(zoomFactor.get() * mult);
    }


    /**
     * Call to indicate the start of the panning.
     *
     * @param sceneX Scene X coordinate
     * @param sceneY Scene Y coordinate
     */
    public void startPan(double sceneX, double sceneY) {
        panStart = new Point2D(sceneX, sceneY);
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
            startPan(sceneX, sceneY);
        } else {
            final double dX = (sceneX - panStart.getX()) / 1f; //(pivotLogicalExtent.widthProperty().get() * zoomFactor.get());
            final double dY = (sceneY - panStart.getY()) / 1f; //(pivotLogicalExtent.heightProperty().get() * zoomFactor.get());


            panStart = new Point2D(sceneX, sceneY);
            pan.add(-dX, -dY);

            updateTransform();
            //hscroll.setValue(hscroll.getValue() - dX);
            //vscroll.setValue(vscroll.getValue() - dY);

        }
    }


    /**
     * Call to indicate the end of the panning.
     */
    public void endPan() {
        panStart = null;
        setCursor(Cursor.DEFAULT);
    }


    private void setupScrollbar(final ScrollBar scroll, final Orientation orientation, final double min, final double max, final double unitIncrement) {
        scroll.setOrientation(orientation);
        scroll.setMin(min);
        scroll.setMax(max);
        scroll.setUnitIncrement(unitIncrement);
    }


    private void setupConstraints() {
        final ColumnConstraints c1 = new ColumnConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true);
        final ColumnConstraints c2 = new ColumnConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, HPos.RIGHT, false);
        final RowConstraints r1 = new RowConstraints(0.0, 0.0, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true);
        final RowConstraints r2 = new RowConstraints(0.0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.NEVER, VPos.BOTTOM, false);

//        getColumnConstraints().addAll(c1, c2);
//        getRowConstraints().addAll(r1, r2);
//
//        GridPane.setConstraints(contentPane, 0, 0);
//        GridPane.setConstraints(hscroll, 0, 1);
//        GridPane.setConstraints(vscroll, 1, 0);
    }


    private void setupClipping() {
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }


    private void setupStyle() {
        //contentPane.setStyle("-fx-background-color: GREY");
        this.setStyle("-fx-border-width: 0px");
    }


    private void setupBindings() {
        final ObservableDimension2D viewportPhysicalSize = new ObservableDimension2D();
        final ObservableBounds contentLogicalBounds = new ObservableBounds();

        setOnScroll((event) -> {
            double mult = 1.0d + (event.getDeltaY() / SCROLLING_DIVISOR);
            zoomFactor.set(zoomFactor.get() * mult);
        });

        layoutBoundsProperty().addListener((sender, oldValue, newValue) -> {
            viewportPhysicalSize.setWidth(newValue.getWidth());
            viewportPhysicalSize.setHeight(newValue.getHeight());
        });

        contentGroup.boundsInLocalProperty().addListener((sender, oldValue, newValue) -> {
            contentLogicalBounds.setMinX(newValue.getMinX());
            contentLogicalBounds.setMinY(newValue.getMinY());
            contentLogicalBounds.setMaxX(newValue.getMaxX());
            contentLogicalBounds.setMaxY(newValue.getMaxY());
        });

        updateTransform();
    }

    private void updateTransform() {
        final ObservableDimension2D viewportPhysicalSize = new ObservableDimension2D();
        final ObservableBounds contentLogicalBounds = new ObservableBounds();


        final Scale scale = new Scale();
        final Translate translate = new Translate();

        final ObservableDimension2D viewportLogicalHalfSize = new ObservableDimension2D(
                viewportPhysicalSize.widthProperty().divide(zoomFactor).multiply(0.5),
                viewportPhysicalSize.heightProperty().divide(zoomFactor).multiply(0.5));

        pivotLogicalExtent.minXProperty().bind(contentLogicalBounds.minXProperty().add(viewportLogicalHalfSize.widthProperty()));
        pivotLogicalExtent.minYProperty().bind(contentLogicalBounds.minYProperty().add(viewportLogicalHalfSize.heightProperty()));
        pivotLogicalExtent.maxXProperty().bind(contentLogicalBounds.maxXProperty().subtract(viewportLogicalHalfSize.widthProperty()));
        pivotLogicalExtent.maxYProperty().bind(contentLogicalBounds.maxYProperty().subtract(viewportLogicalHalfSize.heightProperty()));

        final ObservablePoint2D pivotLogicalCoords = new ObservablePoint2D(
                pivotLogicalExtent.minXProperty().add(pivotLogicalExtent.widthProperty().multiply(pan.getX())), //hscroll.valueProperty())),
                pivotLogicalExtent.minYProperty().add(pivotLogicalExtent.heightProperty().multiply(pan.getY()))); //vscroll.valueProperty())));

        final ObservableBounds viewportLogicalBounds = new ObservableBounds(
                pivotLogicalCoords.xProperty().subtract(viewportLogicalHalfSize.widthProperty()),
                pivotLogicalCoords.yProperty().subtract(viewportLogicalHalfSize.heightProperty()),
                pivotLogicalCoords.xProperty().add(viewportLogicalHalfSize.widthProperty()),
                pivotLogicalCoords.yProperty().add(viewportLogicalHalfSize.heightProperty()));

        //hscroll.visibleAmountProperty().bind(viewportLogicalBounds.widthProperty().divide(contentLogicalBounds.widthProperty()));
        //vscroll.visibleAmountProperty().bind(viewportLogicalBounds.heightProperty().divide(contentLogicalBounds.heightProperty()));

        System.out.println(pan + " " + zoomFactor.floatValue());

        translate.setX(pan.getX());
        translate.setY(pan.getY());
        scale.xProperty().set(zoomFactor.floatValue());
        scale.xProperty().set(zoomFactor.floatValue());

        //translate.xProperty().bind(viewportLogicalBounds.minXProperty().multiply(-1));
        //translate.yProperty().bind(viewportLogicalBounds.minYProperty().multiply(-1));
        //scale.xProperty().bind(zoomFactor);
        //scale.yProperty().bind(zoomFactor);

        contentGroup.getTransforms().setAll(scale, translate);
    }
}