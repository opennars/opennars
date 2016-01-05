/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.util;

import objenome.op.DiffableFunction;
import objenome.op.Scalar;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 *
 * @author thorsten
 */
public enum Utils {
    ;

    public static <T> T[] toArray(Collection c) {
        return (T[]) c.toArray((T[]) Array.newInstance(c.iterator().next().getClass(), c.size()));
    }

    public static double[] join(double[] state, double action) {
        double[] xs = new double[state.length + 1];
        System.arraycopy(state, 0, xs, 0, state.length);
        xs[xs.length - 1] = action;
        return xs;
    }


    public static ArrayRealVector gradient(DiffableFunction f, Scalar[] parameters, ArrayRealVector result) {
        if (result == null)
            result = new ArrayRealVector(parameters.length);

        double[] d = result.getDataRef();
        for (int i = 0; i < parameters.length; ++i) {
            d[i] = f.partialDerive(parameters[i]);
        }
        return result;
    }

    public static double lengthSquare(double[] v) {
        double s = 0;
        for (double x : v) {
            s += x * x;
        }
        return s;
    }
    
    public static double length(double[] v) {
        return Math.sqrt(lengthSquare(v));
    }

    public static double[] add(double[] a, double[] b) {
        assert a.length == b.length;

        double[] v = new double[a.length];

        for (int i = 0; i < a.length; ++i) {
            v[i] = a[i] + b[i];
        }

        return v;
    }

    public static double[] sub(double[] a, double[] b) {
        assert a.length == b.length;

        double[] v = new double[a.length];

        for (int i = 0; i < a.length; ++i) {
            v[i] = a[i] - b[i];
        }

        return v;
    }

    public static double[] mult(double a, double[] b) {
        double[] v = new double[b.length];

        for (int i = 0; i < b.length; ++i) {
            v[i] = a * b[i];
        }

        return v;
    }

    public static double[] normalize(double[] a) {
        double l = length(a);
        if (l < 0.1) {
            l = 0.1;
        }
        double[] v = new double[a.length];

        for (int i = 0; i < a.length; ++i) {
            v[i] = a[i] / l;
        }

        return v;
    }

}
