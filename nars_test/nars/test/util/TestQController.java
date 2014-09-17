package nars.test.util;


import java.util.Arrays;
import nars.core.EventEmitter.Observer;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Param;
import nars.core.build.DefaultNARBuilder;
import nars.grid2d.agent.ql.QLearner;
import nars.io.TextInput;
import nars.test.core.NALTest;
import nars.util.meter.sensor.EventValueSensor;

/**
 * Dynamic inference controller experiment, using QLearning
 * 
 * 
 * Experiment:
<sseehh_> normally, concept priority drops
<sseehh_> to like 0.03
<sseehh_> average concept priority
<sseehh_> this applies every N cycles
<sseehh_> so its looking at the average over the waiting period
<sseehh_> priority may spike for a few concepts, but this affects little
<sseehh_> if it can raise the avg concept priority, then it has significantly affected inference behavior
 */
public class TestQController {
    
    final static String cpm = "concept.priority.mean";
    
    public static abstract class AbstractController implements Observer {
        public final NAR nar;
        
        /** how many cycles to wait before action, then wait again.. */
        private int period;
        
        public AbstractController(NAR n, int period) {
            
            this.nar = n;
            this.period = period;
            
            nar.memory.logic.setActive(true);
            
            start();
        }
        
        public void start() {
            nar.on(Memory.Events.CycleStop.class, this);
        }
        
        /** read sensor values as input */
        abstract public void getSensors();
        
        /** adjust parameter values */
        abstract public void setParameters();


        @Override public void event(final Class event, final Object... arguments) {
            long cycle = nar.getTime();
            
            nar.memory.logic.update(nar.memory);
            
            getSensors();
            
            
            if (cycle % period == (period-1)) {
                setParameters();
            }
        }
        
        public void setPeriod(int period) {
            this.period = period;
        }
                
        public void stop(){
            nar.off(Memory.Events.CycleStop.class, this);
        }

    }
    
    public static class NumericRange {

        private static void vectorize(double[] qIn, int i, double conceptPriority, int i0, int i1, int quant) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        double min, max;
        private double value;
        
        public NumericRange(double center) {
            min = max = center;
            set(center);
        }
        
        public NumericRange(double center, double radius) {            
            min = center - radius;
            max = center + radius;
            set(center);
        }

        public void set(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }                
        
        public double max() {
            return max;
        }

        public double min() {
            return min;
        }

        public double proportion() {
            return proportion(get());
        }
        
        /** normalize to proportional value in range 0..1 */
        public double proportion(final double v) {
            if (max == min)
                return 0;
            
            return (v - min) / (max-min);
        }
        
        /** denormalize to range */
        public double unproportion(final double p) {
            return (p * (max-min)) + min;
        }
        
        /** proportional value normalized to 0..1 then divided into uniform discrete steps */
        public int proportionDiscrete(final double v, final int steps) {
            double p = proportion(v);
            
            //hard limit to range
            p = Math.min(Math.max(p, 0), 1.0);
            
            return (int)(Math.round(p * (steps-1)));
        }
        
        public void vectorize(double[] target, int index, final int steps) {
            vectorize(target, index, get(), steps);
        }
        
        public void vectorize(double[] target, int index, final double v, final int steps) {
            int p = proportionDiscrete(v, steps);            
            target[index + p] = 1;
        }

        private NumericRange add(double v) {
            value += v;
            if (value < min) value = min;
            if (value > max) value = max;
            return this;
        }
        
    }
    
    public static class QController extends AbstractController {

        EventValueSensor meanConceptPriorityMean = new EventValueSensor("meanConceptPriorityMean", true);
        public NumericRange nextConceptCyclesToForget;
        public NumericRange nextTaskCyclesToForget;
        public NumericRange nextBeliefCyclesToForget;
        private NumericRange uniformRange = new NumericRange(0.5, 0.5);
        
        private final QLearner q;
        
        int numParams = 3;
        
        int paramQuant = 5;
        private int inputQuant = 21;
        
        int numInputs =  inputQuant + paramQuant * numParams;
        int numOutputs = 2 * numParams + 1;
        private double conceptPriority;
        

        public QController(NAR n, int period) {
            super(n, period);
                        
            Param p = nar.param();
            q = new QLearner();
            q.init(numInputs, numOutputs);
            
            nextConceptCyclesToForget = new NumericRange(p.conceptCyclesToForget.get(), 4);
            nextTaskCyclesToForget = new NumericRange(p.taskCyclesToForget.get(), 4);
            nextBeliefCyclesToForget = new NumericRange(p.beliefCyclesToForget.get(), 4);
        }

        @Override
        public void getSensors() {
            meanConceptPriorityMean.commit(nar.memory.logic.d(cpm, 0));
        }

        @Override
        public void setParameters() {
            conceptPriority = meanConceptPriorityMean.getReset().mean();
            
            //learn
            double[] qIn = q.getSensor();
            Arrays.fill(qIn, 0);
            int i = 0;
            uniformRange.vectorize(qIn, i, conceptPriority, inputQuant);  i += inputQuant;
            nextConceptCyclesToForget.vectorize(qIn, i, paramQuant); i += paramQuant;
            nextTaskCyclesToForget.vectorize(qIn, i, paramQuant); i += paramQuant;
            nextTaskCyclesToForget.vectorize(qIn, i, paramQuant); i += paramQuant;
            
            
            System.out.println(" input: " + Arrays.toString(qIn));
        
            q.step(reward());
            
            double[] a = q.getAction();
            //System.out.println("out: " + Arrays.toString(a) + " " + nextConceptCyclesToForget.get());
            if (a[0] > 0)
                nextConceptCyclesToForget.add(-1);
            if (a[2] > 0)
                nextConceptCyclesToForget.add(1);
                                    
            nar.param().conceptCyclesToForget.set((int)nextConceptCyclesToForget.get());
            nar.param().taskCyclesToForget.set((int)nextTaskCyclesToForget.get());
            nar.param().beliefCyclesToForget.set((int)nextBeliefCyclesToForget.get());
        }
        
        public double reward() {
            //maximize concept priority
            return conceptPriority;
        }
    }

    public static void input(String example, NAR... n) {
        for (NAR x : n)
            x.addInput(new TextInput(NALTest.getExample(example)));             
    }
    
    public static void main(String[] arg) {
        
//n has controller
        NAR n = new DefaultNARBuilder().build();        
        QController q = new QController(n, 8);
        
        //m has no controller
        NAR m = new DefaultNARBuilder().build();        
        m.memory.logic.setActive(true);

        input("nal/Examples/Example-MultiStep-edited.txt", n, m);
        input("nal/test/nars_multistep_1.nal", n, m);
        input("nal/test/nars_multistep_2.nal", n, m);
        
        while (true ) {
            n.step(1);
            
            m.step(1);
            m.memory.logic.update(m.memory);
            
            double mm = m.memory.logic.d(cpm);
            double nn = n.memory.logic.d(cpm);
            
            System.out.println(
                    (int)((nn-mm)/((nn+mm)/2.0)*100.0) + "%: " + 
                            mm + " | " + nn + " <-- [" +
                            q.nextConceptCyclesToForget.get() + " " + 
                            q.nextBeliefCyclesToForget.get() + " " + 
                            q.nextTaskCyclesToForget.get() + "]");
        }
                
    }
}
