/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import java.util.ArrayList;
import java.util.List;

/**
 * parallel cogs
 */
public class CogBox {
    
    Ziptie zip;
    List<Cog> cogs;
    private final int cogInputs;
    private final int inputs;
    private final double[] output;

    public CogBox(int inputs, int ncogs, int outputs) {
        if (outputs % ncogs!=0)
            throw new RuntimeException("# outputs must be a multiple of # cogs");
        
        this.inputs = inputs;
        output = new double[outputs];
        cogInputs = (int)Math.ceil(((double)inputs) / ncogs);
        
        cogs = new ArrayList(ncogs);
        for (int c = 0; c < ncogs; c++) {
            cogs.add(new Cog(new Daisychain(), new AEZiptie2(cogInputs*cogInputs, outputs / ncogs)));
        }
        
    }
    
    public double[] in(double[] d) {
        
        double[] ci = new double[cogInputs];
        int p = 0, s = 0;
        for (Cog c : cogs) {
            int ii = Math.min(d.length - p, cogInputs);
            System.arraycopy(d, p, ci, 0, ii);
            double[] co = c.in(ci);
            System.arraycopy(co, 0, output, s, co.length);
            p += cogInputs;
            s += co.length;
        }
        
        return output;
    }
    
    
    
}
