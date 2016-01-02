/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.predict;

import automenta.vivisect.swing.NWindow;
import automenta.vivisect.swing.PCanvas;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.TimelineVis;
import nars.core.Events.TaskImmediateProcess;
import nars.core.NAR;
import nars.core.Parameters;
import nars.cycle.NAL;
import nars.gui.NARSwing;
import nars.io.ChangedTextInput;
import nars.model.Default;
import nars.nal.entity.Task;
import nars.nal.language.Term;
import nars.narsese.Narsese;

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
    static double maxval=0;
    
    public static void main(String[] args) throws Narsese.InvalidInputException, InterruptedException {

        Parameters.DEBUG = true;
        int duration = 8;
        float freq = 1.0f / duration * 0.1f;        
        int thinkInterval = 50;
        double discretization = 3;

        NAR n = new NAR(new Default());
        n.param.duration.set(duration);
        n.param.noiseLevel.set(0);
        //n.param.conceptForgetDurations.set(16);
        
        n.on(TaskImmediateProcess.class, new TaskImmediateProcess() {
            int curmax=0;
            @Override
            public void onProcessed(Task t, NAL n) {
                if (t.sentence.getOccurenceTime() >= n.memory.time() && t.sentence.truth.getExpectation()>0.5) {
                    Term term = t.getTerm();
                    int time = (int) t.sentence.getOccurenceTime();
                    int value = -1;
                    String ts = term.toString();
                    if (ts.startsWith("<{x} --> y")) {
                        char cc = ts.charAt("<{x} --> y".length());
                        value = cc - '0';
                        if(time>=curmax) {
                            curmax=time;
                        }
                        maxval=Math.max(maxval, (value)/10.0);
                        predictions[0].add(time, (value)/10.0 );
                        
                    }
                }
            }
        });
        
        
        TreeMLData observed = new TreeMLData("value", Color.WHITE).setRange(0, 1f);
        predictions = new TreeMLData[(int)discretization];
        TreeMLData[] reflections = new TreeMLData[(int)discretization];

        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = new TreeMLData("Pred" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            
            reflections[i] = new TreeMLData("Refl" + i,
                    Color.getHSBColor(0.25f + i / 4f, 0.85f, 0.85f));
            reflections[i].setDefaultValue(0.0);
        }
        TimelineVis tc = new TimelineVis(
                new LineChart(0,1,observed).thickness(16f).height(128),                          new LineChart(predictions[0]).thickness(16f).height(128)
                //new BarChart(reflections).thickness(16f).height(128)
                /*new LineChart(predictions[1]).thickness(16f).height(128),
                new LineChart(predictions[2]).thickness(16f).height(128),*/
        );

        new NWindow("_", new PCanvas(tc)).show(800, 800, true);

        n.run((int)discretization*4);
        
        NARSwing.themeInvert();

        new NARSwing(n);
        
        ChangedTextInput chg=new ChangedTextInput(n);
        double lastsignal=0;
        double lasttime=0;
        
        while (true) {

            n.run(thinkInterval);
            Thread.sleep(30);
            
            //signal  = (float)Math.max(0, Math.min(1.0, Math.tan(freq * n.time()) * 0.5f + 0.5f));
            signal  = (float)Math.sin(freq * n.time()) * 0.5f + 0.5f;
            //signal = ((float) Math.sin(freq * n.time()) > 0 ? 1f : -1f) * 0.5f + 0.5f;
            //signal *= 1.0 + (Math.random()-0.5f)* 2f * noiseRate;
            
            observed.removeData((int) (lasttime+1));  //this
            observed.removeData((int) (lasttime+2));  //is not good practice
            observed.add((int) n.time(), signal);
            observed.add((int) n.time()+1, -1); //but is fine
            observed.add((int) n.time()+2, 1); //for now (just wanted a line at the end)
            
            lastsignal=signal;
            lasttime=n.time();
            predictions[0].setData(0, maxval);
            //if(cnt<1000) { //switch to see what NARS does when observations end :)
                int val=(int)(((int)((signal*discretization))*(10.0/discretization)));
                chg.set("<{x} --> y"+val+">. :|:");
                //System.out.println(val);
            /*} else if (cnt==1000){
                System.out.println("observation phase end, residual predictions follow");
            }*/

        }

    }
}