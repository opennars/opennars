package objenome.op.cas;

import java.util.HashMap;

public class Undef extends Expr {
    
    public Expr deriv(Var respected) {
        return new Undef();
    }
    
    public String pretty() {
        return "undef";
    }
    
    public boolean equalsExpr(Expr expr) {
        return false;
    }
    
    public boolean notEqualsExpr(Expr expr) {
        return false;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> substitutions) {
        return new Undef();
    }
    
    public int sign() {
        return 2;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        return o instanceof Undef;
    }
    
}
