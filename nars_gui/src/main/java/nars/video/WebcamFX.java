package nars.video;

import boofcv.io.webcamcapture.UtilWebcamCapture;
import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import nars.guifx.NARfx;

import java.awt.image.BufferedImage;

import static javafx.application.Platform.runLater;


public class WebcamFX extends BorderPane implements Runnable {

    private ImageView view;
    private Webcam webcam = null;

    boolean running = true;

    final int fps = 25;

    public WebcamFX() {
        super();

        try {
            int w = 640;
            int h = 480;
            webcam = UtilWebcamCapture.openDefault(w, h);

            view = new ImageView();
            view.maxWidth(Double.MAX_VALUE);
            view.maxHeight(Double.MAX_VALUE);

            setCenter(view);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


            new Thread(this).start();

            autosize();
        }
        catch (Exception e) {
            setCenter(new Label(e.toString()));
        }
    }

    public static void main(String[] args) {

        NARfx.run((a, b) -> {


            BorderPane bv = new WebcamFX();


            b.setScene(new Scene(bv));
            b.sizeToScene();

            b.show();

        });


    }

    @Override
    public void run() {
        WritableImage image = null;
        while (running) {
            if (webcam.isOpen() && webcam.isImageNew()) {
                BufferedImage bimage = webcam.getImage();

                //TODO blit the image directly, this is likely not be the most efficient:
                image = SwingFXUtils.toFXImage(bimage, image);

                final WritableImage finalImage = image;
                runLater(() -> {
                    view.setImage(finalImage);
                });
            }

            try {
                Thread.sleep((long)(1000.0/fps));
            } catch (InterruptedException e) {                    }
        }
    }
}