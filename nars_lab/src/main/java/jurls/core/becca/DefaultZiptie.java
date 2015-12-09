/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.becca;

import com.google.common.util.concurrent.AtomicDouble;
import jurls.core.approximation.Scalar;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.Math.pow;

/**
 * Ziptie implemented according to BECCA specifications
 * TODO complete, it is not finished yet
 */
public class DefaultZiptie extends Ziptie {
    
    
    public final List<Cable> cables = new ArrayList();
    public final List<Bundle> bundles = new ArrayList();
    
    /** adjacency matrix representing the membership strength (0 = disconnected, 1=fully connected) of a cable (row) to bundle (column) */
    RealMatrix c = new OpenMapRealMatrix(0,0);
    
    final double inhibitionExponent = 6;
    final double meanExponent = -4; 
    
    protected void resized() {
        
        assert(c.getRowDimension()!=cables.size());
        assert(c.getColumnDimension()!=bundles.size());
        
        RealMatrix newConnection = c.createMatrix(cables.size(), bundles.size());
        c.walkInOptimizedOrder(new CopySubmatrix(newConnection));
    }

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
            this(index, new Scalar());
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
            c.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {

                @Override
                public void visit(int row, int column, double membershipStrength) {
                    
                    double activity = membershipStrength * bundles.get(column).getInitialAcitvity();
                    if (activity > maxBundleActivity) maxBundleActivity = activity;
                }
                
            }, index, index, 0, c.getColumnDimension());
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
                    getRowGeneralizedMean(c, row -> cables.get(row).getActivity(), meanExponent, 0, c.getRowDimension(), index);
        }
        
        public double updateFinalActivity() {
            return activity = 
                    getRowGeneralizedMean(c, row -> cables.get(row).getInhibitedActivity(Bundle.this), meanExponent, 0, c.getRowDimension(), index);
        }
        
        public double getActivity() {
            return activity;
        }

    }
    
    
    @Override
    public double[] in(double[] signal, double[] result) {
        
        boolean resized = false;
        while (signal.length > cables.size()) {
            cables.add(new Cable(cables.size()));
            resized = true;
        }
        
        for (int i = 0; i < signal.length; i++)
            cables.get(i).set( signal[i] );
        
        if (resized)
            resized();
            
        in();
        
        return getBundleActivities(result);
    }
    
    
    protected void addBundle() {
        
        Bundle b = new Bundle(bundles.size());
        bundles.add(b);
        
        resized();
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

    
    static class CopySubmatrix extends DefaultRealMatrixPreservingVisitor {

        private final RealMatrix newConnection;

        public CopySubmatrix(RealMatrix target) {
            newConnection = target;
        }

        @Override
        public void visit(int row, int column, double value) {
            newConnection.setEntry(row, column, value);
        }
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

       public double getRowGeneralizedMean(RealMatrix c, Function<Integer, Double> rowEntryMultiplier, double exponent, int rowStart, int rowEnd, int column) {
            AtomicDouble s = new AtomicDouble(0);
            AtomicInteger n = new AtomicInteger(0);
            c.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {
                @Override public void visit(int row, int column, double value) {
                    double a = Math.pow(value, exponent);
                    double b = rowEntryMultiplier.apply(row);
                    s.addAndGet( a * b );
                    n.incrementAndGet();
                }               
            }, rowStart, rowEnd, column, column);
            
            return (1.0 / n.doubleValue()) * Math.pow( s.doubleValue(), 1.0 / exponent );           
        }        
    
}
