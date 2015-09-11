package nars.guifx.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import nars.guifx.Spacegraph;
import nars.guifx.TerminalPane;
import nars.guifx.util.CodeInput;
import nars.guifx.util.Windget;
import nars.nar.Default;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;
import za.co.knonchalant.sample.pojo.SampleClass;

import java.util.ArrayList;
import java.util.List;


public class RunSpacegraph extends Application {

    final Spacegraph space = new Spacegraph();

    @Override
    public void start(Stage primaryStage) {

        Scene scene = space.newScene(1200, 800);

        // init and show the stage
        primaryStage.setTitle("WignerFX Spacegraph Demo");
        primaryStage.setScene(scene);
        primaryStage.show();


        Platform.runLater(() -> {
            start();
        });
    }

    protected void start() {

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
        chart.setCacheHint(CacheHint.SPEED);

        Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-100,-100);
        cc.addOverlay(new Windget.RectPort(cc, true, +1, -1, 30, 30));

        Windget wc = new Windget("Chart", chart, 400, 400);
        wc.addOverlay(new Windget.RectPort(wc, true, -1, +1, 30, 30));


        //Region jps = new FXForm(new NAR(new Default()));  // create the FXForm node for your bean


        TaggedParameters taggedParameters = new TaggedParameters();
        List<String> range = new ArrayList<>();
        range.add("Ay");
        range.add("Bee");
        range.add("See");
        taggedParameters.addTag("range", range);
        Pane jps = POJONode.build(new SampleClass(), taggedParameters);

//        Button button = new Button("Read in");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                //SampleClass sample = POJONode.read(mainPane, SampleClass.class);
//                //System.out.println(sample.getTextString());
//            }
//        });

        jps.setStyle("-fx-font-size: 75%");
        Windget wd = new Windget("WTF",
                jps,
                //new Button("XYZ"),
                400, 400);
        wd.addOverlay(new Windget.RectPort(wc, true, 0, +1, 10, 10));

        space.addNodes(
                wc,
                cc,
                wd
        );




        TerminalPane np = new TerminalPane(new Default());

        Windget nd = new Windget("NAR",
                np, 200, 200
                ).move(-200,300);

        space.addNodes(nd);

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
