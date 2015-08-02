package automenta.vivisect.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

/**
 * Third VFXWindows tutorial.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class RunSpacegraph extends Application {


    @Override
    public void start(Stage primaryStage) {

        Spacegraph space = new Spacegraph();

        //BrowserWindow.createAndAddWindow(space, "http://www.google.com");


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

        space.getContent().addAll(
                new Windget("Chart", chart, 400, 400),
                new Windget("Edit", new CodeInput(), 300, 200)
        );

        Scene scene = space.newScene(1200, 800);

        // init and show the stage
        primaryStage.setTitle("WignerFX Spacegraph Demo");
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
        //JFX.popup(new VFXWindowsTutorial3());
        launch(args);
    }

}
