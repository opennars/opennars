package nars.nal;

import nars.NAR;
import nars.rl.elsy.QLearner;

import java.util.ArrayList;
import java.util.List;


public abstract class QController extends AbstractController {

    QLearner q;
    public List<ControlSensor> inputs = new ArrayList();
    private int actions;
    private int numInputs;
    private double[] a;
    private double[] s;
    boolean active = true;

    //even when not active, vectorize inputs because it may affect sensor readings that determine reward, which we may want to evaluate
    public QController(NAR nar, int updatePeriod) {
        super(nar, updatePeriod);

    }

    public <C extends ControlSensor> C add(C s) {
        inputs.add(s);
        return s;
    }

    public void init(int actions) {
        this.actions = actions;
        numInputs = 0;
        for (ControlSensor cs : inputs) {
            numInputs += cs.quantization;
        }
        System.out.println("Inputs=" + numInputs + ", Outputs=" + actions);
        this.q = new QLearner(numInputs, actions, getFeedForwardLayers(numInputs));
    }

    protected abstract int[] getFeedForwardLayers(int inputSize);

    protected abstract void act(int action);

    public abstract double reward();

    @Override
    public void getSensors() {
        for (ControlSensor cs : inputs) {
            cs.update();
        }
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
            for (int j = 0; j < a.length; j++) {
                if (a[j] > 0) {
                    act(j);
                }
            }
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


    public int getNumActions() {
        return actions;
    }

}
