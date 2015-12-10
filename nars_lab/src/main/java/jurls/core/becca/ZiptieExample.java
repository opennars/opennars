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
public abstract class ZiptieExample extends IOMatrixExample  {

    int delayMS = 5;
    final Ziptie ziptie;
    double[] output;
    
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ZiptieExample(Ziptie z) {

        ziptie = z;
    }

    @Override
    public long getDelayMS() {
        return delayMS;
    }

    @Override
    public double[] update(int cycle, double[] in) {
        return output = ziptie.in(in, output);
    }
    
    
    
    public static void main(String[] args) {
        
        int inputs = 32;
        int outputs = 8;
        
        Ziptie z = new AEZiptie2(inputs, outputs);
        
        ZiptieExample ze = new ZiptieExample(z) {

            double[] in = new double[inputs];
            
            @Override
            public double[] input(int cycle) {
                double baseFreq = 0.01;
                double target = cycle * 0.1;
                
                for (int i = 0; i < in.length; i++) {
                    //in[i] = ((cycle & (1 << i)) > 0) ? 1.0 : 0;
                    //in[i] = 0.5 + 0.5 * Math.sin(cycle * baseFreq * (1+i));
                    in[i] = 1.0 / (1.0 + Math.abs( i - (target % in.length) ));
                    
                    
                }            
                return in;
            }

            
        };
                
        ze.newWindow(500, 500);
        
        new Thread(ze).start();
    }

    
}
