/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jurls.core.approximation.Scalar.ArrayIndexScalar;
import jurls.core.approximation.Scalar.AtomicScalar;

/**
 *
 * @author thorsten
 */
public class Generator {

    public static Scalar newBoundedParameter(double lower, double upper, int name, double[] array, List<ArrayIndexScalar> parameterList) {
        Scalar s = newParameter(lower, upper, name, array, parameterList);
        s.setUpperBound(upper);
        s.setLowerBound(lower);
        return s;
    }

    public static Scalar newParameter(double lower, double upper, int name, double[] array, Collection<ArrayIndexScalar> parameterList) {        
        ArrayIndexScalar s = new ArrayIndexScalar(array, name, Math.random() * (upper - lower) + lower, "p" + name);
        parameterList.add(s);
        return s;
    }

    public static DiffableFunctionGenerator generateTanhFFNN() {
        return (Scalar[] inputs, double[] array, List<ArrayIndexScalar> parameterList, int numFeatures) -> {
            List<DiffableFunction> xs = new ArrayList<>();
            int pi = 0;

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunction> ys = new ArrayList<>();

                for (final Scalar input : inputs) {
                    ys.add(
                            new Product(
                                    newParameter(-10, 10, pi++, array, parameterList),
                                    input
                            )
                    );
                }
                ys.add(newParameter(-3, 0, pi++, array, parameterList));

                xs.add(
                        new Product(
                                newParameter(-1, 1, pi++, array, parameterList),
                                new TanhSigmoid(
                                        new Sum(ys.toArray(new DiffableFunction[ys.size()]))
                                )
                        )
                );
            }
            xs.add(newParameter(-1, 1, pi++, array, parameterList));

            return new Sum(
                    newParameter(0.5, 0.5, pi++, array, parameterList),
                    new Product(
                            newParameter(0.01, 0.01, pi++, array, parameterList),
                            new TanhSigmoid(
                                    new Sum(xs.toArray(new DiffableFunction[xs.size()]))
                            )
                    )
            );
        };
    }

    public static DiffableFunctionGenerator generateATanFFNN() {
        return (Scalar[] inputs, double[] array, List<ArrayIndexScalar> parameterList, int numFeatures) -> {
            List<DiffableFunction> xs = new ArrayList<>();
            int pi = 0;

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunction> ys = new ArrayList<>();

                for (final Scalar input : inputs) {
                    ys.add(
                            new Product(newParameter(-10, 10, pi++, array, parameterList), input)
                    );
                }
                ys.add(newParameter(-3, 0, pi++, array, parameterList));

                xs.add(
                        new Product(
                                newParameter(-1, 1, pi++, array, parameterList),
                                new ATanhSigmoid(
                                        new Sum(ys.toArray(new DiffableFunction[ys.size()]))
                                )
                        )
                );
            }
            xs.add(newParameter(-1, 1, pi++, array, parameterList));

            return new Sum(
                    newParameter(0.5, 0.5, pi++, array, parameterList),
                    new Product(
                            newParameter(0.01, 0.01, pi++, array, parameterList),
                            new ATanhSigmoid(
                                    new Sum(xs.toArray(new DiffableFunction[xs.size()]))
                            )
                    )
            );
        };
    }

    public static DiffableFunctionGenerator generateLogisticFFNN() {
        return (Scalar[] inputs, double[] array, List<ArrayIndexScalar> parameterList, int numFeatures) -> {
            List<DiffableFunction> xs = new ArrayList<>();
            int pi = 0;

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunction> ys = new ArrayList<>();

                for (final Scalar input : inputs) {
                    ys.add(
                            new Product(newParameter(-10, 10, pi++, array, parameterList), input)
                    );
                }
                ys.add(newParameter(-3, 0, pi++, array, parameterList));

                xs.add(
                        new Product(
                                newParameter(-1, 1, pi++, array, parameterList),
                                new LogisticSigmoid(
                                        new Sum(ys.toArray(new DiffableFunction[ys.size()]))
                                )
                        )
                );
            }
            xs.add(newParameter(-1, 1, pi++, array, parameterList));

            return new Sum(
                    newParameter(0.5, 0.5, pi++, array, parameterList),
                    new Product(
                            newParameter(0.01, 0.01, pi++, array, parameterList),
                            new LogisticSigmoid(
                                    new Sum(xs.toArray(new DiffableFunction[xs.size()]))
                            )
                    )
            );
        };
    }

    public static DiffableFunctionGenerator generateRBFNet() {
        return (Scalar[] inputs, double[] array, List<ArrayIndexScalar> parameterList, int numFeatures) -> {
            List<DiffableFunction> xs = new ArrayList<>();
            int pi = 0;
            final double fact = 2 * numFeatures;

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunction> ys = new ArrayList<>();

                for (final Scalar input : inputs) {
                    ys.add(
                            new Product(
                                    newParameter(fact, fact, pi++, array, parameterList),
                                    new Sum(newParameter(-1, 0, pi++, array, parameterList), input)
                            )
                    );
                }
                ys.add(newParameter(0, 0, pi++, array, parameterList));
                DiffableFunction sum = new Sum(ys.toArray(new DiffableFunction[ys.size()]));
                DiffableFunction sqr = new Product(sum, sum);

                Scalar p = newParameter(-1, -1, pi++, array, parameterList);
                p.setUpperBound(0);

                xs.add(
                        new Product(
                                newParameter(1, 1, pi++, array, parameterList),
                                new Exp(
                                        new Product(p, sqr)
                                )
                        )
                );
            }

            xs.add(newParameter(0, 0, pi++, array, parameterList));

            return new Sum(
                    newParameter(0.5, 0.5, pi++, array, parameterList),
                    new Product(
                            newParameter(0.01, 0.01, pi++, array, parameterList),
                            new Sum(xs.toArray(new DiffableFunction[xs.size()]))
                    )
            );
        };
    }

    public static DiffableFunctionGenerator generateFourierBasis() {
        return (Scalar[] inputs, double[] array, List<ArrayIndexScalar> parameterList, int numFeatures) -> {
            List<DiffableFunction> xs = new ArrayList<>();
            int pi = 0;

            for (int i = 1; i <= numFeatures; ++i) {
                List<DiffableFunction> ys = new ArrayList<>();

                for (final Scalar input : inputs) {
                    ys.add(
                            new Sum(
                                    new Product(
                                            newParameter(1, 1, pi++, array, parameterList),
                                            input
                                    ),
                                    newParameter(-1, 0, pi++, array, parameterList)
                            )
                    );
                }
                ys.add(newParameter(0, 0, pi++, array, parameterList));

                final double f = Math.PI * i;
                xs.add(
                        new Product(
                                newParameter(1, 1, pi++, array, parameterList),
                                new Cosine(
                                        new Product(
                                                newParameter(f, f, pi++, array, parameterList),
                                                new Sum(ys.toArray(new DiffableFunction[ys.size()]))
                                        )
                                )
                        )
                );

            }

            xs.add(newParameter(0, 0, pi++, array, parameterList));
            return new Sum(
                    newParameter(0.5, 0.5, pi++, array, parameterList),
                    new Product(
                            newParameter(0.01, 0.01, pi++, array, parameterList),
                            new Sum(xs.toArray(new DiffableFunction[xs.size()]))
                    )
            );
        };
    }

    public static ParameterizedFunctionGenerator generateGradientFourierBasis(
            ApproxParameters approxParameters,
            int numFeatures) {
        return (int numInputVectorElements) -> {
            return new OutputNormalizer(
                    new InputNormalizer(
                            new GradientFitter(
                                    approxParameters,
                                    new DiffableFunctionMarshaller(
                                            Generator.generateFourierBasis(),
                                            numInputVectorElements,
                                            numFeatures
                                    )
                            )
                    )
            );
        };
    }

    public static ParameterizedFunctionGenerator generateGradientATanFFNN(
            ApproxParameters approxParameters,
            int numFeatures) {
        return (int numInputVectorElements) -> {
            return new OutputNormalizer(
                    new InputNormalizer(
                            new GradientFitter(
                                    approxParameters,
                                    new DiffableFunctionMarshaller(
                                            Generator.generateATanFFNN(),
                                            numInputVectorElements,
                                            numFeatures
                                    )
                            )
                    )
            );
        };
    }

    public static ParameterizedFunctionGenerator generateCNFFunction(
            int numInputBits,
            int numOutputBits
    ) {
        return (int numInputVectorElements) -> {
            return new OutputNormalizer(
                    new InputNormalizer(
                            new CNFBooleanFunction(
                                    numInputBits,
                                    numOutputBits,
                                    numInputVectorElements
                            )
                    )
            );
        };
    }
}
