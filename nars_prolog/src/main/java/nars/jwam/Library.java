package nars.jwam;

import nars.jwam.builtins.Arithmetic;
import nars.jwam.builtins.PredicateDynamics;

public class Library {

    public final WAM wam;
    public final Arithmetic arithmetic;
    public final PredicateDynamics pred_dynamics;

    public Library(WAM wam) {
        this.wam = wam;
        arithmetic = new Arithmetic(wam);
        pred_dynamics = new PredicateDynamics(wam);
    }

    public boolean call(int argument) {
        if (arithmetic.canHandle(argument)) {
            return arithmetic.handleOperator(argument);
        } else if (pred_dynamics.canHandle(argument)) {
            return pred_dynamics.handleOperator(argument);
        }
        return false;
    }

    public PredicateDynamics getPredDynamics() {
        return pred_dynamics;
    }
    
}
