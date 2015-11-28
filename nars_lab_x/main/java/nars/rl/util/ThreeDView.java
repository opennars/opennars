/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.rl.util;


/*
 * Copyright (c) 2011, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import javafx.animation.Timeline;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import nars.Video;
import nars.gui.output.graph.nengo.UIVertex;
import nars.term.Term;
import org.apache.commons.math3.linear.ArrayRealVector;

import static javafx.application.Application.launch;

/**
 * MoleculeSampleApp
 */
public class ThreeDView extends JFXPanel {

    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 1450;
    final Xform space = new Xform();
    private Timeline timeline;
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0 / 24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    private final HyperassociativeMap map;
    javafx.scene.text.Font font = new javafx.scene.text.Font("Monospaced", 36f);

    private void buildScene() {
        root.getChildren().add(world);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(0.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(320.0);
        cameraXform.rx.setAngle(40);
    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(240.0, 1, 1);
        final Box yAxis = new Box(1, 240.0, 1);
        final Box zAxis = new Box(1, 1, 240.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * modifierFactor * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
                }
            }
        });
    }

    private void handleKeyboard(Scene scene, final Node root) {
        final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Duration currentTime;
                switch (event.getCode()) {
                    case Z:
                        if (event.isShiftDown()) {
                            cameraXform.ry.setAngle(0.0);
                            cameraXform.rx.setAngle(0.0);
                            camera.setTranslateZ(-300.0);
                        }
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        break;
                    case X:
                        if (event.isControlDown()) {
                            if (axisGroup.isVisible()) {
                                axisGroup.setVisible(false);
                            } else {
                                axisGroup.setVisible(true);
                            }
                        }
                        break;
                    case S:
                        if (event.isControlDown()) {
                            if (space.isVisible()) {
                                space.setVisible(false);
                            } else {
                                space.setVisible(true);
                            }
                        }
                        break;
                    case SPACE:
                        if (timelinePlaying) {
                            timeline.pause();
                            timelinePlaying = false;
                        } else {
                            timeline.play();
                            timelinePlaying = true;
                        }
                        break;
                    case UP:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case DOWN:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case RIGHT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                        }
                        break;
                    case LEFT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                        }
                        break;
                }
            }
        });
    }
    
    public void addPoint(Object o, double radius, double x, double y, double z) {
        
        Term t =  (Term)o;
        
        final PhongMaterial mat = new PhongMaterial();
        Color c = Color.hsb(255.0*Video.hashFloat(o.toString().hashCode()), 0.75f, 0.85f);
        mat.setDiffuseColor(c);
        System.out.println(o.getClass());
        Sphere point = new Sphere(radius / t.complexity());
        point.setOpacity(0.85);
        point.setTranslateX(x);
        point.setTranslateY(y);
        point.setTranslateZ(z);
        point.setMaterial(mat);
       
        space.getChildren().add(point);
        
        Text text = new Text(o.toString());
        text.setFill(c);
        text.setFontSmoothingType(FontSmoothingType.LCD);
        text.setSmooth(true);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(font);
        /*
        text.setScaleX(0.05);
        text.setScaleY(0.05);
        text.setScaleZ(0.05);
        */
        text.setTranslateX(x);
        text.setTranslateY(y);
        text.setTranslateZ(z);
        space.getChildren().add(text);
        
    }

    public ThreeDView(HyperassociativeMap h) {
        super();
        
        this.map = h;
        buildScene();
        buildCamera();
        buildAxes();
        world.getChildren().addAll(space);
        
        double scale = 200.0;
        for (Object c : h.keys()) {
            ArrayRealVector v = h.getPosition((UIVertex)c);
            if (v.getDimension()>=3) {
                double x = v.getEntry(0);
                double y = v.getEntry(1);
                double z = v.getEntry(2);
                addPoint(c, 40.0, x*scale,y*scale,z*scale);
            }
        }

        

        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
        handleKeyboard(scene, world);
        handleMouse(scene, world);

        
        setScene(scene);
        

        scene.setCamera(camera);
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
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }

    public static class Xform extends Group {

        public enum RotateOrder {

            XYZ, XZY, YXZ, YZX, ZXY, ZYX
        }

        public Translate t = new Translate();
        public Translate p = new Translate();
        public Translate ip = new Translate();
        public Rotate rx = new Rotate();

        {
            rx.setAxis(Rotate.X_AXIS);
        }
        public Rotate ry = new Rotate();

        {
            ry.setAxis(Rotate.Y_AXIS);
        }
        public Rotate rz = new Rotate();

        {
            rz.setAxis(Rotate.Z_AXIS);
        }
        public Scale s = new Scale();

        public Xform() {
            super();
            getTransforms().addAll(t, rz, ry, rx, s);
        }

        public Xform(RotateOrder rotateOrder) {
            super();
            // choose the order of rotations based on the rotateOrder
            switch (rotateOrder) {
                case XYZ:
                    getTransforms().addAll(t, p, rz, ry, rx, s, ip);
                    break;
                case XZY:
                    getTransforms().addAll(t, p, ry, rz, rx, s, ip);
                    break;
                case YXZ:
                    getTransforms().addAll(t, p, rz, rx, ry, s, ip);
                    break;
                case YZX:
                    getTransforms().addAll(t, p, rx, rz, ry, s, ip);  // For Camera
                    break;
                case ZXY:
                    getTransforms().addAll(t, p, ry, rx, rz, s, ip);
                    break;
                case ZYX:
                    getTransforms().addAll(t, p, rx, ry, rz, s, ip);
                    break;
            }
        }

        public void setTranslate(double x, double y, double z) {
            t.setX(x);
            t.setY(y);
            t.setZ(z);
        }

        public void setTranslate(double x, double y) {
            t.setX(x);
            t.setY(y);
        }

    // Cannot override these methods as they are final:
        // public void setTranslateX(double x) { t.setX(x); }
        // public void setTranslateY(double y) { t.setY(y); }
        // public void setTranslateZ(double z) { t.setZ(z); }
        // Use these methods instead:
        public void setTx(double x) {
            t.setX(x);
        }

        public void setTy(double y) {
            t.setY(y);
        }

        public void setTz(double z) {
            t.setZ(z);
        }

        public void setRotate(double x, double y, double z) {
            rx.setAngle(x);
            ry.setAngle(y);
            rz.setAngle(z);
        }

        public void setRotateX(double x) {
            rx.setAngle(x);
        }

        public void setRotateY(double y) {
            ry.setAngle(y);
        }

        public void setRotateZ(double z) {
            rz.setAngle(z);
        }

        public void setRx(double x) {
            rx.setAngle(x);
        }

        public void setRy(double y) {
            ry.setAngle(y);
        }

        public void setRz(double z) {
            rz.setAngle(z);
        }

        public void setScale(double scaleFactor) {
            s.setX(scaleFactor);
            s.setY(scaleFactor);
            s.setZ(scaleFactor);
        }

        public void setScale(double x, double y, double z) {
            s.setX(x);
            s.setY(y);
            s.setZ(z);
        }

    // Cannot override these methods as they are final:
        // public void setScaleX(double x) { s.setX(x); }
        // public void setScaleY(double y) { s.setY(y); }
        // public void setScaleZ(double z) { s.setZ(z); }
        // Use these methods instead:
        public void setSx(double x) {
            s.setX(x);
        }

        public void setSy(double y) {
            s.setY(y);
        }

        public void setSz(double z) {
            s.setZ(z);
        }

        public void setPivot(double x, double y, double z) {
            p.setX(x);
            p.setY(y);
            p.setZ(z);
            ip.setX(-x);
            ip.setY(-y);
            ip.setZ(-z);
        }

        public void reset() {
            t.setX(0.0);
            t.setY(0.0);
            t.setZ(0.0);
            rx.setAngle(0.0);
            ry.setAngle(0.0);
            rz.setAngle(0.0);
            s.setX(1.0);
            s.setY(1.0);
            s.setZ(1.0);
            p.setX(0.0);
            p.setY(0.0);
            p.setZ(0.0);
            ip.setX(0.0);
            ip.setY(0.0);
            ip.setZ(0.0);
        }

        public void resetTSP() {
            t.setX(0.0);
            t.setY(0.0);
            t.setZ(0.0);
            s.setX(1.0);
            s.setY(1.0);
            s.setZ(1.0);
            p.setX(0.0);
            p.setY(0.0);
            p.setZ(0.0);
            ip.setX(0.0);
            ip.setY(0.0);
            ip.setZ(0.0);
        }
    }

    private void buildMolecule() {

//        final PhongMaterial redMaterial = new PhongMaterial();
//        redMaterial.setDiffuseColor(Color.DARKRED);
//        redMaterial.setSpecularColor(Color.RED);
//
//        final PhongMaterial whiteMaterial = new PhongMaterial();
//        whiteMaterial.setDiffuseColor(Color.WHITE);
//        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
//
//        final PhongMaterial greyMaterial = new PhongMaterial();
//        greyMaterial.setDiffuseColor(Color.DARKGREY);
//        greyMaterial.setSpecularColor(Color.GREY);
//
//        // Molecule Hierarchy
//        // [*] moleculeXform
//        //     [*] oxygenXform
//        //         [*] oxygenSphere
//        //     [*] hydrogen1SideXform
//        //         [*] hydrogen1Xform
//        //             [*] hydrogen1Sphere
//        //         [*] bond1Cylinder
//        //     [*] hydrogen2SideXform
//        //         [*] hydrogen2Xform
//        //             [*] hydrogen2Sphere
//        //         [*] bond2Cylinder
//        Xform moleculeXform = new Xform();
//        Xform oxygenXform = new Xform();
//        Xform hydrogen1SideXform = new Xform();
//        Xform hydrogen1Xform = new Xform();
//        Xform hydrogen2SideXform = new Xform();
//        Xform hydrogen2Xform = new Xform();
//
//        final PhongMaterial whiteMaterial = new PhongMaterial();
//        whiteMaterial.setDiffuseColor(Color.WHITE);
//        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);//        Sphere oxygenSphere = new Sphere(40.0);
//        oxygenSphere.setMaterial(redMaterial);
//
//        Sphere hydrogen1Sphere = new Sphere(30.0);
//        hydrogen1Sphere.setMaterial(whiteMaterial);
//        hydrogen1Sphere.setTranslateX(0.0);
//
//        Sphere hydrogen2Sphere = new Sphere(30.0);
//        hydrogen2Sphere.setMaterial(whiteMaterial);
//        hydrogen2Sphere.setTranslateZ(0.0);
//
//        Cylinder bond1Cylinder = new Cylinder(5, 100);
//        bond1Cylinder.setMaterial(greyMaterial);
//        bond1Cylinder.setTranslateX(50.0);
//        bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
//        bond1Cylinder.setRotate(90.0);
//
//        Cylinder bond2Cylinder = new Cylinder(5, 100);
//        bond2Cylinder.setMaterial(greyMaterial);
//        bond2Cylinder.setTranslateX(50.0);
//        bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
//        bond2Cylinder.setRotate(90.0);
//
//        moleculeXform.getChildren().add(oxygenXform);
//        moleculeXform.getChildren().add(hydrogen1SideXform);
//        moleculeXform.getChildren().add(hydrogen2SideXform);
//        oxygenXform.getChildren().add(oxygenSphere);
//        hydrogen1SideXform.getChildren().add(hydrogen1Xform);
//        hydrogen2SideXform.getChildren().add(hydrogen2Xform);
//        hydrogen1Xform.getChildren().add(hydrogen1Sphere);
//        hydrogen2Xform.getChildren().add(hydrogen2Sphere);
//        hydrogen1SideXform.getChildren().add(bond1Cylinder);
//        hydrogen2SideXform.getChildren().add(bond2Cylinder);
//
//        hydrogen1Xform.setTx(100.0);
//        hydrogen2Xform.setTx(100.0);
//        hydrogen2SideXform.setRotateY(104.5);

        //space.getChildren().add(moleculeXform);

    }

    
}
