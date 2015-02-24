/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author thorsten
 * @see http://en.wikipedia.org/wiki/Conjunctive_normal_form
 */
public class CNFBooleanFunction extends AbstractParameterizedFunction {
    protected final int numInputBits;
    protected final int numOutputBits;
       

    protected final int[] numBitsPerVariable;    
    private final int[][] cnf;
    private final long[] parameters;
    private final long[] variables;
    private final long[] intermediates;
    private final Random random = new Random();

    public CNFBooleanFunction(int numInputBits, int numOutputBits, int numInputs) {        
        super(numInputs);
        
        this.numInputBits = numInputBits;
        this.numOutputBits = numOutputBits;
        variables = new long[numInputBits];
        numBitsPerVariable = new int[numInputs];
        
        int error = 0;
        int sum = 0;
        for (int i = 0; i < numBitsPerVariable.length; ++i) {
            numBitsPerVariable[i] = 0;
            while (error < numInputBits && sum < numInputBits) {
                numBitsPerVariable[i]++;
                sum++;
                error += numInputs;
            }
            error -= numBitsPerVariable[i] * numInputs;
        }

        ArrayList<List<Integer>> cnf2 = new ArrayList<>();

        for (int i = 1; i <= numInputBits; ++i) {
            for (int j = i + 1; j <= numInputBits; ++j) {
                for (int k = j + 1; k <= numInputBits; ++k) {
                    for (int l = k + 1; l <= numInputBits; ++l) {
                        cnf2.add(Arrays.asList(l, i, j, k));
                        cnf2.add(Arrays.asList(l, i, j, -k));
                        cnf2.add(Arrays.asList(l, i, -j, k));
                        cnf2.add(Arrays.asList(l, i, -j, -k));
                        cnf2.add(Arrays.asList(l, -i, j, k));
                        cnf2.add(Arrays.asList(l, -i, j, -k));
                        cnf2.add(Arrays.asList(l, -i, -j, k));
                        cnf2.add(Arrays.asList(l, -i, -j, -k));
                        cnf2.add(Arrays.asList(-l, i, j, k));
                        cnf2.add(Arrays.asList(-l, i, j, -k));
                        cnf2.add(Arrays.asList(-l, i, -j, k));
                        cnf2.add(Arrays.asList(-l, i, -j, -k));
                        cnf2.add(Arrays.asList(-l, -i, j, k));
                        cnf2.add(Arrays.asList(-l, -i, j, -k));
                        cnf2.add(Arrays.asList(-l, -i, -j, k));
                        cnf2.add(Arrays.asList(-l, -i, -j, -k));
                    }
                }
            }
        }

        cnf = new int[cnf2.size()][];
        for (int i = 0; i < cnf.length; ++i) {
            List<Integer> maxTerm = cnf2.get(i);
            cnf[i] = new int[maxTerm.size()];
            for (int j = 0; j < maxTerm.size(); ++j) {
                cnf[i][j] = maxTerm.get(j);
            }
        }

        parameters = new long[cnf.length];
        Arrays.fill(parameters, ~0l);
        intermediates = new long[cnf.length];
    }

    private long compute(int clauseIndex) {
        long b = 0;
        int[] maxTerm = cnf[clauseIndex];

        for (int i = 0; i < maxTerm.length; ++i) {
            int literal = maxTerm[i];
            if (literal > 0) {
                b |= variables[literal - 1];
            } else {
                b |= ~variables[-literal - 1];
            }
        }
        b |= parameters[clauseIndex];

        return b;
    }

    private long compute() {
        long a = ~0l;

        for (int j = 0; j < cnf.length; ++j) {
            long b = compute(j);
            intermediates[j] = b;
            a &= b;
        }

        a &= (1l << numOutputBits) - 1l;
        return a;
    }

    @Override
    public double value(double[] xs) {
        int j = 0;

        for (int i = 0; i < xs.length; ++i) {
            long v = FastMath.round(((1l << numBitsPerVariable[i]) - 1) * xs[i]);

            for (int k = 0; k < numBitsPerVariable[i]; ++k, ++j) {
                if (((v >> k) & 1) == 1) {
                    variables[j] = ~0l;
                } else {
                    variables[j] = 0l;
                }
            }
        }

        return (double) compute() / (double) ((1l << numOutputBits) - 1);
    }

    @Override
    public void learn(double[] xs, double y) {
        long currents = FastMath.round(value(xs) * ((1l << numOutputBits) - 1));
        long targets = FastMath.round(y * ((1l << numOutputBits) - 1));

        final ArrayList<Integer> ps = new ArrayList<>(numOutputBits);
        for (int i = 0; i < numOutputBits; ++i) {
            boolean target = ((targets >> i) & 1) == 1;
            boolean current = ((currents >> i) & 1) == 1;
            ps.clear();
            if (target && !current) {
                for (int j = 0; j < intermediates.length; ++j) {
                    if (((intermediates[j] >> i) & 1) == 0) {
                        ps.add(j);
                    }
                }
            } else if (!target && current) {
                for (int j = 0; j < parameters.length; ++j) {
                    parameters[j] ^= 1l << i;
                    if (((compute(j) >> i) & 1) == 0) {
                        ps.add(j);
                    }
                    parameters[j] ^= 1l << i;
                }
            }

            // ps is the "LOGIC GRADIENT" (my invention)
            if (!ps.isEmpty()) {
                int p = ps.get(random.nextInt(ps.size()));
                parameters[p] ^= 1l << i;
            }
        }
    }

    @Override
    public int numberOfParameters() {
        return parameters.length;
    }

    @Override
    public int numberOfInputs() {
        return numBitsPerVariable.length;
    }

    @Override
    public double getParameter(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setParameter(int i, double v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
