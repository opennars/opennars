package nars.video;

import boofcv.io.webcamcapture.UtilWebcamCapture;
import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import nars.guifx.NARfx;

import java.awt.image.BufferedImage;

import static javafx.application.Platform.runLater;


public class WebcamFX extends StackPane implements Runnable {

    public ImageView view;
    public Webcam webcam = null;

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

            getChildren().add(view);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


            new Thread(this).start();

            autosize();
        }
        catch (Exception e) {
            getChildren().add(new Label(e.toString()));
        }
    }

    public static void main(String[] args) {

        NARfx.run((a, b) -> {


            Pane bv = new WebcamFX();


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

                BufferedImage bimage = process(webcam.getImage());



                //TODO blit the image directly, this is likely not be the most efficient:
                image = SwingFXUtils.toFXImage(bimage, image);

                final WritableImage finalImage = process(image);
                runLater(() -> {
                    view.setImage(finalImage);
                });
            }

            try {
                Thread.sleep((long)(1000.0/fps));
            } catch (InterruptedException e) {                    }
        }
    }

    /** after webcam input */
    protected BufferedImage process(BufferedImage img) {
        return img;
    }

    /** before display output */
    protected WritableImage process(WritableImage finalImage) {
        return null;
    }
}