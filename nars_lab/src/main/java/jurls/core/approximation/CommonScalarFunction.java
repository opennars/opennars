///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.core.approximation;
//
//import org.apache.commons.math3.analysis.UnivariateFunction;
//
///**
// * DiffableFunction adapter to CommonsMath interfaces
// */
//public class CommonScalarFunction implements ScalarFunction, UnivariateFunction {
//
//    public final DiffableFunction x;
//    private final UnivariateFunction function;
//
//    public CommonScalarFunction(UnivariateFunction function, DiffableFunction x) {
//        this.function = function;
//        this.x = x;
//    }
//
//    @Override
//    public double value() {
//        return value(x.value());
//    }
//
//    @Override
//    public double value(double d) {
//        return function.value(d);
//    }
//
//
// }
