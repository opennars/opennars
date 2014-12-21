/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.predict;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.TimelineVis;
import java.awt.Color;
import nars.core.Events.TaskImmediateProcess;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.core.control.NAL;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.output.ConceptsPanel;
import nars.io.ChangedTextInput;
import nars.io.TextOutput;
import nars.io.narsese.Narsese;
import nars.language.Tense;
import nars.language.Term;

/**
 *
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/predict/sunspot/PredictSunspotElman.java
 * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/neural/recurrent/elman/ElmanXOR.java
 *
 * @author me
 */
public class Predict_NARS_Core {
    
    static float signal = 0;
    
    static TreeMLData[] predictions;
    
    protected boolean allowTask(NAR n,Task t) {
        if (t.sentence.isEternal()) {
            return false;
        }
        if ((t.sentence.getOccurenceTime() > n.time())) {


            Term term = t.getTerm();
            int time = (int) t.sentence.getOccurenceTime();
            int value = -1;
            float conf = t.sentence.truth.getConfidence();
            float expect = 2f * (t.sentence.truth.getFrequency() - 0.5f) * conf;
            String ts = term.toString();
            if (ts.startsWith("<x_t0 --> y")) {
                char cc = ts.charAt("<x_t0 --> y".length());

                value = cc - '0';
                predictions[0].add((int)n.time(), value );
            }


           /* if (value != -1) {

                //predictions[(int)value].addPlus(time, expect);
                for (int tt = time - duration / 2; tt <= time + duration / 2; tt++) {

                    double smooth = 1;
                    expect *= getPredictionEnvelope(time-tt, smooth * duration*2f);

                    //
                    //if (future)
                   //     predictions[value].addPlus(tt, expect);
                   // else
                   //     reflections[value].addPlus(tt, expect);
                    ///

                }

            }*/



            return true;
        }
        return false;

    }
    
    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {

        Parameters.DEBUG = true;
        int duration = 8;
        float freq = 1.0f / duration * 0.15f;        
        double missingDataRate = 0.1;
        double noiseRate = 0.02;
        boolean onlyNoticeChange = false;
        int thinkInterval = onlyNoticeChange ? 1 : 2;
        int discretization = 3;

        NAR n = new NAR(new Default().setInternalExperience(null));
        n.param.duration.set(duration);
        //n.param.duration.setLinear(0.5);
        n.param.conceptBeliefsMax.set(64);
        //n.param.conceptForgetDurations.set(16);
        
        n.on(TaskImmediateProcess.class, new TaskImmediateProcess() {
            int curmax=0;
            @Override
            public void onProcessed(Task t, NAL n) {
                if (t.sentence.getOccurenceTime() >= n.memory.time()) {
                    Term term = t.getTerm();
                    int time = (int) t.sentence.getOccurenceTime();
                    int value = -1;
                    float conf = t.sentence.truth.getConfidence();
                    float expect = 2f * (t.sentence.truth.getFrequency() - 0.5f) * conf;
                    String ts = term.toString();
                    if (ts.startsWith("<{x} --> y")) {
                        char cc = ts.charAt("<{x} --> y".length());

                        value = cc - '0';
                        //predictions[0].addPlus((int) n.memory.time(), Math.random()*100);
                        if((int) t.sentence.getOccurenceTime()>=curmax)
                            predictions[0].add((int) t.sentence.getOccurenceTime(), (value)/10.0 );
                        curmax=(int) Math.max(t.sentence.getOccurenceTime(), curmax);
                    }
                }
            }
        });
        
        Discretize discretize = new Discretize(n, discretization);
        
        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);
        predictions = new TreeMLData[discretization];
        TreeMLData[] reflections = new TreeMLData[discretization];

        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = new TreeMLData("Pred" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            
            reflections[i] = new TreeMLData("Refl" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            reflections[i].setDefaultValue(0.0);
        }
        TimelineVis tc = new TimelineVis(
                new LineChart(observed).thickness(16f).height(128),                          new LineChart(predictions).thickness(16f).height(128),
                new LineChart(reflections).thickness(16f).height(128)
                /*new LineChart(predictions[1]).thickness(16f).height(128),
                new LineChart(predictions[2]).thickness(16f).height(128),*/
        );

        new NWindow("_", new PCanvas(tc)).show(800, 800, true);

        for (Term t : discretize.getValueTerms("x"))
            n.believe(t.toString(), Tense.Present, 0.5f, 0.5f);
        
        n.run(discretization*4);
        

        Concept[] valueBeliefConcepts = discretize.getValueConcepts("x");
        
        NARSwing.themeInvert();
        new NWindow("x", new ConceptsPanel(n, valueBeliefConcepts)).show(900, 600, true);

       // new NARSwing(n);
        
        ChangedTextInput chg=new ChangedTextInput(n);
        
        int prevY = -1, curY = -1;
        
        while (true) {

            n.run(thinkInterval);
            Thread.sleep(3);
            
            signal  = (float)Math.max(0, Math.min(1.0, Math.tan(freq * n.time()) * 0.5f + 0.5f));
            //signal  = (float)Math.sin(freq * n.time()) * 0.5f + 0.5f;
            //signal = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;
            
            signal *= 1.0 + (Math.random()-0.5f)* 2f * noiseRate;

            if (Math.random() > missingDataRate)
                observed.add((int) n.time(), signal);
            
            prevY = curY;
            curY = discretize.f(signal);

            if ((curY == prevY) && (onlyNoticeChange)) {
                continue;
            }

            //discretize.believe("x", signal, 0);
            chg.set("<{x} --> y"+((int)(signal*10.0))+">. :|:");

        }

    }
}
