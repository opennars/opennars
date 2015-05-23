package jurls.reinforcementlearning.domains.follow;

/**
 * Created by me on 5/21/15.
 */
public class Follow1DThreePoint extends Follow1DTwoPoint {

    public Follow1DThreePoint(double speed, double targetSpeed) {
        this.speed = speed;
        this.targetSpeed = targetSpeed;
    }

    @Override
    public int numActions() {
        return 3;
    }
    @Override
    public boolean takeAction(int action) {
        switch (action) {
            case 0: return takeActionVelocity(-1);
            case 2: return takeActionVelocity(1);
            default:
            case 1: return takeActionVelocity(0);
        }
    }
}
