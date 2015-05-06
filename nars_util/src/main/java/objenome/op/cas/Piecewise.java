package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

import objenome.op.cas.util.ArrayLists;

public class Piecewise extends Operation {
    
    ArrayList<Expr> possibilities;
    
    private Piecewise(ArrayList<? extends Expr> possibilities) {
        this.possibilities = ArrayLists.castAll(possibilities, Expr.class);
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return new Piecewise((ArrayList<Expr>) exprs).simplify();
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return new Piecewise((ArrayList<Expr>) exprs).simplify();
    }
    
    public Expr simplify() {
        if (possibilities.isEmpty()) return new Undef();
        if (possibilities.size() == 1) return possibilities.get(0);
        for (int i = 0; i < possibilities.size(); i++) {
            Expr onExpr = possibilities.get(i);
            if (onExpr instanceof Undef) {
                possibilities.remove(i);
                return this.simplify();
            }
            else if (!(onExpr instanceof Conditional)) {
                return onExpr;
            }
        }
        
        return this;
    }
    
    public String pretty() {
        
        String string = new String();
        Integer classOrder = this.classOrder();
        
        for (int i = 0; i < possibilities.size(); i++) {
            Expr expr = possibilities.get(i);
            
            Integer exprLevelLeft = expr.printLevelLeft();
            Integer exprLevelRight = expr.printLevelRight();
            
            boolean parens = false;
            if (i != 0 && exprLevelLeft != null && classOrder > exprLevelLeft) parens = true;
            if (i != possibilities.size() - 1 && exprLevelRight != null && classOrder > exprLevelRight) parens = true;
            
            String exprString = expr.pretty();
            
            if (i != 0) { string = string.concat(", "); }
            
            string = string.concat((parens?"(":"") + exprString + (parens?")":""));
        }
        
        return string;
    }
    
    public int sign() {
         return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ArrayLists.copyAll(possibilities, subs));
    }
    
    public boolean equalsExpr(Expr expr) {
        return false;
    }
    
    public Expr deriv(Var respected) {
        ArrayList<Expr> newpossibilities = new ArrayList<Expr>();
        for (Expr expr : possibilities) {
            newpossibilities.add(expr.deriv(respected));
        }
        return make(newpossibilities);
    }
    
    public ArrayList<Expr> getExprs() {
        return (ArrayList<Expr>) possibilities.clone();
    }
    
}
