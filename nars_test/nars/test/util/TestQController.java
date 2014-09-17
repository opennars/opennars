package nars.test.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    final static String td = "task.derived";
    
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
        private boolean autoRange;
        
        public NumericRange() {
            min = max = Double.NaN;
            autoRange = true;            
        }
        
        public NumericRange(double center) {
            min = max = center;
            set(center);
        }
        
        public NumericRange(double center, double radius) {            
            min = center - radius;
            max = center + radius;
            set(center);
        }

        public void set(final double value) {            
            this.value = value;
            if (autoRange) {
                if ((Double.isNaN(min)) && !(Double.isNaN(value))) {
                    //first input
                    min = value;
                    max = value;
                }
                else {
                    if (value < min)  {
                        //System.out.println(this + "autorange update min: " + value + " -> " + min);
                        min = value;
                    }
                    if (value > max) {
                        //System.out.println(this + "autorange update max: " + value + " -> " + min);
                        max = value;
                    }
                }
            }
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
                return 0.5;
            if (Double.isNaN(v))
                return 0.5;
            
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
        public void vectorizeSmooth(double[] target, int index, double v, final int steps) {
            set(v);
            v = proportion(v);
            if (Double.isNaN(v)) {
                v = 0.5;
            }
            else {
                v = Math.min(1, v);
                v = Math.max(0, v);
            }
            
            final double stepScale = 1.0 / (steps-1);
            for (int p = 0; p < steps; p++) {
                double pp = ((double)p)* stepScale;                
                double d = 1.0 - Math.abs( pp - v) / stepScale;                
                d = Math.max(d, 0);
                //d = Math.min(d, 1.0);
                //System.out.println(v + " " + pp + " " + d);
                target[index + p] = d;
            }
            
        }

        private NumericRange add(double v) {
            value += v;
            if (value < min) value = min;
            if (value > max) value = max;
            return this;
        }
        
        /** shrinks the distance between min and max around a target value to increase the 'contrast' gradually over time as seen by whatever uses this result */
        public void adaptiveContrast(double rate, double target) {
            min = (1.0 - rate) * min + (rate) * target;
            max = (1.0 - rate) * max + (rate) * target;
        }
    }
    
    public static abstract class ControlSensor {
        
        public final NumericRange range;
        public final int quantization;
        
        
        public ControlSensor(int quantization) {
            this.range = new NumericRange();
            this.quantization = quantization;
        }
        
        public ControlSensor(double min, double max, int quantization) {
            this.range = new NumericRange( (min + max) / 2, (max-min)/2 );
            this.quantization = quantization;
        }
        
        //called each cycle
        abstract public void update();
        
        //called during learning cycle to get the value
        abstract public double get();
        
        /** returns next index */
        public int vectorize(double[] d, int index) {            
            range.vectorizeSmooth(d, index, get(), quantization);
            return quantization;
        }
        
        public void adaptContrast(double rate, double center) {
            range.adaptiveContrast(rate, center);
        }
        
    }
    public static class NControlSensor extends ControlSensor {
        private final Number a;

        public NControlSensor(Number a, int quantization) {
            super(quantization);
            this.a = a;
            range.set(a.doubleValue());
        }
        
        public NControlSensor(Number a, double min, double max, int quantization) {
            super(min, max, quantization);
            this.a = a;
        }
        
        public NControlSensor(Number a, double radius, int quantization) {
            this(a, a.doubleValue() - radius, a.doubleValue() + radius, quantization);
        }

        @Override public void update() {       }

        @Override public double get() {
            double aa = a.doubleValue();;
            range.set(aa);
            return aa;
        }
                
    }
    
    
    abstract public static class QController extends AbstractController {
        private final QLearner q;
        public List<ControlSensor> inputs = new ArrayList();
        private int actions;
        private int numInputs;
        private double[] a;
        private double[] s;
        private boolean active;

        public QController(NAR nar, int updatePeriod) {
            super(nar, updatePeriod);
            this.q = new QLearner();            
        }
        
        public <C extends ControlSensor> C add(C s) { inputs.add(s); return s; }
        
        public void init(int actions) {
            actions = actions;
            
            numInputs = 0;            
            for (ControlSensor cs : inputs) {
                numInputs += cs.quantization;
            }

            System.out.println("Inputs=" + numInputs + ", Outputs=" + actions);            
            q.init(numInputs, actions, getFeedForwardLayers(numInputs));
        }
        
        abstract protected int[] getFeedForwardLayers(int inputSize);
        
        abstract protected void act(int action);
        
        abstract public double reward();
        
        @Override
        public void getSensors() {            
            for (ControlSensor cs : inputs)
                cs.update();
        }

        @Override
        public void setParameters() {
            int i = 0;
            
            //even when not active, vectorize inputs because it may affect sensor readings that determine reward, which we may want to evaluate
            s = q.getSensor();
            for (ControlSensor cs : inputs) {
                i += cs.vectorize(s, i);
            }

            if (active) {
                q.step(reward());

                a = q.getAction();
                for (int j = 0; j < a.length; j++)
                    if (a[j] > 0)
                        act(j);
            }
                    
        }

        public double[] getInput() {
            return s;
        }

        public double[] getOutput() {
            return a;
        }
        
        public void setActive(boolean b) {
            this.active = b;
        }
        
    }
    
    public static class TestController extends QController {

                
                
        private double conceptPriority;
        private double taskDerivedMean;
        private double conceptNewMean;
        
        public TestController(NAR n, int period) {
            super(n, period);
                        
            Param p = nar.param();
            
            add(new NControlSensor(p.conceptCyclesToForget, 2));
            add(new NControlSensor(p.beliefCyclesToForget, 2));
            add(new NControlSensor(p.taskCyclesToForget, 2));
            
            add(new ControlSensor(4) {
                EventValueSensor meanConceptPriorityMean = 
                        new EventValueSensor("a", true);
                
                @Override public void update() {
                    meanConceptPriorityMean.commit(nar.memory.logic.d(cpm, 0));
                }

                @Override public double get() {
                    conceptPriority = meanConceptPriorityMean.getReset().mean();
                    adaptContrast(0.002, conceptPriority);                    
                    return conceptPriority;
                }                
            });
            add(new ControlSensor(4) {
                EventValueSensor taskDerived = 
                        new EventValueSensor("a", true, 2);
                
                
                @Override public void update() {
                    taskDerived.commit(nar.memory.logic.d(td, 0));                    
                }

                @Override public double get() {
                    taskDerivedMean = taskDerived.get().mean();
                    adaptContrast(0.002, taskDerivedMean);
                    return taskDerivedMean;
                }
            });
            add(new ControlSensor(4) {
                EventValueSensor conceptNew = 
                        new EventValueSensor("a", true, 2);
                
                
                @Override public void update() {
                    conceptNew.commit(nar.memory.logic.d("concept.new", 0));                    
                }

                @Override public double get() {
                    conceptNewMean = conceptNew.get().mean();
                    adaptContrast(0.002, conceptNewMean);
                    return conceptNewMean;
                }
            });
            
            init(7);
        }

        @Override
        protected int[] getFeedForwardLayers(int inputSize) {
            return new int[0];
        }

        @Override
        protected void act(int action) {
            Param p = nar.param();
            switch (action) {
                case 0: p.conceptCyclesToForget.set(12);  break;
                case 1: p.conceptCyclesToForget.set(10);   break;
                case 2: p.taskCyclesToForget.set(22);  break;
                case 3: p.taskCyclesToForget.set(20);   break;
                case 4: p.beliefCyclesToForget.set(52);  break;
                case 5: p.beliefCyclesToForget.set(50);   break;
                case 6: 
                    //final input: do nothing                    
                    break;
            }
        }        
        
        @Override
        public double reward() {
            //maximize concept priority
            //return conceptPriority;
            return taskDerivedMean + conceptNewMean;
        }


    }

    public static void input(String example, NAR... n) {
        for (NAR x : n)
            x.addInput(new TextInput(NALTest.getExample(example)));             
    }
    
    public static void main(String[] arg) {
                
        NAR n = new DefaultNARBuilder().build();        
        TestController qn = new TestController(n, 2);
        
        //m has controller deactivated
        NAR m = new DefaultNARBuilder().build();
        TestController qm = new TestController(m, 2);
        qm.setActive(false);

        input("nal/Examples/Example-MultiStep-edited.txt", n, m);
        input("nal/test/nars_multistep_1.nal", n, m);
        input("nal/test/nars_multistep_2.nal", n, m);
        
        
        double mm = 0, nn = 0;
        int displayCycles = 100;
        while (true ) {
            n.step(1);
            
            m.step(1);
            m.memory.logic.update(m.memory);
            
            mm += qm.reward();
            nn += qn.reward();
            
            if (n.getTime() % displayCycles == 0) {
                System.out.println(
                        //((nn-mm)/((nn+mm)/2.0)*100.0) + " , " + 
                                n.getTime() + ", " +
                                mm + " , " + nn + " , " +
                                Arrays.toString(qn.getInput()) + " , " + 
                                Arrays.toString(qn.getOutput())
                                );
                mm = nn = 0;
            }
        }
                
    }
}
