package nars.rl.horde;

import org.apache.commons.math3.linear.RealVector;


public interface LinearLearner {

    RealVector weights();

    void resetWeight(int index);

    double error();

}
