package jurls.reinforcementlearning.domains.wander.brain;

import jurls.reinforcementlearning.domains.wander.Player;
import org.apache.commons.math3.util.MathUtils;

public class MyPerception {

	public static final int RADAR_ANGLES = 8;
	public static final int RADAR_DISTS = 2; // rows
	private static final long serialVersionUID = 1L;
	public static final double RADAR_R = Math.PI * 2.0 / RADAR_ANGLES; // 0.6 =
																		// mostly
																		// front
	public static final double RADAR_D = 25;
	public static final int RADAR_D0 = 1;
	private final Player player;

	public MyPerception(Player player) {
		this.player = player;
		// we omit the prediction of the farthest radar input
		// this.setForeseePerc(new MyForeseePerc());
	}

	public double[] updateInputValues(double[] input) {
		if (input == null)
			input = new double[RADAR_DISTS * (RADAR_ANGLES * 2 + 1) + 1];

		int j = 0;

		// input[j++] = (player.angle % (Math.PI*2) / (Math.PI*2) - 0.5);

		input[j++] = 0.8f * (MathUtils.normalizeAngle(player.angle, 0)
				/ (Math.PI) - 1.0);

		for (int d = RADAR_D0; d <= RADAR_DISTS; d++) {
			for (int a = -RADAR_ANGLES; a <= RADAR_ANGLES; a++) {
				double xPerc = xPerc(d, a);
				double yPerc = yPerc(d, a);
				input[j++] = player.getWorld().pointInObstacle(xPerc, yPerc)
						? 1.0
						: -1.0;
			}
		}
		return input;
	}

	public double yPerc(int d, int a) {
		return player.y + Math.sin(player.angle - a * RADAR_R) * d * RADAR_D;
	}

	public double xPerc(int d, int a) {
		return player.x + Math.cos(player.angle - a * RADAR_R) * d * RADAR_D;
	}

	public boolean isUnipolar() {
		return false;
	}

	public Player getPlayer() {
		return player;
	}

}
