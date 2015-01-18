/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars;

import nars.build.Default;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.Reaction;
import nars.io.ExampleFileInput;
import nars.io.TextOutput;
import nars.io.meter.event.HitMeter;
import nars.io.meter.event.ValueMeter;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.meta.NARMetrics;
import nars.io.NALPerformance;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author me
 */
public class DetectBenefitsOfPrologMirroring implements Reaction {
    private static PrintStream csvOut;
    private static PrintStream logOut;
    private final NAR prolog;
    private final NARMetrics trace;

    static long randomSeed = 1;

    static int line = 0;

    static int numCycles;

    static String outputFile = null;// "/tmp/h.csv";
    static String logFile = null; //"/tmp/h.log";

    private ValueMeter prologEternalBeliefs;
    private HitMeter prologEternalAnswers;

    private NARPrologMirror prologMirror;

    static double totalPrologScore = 0;



    public DetectBenefitsOfPrologMirroring(String path, int cycles) throws Exception {
        Parameters.DEBUG = false;



        String input = ExampleFileInput.get(path).getSource();


        Memory.resetStatic(randomSeed);
        NAR normal = newNAR();
        NALPerformance np = new NALPerformance(normal, input);

        if (logOut!=null) {
            logOut.println(path);
            logOut.println(input + "\n");
            logOut.println("RUN NORMAL");
            new TextOutput(normal, logOut);
        }
        np.run(cycles);

        double controlScore = np.getScore();
        //normal.reset();
        //normal = null; //GC


        Memory.resetStatic(randomSeed);
        prolog = newPrologNAR(newNAR());

        NALPerformance pp = new NALPerformance(prolog, input);

        trace = new NARMetrics(prolog, 128);
        trace.addMeter(prologEternalBeliefs = new ValueMeter("prolog.eternal.beliefs"));
        trace.addMeter(prologEternalAnswers = new HitMeter("prolog.eternal.answers"));

        if (logOut!=null) {
            logOut.println("RUN PROLOG");
            new TextOutput(prolog, logOut);
        }
        pp.run(cycles);


        //np.printResults(System.out);
        //pp.printResults(System.out);
        double prologScore = pp.getScore();


        /*
        if (normal.time() != prolog.time())
            System.out.println(normal + " " + normal.time() + " " + prolog + " " + prolog.time());
        */
        String summary = "\"" + path + "\"," + controlScore + "," + prologScore;
        //if (controlScore != prologScore) {
            System.out.println(summary);
        //}

        if (!Double.isFinite(controlScore)) controlScore = cycles;
        if (!Double.isFinite(prologScore)) prologScore = cycles;

        DetectBenefitsOfPrologMirroring.totalPrologScore += (controlScore - prologScore);
        
        //System.out.println(prologMirror.getBeliefsTheory());
        //System.out.println(currentPrologPresent.getBeliefsTheory());

        prolog.reset();

        if (csvOut !=null) {
            csvOut.flush();
        }
        if (logOut!=null) {
            logOut.println(summary + "\n\n");
            logOut.flush();
        }
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.CycleEnd.class) {
            prologEternalBeliefs.set(prologMirror.getBeliefs().size());

            if (csvOut !=null) {
                if (line++ == 0)
                    trace.getMetrics().printCSVHeader(csvOut);
                trace.getMetrics().printCSVLastLine(csvOut);
            }
        }
    }
    
    public static void main(String[] arg) throws Exception {
        if (outputFile!=null)
            csvOut = new PrintStream(new FileOutputStream(outputFile));
        if (logFile!=null)
            logOut = new PrintStream(new FileOutputStream(logFile));

        int numTests = 0;
        totalPrologScore = 0;
        numCycles = 200;
        for (String path : ExampleFileInput.getUnitTestPaths()) {
            new DetectBenefitsOfPrologMirroring(path, numCycles);
            numTests++;
        }
        System.err.println("RESULT: " + totalPrologScore / numTests);
    }

    private NAR newNAR() {
        //NAR nar = new NAR(new Discretinuous().setInternalExperience(null));
        NAR nar = new NAR(new Default());
        return nar;
    }

    private NAR newPrologNAR(NAR n) {
        float confidenceThresh = 0.5f;
        prologMirror = new NARPrologMirror(n, confidenceThresh, true, true, true) {
            @Override
            public Term answer(Task question, Term t, nars.prolog.Term pt) {
                Term a = super.answer(question, t, pt);
                if (a!=null) prologEternalAnswers.hit();
                return a;
            }
        }.setInputMode(AbstractMirror.InputMode.InputTask);

        n.on(Events.CycleEnd.class, this);
        return n;
    }


}
