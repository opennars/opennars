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
public abstract class CogExample extends IOMatrixExample  {

    int delayMS = 1;
    final Cog cog;
    double[] output;
    
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public CogExample(Cog z) {

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
        
        int inputs = 26;
        int numOutputs = 4;
        
        Cog z = new Cog(new Daisychain(), new AEZiptie(inputs*inputs, numOutputs));
        
        CogExample ze = new CogExample(z) {

            double[] in = new double[inputs];
            
            @Override
            public double[] input(int cycle) {
                double baseFreq = 0.01;
                double freqDiff = 0.03;
                for (int i = 0; i < in.length; i++) {
                    //in[i] = ((cycle & (1 << i)) > 0) ? 1.0 : 0;
                    in[i] = 0.5 + 0.5 * Math.sin(cycle * baseFreq * (1+i * freqDiff) );
                }            
                return in;
            }

            
        };
                
        ze.newWindow(500, 500);
        
        new Thread(ze).start();
    }

    
}
