package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

public class Cos extends Function {
    
    private Expr ofExpr;
    
    private Cos(Expr ofExpr) {
        this.ofExpr = ofExpr;
    }
    
    public static Expr make(Expr ofExpr) {
        return new Cos(ofExpr).simplify();
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return make(exprs.get(0));
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public Expr deriv(Var respected) {
        return Product.make(ofExpr.deriv(respected), Product.negative(Sin.make(ofExpr)));
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<Expr>();
        arrayList.add(ofExpr);
        return arrayList;
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        Expr sinSimplified = Sin.make(Sum.make(ofExpr, Division.make(new Pi(), Num.make(2))));
        if (sinSimplified.isConstant()) return sinSimplified;
        
        return this;
    }
    
    public String pretty() {
        String string;
        boolean parens = ofExpr.functionalParens();
        
        string = "cos";
        if (!parens) string = string.concat(" ");
                
        string = string.concat((parens?"(":"") + ofExpr.pretty() + (parens?")":""));
        
        return string;
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Cos)) return false;
        
        if (ofExpr.equalsExpr(((Operation) expr).getExprs().get(0))) return true;
        
        return false;
    }
    
    public boolean notEqualsExpr(Expr expr) {
        return false;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ofExpr.copy(subs));
    }
    
    public int sign() {
        return 2;
    }
    
}
