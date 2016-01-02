package nars.video;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.background.stationary.BackgroundStationaryGaussian;
import boofcv.factory.background.ConfigBackgroundGaussian;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import nars.guifx.NARfx;
import nars.util.event.ArraySharingList;

import javax.sound.sampled.LineUnavailableException;
import java.awt.image.BufferedImage;


public class ExampleTrackerObjectQuad extends WebcamFX {

    TrackerObjectQuad tracker = null;



    // specify the target's initial location and initialize with the first frame
    Quadrilateral_F64 location = new Quadrilateral_F64(211.0, 162.0, 326.0, 153.0, 335.0, 258.0, 215.0, 249.0);

    ImageView motionOverlay = new ImageView();
    Canvas raw = new Canvas();
    private ArraySharingList<ExampleFeatureSurf.SURFPoint> desc = new ArraySharingList<>(
            ExampleFeatureSurf.SURFPoint[]::new
    );
    private ImageUInt8 segmented;
    private BufferedImage bgMotionImage;
    private BackgroundStationaryGaussian background;
    private WritableImage bgConverted;
    private final boolean surf = false;


    public static void main(String[] args) {

        NARfx.run((a, b) -> {


            Pane bv = new ExampleTrackerObjectQuad();


            b.setScene(new Scene(bv));
            b.sizeToScene();

            b.show();

        });


    }

    public ExampleTrackerObjectQuad() throws LineUnavailableException {

        motionOverlay.setOpacity(0.5);
        motionOverlay.setBlendMode(BlendMode.DIFFERENCE);
        view.setOpacity(1.0);
        getChildren().setAll(
                //view,
                motionOverlay);
    }

    // Track the object across each video frame and display the results
    @Override protected BufferedImage process(BufferedImage wimg) {


        //ImageBase frame = tracker.getImageType().createImage(wimg.getWidth(), wimg.getHeight());

        ImageFloat32 frame = new ImageFloat32(wimg.getWidth(), wimg.getHeight());
        ConvertBufferedImage.convertFrom(wimg, frame);


        if (tracker == null) {
            // Create the tracker.  Comment/Uncomment to change the tracker.
            tracker =
                    //            FactoryTrackerObjectQuad.circulant(null, ImageUInt8.class);
                    //				FactoryTrackerObjectQuad.sparseFlow(null,ImageUInt8.class,null);
                    //FactoryTrackerObjectQuad.tld(null,ImageUInt8.class);
                    FactoryTrackerObjectQuad.tld(null, ImageFloat32.class);
            //				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(), ImageType.ms(3, ImageUInt8.class));
            //				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(true),ImageType.ms(3,ImageUInt8.class));

            // Mean-shift likelihood will fail in this video, but is excellent at tracking objects with
            // a single unique color.  See ExampleTrackerMeanShiftLikelihood
            //				FactoryTrackerObjectQuad.meanShiftLikelihood(30,5,255, MeanShiftLikelihoodType.HISTOGRAM,ImageType.ms(3,ImageUInt8.class));

            tracker.initialize(frame, location);



            // Comment/Uncomment to switch algorithms
            ConfigBackgroundGaussian configGaussian;
            configGaussian = new ConfigBackgroundGaussian(12,0.05f);
            configGaussian.initialVariance = 100;
            configGaussian.minimumDifference = 10;

            background =
                    //FactoryBackgroundModel.stationaryBasic(new ConfigBackgroundBasic(35, 0.005f), ImageType.single(ImageFloat32.class));
                    FactoryBackgroundModel.stationaryGaussian(configGaussian, ImageType.single(ImageFloat32.class));

            raw.setWidth(frame.getWidth());
            raw.setHeight(frame.getHeight());



            // Declare storage for segmented image.  1 = moving foreground and 0 = background
            segmented = new ImageUInt8(frame.getWidth(),frame.getHeight());
        }

        background.segment(frame,segmented);
        background.updateBackground(frame);


        bgMotionImage = VisualizeBinaryData.renderBinary(segmented, true, bgMotionImage);

        bgConverted = SwingFXUtils.toFXImage(bgMotionImage, bgConverted);
        motionOverlay.setImage(bgConverted);


        if (surf) {
            desc = ExampleFeatureSurf.harder(frame, desc);

            boolean visible = tracker.process(frame, location);
            //System.out.println(visible + " " +location);
        }


        return wimg;
    }

    @Override
    protected WritableImage process(WritableImage img) {
        GraphicsContext g = raw.getGraphicsContext2D();
        if (g == null || img == null) return img;





        g.clearRect(0, 0, img.getWidth(), img.getHeight());

        g.setStroke(Color.YELLOW);
        g.setLineWidth(1);

        Point2D_F64 p = location.get(3);
        for (int i = 0; i < 4; i++) {
            Point2D_F64 n = location.get(i);
            g.strokeLine(n.getX(), n.getY(), p.getX(), p.getY());
            p = n;
        }


        desc.forEach((f) -> {
            //descriptor.describe(p.x,p.y,angle,p.scale,desc);


            double cx = f.point.getX();
            double cy = f.point.getY();
            double a = f.angle;
            double dx = 4 * Math.cos(a);
            double dy = 4 * Math.sin(a);
            g.strokeLine(cx,cy,cx+dx, cy+dy);

            double sc = f.point.getScale();
            g.strokeOval(cx, cy,
                    sc*3, sc*3);

        });

        return img;
    }
}