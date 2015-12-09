package objenome.op.cas;

import java.util.ArrayList;

public abstract class Comparison extends Operation {
    
    Expr expr1;
    Expr expr2;
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(expr1);
        arrayList.add(expr2);
        return arrayList;
    }
    
    public boolean notEqualsExpr(Expr expr) {
        return false;
    }
    
    public Expr deriv(Var respected) {
        return Conditional.make(NotEqual.make(expr1, expr2), nope());
    }
    
}
