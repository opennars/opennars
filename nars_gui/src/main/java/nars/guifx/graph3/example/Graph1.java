package nars.guifx.graph3.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nars.guifx.demo.NARide;
import nars.guifx.graph3.SpacenetApp;
import nars.guifx.graph3.Xform;
import nars.nar.Default;
import za.co.knonchalant.builder.POJONode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by me on 8/5/15.
 */
public class Graph1 extends SpacenetApp {

    final Xform g = new Xform();
    private static final double HYDROGEN_ANGLE = 35.5;


    public static class RectNode extends Xform {

        final Box base;

        public RectNode() {

            base = new Box(25, 25, 1);
            base.setTranslateZ(4);
            base.setMouseTransparent(true);

            PhongMaterial m = new PhongMaterial();
            //m.setDiffuseColor(Color.web("#ffff0080"));
            base.setMaterial(m);


            getChildren().add(base);
        }
    }

    @Override
    public Xform getRoot() {

        PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

        PhongMaterial greyMaterial = new PhongMaterial();
        greyMaterial.setDiffuseColor(Color.DARKGREY);
        greyMaterial.setSpecularColor(Color.GREY);

        // Molecule Hierarchy
        // [*] moleculeXform
        //     [*] oxygenXform
        //         [*] oxygenSphere
        //     [*] hydrogen1SideXform
        //         [*] hydrogen1Xform
        //             [*] hydrogen1Sphere
        //         [*] bond1Cylinder
        //     [*] hydrogen2SideXform
        //         [*] hydrogen2Xform
        //             [*] hydrogen2Sphere
        //         [*] bond2Cylinder
        Xform moleculeXform = new Xform();
        Xform oxygenXform = new Xform();
        Xform hydrogen1SideXform = new Xform();
        Xform hydrogen1Xform = new Xform();
        Xform hydrogen2SideXform = new Xform();
        Xform hydrogen2Xform = new Xform();

        RectNode oxygenSphere = new RectNode();
        //oxygenSphere.setMaterial(redMaterial);
        //oxygenSphere.getChildren().add(new Button(""));

        NARide w = new NARide(new Default().loop());
        w.setScaleX(0.05f);
        w.setScaleY(0.03f);
        w.setScaleZ(0.1f);
        oxygenSphere.getChildren().add(w);

        RectNode hydrogen1Sphere = new RectNode();
        hydrogen1Sphere.base.setMaterial(redMaterial);


        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Grapefruit", 13),
                        new PieChart.Data("Oranges", 25),
                        new PieChart.Data("Plums", 10),
                        new PieChart.Data("Pears", 22),
                        new PieChart.Data("Apples", 30));
        PieChart wc = new PieChart(pieChartData);
        wc.setTitle("TEST");
        //wc.setCacheHint(CacheHint.SPEED);

        wc.autosize();

        wc.setTranslateX(40);
        wc.setTranslateY(40);


        wc.setTranslateZ(-0.2);
        wc.setScaleX(0.08f);
        wc.setScaleY(0.08f);
        wc.setScaleZ(0.1f);
        hydrogen1Sphere.getChildren().setAll(new AnchorPane(wc));

        hydrogen1Sphere.setRotateY(HYDROGEN_ANGLE);
        hydrogen1Sphere.setTx(8.0);
        hydrogen1Sphere.setTy(8.0);



        moleculeXform.getChildren().addAll(oxygenSphere, hydrogen1Sphere);


        g.getChildren().add(moleculeXform);
        return g;
    }

    public Pane getControls() {
        GraphVis g = new GraphVis();
        List<String> range = new ArrayList<>();
        g.add("Force Directed");
        g.add("Circle");
        g.add("Grid");
        Pane jps = POJONode.build(g);
        jps.setStyle("-fx-background: #421");
        jps.setOpacity(0.8);
        jps.setMaxWidth(350.0f);
        jps.setMaxHeight(400.0f);

        BorderPane screen = new BorderPane();
        screen.setLeft(jps);
        screen.setPickOnBounds(false);
        return screen;
    }

//    public void start(Stage primaryStage) {
//        super.start(primaryStage);
//        root.content.getChildren().add(getControls());
//    }
//


    public static void main(String[] args) {
        launch(args);
    }

}
