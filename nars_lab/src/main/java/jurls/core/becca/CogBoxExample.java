/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

/**
 *
 * @author me
 */
public abstract class CogBoxExample extends IOMatrixExample  {

    int delayMS = 5;
    final CogBox cog;
    double[] output;
    
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public CogBoxExample(CogBox z) {

        cog = z;
    }

    @Override
    public long getDelayMS() {
        return delayMS;
    }

    @Override
    public double[] update(int cycle, double[] in) {
        return output = cog.in(in);
    }
    
    
    
    public static void main(String[] args) {
        
        int inputs = 16;
        int cogs = 2;
        int numOutputs = 16;
        
        CogBox z = new CogBox(inputs, cogs, numOutputs);
        
        CogBoxExample ze = new CogBoxExample(z) {

            double[] in = new double[inputs];
            
            @Override
            public double[] input(int cycle) {
                double baseFreq = 0.01;
                double freqDiff = 0.03;
                double target = cycle * 0.1;
                for (int i = 0; i < in.length; i++) {
                    //in[i] = ((cycle & (1 << i)) > 0) ? 1.0 : 0;
                    //in[i] = 0.5 + 0.5 * Math.sin(cycle * baseFreq * (1+i * freqDiff) );
                    in[i] = 1.0 / (1.0 + Math.abs( i - (target % in.length) ));
                }            
                
                return in;
            }

            
        };
                
        ze.newWindow(500, 500);
        
        new Thread(ze).start();
    }

    
}
