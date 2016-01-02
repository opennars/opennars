package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;

public class Conditional extends Operation {
    
    public Expr ifIs;
    public Expr then;
    
    private Conditional(Expr ifIs, Expr then) {
        this.ifIs = ifIs;
        this.then = then;
    }
    
    public static Expr make(Expr ifIs, Expr then) {
        Conditional con = new Conditional(ifIs, then);
        return con.simplify();
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return make(exprs.get(1), exprs.get(0));
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs.get(1), exprs.get(0));
    }
    
    public Expr simplify() {
        if (ifIs.isTrue()) return then;
        if (ifIs.isFalse() || hasUndef()) return new Undef();
        if (ifIs instanceof Conditional) return make(And.make(ifIs.defined(), ifIs.condition()), then);
        if (then instanceof Conditional) {
            if (ifIs.implies(then.condition())) return new Conditional(ifIs, then.defined());
            return make(And.make(then.condition(), ifIs), then.defined());
        }
        
        HashMap<Expr, Expr> subs = new HashMap<>();
        subs.put(ifIs, Expr.yep());
        then = then.copy(subs);
        return this;
    }
    
    public String pretty() {

        Integer thisClassOrder = classOrder();
        
        boolean thenParens = false;
        if (thisClassOrder > then.printLevelRight()) thenParens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + then + ", printLevelRight=" + then.printLevelRight());
        boolean ifIsParens = false;
        if (thisClassOrder > ifIs.printLevelLeft()) ifIsParens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + ifIs + ", printLevelLeft=" + ifIs.printLevelLeft());

        String string = "";
        string = string + (thenParens ? "(" : "") + then.pretty() + (thenParens ? ")" : "");
        string = string + " if ";
        string = string + (ifIsParens ? "(" : "") + ifIs.pretty() + (ifIsParens ? ")" : "");

        return string;
    }
    
    public int sign() {
        return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ifIs.copy(subs), then.copy(subs));
    }
    
    public boolean equalsExpr(Expr expr) {
        return false;
    }
    
    public Expr deriv(Var respected) {
        return new Conditional(ifIs, then.deriv(respected));
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(then);
        arrayList.add(ifIs);
        return arrayList;
    }
}
