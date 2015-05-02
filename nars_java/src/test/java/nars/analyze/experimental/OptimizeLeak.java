package nars.analyze.experimental;

import nars.ProtoNAR;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.Task;
import nars.nal.filter.ConstantDerivationLeak;
import nars.prototype.Default;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.util.Arrays;

/**
 * Created by me on 5/1/15.
 */
public class OptimizeLeak {


    /**
     * from: https://git1-us-west.apache.org/repos/asf?p=commons-math.git;a=blob_plain;f=src/test/java/org/apache/commons/math4/optim/nonlinear/scalar/noderiv/BOBYQAOptimizerTest.java;hb=cb21480cb1255771ee5f5ea5fe108cd994048759
     * @param func Function to optimize.
     * @param startPoint Starting point.
     * @param boundaries Upper / lower point limit.
     * @param goal Minimization or maximization.
     * @param fTol Tolerance relative error on the objective function.
     * @param pointTol Tolerance for checking that the optimum is correct.
     * @param maxEvaluations Maximum number of evaluations.
     * @param additionalInterpolationPoints Number of interpolation to used
     * in addition to the default (2 * dim + 1).
     * @param expected Expected point / value.
     */
    public static void optimize(MultivariateFunction func,
                        double[] startPoint,
                        double[][] boundaries,
                        GoalType goal,
                        //double fTol,
                        //double pointTol,
                        int maxEvaluations,
                        int additionalInterpolationPoints) {

//         System.out.println(func.getClass().getName() + " BEGIN"); // XXX

        int dim = startPoint.length;
        final int numIterpolationPoints = 2 * dim + 1 + additionalInterpolationPoints;
        BOBYQAOptimizer optim = new BOBYQAOptimizer(numIterpolationPoints);
        PointValuePair result = boundaries == null ?
                optim.optimize(new MaxEval(maxEvaluations),
                        new ObjectiveFunction(func),
                        goal,
                        SimpleBounds.unbounded(dim),
                        new InitialGuess(startPoint)) :
                optim.optimize(new MaxEval(maxEvaluations),
                        new ObjectiveFunction(func),
                        goal,
                        new InitialGuess(startPoint),
                        new SimpleBounds(boundaries[0],
                                boundaries[1]));
//        System.out.println(func.getClass().getName() + " = "
//              + optim.getEvaluations() + " f(");
//        for (double x: result.getPoint())  System.out.print(x + " ");
//        System.out.println(") = " +  result.getValue());
        //Assert.assertEquals(assertMsg, expected.getValue(), result.getValue(), fTol);
//        for (int i = 0; i < dim; i++) {
//            Assert.assertEquals(expected.getPoint()[i],
//                    result.getPoint()[i], pointTol);
//        }

//         System.out.println(func.getClass().getName() + " END"); // XXX
    }


    private static double[][] boundaries(int dim,
                                         double lower, double upper) {
        double[][] boundaries = new double[2][dim];
        for (int i = 0; i < dim; i++)
            boundaries[0][i] = lower;
        for (int i = 0; i < dim; i++)
            boundaries[1][i] = upper;
        return boundaries;
    }

    public static void main(String[] args) {
        final int cycles = 1000;
        final int iterations = 2000;

        optimize(new MultivariateFunction() {
            @Override
            public double value(double[] d) {
                final float jLeakPri = (float)d[0];
                final float jLeakDur = (float)d[1];
                final float gLeakPri = (float)d[2];
                final float gLeakDur = (float)d[3];
                final float qLeakPri = (float)d[4];
                final float qLeakDur = (float)d[5];

                ProtoNAR b = new Default() {
                    @Override
                    protected void initDerivationFilters() {

                        getNALParam().derivationFilters.add(new ConstantDerivationLeak(0,0) {
                            @Override
                            protected void leak(Task derived) {
                                switch (derived.getPunctuation()) {

                                    case Symbols.JUDGMENT:
                                        derived.mulPriority(jLeakPri);
                                        derived.mulDurability(jLeakDur);
                                        break;

                                    case Symbols.GOAL:
                                        derived.mulPriority(gLeakPri);
                                        derived.mulDurability(gLeakDur);
                                        break;

                                    case '?':
                                    case Symbols.QUEST:
                                        derived.mulPriority(qLeakPri);
                                        derived.mulDurability(qLeakDur);
                                        break;
                                }
                            }
                        });
                    }
                };

                ExampleScores e = new ExampleScores(b, cycles);
                System.out.println(Arrays.toString(d) + ", " + e.totalCost);

                return e.totalCost;
            }
        }, new double[] { 0.4, 0.4, 0.4, 0.4, 0.4, 0.4 }, boundaries(6, 0.05, 1.0), GoalType.MINIMIZE, iterations, 1);
    }

    public static void main2(String[] args) {
        final int cycles = 1500;

        for (double p = 0.1; p <= 0.6; p += 0.1) {
            for (double d = 0.1; d <= 0.6; d += 0.1) {

                final double finalD = d;
                final double finalP = p;
                ProtoNAR b = new Default() {
                    @Override
                    protected void initDerivationFilters() {
                        final float DERIVATION_PRIORITY_LEAK = (float)finalP; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                        final float DERIVATION_DURABILITY_LEAK = (float)finalD; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                        getNALParam().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
                    }
                };

                ExampleScores e = new ExampleScores(b, cycles);
                System.out.println(Texts.n4(p) + ", " + Texts.n4(d) + ", " + e.totalCost);
            }
        }
    }
}
