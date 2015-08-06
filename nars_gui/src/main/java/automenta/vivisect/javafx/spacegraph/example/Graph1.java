package automenta.vivisect.javafx.spacegraph.example;

import automenta.vivisect.javafx.spacegraph.SpaceNet;
import automenta.vivisect.javafx.spacegraph.Xform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import nars.NAR;
import nars.guifx.NARPane;
import nars.nar.Default;
import za.co.knonchalant.builder.POJONode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by me on 8/5/15.
 */
public class Graph1 extends SpaceNet {

    final Xform g = new Xform();
    private static final double HYDROGEN_ANGLE = 104.5;


    public static class RectNode extends Xform {

        final Box base;

        public RectNode() {
            super();

            base = new Box(10, 10, 1);
            base.setTranslateZ(4);
            base.setMouseTransparent(true);

            final PhongMaterial m = new PhongMaterial();
            //m.setDiffuseColor(Color.web("#ffff0080"));
            base.setMaterial(m);


            getChildren().add(base);
        }
    }

    @Override
    public Xform getRoot() {

        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

        final PhongMaterial greyMaterial = new PhongMaterial();
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
        {
            //oxygenSphere.getChildren().add(new Button("fuck"));

            NARPane w = new NARPane(new NAR(new Default()));
            w.setScaleX(0.1f);
            w.setScaleY(0.1f);
            w.setScaleZ(0.1f);
            oxygenSphere.getChildren().setAll(w);
        }



        Sphere hydrogen1Sphere = new Sphere(5.0);
        hydrogen1Sphere.setMaterial(whiteMaterial);
        hydrogen1Sphere.setTranslateX(0.0);

        Sphere hydrogen2Sphere = new Sphere(5.0);
        hydrogen2Sphere.setMaterial(whiteMaterial);
        hydrogen2Sphere.setTranslateZ(0.0);

        Cylinder bond1Cylinder = new Cylinder(2, 100);
        bond1Cylinder.setMaterial(greyMaterial);
        bond1Cylinder.setTranslateX(50.0);
        bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
        bond1Cylinder.setRotate(90.0);

        Cylinder bond2Cylinder = new Cylinder(2, 100);
        bond2Cylinder.setMaterial(greyMaterial);
        bond2Cylinder.setTranslateX(50.0);
        bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
        bond2Cylinder.setRotate(90.0);

        moleculeXform.getChildren().add(oxygenXform);
        moleculeXform.getChildren().add(hydrogen1SideXform);
        moleculeXform.getChildren().add(hydrogen2SideXform);
        oxygenXform.getChildren().add(oxygenSphere);
        hydrogen1SideXform.getChildren().add(hydrogen1Xform);
        hydrogen2SideXform.getChildren().add(hydrogen2Xform);
        hydrogen1Xform.getChildren().add(hydrogen1Sphere);
        hydrogen2Xform.getChildren().add(hydrogen2Sphere);
        hydrogen1SideXform.getChildren().add(bond1Cylinder);
        hydrogen2SideXform.getChildren().add(bond2Cylinder);

        hydrogen1Xform.setTx(100.0);
        hydrogen2Xform.setTx(100.0);
        hydrogen2SideXform.setRotateY(HYDROGEN_ANGLE);

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
        jps.setMaxWidth(350f);
        jps.setMaxHeight(400f);

        BorderPane screen = new BorderPane();
        screen.setLeft(jps);
        screen.setPickOnBounds(false);
        return screen;
    }

    public void start(Stage primaryStage) {
        super.start(primaryStage);
        content.getChildren().add(getControls());
    }

    public static void main(String[] args) {
        launch(args);
    }

}
