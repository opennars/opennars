package nars.vision;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.image.*;
import com.github.sarxos.webcam.Webcam;
import georegression.struct.point.Point2D_I32;
import nars.gui.NARSwing;
import nars.NAR;
import nars.model.impl.Default;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Class for NARS Vision using a webcam with raster hierarchy representation.
 * Includes visualization.  All relevant parameters can be adjusted in real time
 * and will update the visualization.
 *
 * @author James McLaughlin
 */
public class RasterHierarchy extends JPanel
{
    // The number of rasters to calculate.
    int numberRasters;


    // The dimensions of the input frame.
    int frameWidth, frameHeight;

    // The number of blocks to divide the coarsest raster into.
    int divisions;

    // The scaling factor for each raster in the hierarchy.
    int scalingFactor;

    // The center of the region of focus
    Point2D_I32 focusPoint = new Point2D_I32();

    // Image for visualization
    BufferedImage workImage;

    // Window for visualization
    JFrame window;

    /**
     * Configure the Raster Hierarchy
     *
     * @param numberRasters The number of rasters to generate
     * @param frameWidth The desired size of the input stream
     * @param frameHeight The desired height of the input stream
     * @param divisions The number of blocks to divide the coarsest grained raster into
     * @param scalingFactor The scaling factor for each raster in the heirarchy.
     */
    public  RasterHierarchy(int numberRasters, int frameWidth, int frameHeight, int divisions, int scalingFactor)
    {
        this.numberRasters = numberRasters;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        this.divisions = divisions;
        this.scalingFactor = scalingFactor;

        // Set the default focus to the center
        this.setFocus(frameWidth/2, frameHeight/2);

        window = new JFrame("Hierarchical Raster Vision Representation");
        window.setContentPane(this);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Set the focus to the given location.  All rasters (other than the most coarse-grained) are centered on
     * this point.
     *
     * @param x The x-coordinate of the focal point
     * @param y The y-coordinate of the focal point
     */
    public void setFocus(int x, int y)
    {
        this.focusPoint.set(x, y);
    }

    /**
     * Generate the raster hierarchy for a given image.
     * C
     * @param input The image to rasterize
     * @return The rasterized image.
     */
    int updaterate=30;
    int cnt=1;
    public BufferedImage rasterizeImage(BufferedImage input)
    {
        boolean putin=false; //vladimir
        cnt--;
        if(cnt==0) {
            putin = true;
            cnt=updaterate;
        }

        int red, green, blue;
        int redSum, greenSum, blueSum;
        int x, y, startX, startY;
        int newX, newY;

        int width = input.getWidth();
        int height = input.getHeight();

        int blockXSize = width/divisions;
        int blockYSize = height/divisions;

        MultiSpectral<ImageUInt8> image = ConvertBufferedImage.convertFromMulti(input,null,true,ImageUInt8.class);
        MultiSpectral<ImageUInt8> output = new MultiSpectral<>(ImageUInt8.class, width, height, 3);

        BufferedImage rasterizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Set the initial raster region
        int regionWidth = width;
        int regionHeight = height;
        newX = 0;
        newY = 0;
        startX = 0;
        startY = 0;

        for (int step = 1; step <= numberRasters; step++) {

            // For each step we need to reduce the dimensions of the area that is pixelated and
            // also reduce the block size.

            if (step > 1) {
                newX = startX + (regionWidth - regionWidth / scalingFactor) / scalingFactor;
                newY = startY + (regionHeight - regionHeight / scalingFactor) / scalingFactor;
                if (newX < 0) {newX = 0;}
                if (newY < 0) {newY = 0;}

                regionWidth  = regionWidth/ scalingFactor;
                regionHeight = regionHeight/ scalingFactor;

                blockXSize = blockXSize/ scalingFactor;
                blockYSize = blockYSize/ scalingFactor;
                if (blockXSize < 1) {blockXSize = 1;}
                if (blockYSize < 1) {blockYSize = 1;}
            }

            // Set the starting point for the next step
            startX = this.focusPoint.getX() - ((regionWidth)/2);
            startY = this.focusPoint.getY() - ((regionHeight)/2);

            int pixelCount = blockXSize * blockYSize; // Number of pixels per block

            for (x = newX; x < ((step == 1 ? 0 : startX) + regionWidth); x += blockXSize) {
                for (y = newY; y < ((step == 1 ? 0 : startY) + regionHeight); y += blockYSize) {

                    redSum = 0;
                    greenSum = 0;
                    blueSum = 0;

                    for (int pixelX = 0; (pixelX < blockXSize) && (x + pixelX < width); pixelX++) {
                        for (int pixelY = 0; (pixelY < blockYSize) && (y + pixelY < height); pixelY++) {
                            redSum += image.getBand(0).get(x + pixelX, y + pixelY);
                            greenSum += image.getBand(1).get(x + pixelX, y + pixelY);
                            blueSum += image.getBand(2).get(x + pixelX, y + pixelY);
                        }
                    }

                    red = redSum / pixelCount;
                    green = greenSum / pixelCount;
                    blue = blueSum / pixelCount;


                    if(putin) {
                        float fred=((float) red)/255.0f;
                        float fgreen=((float) red)/255.0f;
                        float fblue=((float) red)/255.0f;
                        float brightness = (fred+fgreen+fblue)/3.0f; //maybe not needed

                        String st="<(*,raster"+ String.valueOf(step)+","+String.valueOf(x)+","+String.valueOf(y)+") --> RED>. :|: %"+String.valueOf(fred)+"%";
                        nar.input(st);
                    }
                    // Here we can generate NAL, since we know all of the required values.

                    ImageMiscOps.fillRectangle(output.getBand(0), red, x, y, blockXSize, blockYSize);
                    ImageMiscOps.fillRectangle(output.getBand(1), green, x, y, blockXSize, blockYSize);
                    ImageMiscOps.fillRectangle(output.getBand(2), blue, x, y, blockXSize, blockYSize);
                }
            }
        }

        ConvertBufferedImage.convertTo(output, rasterizedImage, true);
        return rasterizedImage;
    }

    /**
     * Invoke to start the main processing loop.
     */
    public void process() {
        Webcam webcam = UtilWebcamCapture.openDefault(frameWidth, frameHeight);

        // adjust the window size and let the GUI know it has changed
        Dimension actualSize = webcam.getViewSize();
        setPreferredSize(actualSize);
        setMinimumSize(actualSize);
        window.setMinimumSize(actualSize);
        window.setPreferredSize(actualSize);
        window.setVisible(true);

        BufferedImage input, buffered;

        workImage = new BufferedImage(actualSize.width, actualSize.height, BufferedImage.TYPE_INT_RGB);

        //int counter = 0;

        while( true ) {
                /*
                 * Uncomment this section to scan the focal point across the frame
                 * automatically - just for demo purposes.
                 */
                /*
                int xx = this.focusPoint.getX();
                int yy = this.focusPoint.getY();
                xx += 1;

                if(xx > frameWidth)
                {
                    xx = 0;
                    yy += 1;
                    if (yy > frameHeight)
                        yy = 0;
                }

                this.setFocus(xx, yy);
                */
            input = webcam.getImage();

            synchronized( workImage ) {
                // copy the latest image into the work buffer
                Graphics2D g2 = workImage.createGraphics();

                buffered = this.rasterizeImage(input);
                g2.drawImage(buffered,0,0,null);
            }

            repaint();
        }
    }

    @Override
    public void paint (Graphics g) {
        if( workImage != null ) {
            // draw the work image and be careful to make sure it isn't being manipulated at the same time
            synchronized (workImage) {
                ((Graphics2D) g).drawImage(workImage, 0, 0, null);
            }
        }
    }

    static NAR nar;
    public static void main(String[] args) {

        //RasterHierarchy rh = new RasterHierarchy(8, 640, 480, 12, 2);
       // RasterHierarchy rh = new RasterHierarchy(3, 640, 480, 5, 2);
        nar = new NAR(new Default.CommandLineNARBuilder(args));

        NARSwing swing = new NARSwing(nar);

        RasterHierarchy rh = new RasterHierarchy(3, 640, 480, 4, 4);

        rh.process();
    }

    public int getNumberRasters() {
        return numberRasters;
    }

    public void setNumberRasters(int numberRasters) {
        this.numberRasters = numberRasters;
    }

    public int getDivisions() {
        return divisions;
    }

    public void setDivisions(int divisions) {
        this.divisions = divisions;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }
}
