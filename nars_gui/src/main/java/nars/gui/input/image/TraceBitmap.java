package nars.gui.input.image;

//package nars.gui.input.image;
//
//import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
//import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
//import boofcv.alg.feature.detect.edge.CannyEdge;
//import boofcv.alg.feature.detect.edge.EdgeContour;
//import boofcv.alg.filter.binary.BinaryImageOps;
//import boofcv.alg.filter.binary.Contour;
//import boofcv.core.image.ConvertBufferedImage;
//import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
//import boofcv.factory.feature.detect.line.ConfigHoughPolar;
//import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
//import boofcv.gui.binary.VisualizeBinaryData;
//import boofcv.gui.feature.ImageLinePanel;
//import boofcv.gui.image.ShowImages;
//import boofcv.struct.ConnectRule;
//import boofcv.struct.image.ImageFloat32;
//import boofcv.struct.image.ImageSInt16;
//import boofcv.struct.image.ImageSingleBand;
//import boofcv.struct.image.ImageUInt8;
//import georegression.struct.line.LineParametric2D_F32;
//import georegression.struct.line.LineSegment2D_F32;
//import java.awt.Dimension;
//import java.awt.image.BufferedImage;
//import java.other.List;
//
///**
// * Transform bitmap to shapes
// * @author me
// */
//public class TraceBitmap {
//
//	// adjusts edge threshold for identifying pixels belonging to a line
//	private static final float edgeThreshold = 25;
//	// adjust the maximum number of found lines in the image
//	private static final int maxLines = 10;
//
//	/**
//	 * Detects lines inside the image using different types of Hough detectors
//	 *
//	 * @param image Input image.
//	 * @param imageType Type of image processed by line detector.
//	 * @param derivType Type of image derivative.
//	 */
//	public static<T extends ImageSingleBand, D extends ImageSingleBand>
//			void detectLines( BufferedImage image , 
//							  Class<T> imageType ,
//							  Class<D> derivType )
//	{
//		// convert the line into a single band image
//		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType );
//
//		// Comment/uncomment to try a different type of line detector
//		DetectLineHoughPolar<T,D> detector = FactoryDetectLineAlgs.houghPolar(
//				new ConfigHoughPolar(3, 30, 2, Math.PI / 180,edgeThreshold, maxLines), imageType, derivType);
////		DetectLineHoughFoot<T,D> detector = FactoryDetectLineAlgs.houghFoot(
////				new ConfigHoughFoot(3, 8, 5, edgeThreshold,maxLines), imageType, derivType);
////		DetectLineHoughFootSubimage<T,D> detector = FactoryDetectLineAlgs.houghFootSub(
////				new ConfigHoughFootSubimage(3, 8, 5, edgeThreshold,maxLines, 2, 2), imageType, derivType);
//
//		List<LineParametric2D_F32> found = detector.detect(input);
//
//		// display the results
//		ImageLinePanel gui = new ImageLinePanel();
//		gui.setBackground(image);
//		gui.setLines(found);
//		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
//
//		ShowImages.showWindow(gui,"Found Lines");
//	}
//
//	/**
//	 * Detects segments inside the image
//	 *
//	 * @param image Input image.
//	 * @param imageType Type of image processed by line detector.
//	 * @param derivType Type of image derivative.
//	 */
//	public static<T extends ImageSingleBand, D extends ImageSingleBand>
//	void detectLineSegments( BufferedImage image ,
//							 Class<T> imageType ,
//							 Class<D> derivType )
//	{
//		// convert the line into a single band image
//		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType );
//
//		// Comment/uncomment to try a different type of line detector
//		DetectLineSegmentsGridRansac<T,D> detector = FactoryDetectLineAlgs.lineRansac(40, 30, 2.36, true, imageType, derivType);
//
//		List<LineSegment2D_F32> found = detector.detect(input);
//
//		// display the results
//		ImageLinePanel gui = new ImageLinePanel();
//		gui.setBackground(image);
//		gui.setLineSegments(found);
//		gui.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
//
//		ShowImages.showWindow(gui,"Found Line Segments");
//	}
//	
//
//	public static void detectCanny(BufferedImage image) {
//
//		ImageUInt8 gray = ConvertBufferedImage.convertFrom(image,(ImageUInt8)null);
//		ImageUInt8 edgeImage = new ImageUInt8(gray.width,gray.height);
//
//		// Create a canny edge detector which will dynamically compute the threshold based on maximum edge intensity
//		// It has also been configured to save the trace as a graph.  This is the graph created while performing
//		// hysteresis thresholding.
//		CannyEdge<ImageUInt8,ImageSInt16> canny = FactoryEdgeDetectors.canny(2,true, true, ImageUInt8.class, ImageSInt16.class);
//
//		// The edge image is actually an optional parameter.  If you don't need it just pass in null
//		canny.process(gray,0.1f,0.3f,edgeImage);
//
//		// First get the contour created by canny
//		List<EdgeContour> edgeContours = canny.getContours();
//		// The 'edgeContours' is a tree graph that can be difficult to process.  An alternative is to extract
//		// the contours from the binary image, which will produce a single loop for each connected cluster of pixels.
//		// Note that you are only interested in external contours.
//		List<Contour> contours = BinaryImageOps.contour(edgeImage, ConnectRule.EIGHT, null);
//
//		// display the results
//		BufferedImage visualBinary = VisualizeBinaryData.renderBinary(edgeImage, null);
//		BufferedImage visualCannyContour = VisualizeBinaryData.renderContours(edgeContours,null,
//				gray.width,gray.height,null);
//		BufferedImage visualEdgeContour = VisualizeBinaryData.renderExternal(contours, null,
//				gray.width, gray.height, null);
//
//		ShowImages.showWindow(visualBinary,"Binary Edges from Canny");
//		ShowImages.showWindow(visualCannyContour,"Canny Trace Graph");
//		ShowImages.showWindow(visualEdgeContour,"Contour from Canny Binary");
//	}
//        
//	public static void main( String args[] ) throws Exception {
//		BufferedImage input = new Screenshot().capture();
//
//		detectLines(input,ImageUInt8.class,ImageSInt16.class);
//
//		// line segment detection is still under development and only works for F32 images right now
//		detectLineSegments(input, ImageFloat32.class, ImageFloat32.class);
//                
//                detectCanny(input);
//	}
//    
// }
