package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.util.UIEnvironment;
import org.piccolo2d.PCamera;
import org.piccolo2d.PLayer;
import org.piccolo2d.PNode;
import org.piccolo2d.PRoot;
import org.piccolo2d.util.PPaintContext;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A Grid layer which is zoomable and pannable.
 * 
 * @author Shu Wu
 */
public class PXGrid extends PXLayer {

	private static final Line2D gridLine = new Line2D.Double();

    private static final Stroke gridStroke = new BasicStroke(1);

	private static boolean gridVisible = true;

	private static final long serialVersionUID = 1L;

	public static PXGrid createGrid(PCamera camera, PRoot root,
			Color gridPaint, double gridSpacing) {

        PXGrid gridLayer = new PXGrid(gridPaint, gridSpacing);
		gridLayer.setBounds(camera.getViewBounds());

		root.addChild(gridLayer);

		camera.addLayer(0, gridLayer);

		// add constrains so that grid layers bounds always match cameras view
		// bounds. This makes
		// it look like an infinite grid.
		camera.addPropertyChangeListener(PNode.PROPERTY_BOUNDS,
				new CameraPropertyChangeListener(camera, gridLayer));

		camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM,
				new CameraPropertyChangeListener(camera, gridLayer));

		return gridLayer;
	}

	public static boolean isGridVisible() {
		return gridVisible;
	}

	public static void setGridVisible(boolean gridVisible) {
		PXGrid.gridVisible = gridVisible;
		if (UIEnvironment.getInstance() != null)
			UIEnvironment.getInstance().getWorld().repaint();
	}

	private Color gridPaint;
    private Color bgColor = null;

	private final double gridSpacing;

	protected PXGrid(Color gridPaint, double gridSpacing) {
		super();
		this.gridPaint = gridPaint;
		this.gridSpacing = gridSpacing;
		setPickable(false);
		setChildrenPickable(false);
	}

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    @Override
	protected void paint(PPaintContext paintContext) {

        Graphics2D g2 = paintContext.getGraphics();

        double x = getX();
        double y = getY();
        final double ww = getWidth();
        final double hh = getHeight();

        if (bgColor!=null) {
            g2.setPaint(bgColor);

            g2.fillRect((int)x, (int)y, (int)ww, (int)hh);
        }

		// make sure grid gets drawn on snap to grid boundaries. And
		// expand a little to make sure that entire view is filled.

        double bx = (x - (x % gridSpacing)) - gridSpacing;
		double by = (y - (y % gridSpacing)) - gridSpacing;
		double rightBorder = x + ww + gridSpacing;
		double bottomBorder = y + hh + gridSpacing;

		Rectangle2D clip = paintContext.getLocalClip();

		g2.setStroke(gridStroke);
		g2.setPaint(gridPaint);

		for (double cx = bx; cx < rightBorder; cx += gridSpacing) {
			gridLine.setLine(cx, by, cx, bottomBorder);
			if (clip.intersectsLine(gridLine)) {
				g2.draw(gridLine);
			}
		}

		for (double cy = by; cy < bottomBorder; cy += gridSpacing) {
			gridLine.setLine(bx, cy, rightBorder, cy);
			if (clip.intersectsLine(gridLine)) {
				g2.draw(gridLine);
			}
		}
	}

    public void setGridColor(Color gridColor) {
        this.gridPaint = gridColor;
    }
}

class CameraPropertyChangeListener implements PropertyChangeListener {
	private final PCamera camera;

	private final PLayer gridLayer;

	public CameraPropertyChangeListener(PCamera camera, PLayer gridLayer) {
		super();
		this.camera = camera;
		this.gridLayer = gridLayer;
	}

	public void propertyChange(PropertyChangeEvent evt) {
        gridLayer.setBounds(camera.getViewBounds());
	}
}
