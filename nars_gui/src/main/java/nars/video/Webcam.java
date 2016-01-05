package nars.video;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.webcamcapture.UtilWebcamCapture;

import java.awt.image.BufferedImage;


public enum Webcam {
    ;

    public static void main(String[] args) {

        // Open a webcam at a resolution close to 640x480
        com.github.sarxos.webcam.Webcam webcam = UtilWebcamCapture.openDefault(640, 480);

        // Create the panel used to display the image and
        ImagePanel gui = new ImagePanel();
        gui.setPreferredSize(webcam.getViewSize());

        ShowImages.showWindow(gui, "CAM");

        //noinspection InfiniteLoopStatement
        while( true ) {
            if (webcam.isOpen() && webcam.isImageNew()) {
                BufferedImage image = webcam.getImage();


                gui.setBufferedImageSafe(image);
            }
        }
    }
}