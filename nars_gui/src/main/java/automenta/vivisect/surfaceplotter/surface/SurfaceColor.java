package automenta.vivisect.surfaceplotter.surface;

import java.awt.*;

/** Interface used by JSurface for every color. Warning, some color are not suitable for some drawing, be careful to sync it with the SurfaceModel
 * 
 * @author eric
 *
 */
public interface SurfaceColor {

	public Color getBackgroundColor();

	public Color getLineBoxColor();

	public Color getBoxColor();

	public Color getLineColor();

	public Color getTextColor();

	public Color getLineColor(int curve, float z);

	public Color getPolygonColor(int curve, float z);

	public Color getFirstPolygonColor(float z);

	public Color getSecondPolygonColor(float z);

}