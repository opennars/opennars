package nars.rl.horde.math;

import java.io.Serializable;


public class MovingAverage implements Serializable {
    private static final long serialVersionUID = -303484486232439250L;
    private final double alpha;
    private double average = 0.0;
    private double d = 0.0;
    protected double movingAverage = 0.0;

    static public double discountToTimeSteps(double discount) {
        //assert discount >= 0 && discount < 1.0;
        return 1 / (1 - discount);
    }

    static public double timeStepsToDiscount(int timeSteps) {
        //assert timeSteps > 0;
        return 1.0 - 1.0 / timeSteps;
    }

    public MovingAverage(int timeSteps) {
        alpha = 1.0 - timeStepsToDiscount(timeSteps);
    }

    public MovingAverage(double tau) {
        this.alpha = tau;
    }

    public double update(double value) {
        average = (1 - alpha) * average + alpha * value;
        d = (1 - alpha) * d + alpha;
        movingAverage = average / d;
        return value;
    }

    public double average() {
        return movingAverage;
    }

    public void reset() {
        average = 0.0;
        d = 0.0;
        movingAverage = 0.0;
    }

    public double d() {
        return d;
    }

    public double alpha() {
        return alpha;
    }
}
