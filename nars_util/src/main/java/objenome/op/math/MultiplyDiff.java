///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package objenome.op.math;
//
//import objenome.op.DiffableFunction;
//import objenome.op.Scalar;
//
///**
// *
// * @author thorsten
// */
//public class MultiplyDiff extends ScalarFunction /*extends Multiply<ScalarFunction>*/ implements DiffableFunction {
//
//    private DiffableFunction a;
//    private DiffableFunction b;
//
//    public MultiplyDiff(DiffableFunction a, DiffableFunction b) {
//        this.a = a;
//        this.b = b;
//    }
//
//    @Override
//    public double value() {
//        return a.value() * b.value();
//    }
//
//    @Override
//    public double partialDerive(Scalar parameter) {
//        double a = this.a.partialDerive(parameter);
//        double b = this.b.value();
//        double c = this.a.value();
//        double d = this.b.partialDerive(parameter);
//        return a * b + c * d;
//    }
//
// }
