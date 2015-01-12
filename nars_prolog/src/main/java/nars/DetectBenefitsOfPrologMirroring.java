/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars;

import nars.core.EventEmitter;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Discretinuous;
import nars.entity.Task;
import nars.io.ExampleFileInput;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ValueMeter;
import nars.language.Term;
import nars.util.NALPerformance;
import nars.util.NARTrace;

import java.io.PrintStream;

/**
 *
 * @author me
 */
public class DetectBenefitsOfPrologMirroring implements EventEmitter.EventObserver {
    private final NAR prolog;
    private final NARTrace trace;
    int line = 0;

    private ValueMeter prologEternalBeliefs;
    private HitMeter prologEternalAnswers;
    private ValueMeter prologPresentBeliefs;
    private HitMeter prologPresentAnswers;

    private NARPrologMirror currentPrologEternal;
    private NARPrologMirror currentPrologPresent;

    PrintStream output;

    public DetectBenefitsOfPrologMirroring(String path) throws Exception {
        Parameters.DEBUG = false;

        output = System.out;

        String input = ExampleFileInput.get(path).getSource();
        int cycles = 500;


        NAR normal = newNAR();
        NALPerformance np = new NALPerformance(normal, input, cycles);
        np.run();
        double controlScore = np.getScore();
        normal.reset();
        normal = null; //GC



        prolog = newPrologNAR(newNAR());

        NALPerformance pp = new NALPerformance(prolog, input, cycles);

        trace = new NARTrace(prolog);
        trace.addMeter(prologEternalBeliefs = new ValueMeter("prolog.eternal.beliefs"));
        trace.addMeter(prologEternalAnswers = new HitMeter("prolog.eternal.answers"));
        trace.addMeter(prologPresentBeliefs = new ValueMeter("prolog.present.beliefs"));
        trace.addMeter(prologPresentAnswers = new HitMeter("prolog.present.answers"));

        pp.run();
        

        //np.printResults(System.out);
        //pp.printResults(System.out);
        if (controlScore != pp.getScore()) {
            System.out.println();
            System.out.println(path + "\n  " + np.getScore() + " " + pp.getScore());
        }
        
        //System.out.println(currentPrologEternal.getBeliefsTheory());
        //System.out.println(currentPrologPresent.getBeliefsTheory());

        prolog.reset();
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.CycleEnd.class) {
            prologEternalBeliefs.set(currentPrologEternal.getBeliefs().size());
            prologPresentBeliefs.set(currentPrologPresent.getBeliefs().size());

            if (line++ == 0) {
                trace.metrics.printCSVHeader(output);
            }
            trace.metrics.printCSVLastLine(output);
        }
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
        float confidenceThresh = 0.6f;
        currentPrologEternal = new NARPrologMirror(n, confidenceThresh, true, true, false) {
            @Override
            public Term answer(Task question, Term t, nars.prolog.Term pt) {
                Term a = super.answer(question, t, pt);
                if (a!=null) prologEternalAnswers.hit();
                return a;
            }
        };
        currentPrologPresent = new NARPrologMirror(n, confidenceThresh, true, false, true) {
            @Override
            public Term answer(Task question, Term t, nars.prolog.Term pt) {
                Term a = super.answer(question, t, pt);
                if (a!=null) prologPresentAnswers.hit();
                return a;
            }
        };
        n.on(Events.CycleEnd.class, this);
        return n;
    }


}
