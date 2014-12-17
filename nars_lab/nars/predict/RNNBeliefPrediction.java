/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.predict;

import com.google.common.collect.Lists;
import de.jannlab.Net;
import de.jannlab.core.CellType;
import de.jannlab.data.Sample;
import de.jannlab.data.SampleSet;
import de.jannlab.generator.RNNGenerator;
import de.jannlab.tools.NetTools;
import de.jannlab.training.GradientDescent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import org.encog.util.arrayutil.NormalizeArray;

/** predicts the beliefs of a set of concepts */
public class RNNBeliefPrediction extends BeliefPrediction {
    final Random rnd = new Random();

    private final Net net;
    private SampleSet data;

    int maxDataFrames = 96; //# time frames
        final int trainIterationsPerCycle = 32;
        final double learningrate = 0.05;
        final double momentum = 0.9;
        
     /** how much temporal radius to smudge a time prediction forwrad and backward */
        
    float predictionTimeSpanFactor = 5f;
                   
    protected double[] predictedOutput;
    private GradientDescent trainer;
    private final int frameSize;
    private double[] actual;
    private double[] ideal;
    
    NormalizeArray na = new NormalizeArray();
    
    boolean normalizeInputVectors = true;
    boolean normalizeOutputVector = false;

    public RNNBeliefPrediction(NAR n, Concept... concepts) {        
        this(n, Lists.newArrayList(concepts));
    }
        
    
    public RNNBeliefPrediction(NAR n, List<Concept> concepts) {
        super(n, concepts);
        this.frameSize = concepts.size();
        //https://github.com/JANNLab/JANNLab/blob/master/examples/de/jannlab/examples/recurrent/AddingExample.java
        /*LSTMGenerator gen = new LSTMGenerator();
        gen.inputLayer(frameSize);
        gen.hiddenLayer(
        concepts.size()*4,
        CellType.SIGMOID, CellType.TANH, CellType.TANH, false
        );
        gen.outputLayer(frameSize,  CellType.TANH);
         */
        RNNGenerator gen = new RNNGenerator();
        gen.inputLayer(frameSize);
        gen.hiddenLayer(concepts.size() * 6, CellType.TANH);
        //gen.hiddenLayer(concepts.size() * 8, CellType.TANH);
        gen.outputLayer(frameSize, CellType.TANH);
        
        
        net = gen.generate();
        net.rebuffer(maxDataFrames);
        net.initializeWeights(rnd);
    } //https://github.com/JANNLab/JANNLab/blob/master/examples/de/jannlab/examples/recurrent/AddingExample.java
    /*LSTMGenerator gen = new LSTMGenerator();
    gen.inputLayer(frameSize);
    gen.hiddenLayer(
    concepts.size()*4,
    CellType.SIGMOID, CellType.TANH, CellType.TANH, false
    );
    gen.outputLayer(frameSize,  CellType.TANH);
     */
    //leave as zeros

    protected void train() {
        //
        //double[] target = {((data[x(i1)] + data[x(i2)])/2.0)};
        //new Sample(data, target, 2, length, 1, 1);
        TreeMap<Integer, double[]> d = new TreeMap();
        int cc = 0;
        int hd = Math.round(predictionTimeSpanFactor * nar.memory.getDuration() / 2f);
        for (Concept c : concepts) {
            for (Sentence s : c.beliefs) {
                if (s.isEternal()) {
                    continue;
                }
                int o = (int) s.getOccurenceTime();
                if (o > nar.time()) {
                    continue; //non-future beliefs
                }
                for (int oc = o - hd; oc <= o + hd; oc++) {
                    double[] x = d.get(oc);
                    if (x == null) {
                        x = new double[frameSize];
                        d.put(oc, x);
                    }
                    float freq = 2f * (s.truth.getFrequency() - 0.5f);
                    float conf = s.truth.getConfidence();
                    if (freq < 0) {
                    }
                    x[cc] += freq * conf;
                }
            }
            cc++;
        }
        if (d.size() < 2) {
            data = null;
            return;
        }
        
        data = new SampleSet();
        int first = d.firstKey();
        int last = (int) nar.time();
        if (last - first > maxDataFrames) {
            first = last - maxDataFrames;
        }
        int frames = (int) (last - first);
        int bsize = frameSize * frames;
        
        if (actual==null || actual.length!=bsize) 
            actual = new double[bsize];
        else
            Arrays.fill(actual, 0);
        
        if (ideal == null || ideal.length!=bsize) 
            ideal = new double[bsize];
        else
            Arrays.fill(ideal, 0);
        
        
        int ac = 0;
        double[] prevX = null;
        for (int i = first; i <= last; i++) {
            double[] x = d.get(i);
            if (x == null) {
                x = new double[frameSize];
            }
            else {
                if (normalizeInputVectors) {
                    na.process(x);
                }
            }
            if (prevX != null) {
                System.arraycopy(prevX, 0, ideal, ac, frameSize);
                System.arraycopy(x, 0, actual, ac, frameSize);
                ac += frameSize;
            }
            prevX = x;
        }

        Sample s = new Sample(actual, ideal, frameSize, frameSize);
        data.add(s);

            
            //System.out.println(data);
            

        if (trainer == null) {
            trainer = new GradientDescent();
            trainer.setNet(net);
            trainer.setRnd(rnd);
            trainer.setPermute(true);
            trainer.setTrainingSet(data);
            trainer.setLearningRate(learningrate);
            trainer.setMomentum(momentum);
            trainer.setEpochs(trainIterationsPerCycle);
            trainer.setEarlyStopping(false);
            trainer.setOnline(true);
            trainer.setTargetError(0);
            trainer.clearListener();
        } else {
            //trainer.reset();
            
        }
        
        trainer.train();
        //System.out.println("LSTM error: " + trainer.getTrainingError());
    }

    protected void predict() {
        if (data == null) {
            return;
        }
        if (predictedOutput == null) {
            predictedOutput = new double[frameSize];
        }
        Sample lastSample = data.get(data.size() - 1);
        double error = NetTools.performForward(this.net, lastSample);
        net.output(predictedOutput, 0);
        
        
        if (normalizeOutputVector)
            na.process(predictedOutput);
                
            
        System.out.println("output: " + Arrays.toString(predictedOutput) + " " + error);
    }

    
}
