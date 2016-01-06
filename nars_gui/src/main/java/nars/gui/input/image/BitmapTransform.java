package nars.gui.input.image;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 
 * @author me
 */
public class BitmapTransform {
	BufferedImage image;

	public void createVariations() {
		// TODO: add parameters
	}

	public BitmapTransform(BufferedImage image) {
		this.image = image;
	}

	/**
	 * Translates image in x and y direction by translateX and translateY
	 * pixels.
	 * 
	 * @param translateX
	 *            - number of pixels to move on x axis.
	 * @param translateY
	 *            - number of pixels to move on y axis.
	 * @param backgrounColor
	 *            - background color of new image
	 * @return new translated BufferedImage object.
	 */
	public BufferedImage translate(double translateX, double translateY,
			Color backgrounColor) {
		BufferedImage newImage = new BufferedImage(image.getWidth(),
				image.getHeight(), image.getType());

		Graphics2D graphics = (Graphics2D) newImage.getGraphics();
		// set background color for image
		graphics.setPaint(backgrounColor);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		graphics.translate(translateX, translateY);
		graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
				backgrounColor, null);
		graphics.dispose();
		return newImage;
	}

	/**
	 * Rotates given image by specified angle. Precondition: square image.
	 * 
	 * @param angle
	 *            - angle for rotation in degrees.
	 * @param backgrounColor
	 *            - background color of new image
	 * @return new BufferedImage object which represents original image rotated
	 *         by 'angle' degrees.
	 */
	public BufferedImage rotate(double angle, Color backgrounColor) {

		// TODO: check if we are going to work with NON-sqare pictures?? That
		// need also translation and more work about frame size.
		BufferedImage newImage = new BufferedImage(image.getWidth(),
				image.getHeight(), image.getType());
		Graphics2D graphics = (Graphics2D) newImage.getGraphics();
		graphics.setPaint(backgrounColor);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		graphics.rotate(Math.toRadians(angle), newImage.getWidth() / 2,
				newImage.getHeight() / 2);
		// Use this if pictures are NOT square-shaped.
		// graphics.translate((newImage.getWidth() - image.getWidth()) / 2,
		// (newImage.getHeight() - image.getHeight()) / 2);
		graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
				null);
		graphics.dispose();
		return newImage;
	}

	/**
	 * Scales image and return in x and y direction by factors scaleX and
	 * scaleY.
	 * 
	 * @param scaleX
	 *            - scaling factor for x axis.
	 * @param scaleY
	 *            - scaling factor for y axis.
	 * @return
	 */
	public BufferedImage scale(double scaleX, double scaleY) {
		int width = image.getWidth();
		int height = image.getHeight();
		Image scaledImage = image.getScaledInstance((int) (width * scaleX),
				(int) (height * scaleY), Image.SCALE_SMOOTH);
		BufferedImage bufferedScaledImage = imageToBufferedImage(scaledImage);
		return bufferedScaledImage;
	}

	/**
	 * Helper method for converting Image into BufferedImage.
	 * 
	 * @param img
	 *            - Image object.
	 * @return BufferedImage object representing img parameter.
	 */
	public BufferedImage imageToBufferedImage(Image img) {
		BufferedImage bi = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(img, 0, 0, null);
		bg.dispose();
		return bi;
	}

	/**
	 * Split image into parts and returns it like array.
	 * 
	 * @param vertical
	 *            - number of chunks vertically.
	 * @param horisontal
	 *            - number of chunks horizontally.
	 * @return array containing (vertical*horizontal) chunks of original image.
	 */
	public BufferedImage[] splitImage(int vertical, int horizontal) {

		int numParts = vertical * horizontal;
		int partWidth = image.getWidth() / horizontal;
		int partHeight = image.getHeight() / vertical;

		BufferedImage[] parts = new BufferedImage[numParts];
		int count = 0;

		for (int i = 0; i < horizontal; i++) {
			for (int j = 0; j < vertical; j++) {
				// parts[count++] = ImageUtilities.cropImage(image, i *
				// partWidth, j * partHeight, i * partWidth + partWidth, j *
				// partHeight + partHeight);
			}
		}
		System.out.println("Successfully divided!");
		return parts;

	}
}
