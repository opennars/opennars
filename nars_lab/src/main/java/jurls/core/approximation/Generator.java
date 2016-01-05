/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten
 */
public enum Generator {
    ;

    public static DiffableFunctionGenerator generateWavelets(
            int numFeatures
    ) {
        return (int numInputs) -> {
            GeneratorContext gc = new GeneratorContext(numInputs);
            List<DiffableFunctionSource> xs = new ArrayList<>();

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunctionSource> ys = new ArrayList<>();

                for (Scalar input : gc.getInputScalars()) {
                    DiffableFunctionSource f = new Product(
                            gc.newParameter(1),
                            new Sum(
                                    gc.newParameter(-1, 0),
                                    input
                            )
                    );
                    ys.add(new Product(f, f));
                }
                DiffableFunctionSource s = new Sum(ys.toArray(new DiffableFunctionSource[ys.size()]));

                DiffableFunctionSource g = new Cosine(new Product(gc.newParameter(50), s));

                Scalar p = gc.newParameter(-5);
                p.setUpperBound(0);
                xs.add(new Product(new Exp(new Product(p, s)), g));
            }
            xs.add(gc.newParameter(0));

            DiffableFunctionSource f = new Sum(
                    gc.newParameter(0.5),
                    new Product(
                            gc.newParameter(0.01),
                            new Sum(xs.toArray(new DiffableFunctionSource[xs.size()]))
                    )
            );

            gc.setDiffableFunctionSource(f);
            return gc;
        };
    }

    public static DiffableFunctionGenerator generateFFNN(
            ActivationFunctionFactory hiddenLayer,
            ActivationFunctionFactory outputLayer,
            int numFeatures
    ) {
        return (int numInputs) -> {
            GeneratorContext gc = new GeneratorContext(numInputs);
            List<DiffableFunctionSource> xs = new ArrayList<>();

            for (int i = 0; i < numFeatures; ++i) {
                List<DiffableFunctionSource> ys = new ArrayList<>();

                for (Scalar input : gc.getInputScalars()) {
                    ys.add(
                            new Product(
                                    gc.newParameter(0, 10),
                                    new Sum(
                                            gc.newParameter(-1, 0),
                                            input
                                    )
                            )
                    );
                }
                ys.add(gc.newParameter(0));
                xs.add(
                        new Product(
                                gc.newParameter(-1, 1),
                                hiddenLayer.newInstance(gc, ys)
                        )
                );
            }
            xs.add(gc.newParameter(0));

            DiffableFunctionSource f = new Sum(
                    gc.newParameter(0.5),
                    new Product(
                            gc.newParameter(0.01),
                            outputLayer.newInstance(gc, xs)
                    )
            );

            gc.setDiffableFunctionSource(f);
            return gc;
        };
    }

    public static DiffableFunctionGenerator generateFourierBasis(
            int numFeatures
    ) {
        return (int numInputs) -> {
            GeneratorContext gc = new GeneratorContext(numInputs);
            List<DiffableFunctionSource> xs = new ArrayList<>();

            for (int i = 1; i <= numFeatures; ++i) {
                List<DiffableFunctionSource> ys = new ArrayList<>();

                for (Scalar input : gc.getInputScalars()) {
                    ys.add(
                            new Product(
                                    gc.newParameter(0, 1),
                                    new Sum(
                                            gc.newParameter(-1, 0),
                                            input
                                    )
                            )
                    );
                }
                ys.add(gc.newParameter(-1,1));

                xs.add(
                        new Product(
                                gc.newParameter(-1,1),
                                new Cosine(
                                        new Product(
                                                gc.newParameter(Math.PI * (i + 1)),
                                                new Sum(ys.toArray(new DiffableFunctionSource[ys.size()]))
                                        )
                                )
                        )
                );

            }

            xs.add(gc.newParameter(0, 0));
            DiffableFunctionSource f = new Sum(
                    gc.newParameter(0.5, 0.5),
                    new Product(
                            gc.newParameter(0.01, 0.01),
                            new Sum(xs.toArray(new DiffableFunctionSource[xs.size()]))
                    )
            );

            gc.setDiffableFunctionSource(f);
            return gc;
        };
    }

    public static ParameterizedFunctionGenerator generateGradientFourierBasis(
            ApproxParameters approxParameters,
            int numFeatures) {
        return (int numInputVectorElements) -> new OutputNormalizer(
                new InputNormalizer(
                        new GradientFitter(
                                approxParameters,
                                new DiffableFunctionMarshaller(
                                        Generator.generateFourierBasis(numFeatures),
                                        numInputVectorElements
                                )
                        )
                )
        );
    }

    public static ParameterizedFunctionGenerator generateGradientFFNN(
            ActivationFunctionFactory hiddenLayer,
            ActivationFunctionFactory outputLayer,
            ApproxParameters approxParameters,
            int numFeatures) {
        return (int numInputVectorElements) -> new OutputNormalizer(
                new InputNormalizer(
                        new GradientFitter(
                                approxParameters,
                                new DiffableFunctionMarshaller(
                                        Generator.generateFFNN(
                                                hiddenLayer,
                                                outputLayer,
                                                numFeatures
                                        ),
                                        numInputVectorElements
                                )
                        )
                )
        );
    }

    public static ParameterizedFunctionGenerator generateCNFFunction(
            int numInputBits,
            int numOutputBits
    ) {
        return (int numInputVectorElements) -> new OutputNormalizer(
                new InputNormalizer(
                        new CNFBooleanFunction(
                                numInputBits,
                                numOutputBits,
                                numInputVectorElements
                        )
                )
        );
    }
}
