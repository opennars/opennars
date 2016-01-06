///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.core.approximation;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//import org.apache.commons.math3.linear.ArrayRealVector;
//
///**
// * TODO allow evaluating on a history of function evaluations, not just the current
// * @author thorsten
// */
//public class GeneticFitter implements ParameterizedFunction {
//
//    private final int poolSize = 100;
//    private final ParameterizedFunction[] pool = new ParameterizedFunction[poolSize];
//    private final Map<ParameterizedFunction, Double> poolValues = new HashMap();
//
//    private final Random random = new Random();
//    final int learningIterations = 2;
//    private int numMutations = 1;
//    private double mutationRate = 0.01;
//    private double survivorRate = 0.1;
//
//    public GeneticFitter(int numInputs, DiffableFunctionGenerator dfg, int numFeatures) {
//        super();
//
//        for (int i = 0; i < pool.length; ++i) {
//            pool[i] = new DiffableFunctionMarshaller(dfg, numInputs, numFeatures);
//        }
//    }
//
//    @Override
//    public double value(double[] xs) {
//        return pool[0].value(xs);
//    }
//
//    @Override
//    public void parameterGradient(double[] output, double... xs) {
//
//    }
//
//    @Override
//    public void addToParameters(double[] deltas) {
//
//    }
//
//    @Override
//    public ArrayRealVector parameterGradient(ArrayRealVector output, double... xs) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void addToParameters(ArrayRealVector deltas) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void learn(double[] xs, double y) {
//
//        //cache the values for xs to avoid duplicate evaluations in sort
//        for (ParameterizedFunction p : pool) {
//            poolValues.put(p, p.value(xs));
//        }
//
//        int descendants = (int)(pool.length * (1 - survivorRate));
//        int survivors = pool.length - descendants;
//
//        for (int k = 0; k < learningIterations; ++k) {
//
//
//            for (int d = 0; d < descendants; d++) {
//
//                int i = 0, j = 0;
//
//                while (i == j) {
//                    double a = Math.random();
//                    //a = a * a;
//                    double b = Math.random();
//                    //b = b * b;
//                    i = (int) Math.round(a * (survivors - 1));
//                    j = (int) Math.round(b * (survivors - 1));
//                }
//
//                //crossover
//                ParameterizedFunction f = pool[pool.length - d - 1];
//                int center = (int) (Math.random() * f.numberOfParameters());
//                for (int l = 0; l < f.numberOfParameters(); ++l) {
//                    if (l < center) {
//                        f.setParameter(l, pool[i].getParameter(l));
//                    } else {
//                        f.setParameter(l, pool[j].getParameter(l));
//                    }
//                }
//
//                //mutate
//                for (int m = 0; m < numMutations; m++) {
//                    int l = random.nextInt(f.numberOfParameters());
//
//                    Scalar s = f.getScalarParameter(l);
//                    double low = s.getLowerBound();
//                    double high = s.getUpperBound();
//                    if (Double.isFinite(low) && Double.isFinite(high)) {
//                        double newRandom = Math.random() * (high - low) + low;
//                        s.lerp(newRandom, mutationRate);
//                    }
//                    else {
//                        s.setValue( s.value() + mutationRate * (Math.random() - 0.5) );
//                    }
//                }
//
//                poolValues.put(f, f.value(xs));
//            }
//
//            Arrays.sort(pool, (ParameterizedFunction o1, ParameterizedFunction o2) -> (int) Math.signum(Math.abs(poolValues.get(o1) - y) - Math.abs(poolValues.get(o2) - y)));
//        }
//    }
//
//    @Override
//    public int numberOfParameters() {
//        return pool[0].numberOfParameters();
//    }
//
//    @Override
//    public int numberOfInputs() {
//        return pool[0].numberOfInputs();
//    }
//
//    @Override
//    public double minOutputDebug() {
//        return pool[0].minOutputDebug();
//    }
//
//    @Override
//    public double maxOutputDebug() {
//        return pool[0].maxOutputDebug();
//    }
//
//    @Override
//    public double getParameter(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void setParameter(int i, double v) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
// }
