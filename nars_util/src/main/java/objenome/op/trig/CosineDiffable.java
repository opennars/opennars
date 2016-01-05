///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package objenome.op.trig;
//
//import objenome.op.DiffableFunction;
//import objenome.op.Scalar;
//
///**
// *
// * @author me
// */
//public class CosineDiffable extends Cosine<ScalarFunction> implements DiffableFunction {
//
//    public CosineDiffable() {
//        super();
//    }
//
//    public CosineDiffable(ScalarFunction child) {
//        super(child);
//    }
//
//
//    @Override
//    public double partialDerive(Scalar parameter) {
//        return -input().partialDerive(parameter) * Math.sin(input().value());
//    }
// }
