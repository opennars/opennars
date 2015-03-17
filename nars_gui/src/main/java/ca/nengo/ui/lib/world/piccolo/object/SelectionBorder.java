package ca.nengo.ui.lib.world.piccolo.object;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.WorldObject.Property;
import ca.nengo.ui.lib.world.WorldSky;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A Border instance that can change its object of focus (As long as the object
 * are within the world that the frame is in). Border is attached to the sky
 * layer so there is no attenuation of the edge width when the ground is viewed
 * at a low scale.
 * 
 * @author Shu Wu
 */
public class SelectionBorder implements Listener {
	private final ShapeObject frame;

	private Color frameColor = NengoStyle.COLOR_BORDER_SELECTED;

	private WorldSky frameHolder;

	private WorldObject selectedObj;
    private WorldObject nextSelected;

    /**
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 */
	public SelectionBorder(World world) {
		super();
        frame = ShapeObject.createRectangle(0f, 0f, 1f, 1f);
		init(world);
	}

	/**
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 * @param objSelected
	 *            Object to select initially
	 */
	public SelectionBorder(World world, WorldObject objSelected) {
		this(world);
		setSelected(objSelected);
	}

	/**
	 * Initializes this instance
	 * 
	 * @param world
	 *            World, whose sky, this border shall be added to.
	 */
	private void init(World world) {
		this.frameHolder = world.getSky();

		frame.setPickable(false);

		frameHolder.addChild(frame);
	}

	/**
	 * Updates the bounds of the border to match those of the selected object
	 */
	protected boolean updateBounds() {
		if (selectedObj != null) {
			if (selectedObj.getVisible()) {
				Rectangle2D bounds = selectedObj.objectToSky(selectedObj.getBounds());

                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                if ((w == 0) || (h == 0)) {
                    hide();
                }
                else {
                    frame.setBounds((float) bounds.getX(), (float) bounds.getY(), w, h);
                    frame.setPaint(null);
                    frame.setStrokePaint(frameColor);
                    show();
                    return true;
                }

			} else {
				hide();
			}
		} else {
            if (isShow())
                hide();
		}
        return false;
	}

    public boolean isShow() { return frame.getVisible(); }

    public void destroy() {
        frame.destroy();
    }

    public void show() {
        frameHolder.addPropertyChangeListener(Property.VIEW_TRANSFORM, this);
        frame.setVisible(true);
    }
	public void hide() {
        frameHolder.removePropertyChangeListener(Property.VIEW_TRANSFORM, this);
        frame.setVisible(false);
        this.selectedObj = null;
	}

	public Color getFrameColor() {
		return frameColor;
	}

	public void propertyChanged(Property event) {
		if (event == Property.REMOVED_FROM_WORLD) {
			setSelected(null);
            destroy();
		}
        else
		    updateBounds();
	}

	public void setFrameColor(Color frameColor) {
		this.frameColor = frameColor;
		updateBounds();
	}

	public boolean setSelected(WorldObject newSelected) {
		if (newSelected == selectedObj) {
            if (newSelected!=null)
                updateBounds();
			return false;
		}



        if (this.nextSelected == null) {
            this.nextSelected = newSelected;
            SwingUtilities.invokeLater(this::updateFrame);
        }
        else {
            //will update on the already queued swingevent
            this.nextSelected = newSelected;
        }


        return true;
	}

    /** called in synchrony via event thread */
    private void updateFrame() {

        if (selectedObj != null) {
            selectedObj.removePropertyChangeListener(Property.GLOBAL_BOUNDS, this);
            selectedObj.removePropertyChangeListener(Property.REMOVED_FROM_WORLD, this);
        }


        WorldObject newSelected = nextSelected;
        nextSelected = null;

        selectedObj = newSelected;
        if (selectedObj != null) {
            if (updateBounds()) {
                selectedObj.addPropertyChangeListener(Property.GLOBAL_BOUNDS, this);
                selectedObj.addPropertyChangeListener(Property.REMOVED_FROM_WORLD, this);
            }
            else {
                selectedObj = null;
            }
        } else {
            hide();
        }

    }

}
