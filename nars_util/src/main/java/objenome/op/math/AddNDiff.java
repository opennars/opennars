///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package objenome.op.math;
//
//import objenome.op.Scalar;
//import objenome.op.DiffableFunction;
//
///**
// *
// * Differentiable N-ary Add
// * @author thorsten
// */
//public class AddNDiff extends ScalarFunction {
//
//    private final DiffableFunction[] xs;
//
//    public AddNDiff(DiffableFunction... xs) {
//        this.xs = xs;
//    }
//
//    @Override
//    public double value() {
//        double s = 0;
//        for (DiffableFunction x : xs) {
//            s += x.value();
//        }
//        return s;
//    }
//
//    @Override
//    public double partialDerive(Scalar parameter) {
//        double s = 0;
//        for (DiffableFunction x : xs) {
//            s += x.partialDerive(parameter);
//        }
//        return s;
//    }
//
//
// }
