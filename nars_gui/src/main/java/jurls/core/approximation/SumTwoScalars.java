///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package jurls.core.approximation;
//
//import java.util.Arrays;
//
///**
// *
// * @author thorsten
// */
//public class SumTwoScalars implements DiffableFunction<Scalar> {
//
//    private final Scalar a, b;
//
//    public SumTwoScalars(Scalar a, Scalar b) {
//        this.a = a;
//        this.b = b;
//    }
//
//    @Override
//    final public double value() {
//        return a.value() + b.value();
//    }
//
//    @Override
//    final public double partialDerive(Scalar parameter) {
//        return a.partialDerive(parameter) + b.partialDerive(parameter);
//    }
//
//    @Override
//    public Object[] getDependencies() {        
//        return new Object[] { a, b };
//    }
//    
//    
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + Arrays.toString(getDependencies());
//                
//    }
//        
//}
