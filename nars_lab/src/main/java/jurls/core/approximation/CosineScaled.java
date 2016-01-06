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
// * S * cos(x)
// * Equivalent (but more computationally efficient) than Product(S, COS)
// */
//public class CosineScaled implements DiffableFunction, Functional {
//
//    private final Scalar s;
//    private final DiffableFunction x;
//
//    public CosineScaled(Scalar s, DiffableFunction x) {
//        this.x = x;
//        this.s = s;
//    }
//
//    public final double cosValue() {
//        return Math.cos(x.value());
//    }
//    
//    
//    public final double cosDeriv(Scalar parameter) {
//        return -x.partialDerive(parameter) * Math.sin(x.value());
//    }
//    
//    @Override
//    public double value() {
//        return s.value() * cosValue();
//    }
//
//    @Override
//    public double partialDerive(Scalar parameter) {
//
//        //this.a = scalar term, this.b = cosine term
//        
//        double b = this.cosValue();
//        double a = s.partialDerive(parameter);
//        
//        
//        double c = s.value();
//        double d = cosDeriv(parameter);
//        
//        return a* b + c * d;
//    }
//
//    @Override
//    public Object[] getDependencies() {
//        return new Object[] { s, x };
//    }
//
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + Arrays.toString(getDependencies());
//    }    
//    
// }
