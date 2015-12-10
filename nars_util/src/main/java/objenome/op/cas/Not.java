package objenome.op.cas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Not extends Operation {
    
    Expr notExpr;
    
    public Not(Expr notExpr) {
        this.notExpr = notExpr;
    }
    
    public static Expr make(Expr notExpr) {
        if (notExpr instanceof Not) return ((Operation) notExpr).getExpr(0);
        return new Not(notExpr).simplify();
    }
    
    public static Expr make(List<? extends Expr> exprs) {
        return make(exprs.get(0));
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        if (notExpr.equalsExpr(yep())) return nope();
        if (notExpr.equalsExpr(nope())) return yep();
        if (notExpr instanceof Or && ((Operation) notExpr).getExprs().size() == 2
                && ((Operation) notExpr).getExpr(0) instanceof LessThan
                && ((Operation) notExpr).getExpr(1) instanceof Equals
                && ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(0).equalsExpr(
                   ((Operation) ((Operation) notExpr).getExpr(1)).getExpr(0))
                && ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(1).equalsExpr(
                   ((Operation) ((Operation) notExpr).getExpr(1)).getExpr(1)))
                   return new GreaterThan(((Operation) ((Operation) notExpr).getExpr(0)).getExpr(0),
                                          ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(1));
        return Sum.make(Num.make(1), Product.negative(notExpr));
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(notExpr);
        return arrayList;
    }
    
    public Expr printSimplifyPass() {
        if (notExpr instanceof Equals) return new NotEqual(((Operation) notExpr).getExpr(0),
                                                           ((Operation) notExpr).getExpr(1));
        if (notExpr instanceof Or) {
            boolean aNot = false;
            for (Expr maybeNot : ((Operation) notExpr).getExprs()) {
                if (maybeNot.printSimplify() instanceof Not) aNot = true;
            }
            if (aNot) {
                ArrayList<Expr> ands = ((Operation) notExpr).getExprs().stream().map(Not::make).collect(Collectors.toCollection(ArrayList::new));
                return new And(ands);
            }
        }
        printSimplified = true;
        return this;
    }
    
    public String pretty() {
        if (!printSimplified) return printSimplify().pretty();

        boolean parens = notExpr.functionalParens();

        String string = "not";
        if (!parens) string = string + " ";
                
        string = string + (parens ? "(" : "") + notExpr.pretty() + (parens ? ")" : "");
        
        return string;
    }
    
    public int sign() {
        return 2;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(notExpr.copy(subs));
    }
    
    public boolean equalsExpr(Expr expr) {
        return expr instanceof Not && notExpr.equalsExpr(((Operation) expr).getExpr(0));
    }
    
    public Expr deriv(Var respected) {
        return simplify().deriv(respected);
    }
    
}
