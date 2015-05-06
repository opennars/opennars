package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

import objenome.op.cas.util.ArrayLists;

public class And extends Operation {
    
    ArrayList<Expr> exprs;
    
    public And(ArrayList<? extends Expr> exprs) {
        this.exprs = ArrayLists.castAll(exprs, Expr.class);
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return new And(exprs).simplify();
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        ArrayList<Expr> exprs = new ArrayList<Expr>();
        exprs.add(expr1);
        exprs.add(expr2);
        return make(exprs);
    }
    
    public Expr simplify() {
        // no conditions check
        
        while (exprs.remove(yep())) {}
        if (exprs.isEmpty()) return yep();
        if (exprs.size() == 1) return exprs.get(0);
        
        ArrayList<Expr> ors = new ArrayList<Expr>();
        for (Expr expr : exprs) {
            ors.add(Not.make(expr));
        }
        return Not.make(Or.make(ors));
    }
    
    public ArrayList<Expr> getExprs() {
        return (ArrayList<Expr>) exprs.clone();
    }
    
    public String pretty() {
        String string = new String();
        Integer classOrder = this.classOrder();
        
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            
            Integer exprLevelLeft = expr.printLevelLeft();
            Integer exprLevelRight = expr.printLevelRight();
            
            boolean parens = false;
            if (i != 0 && exprLevelLeft != null && classOrder > exprLevelLeft) parens = true;
            if (i != exprs.size() - 1 && exprLevelRight != null && classOrder > exprLevelRight) parens = true;
            
            String exprString = expr.pretty();
            
            if (i != 0) { string = string.concat(" and "); }
            
            string = string.concat((parens?"(":"") + exprString + (parens?")":""));
        }
        
        return string;
    }
    
    public int sign() {
        return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ArrayLists.copyAll(exprs, subs));
    }
    
    public boolean equalsExpr(Expr expr) {
        return false;
    }
    
    public Expr deriv(Var respected) {
        return nope();
    }
    
    public boolean implies(Expr expr) {
        for (Expr expr2 : expr.implies()) {
            if (equalsExpr(expr2)) continue;
            boolean found = false;
            for (Expr expr3 : exprs) {
                if (expr3.implies(expr)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
    
    public ArrayList<Expr> implies() {
        return getExprs();
    }
    
}
