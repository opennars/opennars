/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.goal.numeric;

import objenome.Objenome;
import objenome.solution.SetNumericValue;
import objenome.solver.NumericSolver;
import objenome.solver.Solver;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import java.util.List;
import java.util.function.Function;

import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MAXIMIZE;

/**
 * Multivariate Optmization with Multistart which uses different starting points 
 * (trying to avoid being trapped in a local extremum when looking for a global one)
 * @see http://commons.apache.org/proper/commons-math/javadocs/api-3.3/src-html/org/apache/commons/math3/optim/nonlinear/scalar/MultiStartMultivariateOptimizer.html
 */
public class OptimizeMultivariate<C> extends NumericSolver<C> implements MultivariateFunction {

    private int numStarts = 4;
    //double convergeRel = 0.01;
    //double convergeAbs = 0.02;
    private Double bestValue;
    private final int evaluations = 200;
    private GoalType goal = MAXIMIZE;
    private List<SetNumericValue> variables;
    private Objenome objenome;

    public OptimizeMultivariate(Class<? extends C> model, Function<C, Double> function) {
        super(model, function);
    }

    @Override
    public synchronized void solve(Objenome o, List<SetNumericValue> variables) {
        
        this.variables = variables;
        objenome = o;
        
        if (numStarts==-1)
            numStarts = variables.size() * 2;
        
        double[] lower = new double[variables.size()];
        double[] upper = new double[variables.size()];
        double[] mid = new double[variables.size()];
        int j = 0;
        for (SetNumericValue n : variables) {
            lower[j] = n.getMin().doubleValue();
            upper[j] = n.getMax().doubleValue();
            mid[j] = (lower[j] + upper[j]) * 0.5f;
            j++;
        }
        
        //TODO add MultiStart
        
        MultivariateOptimizer optimize = 
                new BOBYQAOptimizer(variables.size()*2);
                //new PowellOptimizer(0.01, 0.05);
        
        RandomGenerator rng = getRandomGenerator();
        
        MultiStartMultivariateOptimizer multiOptimize = new MultiStartMultivariateOptimizer(optimize, numStarts, new UncorrelatedRandomVectorGenerator(variables.size(), new UniformRandomGenerator(rng)));
        
        PointValuePair result = multiOptimize.optimize(
                new MaxEval(evaluations),
                new SimpleBounds(lower, upper),
                goal,
                new InitialGuess(mid),
                new ObjectiveFunction(this)
                );        
        
        apply(result.getPointRef());

        bestValue = result.getValue();
    }

    /** the resulting scalar evaluation of the sought maxima/minima */
    public Double getBestValue() {
        return bestValue;
    }
       
    
    protected void apply(double[] values) {        
        for (int i = 0; i < values.length; i++)
            variables.get(i).setValue(values[i]);
        
        objenome.commit();        
    }

    @Override
    public double value(double[] v) {
        apply(v);
        
        C instance = objenome.get(model);
        
        return function.apply( instance );
    }
        

    
//    public MultivariateOptimizer getOptimizer() {
//        return new SimplexOptimizer(convergeRel, convergeAbs);
//    }
    

//    private RandomVectorGenerator getRandomVectorizer() {
//        return new UnitSphereRandomVectorGenerator(variables.size());
//    }

    public Solver minimize() {
        goal = GoalType.MINIMIZE;
        return this;
    }

    protected RandomGenerator getRandomGenerator() {
        return new JDKRandomGenerator();
    }

    
    
}
