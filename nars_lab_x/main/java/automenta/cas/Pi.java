package objenome.op.cas;

import java.util.HashMap;

public class Pi extends Constant {
    
    public Pi() {
    }
    
    public Expr deriv(Var respected) {
        return Num.make(0);
    }
    
    public String pretty() {
        return "pi";
    }
    
    public boolean equalsExpr(Expr expr) {
        return expr instanceof Pi;
    }
    
    public boolean notEqualsExpr(Expr expr) {
        return false;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return new Pi();
    }
    
    public int sign() {
        return 1;
    }

}
