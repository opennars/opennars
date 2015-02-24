/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jurls.core.approximation.DiffableSymbols;
import jurls.core.approximation.Scalar.ArrayIndexScalar;
import jurls.core.approximation.Scalar.AtomicScalar;
import jurls.core.utils.compile.FastExpression;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public class DiffableFunctionMarshaller implements ParameterizedFunction, Functional {

    double parametersData[];
    public final Scalar[] parameters;
    
    private final DiffableFunction f;
    private final Scalar[] inputs;
    
    //private final ArrayRealVector gr;
    private double minOutputDebug = Double.POSITIVE_INFINITY;
    private double maxOutputDebug = Double.NEGATIVE_INFINITY;
    private final double[] inputData;

    public DiffableFunctionMarshaller(DiffableFunctionGenerator diffableFunctionGenerator, int numInputs, int numFeatures) {
        
        inputs = new Scalar[numInputs];
        inputData = new double[numInputs];
        for (int i = 0; i < numInputs; ++i) {
            inputs[i] = new ArrayIndexScalar(inputData, i, 0, "x" + i);
        }
        
        List<ArrayIndexScalar> ps = new ArrayList<>();

        //TODO get a real size for this array
        parametersData = new double[numInputs * numFeatures * 10];
        
        f = diffableFunctionGenerator.generate(inputs, parametersData, ps, numFeatures);
        parameters = ps.toArray(new Scalar[ps.size()]);
        //gr = new ArrayRealVector(parameters.length);
    }

    private void setInputs(double[] inputs) {
        for (int i = 0; i < inputs.length; ++i) {
            this.inputs[i].setValue(inputs[i]);
        }
    }

    @Override
    public double value(double[] xs) {
        setInputs(xs);
        double y = f.value();

        if (y > maxOutputDebug) {
            maxOutputDebug = y;
        }
        if (y < minOutputDebug) {
            minOutputDebug = y;
        }

        return y;
    }

    @Override
    public void learn(double[] xs, double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int numberOfParameters() {
        return parameters.length;
    }

    @Override
    public int numberOfInputs() {
        return inputs.length;
    }


    DiffableSymbols gradientExpressionSymbols = null;
    AtomicScalar[] scalarSymbols;
    DiffableExpression gradientExpression = null;
    boolean compile = true;
    
    @Override
    public ArrayRealVector parameterGradient(ArrayRealVector output, double[] xs) {
        output = ParameterizedFunction.array(output, parameters.length);
        
        setInputs(xs);
        
        if (compile)  {
            if (gradientExpression == null) {
                try {
                    gradientExpression = compile();
                    scalarSymbols = gradientExpressionSymbols.scalars();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
            
            gradientExpression.update(inputData, parametersData, output.getDataRef());
        }
        else {
            //interpreted
            double d[] = output.getDataRef();
            for (int i = 0; i < parameters.length; ++i) {
                d[i] = f.partialDerive(parameters[i]);
            }
        }
        return output;
    }
    
    private synchronized DiffableExpression compile() throws Exception {
        if (gradientExpression!=null) return gradientExpression;
        
        gradientExpressionSymbols = new DiffableSymbols(inputData, parametersData);
        DiffableExpression e = new FastExpression().compileGradientExpression(partialDeriveExpression(new StringBuilder(), gradientExpressionSymbols));
        return e;
    }
    
    public String partialDeriveExpression(StringBuilder sb, DiffableSymbols ds) {
        
    
    
        
        for (int i = 0; i < parameters.length; i++) {
            sb.append("output[").append(i).append("] = ");
            f.partialDeriveExpression(sb, ds, parameters[i]).append(';').append('\n');
        }
        return sb.toString();
    }

    @Override
    public void addToParameters(ArrayRealVector deltas) {
        double d[] = deltas.getDataRef();
        for (int i = 0; i < parameters.length; ++i) {            
            parameters[i].addSelf(d[i]);
        }
    }

    @Override
    public double minOutputDebug() {
        return minOutputDebug;
    }

    @Override
    public double maxOutputDebug() {
        return maxOutputDebug;
    }

    @Override
    public Object[] getDependencies() {
        
        return new Object[] { 
            f,  
            
            new Functional() {
                @Override public String toString() {
                    return "inputs";
                }

                @Override public Object[] getDependencies() {
                    return inputs;
                }
            },
            new Functional() {
                @Override public String toString() {
                    return "parameters";
                }

                @Override public Object[] getDependencies() {
                    return parameters;
                }
                
            }
        };
    }

    @Override
    public double getParameter(int i) {
        return parameters[i].value();
    }
    
    @Override
    public Scalar getScalarParameter(int i) {
        return parameters[i];
    }

    @Override
    public void setParameter(int i, double v) {
        parameters[i].setValue(v);
    }

    
    
    
}
