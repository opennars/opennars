package nars.vision;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.shapes.ShapeFittingOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.ConvertImage;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.image.*;
import com.github.sarxos.webcam.Webcam;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.Rectangle2D_F64;
import nars.util.data.random.XORShiftRandom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;


/**
 * Class for NARS Vision using a webcam and raster hierarchy
 *
 * @author Jamie McLaughlin
 */
public class RasterHierarchy extends JPanel
{
    // The number of rasters to calculate.
    int numberRasters;

    // The dimensions of the input frame.
    int frameWidth, frameHeight;


    // location of the target being tracked
    //Quadrilateral_F64 target = new Quadrilateral_F64();

    // The center of the region of focus
    Point2D_I32 focusPoint = new Point2D_I32();


    volatile int mode = 0;

    BufferedImage workImage;

    JFrame window;


    // Polynomial fitting tolerances
    //static double toleranceDist = 8;
    //static double toleranceAngle= Math.PI/10;

    /**
     * Configure the RasterHierarchy representation
     *
     * @param numberRasters The number of rasters to generate
     * @param frameWidth The desired size of the input stream
     * @param frameHeight The desired height of the input stream
     */
    public  RasterHierarchy(int numberRasters, int frameWidth, int frameHeight)
    {
        this.numberRasters = numberRasters;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

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

    public BufferedImage rasterizeImage(BufferedImage input)
    {
        int red, green, blue;

        int redSum   = 0;
        int greenSum = 0;
        int blueSum  = 0;

        int width = input.getWidth();
        int height = input.getHeight();

        int divisions = 12;//height/12; //32;

        int blockXSize = width/divisions;
        int blockYSize = height/divisions;

        MultiSpectral<ImageUInt8> image = ConvertBufferedImage.convertFromMulti(input,null,true,ImageUInt8.class);
        MultiSpectral<ImageUInt8> output = new MultiSpectral<ImageUInt8>(ImageUInt8.class, width, height, 3);// ConvertBufferedImage.convertFromMulti(input,null,true,ImageUInt8.class);
        //MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class, width, height, 3);//ConvertBufferedImage.convertFromMulti(input, null, true, ImageUInt8.class);
        //ConvertBufferedImage.convertFrom(input, image, true);
        BufferedImage rasterizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // set a default focus to the center...

        int focalwidth = width/4; // pixel size of the focal width.
        int focalHeight = height/4; // pixel size of the focal height.

        int focalResolution = 1; // 1 pixel resolution for the focal area.

        // divisions gives the minimum resoluton, so we have to find how many steps are between.
        int steps = divisions/(2*focalResolution);

        //System.out.println("Div: " + divisions + " Steps: " + steps);
        int regionWidth = width;
        int regionHeight = height;
        int x, y, startX, startY;
        int newX, newY;

        newX = 0;
        newY = 0;
        startX = 0;
        startY = 0;
        steps = 6;
        this.setFocus(400, 200);
        for (int step = 1; step <= steps; step++) {
            // For each step we need to reduce the dimensions of the area that is pixelated and
            // also reduce the block size.

            int factor = 2;//step

            if (step > 1) {
                newX = startX + (regionWidth - regionWidth / factor) / factor;
                newY = startY + (regionHeight - regionHeight / factor) / factor;
                if (newX < 0) {newX = 0;}
                if (newY < 0) {newY = 0;}

                regionWidth  = regionWidth/factor;
                regionHeight = regionHeight/factor;

                blockXSize = blockXSize/factor;//*factor);
                blockYSize = blockYSize/factor;//(factor*factor);
                if (blockXSize < 1) {blockXSize = 1;}
                if (blockYSize < 1) {blockYSize = 1;}
            }

            // Set the starting point for the next step
            startX = this.focusPoint.getX() - ((regionWidth)/2);
            startY = this.focusPoint.getY() - ((regionHeight)/2);

            int pixelCount = blockXSize * blockYSize; // Number of pixels per block

            //System.out.println("step: " + step + "wr: " + regionWidth + " hr: " + regionHeight + "(" + newX + ", " + newY + ") blkX: " + blockXSize + " blkY: " + blockYSize);
            //System.out.println("  xmax: " + ((step == 1 ? 0 : startX) + regionWidth) + " ymax: " +  ((step == 1 ? 0 : startY) + regionHeight));

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

                    //System.out.println("(" + x + ", " + y + "), pxlCt: " + pixelCount + " blkX: " + blockXSize + " blkY: " + blockYSize + " red: " + red + " green: " + green + " blue: " + blue );
                    ImageMiscOps.fillRectangle(output.getBand(0), red, x, y, blockXSize, blockYSize);
                    ImageMiscOps.fillRectangle(output.getBand(1), green, x, y, blockXSize, blockYSize);
                    ImageMiscOps.fillRectangle(output.getBand(2), blue, x, y, blockXSize, blockYSize);
                }
            }
        }
        //ImageMiscOps.fillRectangle(image.getBand(0), 255, 0, 0, 10, 10);
        //ConvertBufferedImage.orderBandsIntoBuffered(image, rasterizedImage);
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

        //MultiSpectral<ImageUInt8> input = new MultiSpectral<ImageUInt8>(ImageUInt8.class, actualSize.width, actualSize.height, 3);
        //MultiSpectral<ImageUInt8> input;
        BufferedImage input;
        BufferedImage buffered = new BufferedImage(actualSize.width, actualSize.height, BufferedImage.TYPE_INT_RGB);

        workImage = new BufferedImage(actualSize.width, actualSize.height, BufferedImage.TYPE_INT_RGB);


        while( true ) {
            //input = ConvertBufferedImage.orderBandsIntoRGB(input,webcam.getImage())
            //input = ConvertBufferedImage.convertFromMulti(webcam.getImage(), null, true, ImageUInt8.class);
            input = webcam.getImage();
            // mode is read/written to by the GUI also
            //int mode = this.mode;

            synchronized( workImage ) {
                // copy the latest image into the work buffer
                Graphics2D g2 = workImage.createGraphics();
                //ConvertBufferedImage.convertTo_U8(this.rasterizeImage(input), buffered, false);
                buffered = this.rasterizeImage(input);
                g2.drawImage(buffered,0,0,null);

                //ConvertBufferedImage.convertFrom(buffered, inputFloat);
                //fitCannyBinary(inputFloat, g2);

                // visualize the current results
                //if (mode == 1) {
                //    drawSelected(g2);
                //} else if (mode == 3) {
                //    if( success ) {
                //        drawTrack(g2);
                //    }
                //}
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

    public static void main(String[] args) {

        //ImageType<MultiSpectral<ImageUInt8>> colorType = ImageType.ms(3, ImageUInt8.class);

        RasterHierarchy app = new RasterHierarchy(8, 640, 480);

        app.process();
    }
}
