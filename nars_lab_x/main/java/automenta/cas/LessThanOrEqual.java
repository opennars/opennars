package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LessThanOrEqual extends Comparison {
    
    public LessThanOrEqual(Expr expr1, Expr expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        return new LessThanOrEqual(expr1, expr2).simplify();
    }
    
    public static Expr make(List<? extends Expr> exprs) {
        if (exprs.size() <= 2) return make(exprs.get(0), exprs.get(1));
        throw new UnsupportedOperationException("LessThanOrEqual chaining");
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        return Or.make(LessThan.make(expr1, expr2), Equals.make(expr1, expr2));
    }
    
    public String pretty() {

        Integer thisClassOrder = classOrder();
        
        boolean expr1Parens = false;
        if (thisClassOrder > expr1.printLevelRight()) expr1Parens = true;
        boolean expr2Parens = false;
        if (thisClassOrder > expr2.printLevelLeft()) expr2Parens = true;

        String string = "";
        string = string + (expr1Parens ? "(" : "") + expr1.pretty() + (expr1Parens ? ")" : "");
        string = string + "<=";
        string = string + (expr2Parens ? "(" : "") + expr2.pretty() + (expr2Parens ? ")" : "");
        
        return string;
    }
    
    public int sign() {
        return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(expr1.copy(subs), expr2.copy(subs));
    }
    
}
