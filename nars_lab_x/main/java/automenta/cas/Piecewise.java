package objenome.op.cas;

import objenome.op.cas.util.ArrayLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

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
        Piecewise other = this;
        simplify:
        while (true) {
            if (other.possibilities.isEmpty()) return new Undef();
            if (other.possibilities.size() == 1) return other.possibilities.get(0);
            for (int i = 0; i < other.possibilities.size(); i++) {
                Expr onExpr = other.possibilities.get(i);
                if (onExpr instanceof Undef) {
                    other.possibilities.remove(i);
                    other = other;
                    continue simplify;
                } else if (!(onExpr instanceof Conditional)) {
                    return onExpr;
                }
            }

            return other;
        }
    }
    
    public String pretty() {
        
        String string = "";
        Integer classOrder = classOrder();
        
        for (int i = 0; i < possibilities.size(); i++) {
            Expr expr = possibilities.get(i);
            
            Integer exprLevelLeft = expr.printLevelLeft();
            Integer exprLevelRight = expr.printLevelRight();
            
            boolean parens = false;
            if (i != 0 && exprLevelLeft != null && classOrder > exprLevelLeft) parens = true;
            if (i != possibilities.size() - 1 && exprLevelRight != null && classOrder > exprLevelRight) parens = true;
            
            String exprString = expr.pretty();
            
            if (i != 0) { string = string + ", "; }
            
            string = string + (parens ? "(" : "") + exprString + (parens ? ")" : "");
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
        ArrayList<Expr> newpossibilities = possibilities.stream().map(expr -> expr.deriv(respected)).collect(Collectors.toCollection(ArrayList::new));
        return make(newpossibilities);
    }
    
    public ArrayList<Expr> getExprs() {
        return (ArrayList<Expr>) possibilities.clone();
    }
    
}
