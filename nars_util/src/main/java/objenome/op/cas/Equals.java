package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

public class Equals extends Comparison {
    
    private Equals(Expr expr1, Expr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        return new Equals(expr1, expr2).simplify();
    }
    
    public Expr make(ArrayList<Expr> exprs) {
        if (exprs.size() <= 2) return make(exprs.get(0), exprs.get(1));
        throw new UnsupportedOperationException("Equals chaining");
    }
    
    public Expr makeDefined(ArrayList<Expr> exprs) {
        return make(exprs);
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        if (expr1.equalsExpr(expr2)) return yep();
        if (expr1.notEqualsExpr(expr2)) return nope();
        
        int sign = Sum.make(expr1, Product.negative(expr2)).sign();
        if (sign == 0) return yep();
        if (sign == 2) return this;
        return nope();
    }
    
    public String pretty() {
        String string = "";
        
        Integer thisClassOrder = this.classOrder();
        
        boolean expr1Parens = false;
        if (thisClassOrder > expr1.printLevelRight()) expr1Parens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + expr1 + ", printLevelRight=" + expr1.printLevelRight());
        boolean expr2Parens = false;
        if (thisClassOrder > expr2.printLevelLeft()) expr2Parens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + expr2 + ", printLevelLeft=" + expr2.printLevelLeft());
        
        string = string.concat((expr1Parens?"(":"") + expr1.pretty() + (expr1Parens?")":""));
        string = string.concat("=");
        string = string.concat((expr2Parens?"(":"") + expr2.pretty() + (expr2Parens?")":""));
        
        return string;
    }
    
    public int sign() {
        return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(expr1.copy(subs), expr2.copy(subs));
    }
    
    public Expr deriv(Var respected) {
        return Conditional.make(Not.make(this), nope());
    }
    
}
