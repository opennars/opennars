package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Equals extends Comparison {
    
    private Equals(Expr expr1, Expr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        return new Equals(expr1, expr2).simplify();
    }
    
    public Expr make(List<Expr> exprs) {
        if (exprs.size() <= 2) return Equals.make(exprs.get(0), exprs.get(1));
        throw new UnsupportedOperationException("Equals chaining");
    }
    
    public Expr makeDefined(ArrayList<Expr> exprs) {
        return this.make(exprs);
    }
    
    @Override
    public Expr simplify() {
        Expr conditioned = this.conditioned();
        if (conditioned != null) return conditioned;
        
        if (this.expr1.equalsExpr(this.expr2)) return Expr.yep();
        if (this.expr1.notEqualsExpr(this.expr2)) return Expr.nope();
        
        int sign = Sum.make(this.expr1, Product.negative(this.expr2)).sign();
        switch (sign) {
            case 0:
                return Expr.yep();
            case 2:
                return this;
            default:
                return Expr.nope();
        }
    }
    
    @Override
    public String pretty() {

        Integer thisClassOrder = this.classOrder();
        
        boolean expr1Parens = false;
        if (thisClassOrder > this.expr1.printLevelRight()) expr1Parens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + expr1 + ", printLevelRight=" + expr1.printLevelRight());
        boolean expr2Parens = false;
        if (thisClassOrder > this.expr2.printLevelLeft()) expr2Parens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + expr2 + ", printLevelLeft=" + expr2.printLevelLeft());

        return new StringBuilder().append(expr1Parens ? "(" : "").append(this.expr1.pretty()).append(expr1Parens ? ")" : "").append("=").append(expr2Parens ? "(" : "").append(this.expr2.pretty()).append(expr2Parens ? ")" : "").toString();

     }
    
    @Override
    public int sign() {
        return 2;
    }
    
    @Override
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return Equals.make(this.expr1.copy(subs), this.expr2.copy(subs));
    }
    
    @Override
    public Expr deriv(Var respected) {
        return Conditional.make(Not.make(this), Expr.nope());
    }
    
}
