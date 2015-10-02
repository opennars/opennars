package nars.guifx.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import nars.guifx.IOPane;
import nars.guifx.Spacegraph;
import nars.guifx.util.CodeInput;
import nars.guifx.util.Windget;
import nars.nar.Default;


public class RunSpacegraph extends Application {

    //final Spacegraph space = new Spacegraph();

    public static class DemoSpacegraph extends Spacegraph {

        public DemoSpacegraph() {
            super();
            //BrowserWindow.createAndAddWindow(space, "http://www.google.com");


            ObservableList<PieChart.Data> pieChartData =
                    FXCollections.observableArrayList(
                            new PieChart.Data("Grapefruit", 13),
                            new PieChart.Data("Orange", 25),
                            new PieChart.Data("Human", 10),
                            new PieChart.Data("Pear", 22),
                            new PieChart.Data("Apple", 30));
            final PieChart chart = new PieChart(pieChartData);
            chart.setTitle("Invasive Species");
            chart.setCacheHint(CacheHint.SPEED);

            Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-100,-100);
            cc.addOverlay(new Windget.RectPort(cc, true, +1, -1, 30, 30));

            Windget wc = new Windget("Chart", chart, 400, 400);
            wc.addOverlay(new Windget.RectPort(wc, true, -1, +1, 30, 30));


            //Region jps = new FXForm(new NAR(new Default()));  // create the FXForm node for your bean


//            TaggedParameters taggedParameters = new TaggedParameters();
//            List<String> range = new ArrayList<>();
//            range.add("Ay");
//            range.add("Bee");
//            range.add("See");
//            taggedParameters.addTag("range", range);
//            Pane jps = POJONode.build(new SampleClass(), taggedParameters);

//        Button button = new Button("Read in");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                //SampleClass sample = POJONode.read(mainPane, SampleClass.class);
//                //System.out.println(sample.getTextString());
//            }
//        });
//
//            jps.setStyle("-fx-font-size: 75%");
//            Windget wd = new Windget("WTF",
//                    jps,
//                    //new Button("XYZ"),
//                    400, 400);
//            wd.addOverlay(new Windget.RectPort(wc, true, 0, +1, 10, 10));

            addNodes(
                    wc,
                    cc
            );




            IOPane np = new IOPane(new Default());

            Windget nd = new Windget("NAR",
                    np, 200, 200
            ).move(-200,300);

            addNodes(nd);

        }
    }

    @Override
    public void start(Stage primaryStage) {

        Scene scene = new DemoSpacegraph().newScene(1200, 800);

        // init and show the stage
        primaryStage.setTitle("WignerFX Spacegraph Demo");
        primaryStage.setScene(scene);
        primaryStage.show();

//
//        Platform.runLater(() -> {
//            start();
//        });
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
