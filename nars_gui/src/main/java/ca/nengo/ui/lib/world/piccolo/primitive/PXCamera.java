package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.world.WorldObject;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.activities.PTransformActivity;
import org.piccolo2d.util.PUtil;

import java.awt.geom.AffineTransform;


public class PXCamera extends PCamera implements PiccoloNodeInWorld {

	private static final long serialVersionUID = 1L;

	private PTransformActivity currentActivity;

	private WorldObject wo;

	@Override
	public void addChild(int index, PNode child) {
		super.addChild(index, child);
		if (wo != null && child != null && child instanceof PiccoloNodeInWorld) {
			wo.childAdded(((PiccoloNodeInWorld) child).getWorldObject());
		}
	}

	public PNode removeChild(int arg0) {
		PNode node = super.removeChild(arg0);

		if (wo != null && node != null && node instanceof PiccoloNodeInWorld) {
			wo.childRemoved(((PiccoloNodeInWorld) node).getWorldObject());
		}

		return node;
	}

	/*
	 * Modification to PNode's animateToTransform. This animation is sequenced
	 * so that the previous transform animation finishes first (non-Javadoc)
	 * 
	 * @see edu.umd.cs.piccolo.PNode#animateToTransform(java.awt.geom.AffineTransform,
	 *      long)
	 */
	@Override
	public PTransformActivity animateViewToTransform(AffineTransform destTransform, long duration) {
		if (duration == 0) {
			setViewTransform(destTransform);
			return null;
		} else {
			PTransformActivity.Target t = new PTransformActivity.Target() {
				public void getSourceMatrix(double[] aSource) {
					PXCamera.this.getViewTransform().getMatrix(aSource);
				}

				public void setTransform(AffineTransform aTransform) {
					PXCamera.this.setViewTransform(aTransform);
				}
			};

			if (currentActivity != null) {
				currentActivity.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
			}

			currentActivity = new PTransformActivity(duration, PUtil.DEFAULT_ACTIVITY_STEP_RATE, t,
					destTransform);

			addActivity(currentActivity);
			return currentActivity;
		}
	}

	public WorldObject getWorldObject() {
		return wo;
	}

	public boolean isAnimating(long now) {
		return false;
	}

	public void setWorldObject(WorldObject worldObjectParent) {
		this.wo = worldObjectParent;
	}
}
