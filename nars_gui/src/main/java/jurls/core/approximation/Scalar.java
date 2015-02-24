/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten
 */
abstract public class Scalar implements DiffableFunction {


    private double upperBound = Double.POSITIVE_INFINITY;
    private double lowerBound = Double.NEGATIVE_INFINITY;
    
    /** a scalar that manages its own value */
    public static class AtomicScalar extends Scalar {
        public double value = 0;

        public AtomicScalar(double value) {
            super(value);
        }

        public AtomicScalar(double value, String name) {
            super(value, name);
        }
                

        @Override
        final public double value() {
            return this.value;
        }

        @Override
        public void _setValue(double v) {
            this.value = v;
        }
        
        public void addSelf(double d) {
            value += d;
        }
        
    }
    
    /** a scalar wrapping an element in a larger backing array */
    public static class ArrayIndexScalar extends Scalar {
        
        private final double[] array;
        private final int index;

        public ArrayIndexScalar(double[] array, int index, double value) {
            super();
            this.array = array;
            this.index = index;
            setValue(value);
        }

        public ArrayIndexScalar(double[] array, int index, double value, String name) {
            this(array, index, value);
            this.name = name;            
        }

        
        @Override
        public double value() {
            return array[index];
        }

        @Override
        public void _setValue(double v) {
            array[index] = v;
        }

        @Override
        public void addSelf(double d) {
            array[index] += d;
        }
        
    }
    
    String name = "";

    public Scalar() {
        super();
    }
    
    public Scalar(double value) {
        this(value, "");
    }
    
    public Scalar(double value, String name) {
        super();
        this.name = name;
        setValue(value);
    }

    abstract public double value();

    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {                               
        if (this instanceof ArrayIndexScalar) {
            ArrayIndexScalar ai = (ArrayIndexScalar)this;
            if (ai.array == f.array) {           
                return sb.append("(a[").append(ai.index).append("])");
            }            
            if (ai.array == f.input) {           
                return sb.append("(i[").append(ai.index).append("])");
            }            
        }
        
        throw new RuntimeException("Unsupported scalar in expression: " + this);
        
        /*
        //store it as a registered symbol (slower access)
        int index = f.bind(this);        
        return sb.append("f[" + index + "].value");
        //sb.append("f[" + index + "].value()");
        */
    }

    
    @Override
    final public double partialDerive(Scalar parameter) {
        if (this == parameter) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction parameter) {
        final int value;
        if (this == parameter) value = 1; else value = 0;
        return sb.append(value);                
    }


    public void setValue(double v) {
        if (v < lowerBound)
            v = lowerBound;
        else if (v > upperBound)
            v = upperBound;
        else
            v = v;
        _setValue(v);
    }
    
    abstract public void _setValue(final double v);

    public void setUpperBound(double x) {
        upperBound = x;
    }

    public void setLowerBound(double x) {
        lowerBound = x;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "Scalar[" + name + "]";
    }

    abstract public void addSelf(double d);

    public static double[] doubleArray(Scalar[] s) {
        double d[] = new double[s.length];
        for (int i = 0; i < s.length; i++)
            d[i] = s[i].value();
        return d;
    }

    public double getLowerBound() {
        return lowerBound;
    }
    public double getUpperBound() {
        return upperBound;
    }

    /** linear interpolation; change rate = 1.0 - momentum
     *  (no change) 0 <= rate <= 1.0 (instantly set) */
    void lerp(double targetValue, double rate) {
        setValue( value() * (1.0 - rate) + targetValue * rate);
    }

    
    
}
