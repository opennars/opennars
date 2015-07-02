package objenome.op.cas;

public class Subscript {
    
    private Expr expr;
    
    public Subscript(Expr expr) {
        this.expr = expr;
    }
    
    public Expr getExpr() {
        return expr;
    }
    
    public String toString() {
        return "subscript(" + expr + ')';
    }
    
}
