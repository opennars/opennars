package nars.util.data;

import nars.NAR;

/**
 *
 * @author me
 */


abstract public class VectorMap {

    public final UniformVector input;
    public final UniformVector output;
    
    public VectorMap(NAR n, String prefix, int numInputs, float inputPriority, int numOutputs, float outputPriority) {
        this.input = new UniformVector(n, prefix + "_i", new double[numInputs]).setPriority(inputPriority);
        this.output =  new UniformVector(n, prefix + "_o", new double[numOutputs]).setPriority(outputPriority);
        
    }
    
    public void update() {
       map(input.data, output.data);
       input.update();
       output.update();
    }
    
    abstract protected void map(double[] in, double[] out);

    
}
