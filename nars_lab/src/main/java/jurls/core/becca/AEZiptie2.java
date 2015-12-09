/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import com.google.common.util.concurrent.AtomicDouble;
import jurls.core.approximation.Scalar;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.Math.pow;

/**
 * Ziptie implemented as a hybrid of BECCA and Autoencoder design
 */
public class AEZiptie2 extends AEZiptie {

    public AEZiptie2(int numInputs, int numOutputs) {
        super(numInputs, numOutputs);
        
        for (int i = 0; i < numInputs; i++)
            cables.add( new Cable(cables.size()));
        for (int i = 0; i < numOutputs; i++)
            bundles.add( new Bundle(bundles.size()));
    }
    
    
    
    public final List<Cable> cables = new ArrayList();
    public final List<Bundle> bundles = new ArrayList();

    
    final double inhibitionExponent = 3;
        final double meanExponent = 1; 
    

    private double[] getBundleActivities(double[] result) {
        if ((result == null) || (result.length != bundles.size()))
            result = new double[bundles.size()];
            
        for (int i = 0; i < result.length; i++)
            result[i] = bundles.get(i).getActivity();
        
        return result;
    }


    public class Cable {
        
        public final int index;
        public final Scalar input;
        double maxBundleActivity;
        double inhibitionSum;
        
        public Cable(int index, Scalar input) {
            this.index = index;
            this.input = input;
        }
        public Cable(int index) {
            this(index, new Scalar(0));
        }

        public void set(double signal) {
            input.setValue(signal);
        }

        public double getActivity() {
            return input.getValue();
        }
        
        public double getInhibitedActivity(Bundle b) {
            double i = getActivity() * pow( b.getInitialAcitvity() / maxBundleActivity, inhibitionExponent);            
                        
            inhibitionSum += i;
            
            return i;
        }
        
        public double getNonBundleActivity() {
            return Math.max(0, getActivity() - inhibitionSum);
        }

        protected void updateBundleContribution() {
            maxBundleActivity = Double.NEGATIVE_INFINITY;
            for (int c = 0; c < numBundles; c++) {
                double membershipStrength = getMembership(index, c);
                double activity = membershipStrength * bundles.get(c).getInitialAcitvity();
                if (activity > maxBundleActivity) maxBundleActivity = activity;
            }

            inhibitionSum = 0;
        }

        
    }
    
    public class Bundle extends Scalar {

        public final int index; //corresponds to a column
        private double initialActivity;
        
        private double activity;
        
        public Bundle(int index) {
            this.index = index;
        }

        public double getInitialAcitvity() {
            return initialActivity;
        }        
        
        public double updateInitialActivity() {            
            return initialActivity = 
                    getRowGeneralizedMean(row -> cables.get(row).getActivity(), meanExponent, 0, numCables, index);
        }
        
        public double updateFinalActivity() {
            return activity = 
                    getRowGeneralizedMean(row -> cables.get(row).getInhibitedActivity(Bundle.this), meanExponent, 0, numCables, index);
        }
        
        public double getActivity() {
            return activity;
        }

    }
    
    
    @Override
    public double[] in(double[] signal, double[] result) {
        for (int i = 0; i < signal.length; i++)
            cables.get(i).set( signal[i] );

        super.in(signal, result);
                    
        in();
        
        return getBundleActivities(result);
    }
    

    
    
    /** process incoming activity */
    public void in() {
        bundles.forEach(Bundle::updateInitialActivity);
        cables.forEach(Cable::updateBundleContribution);
        bundles.forEach(Bundle::updateFinalActivity);
    }
    
    /** process outgoing goals */
    public void out() {
        
    }

    

        public static double getExponentialSum(RealMatrix c, double exponent, int rowStart, int rowEnd, int colStart, int colEnd) {            
            AtomicDouble s = new AtomicDouble(0);
            c.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
                @Override public void visit(int row, int column, double value) {
                    s.addAndGet( Math.pow(value, exponent) );
                }               
            }, rowStart, rowEnd, colStart, colEnd);
            return s.doubleValue();
        }

        public static double getGeneralizedMean(RealMatrix c, double exponent, int rowStart, int rowEnd, int colStart, int colEnd) {
            AtomicDouble s = new AtomicDouble(0);
            AtomicInteger n = new AtomicInteger(0);
            c.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
                @Override public void visit(int row, int column, double value) {
                    s.addAndGet( Math.pow(value, exponent) );
                    n.incrementAndGet();
                }               
            }, rowStart, rowEnd, colStart, colEnd);
            
            return (1.0 / n.doubleValue()) * Math.pow( s.doubleValue(), 1.0 / exponent );
        }

       public double getRowGeneralizedMean(Function<Integer, Double> rowEntryMultiplier, double exponent, int rowStart, int rowEnd, int column) {
            double s = 0;
            double n = 0;
            
            for (int r = rowStart; r < rowEnd; r++) {
                double value = getMembership(r, column);
                double a = Math.pow(value, exponent);
                double b = rowEntryMultiplier.apply(r);
                s += a * b;
                n += 1;
            }
            
            return (1.0 / n) * Math.pow( s, 1.0 / exponent );           
        }        
    
}
