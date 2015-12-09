package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

public class Derivative extends Function {
    
    private Expr ofExpr;
    private Var respected;
    
    private Derivative(Expr ofExpr, Var respected/*, int order*/) {
        this.ofExpr = ofExpr;
        this.respected = respected;
    }
    
    public static Expr make(Expr ofExpr, Var respected) {
        return make(ofExpr, respected, true);
    }
    
    public static Expr make(Expr ofExpr, Var respected, boolean simplify) {
        return make(ofExpr, respected, simplify, 1);
    }
    
    public static Expr make(Expr ofExpr, Var respected, int order) {
        return make(ofExpr, respected, true, order);
    }
    
    public static Expr make(Expr ofExpr, Var respected, boolean simplify, int order) {
        for (int i = 0; i < order; i++) {
            ofExpr = new Derivative(ofExpr, respected);
            if (simplify) ofExpr = ((Derivative) ofExpr).simplify();
        }
        return ofExpr;
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return make(exprs.get(0), (Var) exprs.get(1));
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        if (!(ofExpr instanceof Derivative)) {
            return ofExpr.deriv(respected);
        }
        return this;
    }
    
    public Var respected() {
        return respected;
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(ofExpr);
        arrayList.add(respected);
        return arrayList;
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Derivative)) return false;

        return ofExpr.equalsExpr(((Operation) expr).getExprs().get(0)) && respected.equals(((Derivative) expr).respected());

    }
    
    public boolean notEqualsExpr(Expr expr) {
        return false;
    }
    
    public Expr deriv(Var respected) {
         return make(this, respected);
    }
    
    public String pretty() {
        int order = 1;
        Expr botOfExpr = ofExpr;
        while (botOfExpr instanceof Derivative && respected.equals(((Derivative) botOfExpr).respected())) {
            order++;
            botOfExpr = ((Operation) botOfExpr).getExprs().get(0);
        }
        boolean parens = botOfExpr.functionalParens();
        return 'd' + (order > 1 ? "^" + order : "") + ((botOfExpr instanceof Var)?botOfExpr:"") + "/d" + respected + (order > 1 ? "^" + order : "") + ((botOfExpr instanceof Var)?"":(parens?"(":"") + botOfExpr + (parens?")":""));
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ofExpr.copy(subs), (Var) respected.copy(subs));
    }
    
    public Integer personalLevelLeft() {
        int order = 1;
        Expr botOfExpr = ofExpr;
        while (botOfExpr instanceof Derivative && respected.equals(((Derivative) botOfExpr).respected())) {
            order++;
            botOfExpr = ((Operation) botOfExpr).getExprs().get(0);
        }
        
        if (botOfExpr instanceof Var) return classOrderNum - 1;
        
        return super.personalLevelLeft();
    }
    
    public Integer personalLevelRight() {
        int order = 1;
        Expr botOfExpr = ofExpr;
        while (botOfExpr instanceof Derivative && respected.equals(((Derivative) botOfExpr).respected())) {
            order++;
            botOfExpr = ((Operation) botOfExpr).getExprs().get(0);
        }
        
        if (botOfExpr instanceof Var) return classOrderNum - 1;
        
        return super.personalLevelRight();
    }
    
    public int sign() {
        return 2;
    }
    
}
