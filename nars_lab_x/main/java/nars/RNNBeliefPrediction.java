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
import nars.core.NAR;
import nars.nal.entity.Concept;
import nars.nal.entity.Sentence;
import org.apache.commons.math3.util.MathArrays;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/** predicts the beliefs of a set of concepts */
abstract public class RNNBeliefPrediction extends BeliefPrediction {
    final Random rnd = new Random();

    private final Net net;
    private SampleSet data;

    int maxDataFrames = 96; //# time frames
        final int trainIterationsPerCycle = 32;
        final double learningrate = 0.05;
        final double momentum = 0.9;
        
     /** how much temporal radius to smudge a time prediction forwrad and backward */
        
    float predictionTimeSpanFactor = 3f;
                   
    protected double[] predictedOutput;
    private GradientDescent trainer;
    private final int inputSize;
    private double[] actual;
    private double[] ideal;
    
    boolean normalizeInputVectors = true;
    boolean normalizeOutputVector = false;
    final int downSample = 1; //not working yet for values other than 1

    public RNNBeliefPrediction(NAR n, Concept... concepts) {        
        this(n, Lists.newArrayList(concepts));
    }

    public static double[] normalize(double[] x) {
        double d = MathArrays.safeNorm(x);
        if (d == 0) return x;
        for (int i = 0; i < x.length; i++)
            x[i]/=d;
        return x;
    }
    
    public RNNBeliefPrediction(NAR n, List<Concept> concepts) {
        super(n, concepts);
        this.inputSize = concepts.size();
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
        gen.inputLayer(inputSize);
        gen.hiddenLayer(concepts.size() * 6, CellType.TANH);
        //gen.hiddenLayer(concepts.size() * 3, CellType.TANH);
        gen.outputLayer(getPredictionSize(), CellType.TANH);

        
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

    public int getInputSize() {
        return inputSize;
    }
    
    abstract public int getPredictionSize();
    abstract public double[] getTrainedPrediction(double[] input);
    
    @Override
    protected void train() {
        //
        //double[] target = {((data[x(i1)] + data[x(i2)])/2.0)};
        //new Sample(data, target, 2, length, 1, 1);
        TreeMap<Integer, double[]> d = new TreeMap();
        int cc = 0;
        int hd = Math.round(predictionTimeSpanFactor * nar.memory.getDuration() / 2f / downSample);
        for (Concept c : concepts) {
            for (Sentence s : c.beliefs) {
                if (s.isEternal()) {
                    continue;
                }
                int o = (int) Math.round( ((double)s.getOccurenceTime()) / ((double)downSample)) ;
                if (o > nar.time()) {
                    continue; //non-future beliefs
                }
                for (int oc = o - hd; oc <= o + hd; oc++) {
                    double[] x = d.get(oc);
                    if (x == null) {
                        x = new double[inputSize];
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
        if (last - first > maxDataFrames*downSample) {
            first = last - maxDataFrames*downSample;
        }
        int frames = (int) (last - first);
        int bsize = getInputSize() * frames;
        int isize = getPredictionSize() * frames;
        if (actual==null || actual.length!=bsize) 
            actual = new double[bsize];
        else
            Arrays.fill(actual, 0);
        
        if (ideal == null || ideal.length!=isize) 
            ideal = new double[isize];
        else
            Arrays.fill(ideal, 0);
        
        
        int idealSize = getPredictionSize();
        int ac = 0, id = 0;
        double[] prevX = null;
        for (int i = first; i <= last; i++) {
            double[] x = d.get(i);
            if (x == null) {
                x = new double[inputSize];
            }
            else {
                
                if (normalizeInputVectors) {
                    x = normalize(x);
                }
            }
            if (prevX != null) {
                System.arraycopy(prevX, 0, actual, ac, inputSize);
                ac += inputSize;
                
                System.arraycopy(getTrainedPrediction(x), 0, ideal, id, idealSize);
                id += idealSize;
                
            }
            prevX = x;
        }

        Sample s = new Sample(actual, ideal, inputSize, idealSize);
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

    protected double[] predict() {
        if (data == null) {
            return null;
        }
        if (predictedOutput == null) {
            predictedOutput = new double[getPredictionSize()];
        }
        Sample lastSample = data.get(data.size() - 1);
        double error = NetTools.performForward(this.net, lastSample);
        net.output(predictedOutput, 0);
        
        
        if (normalizeOutputVector)
            predictedOutput = normalize(predictedOutput);
                
            
        System.out.println("output: " + Arrays.toString(predictedOutput) + " " + error);
        
        return predictedOutput;
    }

    
}
