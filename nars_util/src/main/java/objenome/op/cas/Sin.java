package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Sin extends Function {
    
    private static final HashMap<Expr, Expr> values = new HashMap<>();
    static {
        values.put(Num.make(0), Num.make(0)); // 0
        values.put(Num.make(1), Num.make(0)); // pi
        values.put(Num.make(0.5), Num.make(1)); // pi/2
        values.put(Num.make(1.5), Num.make(-1)); // 3pi/2
        values.put(Division.make(Num.make(1), Num.make(6)), Num.make(0.5)); // pi/6
        values.put(Division.make(Num.make(5), Num.make(6)), Num.make(0.5)); // 5pi/6
        values.put(Division.make(Num.make(-1), Num.make(6)), Num.make(-0.5)); // -pi/6
        values.put(Division.make(Num.make(-5), Num.make(6)), Num.make(-0.5)); // -5pi/6
        values.put(Num.make(0.25),
                   Division.make(Num.make(1), Exponent.make(Num.make(2), Num.make(0.5)))); // pi/4
        values.put(Num.make(0.75),
                   Division.make(Num.make(1), Exponent.make(Num.make(2), Num.make(0.5)))); // 3pi/4
        values.put(Num.make(-0.25),
                   Division.make(Num.make(-1), Exponent.make(Num.make(2), Num.make(0.5)))); // -pi/4
        values.put(Num.make(-0.75),
                   Division.make(Num.make(-1), Exponent.make(Num.make(2), Num.make(0.5)))); // -3pi/4
        values.put(Division.make(Num.make(1), Num.make(3)),
                   Division.make(Exponent.make(Num.make(3), Num.make(0.5)), Num.make(2))); // pi/3
        values.put(Division.make(Num.make(2), Num.make(3)),
                   Division.make(Exponent.make(Num.make(3), Num.make(0.5)), Num.make(2))); // 2pi/3
        values.put(Division.make(Num.make(-1), Num.make(3)),
                   Division.make(Exponent.make(Num.make(3), Num.make(0.5)), Num.make(-2))); // -pi/3
        values.put(Division.make(Num.make(-2), Num.make(3)),
                   Division.make(Exponent.make(Num.make(3), Num.make(0.5)), Num.make(-2))); // -2pi/3
    }
    
    private Expr ofExpr;
    
    private Sin(Expr ofExpr) {
        this.ofExpr = ofExpr;
    }
    
    public static Expr make(Expr ofExpr) {
        Sin sin = new Sin(ofExpr);
        return sin.simplify();
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return make(exprs.get(0));
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public Expr deriv(Var respected) {
        return Product.make(ofExpr.deriv(respected), Cos.make(ofExpr));
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(ofExpr);
        return arrayList;
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        if (ofExpr.isConstant()) {
            Expr dividedBy2Pi = Division.make(ofExpr, Product.make(Num.make(2), new Pi()));
            
            for (Map.Entry<Expr, Expr> exprExprEntry : values.entrySet()) {
                Expr subtracted = Sum.make(dividedBy2Pi, Division.make(exprExprEntry.getKey(), Num.make(-2)));
                if (subtracted instanceof Num && ((Num) subtracted).isInt()) return exprExprEntry.getValue();
            }
        }
        
        return this;
    }
    
    public String pretty() {
        boolean parens = ofExpr.functionalParens();

        String string = "sin";
        if (!parens) string = string + " ";
                
        string = string + (parens ? "(" : "") + ofExpr.pretty() + (parens ? ")" : "");
        
        return string;
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Sin)) return false;

        return ofExpr.equalsExpr(((Operation) expr).getExprs().get(0));

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
