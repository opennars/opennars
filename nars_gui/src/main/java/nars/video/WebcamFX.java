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

    //private SourceDataLine mLine;
    // private ShortBuffer audioSamples;
    public ImageView view;
    public Webcam webcam = null;

    boolean running = true;

    final int fps = 15;


    public WebcamFX() {

        //newAudioCaptureThread(5).start();

        System.out.println("cams: " + Webcam.getWebcams());

        // Open a webcam at a resolution close to 640x480
        webcam = UtilWebcamCapture.openDefault(800, 600);


        WaveCapture au = new WaveCapture(new AudioSource(0, 20), 20);

        // Create the panel used to display the image and
        //ImagePanel gui = new ImagePanel();
        //gui.setPreferredSize(webcam.getViewSize());

        try {

            view = new ImageView();
            view.prefWidth(webcam.getViewSize().getWidth());
            view.prefHeight(webcam.getViewSize().getHeight());

//            view.maxWidth(Double.MAX_VALUE);
//            view.maxHeight(Double.MAX_VALUE);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            getChildren().addAll(
                    view,
                    au.newMonitorPane());


            new Thread(this).start();

        } catch (Exception e) {
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

                WritableImage finalImage = process(image);
                runLater(() -> view.setImage(finalImage));
            }

            try {
                Thread.sleep((long) (1000.0 / fps));
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * after webcam input
     */
    protected BufferedImage process(BufferedImage img) {
        return img;
    }

    /**
     * before display output
     */
    protected WritableImage process(WritableImage finalImage) {
        return finalImage;
    }
}