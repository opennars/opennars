package nars.guifx.graph2.layout;

import automenta.vivisect.dimensionalize.IterativeLayout;
import nars.guifx.graph2.SpaceGrapher;
import org.apache.commons.math3.linear.ArrayRealVector;


public class None implements IterativeLayout {

    public final ArrayRealVector zero = new ArrayRealVector(2);

    @Override
    public void run(SpaceGrapher graph, int iterations) {

    }


    @Override
    public void init(Object n) {

    }
}
