package nars.video;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.System.out;

/**
 * from https://raw.githubusercontent.com/erikhu/CamaraWeb/master/src/com/webcam/prueba/Camara.java
 */
public class Camara extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    BufferedImage bfi = null;
    ImageView view = null;
    ObjectProperty<Image> img = null;
    Webcam cam = null;

    @Override
    public void start(Stage win) throws Exception {
        win.setTitle("webcam");
        win.setWidth(640);
        win.setHeight(480);
        cam = Webcam.getDefault();
        if (cam != null) {
            cam.setViewSize(new Dimension(640, 480));

            out.println("Posibles dimensiones: ");
            for (Dimension d : cam.getViewSizes()) {
                out.printf("dimension: en x : %d en y: %d ", d.width, d.height);
                out.println();
            }

            cam.open();
            img = new SimpleObjectProperty<>();

            view = new ImageView();

            FlowPane root = new FlowPane();

            root.getChildren().add(view);
            win.setScene(new Scene(root));
            win.show();

            boolean p = true;

            //noinspection OverlyComplexAnonymousInnerClass
            Task<Void> tarea = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    //noinspection LoopConditionNotUpdatedInsideLoop
                    while (p) try {
                        bfi = cam.getImage();
                        if (bfi != null) {
                            Platform.runLater(() ->
                                    img.set(SwingFXUtils.toFXImage(bfi,
                                            null)));
                            bfi.flush();
                        }

                    } catch (Exception e) {
                        out.println("la excepcion : " + e);
                    }
                    return null;
                }

            };

            Thread hilo = new Thread(tarea);
            hilo.setDaemon(true);
            hilo.start();

            view.imageProperty().bind(img);

        }

    }

}