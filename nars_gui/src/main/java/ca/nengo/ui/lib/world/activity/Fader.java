package ca.nengo.ui.lib.world.activity;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.world.WorldObject;
import org.piccolo2d.activities.PInterpolatingActivity;

/**
 * Activity which gradually changes the transparency of an node
 * 
 * @author Shu Wu
 */
public class Fader extends PInterpolatingActivity {

	private final WorldObject node;

	private float startingTransparency;

	private final float targetTransparency;

	/**
	 * @param target
	 *            Node target
	 * @param duration
	 *            Duration of the activity
	 * @param finalOpacity
	 *            Transparency target
	 */
	public Fader(WorldObject target, long duration, float finalOpacity) {
		super(duration,
				(int) (1000 / NengoStyle.ANIMATION_TARGET_FRAME_RATE));
		this.node = target;
		this.targetTransparency = finalOpacity;
	}

	@Override
	protected void activityStarted() {
		startingTransparency = node.getTransparency();
	}

	@Override
	public void setRelativeTargetValue(float zeroToOne) {

		super.setRelativeTargetValue(zeroToOne);

		float transparency = startingTransparency
				+ ((targetTransparency - startingTransparency) * (zeroToOne));

		node.setTransparency(transparency);

	}

}
