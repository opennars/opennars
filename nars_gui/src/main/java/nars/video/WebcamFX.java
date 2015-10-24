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

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static javafx.application.Platform.runLater;


public class WebcamFX extends StackPane implements Runnable {

    private static SourceDataLine mLine;
    public ImageView view;
    public Webcam webcam = null;

    boolean running = true;

    final int fps = 5;

    final private static int FRAME_RATE = 30;
    static Thread newAudioCaptureThread(int device) {
        // Thread for audio capture, this could be in a nested private class if you prefer...
        return new Thread(() -> {
            // Pick a format...
            // NOTE: It is better to enumerate the formats that the system supports,
            // because getLine() can error out with any particular format...
            // For us: 44.1 sample rate, 16 bits, stereo, signed, little endian
            AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 1, true, false);

            // Get TargetDataLine with that format
            Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
            System.out.println(Arrays.toString(minfoSet));

            Mixer mixer = AudioSystem.getMixer(minfoSet[device]);
            System.out.println(mixer);
            System.out.println(mixer.getMixerInfo());
            System.out.println(Arrays.toString(mixer.getControls()));

            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            System.out.println(dataLineInfo);

            try
            {
                // Open and start capturing audio
                // It's possible to have more control over the chosen audio device with this line:
                // TargetDataLine line = (TargetDataLine)mixer.getLine(dataLineInfo);
                TargetDataLine line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
                line.open(audioFormat);
                line.start();

                int sampleRate = (int) audioFormat.getSampleRate();
                int numChannels = audioFormat.getChannels();

                // Let's initialize our audio buffer...
                int audioBufferSize = sampleRate * numChannels;
                byte[] audioBytes = new byte[audioBufferSize];

                // Using a ScheduledThreadPoolExecutor vs a while loop with
                // a Thread.sleep will allow
                // us to get around some OS specific timing issues, and keep
                // to a more precise
                // clock as the fixed rate accounts for garbage collection
                // time, etc
                // a similar approach could be used for the webcam capture
                // as well, if you wish
                ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                exec.scheduleAtFixedRate((Runnable) () -> {
                    try
                    {
                        // Read from the line... non-blocking
                        int nBytesRead = line.read(audioBytes, 0, line.available());

                        // Since we specified 16 bits in the AudioFormat,
                        // we need to convert our read byte[] to short[]
                        // (see source from FFmpegFrameRecorder.recordSamples for AV_SAMPLE_FMT_S16)
                        // Let's initialize our short[] array
                        int nSamplesRead = nBytesRead / 2;
                        short[] samples = new short[nSamplesRead];

                        // Let's wrap our short[] into a ShortBuffer and
                        // pass it to recordSamples
                        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                        System.out.println(sBuff);
                        // recorder is instance of
                        // org.bytedeco.javacv.FFmpegFrameRecorder
                        //recorder.recordSamples(sampleRate, numChannels, sBuff);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }, 0, (long) 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
            }
            catch (LineUnavailableException e1)
            {
                e1.printStackTrace();
            }
        });
    };

    public WebcamFX() {
        super();

        newAudioCaptureThread(5).start();

        System.out.println("cams: " + Webcam.getWebcams());

        // Open a webcam at a resolution close to 640x480
        webcam = UtilWebcamCapture.openDefault(800, 600);


        // Create the panel used to display the image and
        //ImagePanel gui = new ImagePanel();
        //gui.setPreferredSize(webcam.getViewSize());

        try {

            view = new ImageView();
            view.prefWidth(webcam.getViewSize().getWidth());
            view.prefHeight(webcam.getViewSize().getHeight());

//            view.maxWidth(Double.MAX_VALUE);
//            view.maxHeight(Double.MAX_VALUE);

            getChildren().add(view);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


            new Thread(this).start();

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
        return finalImage;
    }
}