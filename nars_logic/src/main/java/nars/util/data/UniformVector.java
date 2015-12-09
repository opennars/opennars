package nars.util.data;

import nars.NAR;
import nars.truth.DefaultTruth;

import java.util.Arrays;

import static nars.util.Texts.n2;

/**
 * Represents a changing 1-dimensional array of double[], each element normalized to 0..1.0
 */
public class UniformVector  {

    float epsilon = DefaultTruth.DEFAULT_TRUTH_EPSILON;
    public double[] lastData = null;
    public final double[] data;
    private final String prefix;
    private final NAR nar;
    private float priority;

    public UniformVector(NAR n, String prefix, double[] data) {
        nar = n;
        this.prefix = prefix;
        this.data = data;
        priority = Float.NaN;
    }

    public void update() {
        if (priority == 0) 
            return;
        
        if ((lastData != null) && (Arrays.equals(lastData, data))) {
            //unchanged
            return;
        }

        boolean changed = false;
        if ((lastData == null) || (lastData.length!=data.length)) {
            //first time
            lastData = new double[data.length];     
            changed = true;
        }

        
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            if ((changed) || (different(v,lastData[i]))) {
                if (v > 1.0) v = 1.0;
                if (v < 0.0) v = 0.0;
                float truth = (float)v;
                float conf = 0.99f;
                s.append('$').append(n2(priority)).append("$ <").append(prefix).append('_').append(i).append(" --> ").append(prefix).append(">. :|: %").append(n2(truth)).append(';').append(n2(conf)).append("%\n");
            }
        }
        
        System.arraycopy(data, 0, lastData, 0, data.length);
        
        nar.input(s.toString());
        
    }

    public boolean different(double a, double b) {
        return Math.abs(a - b) >= epsilon;
    }
    
    public UniformVector setPriority(float p) {
        priority = p;
        return this;
    }
    
    
}
