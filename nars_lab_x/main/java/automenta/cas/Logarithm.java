package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Logarithm extends Function {
    
    private Expr base;
    private Expr ofExpr;
    
    private Logarithm(Expr base, Expr ofExpr) {
        this.base = base;
        this.ofExpr = ofExpr;
    }
    
    public static Expr make(Expr base, Expr ofExpr) {
        Logarithm log = new Logarithm(base, ofExpr);
        
        ArrayList<Expr> norConditions = new ArrayList<>();
        norConditions.add(LessThanOrEqual.make(base, nope()));
        norConditions.add(Equals.make(base, yep()));
        norConditions.add(LessThanOrEqual.make(ofExpr, nope()));
        
        return Conditional.make(Not.make(Or.make(norConditions)), log.simplify());
    }
    
    public static Expr make(List<? extends Expr> exprs) {
        return make(exprs.get(0), exprs.get(1));
    }
    
    public static Expr makeDefined(Expr base, Expr ofExpr) {
        return new Logarithm(base, ofExpr).simplify();
    }
    
    public static Expr makeDefined(ArrayList<Expr> exprs) {
        return makeDefined(exprs.get(0), exprs.get(1));
    }
    
    public Expr ofExpr() {
        return ofExpr;
    }
    
    public Expr deriv(Var respected) {
        if (base instanceof E) return Division.make(ofExpr.deriv(respected), ofExpr);
        if (base.isConstant() && ofExpr.isConstant()) return Num.make();
        return Derivative.make(Division.make(Logarithm.make(new E(), ofExpr), Logarithm.make(new E(), base)), respected);
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(base);
        arrayList.add(ofExpr);
        return arrayList;
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        if (base.sign() <= 0 || (base instanceof Num && ((Num) base).val() == 1)) return new Undef();
        if (ofExpr.sign() <= 0) return new Undef();
        if (base.equalsExpr(ofExpr)) return Num.make(1.0d);
        if (ofExpr instanceof Exponent && base.equals(((Operation) ofExpr).getExprs().get(0))) return ((Operation) ofExpr).getExprs().get(0);
        
        return this;
    }
    
    public String pretty() {
        String string;
        boolean parens = ofExpr.functionalParens();
        
        if (base instanceof E) string = "ln";
        else {
            string = "log";
            if (!(base instanceof Num) || ((Num) base).val() != 10) {
                string = string + '[' + base.pretty() + ']';
            }
        }
        if (!parens) string = string + " ";
                
        string = string + (parens ? "(" : "") + ofExpr.pretty() + (parens ? ")" : "");
        
        return string;
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Logarithm)) return false;

        return base.equalsExpr(((Operation) expr).getExprs().get(0)) && ofExpr.equalsExpr(((Operation) expr).getExprs().get(1));

    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return makeDefined(base.copy(subs), ofExpr.copy(subs));
    }
    
    public int sign() {
        return Product.make(Sum.make(base, Num.make(-1)), Sum.make(ofExpr, Num.make(-1))).sign();
    }
}
