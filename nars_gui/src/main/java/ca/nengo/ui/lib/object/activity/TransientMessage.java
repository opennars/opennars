package ca.nengo.ui.lib.object.activity;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.activity.Fader;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;
import org.piccolo2d.activities.PActivity;

import java.awt.geom.Point2D;

/**
 * A message that appears in the World and disappears smoothly after a duration.
 * 
 * @author Shu Wu
 */
public class TransientMessage extends Text {

	static final int ANIMATE_MSG_DURATION = 4000;

	public TransientMessage(String text) {
		super(text);
		setFont(NengoStyle.FONT_BOLD);
		setTextPaint(NengoStyle.COLOR_NOTIFICATION);
		setConstrainWidthToTextWidth(true);
		setPickable(false);
		setChildrenPickable(false);

	}

	public void popup(long delayMS) {
		long startTime = System.currentTimeMillis() + delayMS;
		this.setVisible(false);

		PActivity showPopupActivity = new PActivity(0) {

			@Override
			protected void activityFinished() {
				TransientMessage.this.setVisible(true);
				Point2D startingOffset = getOffset();
				animateToPositionScaleRotation(startingOffset.getX(),
						startingOffset.getY() - 50, 1, 0, ANIMATE_MSG_DURATION);

				PActivity fadeOutActivity = new Fader(TransientMessage.this,
						ANIMATE_MSG_DURATION, 0f);
				UIEnvironment.getInstance().addActivity(fadeOutActivity);

				PActivity removeActivity = new PActivity(0) {

					@Override
					protected void activityStarted() {
						TransientMessage.this.removeFromParent();
					}

				};
				removeActivity.startAfter(fadeOutActivity);
				UIEnvironment.getInstance().addActivity(removeActivity);
			}
		};
		showPopupActivity.setStartTime(startTime);

		UIEnvironment.getInstance().addActivity(showPopupActivity);

	}
}
