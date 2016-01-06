package automenta.vivisect.surfaceplotter.surface;

import java.awt.*;

/**
 * Interface used by JSurface for every color. Warning, some color are not
 * suitable for some drawing, be careful to sync it with the SurfaceModel
 * 
 * @author eric
 * 
 */
public interface SurfaceColor {

	Color getBackgroundColor();

	Color getLineBoxColor();

	Color getBoxColor();

	Color getLineColor();

	Color getTextColor();

	Color getLineColor(int curve, float z);

	Color getPolygonColor(int curve, float z);

	Color getFirstPolygonColor(float z);

	Color getSecondPolygonColor(float z);

}