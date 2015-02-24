/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core;

import jurls.core.approximation.Cosine;
import jurls.core.approximation.DiffableFunction;
import jurls.core.approximation.Functional;
import jurls.core.approximation.Product;
import jurls.core.approximation.Scalar;
import jurls.core.approximation.Sum;

/**
 *
 * @author me
 */
public class Expression {
    
    public static void print(Object f) {
        print(f, 0);
    }
    public static void print(Object f, int indent) {
        for (int i = 0; i < indent; i++)
            System.out.print("    ");
        
        System.out.println(f);
        
        if (f instanceof Functional) {
            Object[] dependencies = ((Functional)f).getDependencies();
            for (Object o : dependencies)
                print(o, indent + 1);
        }
    }
    
    /** performs recursive optimizations and reductions on a function, modifying the function or its dependents
     */
    
//    public static Functional optimize(Functional f) {
//        return optimize(f, null, 0);        
//    }
//    
//    public static Functional optimize(Functional f, Functional parent, int parentIndex) {
//        
//        Functional r = null;
//        
//        if (f instanceof Product) {
//            Product p = (Product)f;
//            DiffableFunction a = p.getA();
//            DiffableFunction b = p.getB();
//            
//            
//            if ((a instanceof Scalar) && (b instanceof Cosine)) {
//                r = new CosineScaled((Scalar)a, ((Cosine)b).getX());
//            }
//            else if ((b instanceof Scalar) && (a instanceof Cosine)) {
//                r = new CosineScaled((Scalar)b, ((Cosine)a).getX());
//            }
//            
//
//        }
//        
//        if (f instanceof Sum) {
//            Sum s = (Sum)f;
//            Object[] d = s.getDependencies();
//            
//            //TODO handle sum of 1 element --> reduces to that element
//            
//            if (d.length == 2) {
//                if ((d[0] instanceof Scalar) && (d[1] instanceof Scalar)) {
//                    r = new SumTwoScalars((Scalar)d[0], (Scalar)d[1]);
//                }
//            }
//        }
//        
//        if (r!=null) {
//            try {
//                parent.replace(parentIndex, r);
//                f = r;
//            }
//            catch (Throwable t) {
//                //failed to replace, continue as-is. but report error so we can fix it
//                System.err.println(t);
//            }                   
//        }        
//        
//        int i = 0;
//        for (Object o : f.getDependencies()) {
//            if (o instanceof Functional)
//                optimize(((Functional)o), f, i);
//            i++;
//        }
//        return f;
//    }
}
