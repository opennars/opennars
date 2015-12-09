package objenome.op.cas;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Num extends Constant implements Comparable<Num> {
    
    
    public static void main(String[] args) {
        System.out.println(make(1).equalsExpr(make(1)));
        System.out.println(make(1).equalsExpr(make(0)));
        System.out.println(make(1).notEqualsExpr(make(0)));
        System.out.println(make(1).notEqualsExpr(make(1)));
    }

    private Double val;
    
    private Num() {
        this(0.0d);
    }
    
    private Num(double val) {
        this.val = val;
    }
    
    public static Expr make() {
        return make(0);
    }
    
    public static Expr make(double val) {
        if (allowedVal(val)) return new Num(val);
        
        BigDecimal valBD = new BigDecimal(Double.toString(val));
        // if (debug) System.err.println("Number.make: big: " + valBD);
        
        double numerator= valBD.scaleByPowerOfTen(valBD.scale()).doubleValue();
        double denom = Math.pow(10, valBD.scale());
        // if (debug) System.err.println("Number.make: numerator: " + numerator);
            
        return Division.make(new Num(numerator), new Num(denom));
    }
    
    public static boolean allowedVal(double val) {
        //return true;
        return val == new Double(val).longValue();
    }
    
    public Double val() {
        return val;
    }
    
    public boolean isInt() {
        return val == val.longValue();
    }
    
    public int compareTo(Num number) {
        return new Double(val() - number.val()).intValue();
    }
    
    public Num gcd(Num number) {
        if (!isInt() || !number.isInt()) return new Num();
        
        long val1 = Math.abs(val().longValue());
        long val2 = Math.abs(number.val().longValue());
        
        long lowestVal = val1 <= val2 ? val1 : val2;
        
        if (val1 % lowestVal == 0 && val2 % lowestVal == 0) return new Num(lowestVal);
        
        for (long factor = lowestVal / 2; factor >= 2; factor--) {
            if (val1 % factor == 0 && val2 % factor == 0) return new Num(factor);
        }
        
        return new Num(1);
    }
    
    public Expr deriv(Var respected) {
        return make();
    }
    
    public String pretty() {
        Matcher matcher = Pattern.compile("\\.0(?=[eE]|$)").matcher(val.toString());
        return matcher.replaceFirst("");
    }
    
    public boolean equals(Object o) {
        // if (debug) System.err.println("Number.equals: " + dump() + ", " + (o instanceof Expr ? ((Expr) o) : o));
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Num)) return false;
        
        // if (debug) System.err.println("Number.equals: " + val + " " + (val.equals(((Number) o).val())?"==":"!=") + " " + ((Number) o).val());
        
        return val.equals(((Num) o).val());
    }
    
    public boolean equalsExpr(Expr expr) {
        return equals(expr);
    }
    
    public boolean notEqualsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return false;
        if (!(expr instanceof Num)) return false;
        
        return !val.equals(((Num) expr).val());
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(val);
    }
    
    public boolean isNumberPrinted() {
        return true;
    }
    
    public int sign() {
        if (val > 0) return 1;
        if (val < 0) return -1;
        return 0;
    }

}
