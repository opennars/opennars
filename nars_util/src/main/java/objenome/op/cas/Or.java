package objenome.op.cas;

import objenome.op.cas.util.ArrayLists;

import java.util.ArrayList;
import java.util.HashMap;

public class Or extends Operation {
    
    ArrayList<Expr> exprs;
    
    private Or(ArrayList<? extends Expr> exprs) {
        this.exprs = ArrayLists.castAll(exprs, Expr.class);
    }
    
    public static Expr make(ArrayList<Expr> exprs) {
        return new Or(exprs).simplify();
    }
    
    public static Expr makeDefined(ArrayList<Expr> exprs) {
        return make(exprs);
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        ArrayList<Expr> exprs = new ArrayList<>();
        exprs.add(expr1);
        exprs.add(expr2);
        return make(exprs);
    }
    
    public Expr simplify() {
        if (exprs.isEmpty()) return nope();
        if (exprs.size() == 1) return exprs.get(0);
        
        for (int i = 0; i < exprs.size(); i++) {
            for (int j = 0; j < exprs.size(); j++) {
                if (i != j) {
                    HashMap<Expr, Expr> subs = new HashMap<>();
                    subs.put(exprs.get(i), nope());
                    exprs.set(j, exprs.get(j).copy(subs));
                }
            }
        }
        
        boolean isConditioned = false;
        boolean allNot = true;
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            
            if (expr instanceof Or) {
                exprs.remove(i);
                exprs.addAll(i, ((Operation) expr).getExprs());
                i--;
                continue;
            }
            if (expr instanceof Conditional && ((Operation) expr).getExpr(0).equalsExpr(yep())) {
                isConditioned = true;
                exprs.remove(i);
                exprs.add(i, ((Operation) expr).getExpr(1));
                i--;
                continue;
            }
            if (expr instanceof Undef) {
                isConditioned = true;
                exprs.remove(i);
                i--;
                continue;
            }
            
            boolean isDuplicate = false;
            for (int j = 0; j < i; j++) if (expr.equalsExpr(exprs.get(j))) isDuplicate = true;
            if (isDuplicate) {
                exprs.remove(i);
                i--;
                continue;
            }
            
            int sign = expr.sign();
            if (sign == 1) return yep();
            if (sign == 0) {
                exprs.remove(i);
                i--;
                continue;
            }
            if (sign == 2) allNot = false;
        }
        
        Expr orThingy = allNot ? nope() : this;
        return isConditioned ? Conditional.make(orThingy, yep()) : orThingy;
    }
    
    public ArrayList<Expr> getExprs() {
        return (ArrayList<Expr>) exprs.clone();
    }
    
    public String pretty() {
        String string = "";
        Integer classOrder = classOrder();
        
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            
            Integer exprLevelLeft = expr.printLevelLeft();
            Integer exprLevelRight = expr.printLevelRight();
            
            boolean parens = false;
            if (i != 0 && exprLevelLeft != null && classOrder > exprLevelLeft) parens = true;
            if (i != exprs.size() - 1 && exprLevelRight != null && classOrder > exprLevelRight) parens = true;
            
            String exprString = expr.pretty();
            
            if (i != 0) { string = string + " or "; }
            
            string = string + (parens ? "(" : "") + exprString + (parens ? ")" : "");
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
    
}
