/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Discretinuous;
import nars.io.ExampleFileInput;
import nars.util.NALPerformance;

/**
 *
 * @author me
 */
public class DetectBenefitsOfPrologMirroring {
    private NARPrologMirror currentPrologEternal;
    private NARPrologMirror currentPrologPresent;

    public DetectBenefitsOfPrologMirroring(String path) throws Exception {
        Parameters.DEBUG = false;
        
        String input = ExampleFileInput.get(path).getSource();
        
        NAR normal = newNAR();
        NAR prolog = newPrologNAR(newNAR());
        
        int cycles = 2500;
        
        NALPerformance np = new NALPerformance(normal, input, cycles);
        np.run();
        NALPerformance pp = new NALPerformance(prolog, input, cycles);
        pp.run();
        
        
        //np.printResults(System.out);
        //pp.printResults(System.out);
        if (np.getScore() != pp.getScore()) {
            System.out.println();
            System.out.println(path + "\n  " + np.getScore() + " " + pp.getScore());
        }
        
        //System.out.println(currentPrologEternal.getBeliefsTheory());
        //System.out.println(currentPrologPresent.getBeliefsTheory());
        
    }
    
    
    
    public static void main(String[] arg) throws Exception {
        for (String path : ExampleFileInput.getUnitTestPaths()) {
            new DetectBenefitsOfPrologMirroring(path);
        }
    }

    private NAR newNAR() {
        NAR nar = new NAR(new Discretinuous().setInternalExperience(null));
        return nar;
    }

    private NAR newPrologNAR(NAR n) {
        float confidenceThresh = 0.3f;
        currentPrologEternal = new NARPrologMirror(n, confidenceThresh, true, true, false);        
        currentPrologPresent = new NARPrologMirror(n, confidenceThresh, true, false, true);        
        return n;
    }

}
