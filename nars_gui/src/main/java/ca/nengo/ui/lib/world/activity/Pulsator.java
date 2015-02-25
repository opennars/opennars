package ca.nengo.ui.lib.world.activity;

import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.activities.PActivity.PActivityDelegate;

/**
 * Pulsates the target World Object until finished.
 * 
 * @author Shu Wu
 */
public class Pulsator {
	public static final long PULSATION_RATE_PER_SEC = 1;

	private static final long PULSATION_STATE_TRANSITION = (1000 / (PULSATION_RATE_PER_SEC * 2));

	private final WorldObjectImpl target;
	private final float originalTransparency;
	private boolean isPulsating = true;
	PActivity fadeActivity;

	private enum PulsationState {
		FADING_IN, FADING_OUT
	}

	PulsationState pulsationState = PulsationState.FADING_OUT;

	public Pulsator(WorldObjectImpl wo) {
		this.target = wo;
		originalTransparency = wo.getTransparency();
		pulsate();
	}

	public void finish() {
		isPulsating = false;
		fadeActivity.terminate(PActivity.TERMINATE_AND_FINISH);
		target.setTransparency(originalTransparency);
	}

	private void pulsate() {
		if (isPulsating) {
			Util.Assert(fadeActivity == null || !fadeActivity.isStepping(),
					"activities are overlapping");

			if (pulsationState == PulsationState.FADING_IN) {
				pulsationState = PulsationState.FADING_OUT;
				fadeActivity = new Fader(target, PULSATION_STATE_TRANSITION, 1f);
			} else if (pulsationState == PulsationState.FADING_OUT) {
				pulsationState = PulsationState.FADING_IN;
				fadeActivity = new Fader(target, PULSATION_STATE_TRANSITION, 0f);
			} else {
				throw new UnsupportedOperationException();
			}
			fadeActivity.setDelegate(myFaderDelegate);

			UIEnvironment.getInstance().addActivity(fadeActivity);
		}
	}

	final PActivityDelegate myFaderDelegate = new PActivityDelegate() {
		public void activityFinished(PActivity activity) {
			pulsate();
		}

		public void activityStarted(PActivity activity) {
			// do nothing
		}

		public void activityStepped(PActivity activity) {
			// do nothing
		}
	};

}
