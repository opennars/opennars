package automenta.vivisect.javafx;

import dejv.commons.jfx.input.GestureModifiers;
import dejv.commons.jfx.input.MouseButtons;
import dejv.commons.jfx.input.MouseModifiers;
import dejv.commons.jfx.input.handler.DragActionHandler;
import dejv.commons.jfx.input.handler.ScrollActionHandler;
import dejv.commons.jfx.input.properties.GestureEventProperties;
import dejv.commons.jfx.input.properties.MouseEventProperties;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jfxtras.scene.control.window.Window;
import org.fxmisc.richtext.CodeArea;

/**
 * Third VFXWindows tutorial.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VFXWindowsTutorial3 extends Application {

    @Override
    public void start(Stage primaryStage) {

        // create the space where the windows will be added to
        ZoomFX space = new ZoomFX();

        //Pane p = new Pane();
        // create a scrollpane
        //ScrollPane scrollPane = new ScrollPane();
        //scrollPane.setContent(space);



//        space.zoomFactorProperty().addListener((prop, oldVal, newVal) -> zoomFactor.setText(String.format("%d%%", Math.round(newVal.doubleValue() * 100))));
//        bOne.setOnAction((event) -> space.zoomFactorProperty().set(1.0));
//        bMinus.setOnAction((event) -> space.zoomFactorProperty().set(space.zoomFactorProperty().get() * 0.75));
//        bPlus.setOnAction((event) -> space.zoomFactorProperty().set(space.zoomFactorProperty().get() * 1.25));

        GestureEventProperties zoomFXZoom = new GestureEventProperties(GestureModifiers.none());
        MouseEventProperties zoomFXPan = new MouseEventProperties(MouseModifiers.none(), MouseButtons.secondary());

        ScrollActionHandler.with(zoomFXZoom)
                .doOnScroll((event) -> space.zoom(event.getDeltaY()))
                .register(space.getViewport());

        DragActionHandler.with(zoomFXPan)
                .doOnDragStart((event) -> space.startPan(event.getSceneX(), event.getSceneY()))
                .doOnDrag((event) -> space.pan(event.getSceneX(), event.getSceneY()))
                .doOnDragFinish((event) -> space.endPan())
                .register(space.getViewport());

//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setFitToHeight(true);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setContent(space);

        BorderPane anchor = new BorderPane();
        anchor.setCenter(space);

        // create a scene that displays the scrollpane (resolution 1200, 1200)
        Scene scene = new Scene(anchor, 1200, 800);

        //BrowserWindow.createAndAddWindow(space, "http://www.google.com");
        
        Window w = new Window("Chart Sample 01");
        
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                new PieChart.Data("Grapefruit", 13),
                new PieChart.Data("Oranges", 25),
                new PieChart.Data("Plums", 10),
                new PieChart.Data("Pears", 22),
                new PieChart.Data("Apples", 30));
        final PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Imported Fruits");
        //chart.setCacheHint(CacheHint.SPEED);

        w.getContentPane().getChildren().add(chart);
        //w.setScaleX(0.25f);
        //w.setScaleY(0.25f);

        Window w2 = new Window("Edit");
        w2.getContentPane().getChildren().add(new CodeArea());
        
        space.getChildren().addAll(w, w2);

        // init and show the stage
        primaryStage.setTitle("VFXWindows Tutorial 03");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
