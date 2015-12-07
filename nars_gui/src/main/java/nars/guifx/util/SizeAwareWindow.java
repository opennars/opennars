package nars.guifx.util;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nars.guifx.NARfx;
import nars.guifx.NARide;
import nars.nar.Default;
import nars.util.data.map.LMap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static javafx.application.Platform.runLater;

/**
 * Size (and visibility) aware
 */
public class SizeAwareWindow extends Scene {

    private final BorderPane root;
    //private final BorderPane content;
    //private final BorderPane overlay;
    //private Scene scene;
    public Stage window;


    final LMap<double[], Parent> nodes;

    /*
    public static enum BasicLayout {
        Icon, Small, Row, Column, Large
    }

    public SizeAwareWindow(Function<BasicLayout, Supplier<Node>> model) {

    }
    */

    public SizeAwareWindow(Function<double[], Supplier<Parent>> model) {
        this(null, model);
    }

    public SizeAwareWindow(Stage window, Function<double[], Supplier<Parent>> model) {
        super(new BorderPane());


        this.root = (BorderPane)getRoot();

        this.nodes = LMap.newHash(model);

        //root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

//        overlay = new BorderPane();
//
//        //overlay.setBackground(Background.EMPTY);
//        overlay.setOpacity(0.5);
////        overlay.setScaleX(0.5);
////        overlay.setScaleY(0.5);
//        overlay.setCenter(new Label("XYZ"));
//        overlay.setMouseTransparent(true);
//        overlay.setPickOnBounds(false);
//        overlay.setVisible(false);
////
//
//        content = new BorderPane();
//
//        getChildren().addAll(
//            content
//                //overlay
//        );


        if (window == null)
            window = new Stage();

        final Stage finalWindow = this.window = window;
        runLater(() -> {

            finalWindow.setAlwaysOnTop(true);

            finalWindow.setTitle("?");

            finalWindow.setScene(this);

            finalWindow.getScene().getStylesheets().setAll(NARfx.css );

            AtomicBoolean sizeChanging = new AtomicBoolean(false);

            ChangeListener onSizeChange = (observable1, oldValue1, newValue1) -> {
                Scene ss = finalWindow.getScene();


                double w = ss.getWidth();
                double h = ss.getHeight();

                if ((w != 0) && (h != 0)) {
                    //System.out.println(w + " " + h);
                    if (sizeChanging.compareAndSet(false, true)) {

                        runLater(() -> {

                            resized(w, h);
                            sizeChanging.set(false);
                        });
                    }
                }


            };

            //TODO combine into the same method
            finalWindow.heightProperty().addListener(onSizeChange);
            finalWindow.widthProperty().addListener(onSizeChange);


            runLater(() -> {

                //onSizeChange.changed(null, null, null);
                resized(finalWindow.getWidth(),
                        finalWindow.getHeight());

            });


        });


        //setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


    }


    Parent current;

    public static double[] d2(final double a, final double b) {
        return new double[]{a, b};
    }

    final double _d[] = new double[2];

    public void resized(double width, double height) {

        //infoPanelLabel.setText(width + "x" + height);

        _d[0] = width;
        _d[1] = height;

        Parent next = nodes.get(_d);

        if (next == null) {
            setContent(null);
            this.current.setVisible(false);
        } else if (next != this.current) {

            if (next!=null) {
                next.setVisible(true);
                next.maxWidth(Double.MAX_VALUE);
                next.maxHeight(Double.MAX_VALUE);
                next.layout();
            }

            Parent old = this.current;

            System.out.println("setting: " + next + ' ' + next.getLayoutBounds());
            setContent(this.current = next);

            if (old!=null)
                old.setVisible(false);

            getRoot().layout();

        }

    }

    public void setContent(Node n) {
        root.setCenter(n);
    }

    /**
     * for decorating a state node after it's built; by default is pass-through
     */
//    protected Node decorate(Node node) {
//        return node;
//    }

    private static Supplier<Parent> Icon = () -> {
        return new Button(":D");
    };

    private static Supplier<Parent> DefaultNAR = () -> {
        return new NARide(new Default().loop());
    };

    private static Supplier<Parent> Row = () -> {
        return new HBox(2,
                new NSlider(100, 25),
                new NSlider(100, 25),
                new NSlider(100, 25),
                new NSlider(100, 25)

        );
    };
    private static Supplier<Parent> Column = () -> {
        Pane vb = new VBox(
                new TextArea("wtf"),
                new TextArea("X"),
                new TextArea("X"),
                new TextArea("X"),
                new TextArea("X"),
                new TextArea("X")
        );
        //vb.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //vb.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        vb.layout();

        return vb;//scrolled(vb);
    };

    //public static SizeAwareWindow vsplit(

    public static void main(String[] args) {
        NARfx.run((x, y) -> {

            SizeAwareWindow wn = new SizeAwareWindow((d) -> {
                double w = d[0];
                double h = d[1];
                if ((w < 200) && (h < 200)) {
                    return Icon;
                } else if (w < 200) {
                    return Column;
                } else if (h < 200) {
                    return Row;
                }
                return DefaultNAR;
            }).size(800, 800).show();


        });
    }

    public SizeAwareWindow fontscale(double relativeTo1) {
        //TODO
        return this;
    }

    public SizeAwareWindow pixelScale(double virtualToRealPixels) {


        //getRoot().setScaleX(virtualToRealPixels);
        //getRoot().setScaleY(virtualToRealPixels);
        return this;
    }

    public SizeAwareWindow screenScale(double pixelsWideRelativeTo1, double pixelsHighRelativeTo1) {
        //TODO
        return this;
    }

    public SizeAwareWindow above() {
        window.setAlwaysOnTop(true);
        return this;
    }


    public SizeAwareWindow below() {
        window.setAlwaysOnTop(false);
        return this;
    }

    public SizeAwareWindow full(boolean b) {
        window.setFullScreen(b);
        return this;
    }

    public SizeAwareWindow size(double w, double h) {
        runLater(() -> {
            if (window != null) {

                window.setWidth(w);
                window.setHeight(h);
            }
        });
        return this;
    }

    public SizeAwareWindow show() {
        runLater(() -> {
            resized(window.getWidth(), window.getHeight());
            window.show();
        });
        return this;
    }

    public SizeAwareWindow hide() {
        window.hide();
        return this;
    }


}
