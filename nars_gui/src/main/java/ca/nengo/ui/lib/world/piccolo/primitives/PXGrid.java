package ca.nengo.ui.lib.world.piccolo.primitives;

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

	public static PXLayer createGrid(PCamera camera, PRoot root,
			Color gridPaint, double gridSpacing) {

		PXLayer gridLayer = new PXGrid(gridPaint, gridSpacing);
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

	private final Color gridPaint;

	private final double gridSpacing;

	public PXGrid(Color gridPaint, double gridSpacing) {
		super();
		this.gridPaint = gridPaint;
		this.gridSpacing = gridSpacing;
		setPickable(false);
		setChildrenPickable(false);
	}

	@Override
	protected void paint(PPaintContext paintContext) {
		if (!isGridVisible())
			return;

		// make sure grid gets drawn on snap to grid boundaries. And
		// expand a little to make sure that entire view is filled.
		double bx = (getX() - (getX() % gridSpacing)) - gridSpacing;
		double by = (getY() - (getY() % gridSpacing)) - gridSpacing;
		double rightBorder = getX() + getWidth() + gridSpacing;
		double bottomBorder = getY() + getHeight() + gridSpacing;

		Graphics2D g2 = paintContext.getGraphics();
		Rectangle2D clip = paintContext.getLocalClip();

		g2.setStroke(gridStroke);
		g2.setPaint(gridPaint);

		for (double x = bx; x < rightBorder; x += gridSpacing) {
			gridLine.setLine(x, by, x, bottomBorder);
			if (clip.intersectsLine(gridLine)) {
				g2.draw(gridLine);
			}
		}

		for (double y = by; y < bottomBorder; y += gridSpacing) {
			gridLine.setLine(bx, y, rightBorder, y);
			if (clip.intersectsLine(gridLine)) {
				g2.draw(gridLine);
			}
		}
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
