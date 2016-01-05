package nars.video;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.extract.ConfigExtract;
import boofcv.abst.feature.detect.extract.NonMaxSuppression;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.orientation.OrientationIntegral;
import boofcv.alg.feature.detect.interest.FastHessianFeatureDetector;
import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.extract.FactoryFeatureExtractor;
import boofcv.factory.feature.orientation.FactoryOrientationAlgs;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import nars.util.event.ArraySharingList;

import java.util.List;

/**
 * Example of how to use SURF detector and descriptors in BoofCV. 
 * 
 * @author Peter Abeles
 */
public enum ExampleFeatureSurf {
	;

	/**
	 * Use generalized interfaces for working with SURF.  This removes much of the drudgery, but also reduces flexibility
	 * and slightly increases memory and computational requirements.
	 * 
	 *  @param image Input image type. DOES NOT NEED TO BE ImageFloat32, ImageUInt8 works too
	 */
	public static DetectDescribePoint easy( ImageFloat32 image ) {
		// create the detector and descriptors
		DetectDescribePoint<ImageFloat32,SurfFeature> surf = FactoryDetectDescribe.
				surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null,ImageFloat32.class);

		 // specify the image to process
		surf.detect(image);
		return surf;
//
//		System.out.println("Found Features: "+surf.getNumberOfFeatures());
//		System.out.println("First descriptor's first value: "+surf.getDescription(0).value[0]);

	}

	/**
	 * Configured exactly the same as the easy example above, but require a lot more code and a more in depth
	 * understanding of how SURF works and is configured.  Instead of TupleDesc_F64, SurfFeature are computed in
	 * this case.  They are almost the same as TupleDesc_F64, but contain the Laplacian's sign which can be used
	 * to speed up association. That is an example of how using less generalized interfaces can improve performance.
	 *
	 * @param image Input image type. DOES NOT NEED TO BE ImageFloat32, ImageUInt8 works too
	 */
	public static <II extends ImageSingleBand> ArraySharingList<SURFPoint> harder(ImageFloat32 image, ArraySharingList<SURFPoint> target) {
		// SURF works off of integral images
		Class<II> integralType = GIntegralImageOps.getIntegralType(ImageFloat32.class);

		int minPixelScale = 10; //orignil: 2
		// define the feature detection algorithm
		NonMaxSuppression extractor =
				FactoryFeatureExtractor.nonmax(new ConfigExtract(minPixelScale, 0, 5, true));
		FastHessianFeatureDetector<II> detector =
				new FastHessianFeatureDetector<>(extractor, 200, 4, minPixelScale, 4, 4);

		// estimate orientation
		OrientationIntegral<II> orientation =
				FactoryOrientationAlgs.sliding_ii(null, integralType);

		//DescribePointSurf<II> descriptor = FactoryDescribePointAlgs.<II>surfStability(null,integralType);
		
		// compute the integral image of 'image'
		II integral = GeneralizedImageOps.createSingleBand(integralType,image.width,image.height);
		GIntegralImageOps.transform(image, integral);

		// detect fast hessian features
		detector.detect(integral);
		// tell algorithms which image to process
		orientation.setImage(integral);
		//descriptor.setImage(integral);

		List<ScalePoint> points = detector.getFoundPoints();



		target.clear();

		for( ScalePoint p : points ) {
			// estimate orientation
			orientation.setScale(p.scale);
			double angle = orientation.compute(p.x,p.y);

			// extract the SURF description for this region
			//SurfFeature desc = descriptor.createDescription();
			//descriptor.describe(p.x,p.y,angle,p.scale,desc);

			// save everything for processing later on
			//target.add(desc);
			SURFPoint sp = new SURFPoint(p, angle, null);
			target.add(sp);
		}

//		System.out.println("Found Features: "+points.size());
//		System.out.println("First descriptor's first value: "+descriptions.get(0).value[0]);

		return target;
	}

	public static class SURFPoint {
		public final SurfFeature desc;
		public final ScalePoint point;
		public final double angle;

		public SURFPoint(ScalePoint p, double angle, SurfFeature desc) {
			point = p;
			this.angle = angle;
			this.desc = desc;
		}
	}

//	public static void main( String args[] ) {
//
//		ImageFloat32 image = UtilImageIO.loadImage("../data/evaluation/particles01.jpg",ImageFloat32.class);
//
//		// run each example
//		ExampleFeatureSurf.easy(image);
//		ExampleFeatureSurf.harder(image);
//
//		System.out.println("Done!");
//
//	}
}