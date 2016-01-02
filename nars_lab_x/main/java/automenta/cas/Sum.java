package objenome.op.cas;

import objenome.op.cas.util.ArrayLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Sum extends Operation {
    
    public static void main(String[] args) {
        ArrayList<Expr> tmp1 = new ArrayList<>();
        tmp1.add(new E());
        tmp1.add(Num.make(1));
        tmp1.add(new Var('x'));
        tmp1.add(new Var('y'));
        Sum sum = new Sum(tmp1);
        System.out.println(sum);
    }
    
    private ArrayList<Expr> exprs;
    
    private Sum(ArrayList<? extends Expr> exprs) {
        this.exprs = ArrayLists.castAll(exprs, Expr.class);
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        return new Sum(exprs).simplify();
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        return make(exprs);
    }
    
    public static Expr make(Expr expr1, Expr expr2) {
        ArrayList<Expr> tmp1 = new ArrayList<>();
        tmp1.add(expr1);
        tmp1.add(expr2);
        return new Sum(tmp1).simplify();
    }
    
    public Expr deriv(Var respected) {
        // if (debug) System.err.println("derivative of " + dump());
        ArrayList<Expr> exprsDiffed = new ArrayList<>(exprs.size());
        exprsDiffed.addAll(exprs.stream().map(expr -> expr.deriv(respected)).collect(Collectors.toList()));
        // if (debug) System.err.println(dump() + " => ArrayList: " + exprsDiffed);
        // if (debug) System.err.println(dump() + " => " + new Sum(exprsDiffed).toString());
        return Sum.make(exprsDiffed);
    }
    
    public Expr printSimplifyPass() {
        if (exprs.size() == 2 && exprs.get(0) instanceof Product && ((Operation) exprs.get(0)).getExprs().size() == 2
                && ((Operation) exprs.get(0)).getExpr(0).equalsExpr(Num.make(-1))
                && exprs.get(1).equalsExpr(Num.make(1))) return new Not(((Operation) exprs.get(0)).getExpr(1));
        printSimplified = true;
        return this;
    }
    
    public String pretty() {
        if (!printSimplified) return printSimplify().pretty();
        
        if (exprs.size() == 1) return exprs.get(0).pretty();
        
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
            
            if (i != 0 && exprString.charAt(0) != '-') { string = string + "+"; }
            
            string = string + (parens ? "(" : "") + exprString + (parens ? ")" : "");
        }
        
        return string;
    }
    
    public Expr simplify() {
        Expr conditioned = conditioned();
        if (conditioned != null) return conditioned;
        
        List<Expr> constants = new ArrayList<>();
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr = exprs.get(i);
            
            if (expr instanceof Sum) {
                exprs.remove(i);
                exprs.addAll(i, ((Operation) expr).getExprs());
                i--;
            } else if (expr.isConstant()) {
                
                exprs.remove(i);
                i--;
                
                constants.add(expr);
            }
        }
        
        for (int i = 0; i < exprs.size(); i++) {
            Expr expr1 = exprs.get(i);
            Expr expr1mult = Num.make(1);
            Expr expr1OtherThing = expr1;
            
            ArrayList<Expr>[] expr1TopsBottoms = expr1.toTopsBottoms();
            ArrayList<Expr> expr1Tops = expr1TopsBottoms[0];
            ArrayList<Expr> expr1Bottoms = expr1TopsBottoms[1];
            
            while (!expr1Tops.isEmpty() && expr1Tops.get(0).isConstant()) {
                expr1mult = Product.make(expr1mult, expr1Tops.get(0));
                expr1OtherThing = Division.make(expr1OtherThing, expr1Tops.get(0));
                expr1Tops.remove(0);
            }
            while (!expr1Bottoms.isEmpty() && expr1Bottoms.get(0).isConstant()) {
                expr1mult = Division.make(expr1mult, expr1Bottoms.get(0));
                expr1OtherThing = Product.make(expr1OtherThing, expr1Bottoms.get(0));
                expr1Bottoms.remove(0);
            }
            
            for (int j = i + 1; j < exprs.size(); j++) {
                Expr expr2 = exprs.get(j);
                // if (debug) System.err.println("Sum.simplify: (2 Expr): expr1: " + expr1 + "; expr2: " + expr2);
//                 if (debug) System.err.println("Sum.simplify: (2 Expr): expr2.getExpr(0): " + ((Operation) expr2).getExpr(0));
//                 if (debug) System.err.println("Sum.simplify: (2 Expr): -1 == -1: " + Number.make(-1d).equalsExpr(Number.make(-1d)));
                
                Expr expr2mult = Num.make(1);
                Expr expr2OtherThing = expr2;
                
                ArrayList<Expr>[] expr2TopsBottoms = expr2.toTopsBottoms();
                ArrayList<Expr> expr2Tops = expr2TopsBottoms[0];
                ArrayList<Expr> expr2Bottoms = expr2TopsBottoms[1];
                
                while (!expr2Tops.isEmpty() && expr2Tops.get(0).isConstant()) {
                    expr2mult = Product.make(expr2mult, expr2Tops.get(0));
                    expr2OtherThing = Division.make(expr2OtherThing, expr2Tops.get(0));
                    expr2Tops.remove(0);
                }
                while (!expr2Bottoms.isEmpty() && expr2Bottoms.get(0).isConstant()) {
                    expr2mult = Division.make(expr2mult, expr2Bottoms.get(0));
                    expr2OtherThing = Product.make(expr2OtherThing, expr2Bottoms.get(0));
                    expr2Bottoms.remove(0);
                }
                
                // if (debug) System.err.println("Sum.simplify: (2 Expr): expr1: (" + expr1mult + ")*(" + expr1OtherThing + "); "
                //                                                           + "expr2: (" + expr2mult + ")*(" + expr2OtherThing + ")");
                
                if (expr1OtherThing.equalsExpr(expr2OtherThing)) {
                    Expr newElem = Product.make(Sum.make(expr1mult, expr2mult), expr1OtherThing);
                    
                    if (debug) System.err.println("Sum.simplify: combining " + expr1mult + '*' + expr1OtherThing + " + " + expr2mult + '*' + expr2OtherThing + " = " + newElem);
                    
                    exprs.remove(j);
                    
                    exprs.set(i, newElem);
                    
                    j = exprs.size();
                    i = -1;
                }
                else if (expr1mult.equalsExpr(expr2mult) && expr1OtherThing instanceof Exponent && expr2OtherThing instanceof Exponent
                      && ((Operation) expr1OtherThing).getExpr(1).equalsExpr(Num.make(2)) && ((Operation) expr2OtherThing).getExpr(1).equalsExpr(Num.make(2))
                      && ((((Operation) expr1OtherThing).getExpr(0) instanceof Sin && ((Operation) expr2OtherThing).getExpr(0) instanceof Cos)
                       || (((Operation) expr1OtherThing).getExpr(0) instanceof Cos && ((Operation) expr2OtherThing).getExpr(0) instanceof Sin))
                      && ((Operation) ((Operation) expr1OtherThing).getExpr(0)).getExpr(0).equalsExpr(((Operation) ((Operation) expr2OtherThing).getExpr(0)).getExpr(0))) {
                    
                    exprs.remove(j);
                    
                    exprs.set(i, expr1mult);
                    
                    j = exprs.size();
                    i = -1;
                }
                
//                 else if (expr2 instanceof Product && ((Operation) expr2).getExprs().get(0).equalsExpr(Number.make(-1d)) && expr1.equalsExpr(((Operation) expr2).getExprs().get(1))
//                  || expr1 instanceof Product && ((Operation) expr1).getExprs().get(0).equalsExpr(Number.make(-1d)) && expr2.equalsExpr(((Operation) expr1).getExprs().get(1))) {
//                     if (debug) System.err.println("Sum.simplify: (2 Expr): canceling");
//                     
//                     exprs.remove(j);
//                     exprs.remove(i);
//                     j = exprs.size();
//                     i = -1;
//                 }
            }
        }
        
        for (int i = 0; i < constants.size(); i++) {
            Expr constant1 = constants.get(i);
            Double number1 = null;
            if (constant1 instanceof Num) number1 = ((Num) constant1).val();
            if (number1 != null && number1 == 0) {
                constants.remove(i);
                i--;
                continue;
            }
            
            ArrayList<Expr>[] constant1TopsBottoms = constant1.toTopsBottoms();
            ArrayList<Expr> constant1Tops = constant1TopsBottoms[0];
            ArrayList<Expr> constant1Bottoms = constant1TopsBottoms[1];
            
            Expr constant1Mult = Num.make(1);
            Expr constant1OtherThing = constant1;
            
            while (!constant1Tops.isEmpty() && constant1Tops.get(0).isNumber()) {
                constant1Mult = Product.make(constant1Mult, constant1Tops.get(0));
                constant1OtherThing = Division.make(constant1OtherThing, constant1Tops.get(0));
                constant1Tops.remove(0);
            }
            while (!constant1Bottoms.isEmpty() && constant1Bottoms.get(0).isNumber()) {
                constant1Mult = Division.make(constant1Mult, constant1Bottoms.get(0));
                constant1OtherThing = Product.make(constant1OtherThing, constant1Bottoms.get(0));
                constant1Bottoms.remove(0);
            }
//             if (constant1OtherThing.isNumber()) {
//                 constant1Mult = Product.make(constant1Mult, constant1OtherThing);
//                 constant1OtherThing = Number.make(1);
//             }
            
            for (int j = 0; j < constants.size(); j++) {
                if (i != j) {
                    Expr constant2 = constants.get(j);
                    Double number2 = null;
                    if (constant2 instanceof Num) number2 = ((Num) constant2).val();
                    
                    ArrayList<Expr>[] constant2TopsBottoms = constant2.toTopsBottoms();
                    ArrayList<Expr> constant2Tops = constant2TopsBottoms[0];
                    ArrayList<Expr> constant2Bottoms = constant2TopsBottoms[1];
                    
                    Expr constant2Mult = Num.make(1);
                    Expr constant2OtherThing = constant2;
                    
                    while (!constant2Tops.isEmpty() && constant2Tops.get(0).isNumber()) {
                        constant2Mult = Product.make(constant2Mult, constant2Tops.get(0));
                        constant2OtherThing = Division.make(constant2OtherThing, constant2Tops.get(0));
                        constant2Tops.remove(0);
                    }
                    while (!constant2Bottoms.isEmpty() && constant2Bottoms.get(0).isNumber()) {
                        constant2Mult = Division.make(constant2Mult, constant2Bottoms.get(0));
                        constant2OtherThing = Product.make(constant2OtherThing, constant2Bottoms.get(0));
                        constant2Bottoms.remove(0);
                    }
                    
                    boolean combined = false;
                    
                    // if (debug) System.err.println("Sum.simplify: (2 Constants): constant1: (" + constant1Mult + ")*(" + constant1OtherThing + "); "
                    //                                                             + "constant2: (" + constant2Mult + ")*(" + constant2OtherThing + ")");

                    //noinspection IfStatementWithTooManyBranches
                    if (number1 != null && number2 != null && number1 + number2 - number1 - number2 == 0 && number2 + number1 - number2 - number1 == 0) {
                        constants.set(i, Num.make(number1 + number2));
                        combined = true;
                    } else if (constant1 instanceof Division && constant2 instanceof Division && constant1.isConstant() && constant2.isConstant()) {
                        constants.set(i, Division.make(Sum.make(Product.make(((Operation) constant1).getExpr(0), ((Operation) constant2).getExpr(1)), Product.make(((Operation) constant2).getExpr(0), ((Operation) constant1).getExpr(1))), Product.make(((Operation) constant1).getExpr(1), ((Operation) constant2).getExpr(1))));
                        combined = true;
                    } else if (constant1 instanceof Division && constant1.isConstant() && constant2.isConstant()) {
                        constants.set(i, Division.make(Sum.make(((Operation) constant1).getExpr(0), Product.make(constant2, ((Operation) constant1).getExpr(1))), ((Operation) constant1).getExpr(1)));
                        combined = true;
                    } else if (constant1OtherThing.equalsExpr(constant2OtherThing) && !constant1OtherThing.equalsExpr(Num.make(1))) {
                        Expr newElem = Product.make(Sum.make(constant1Mult, constant2Mult), constant1OtherThing);
                    
                        if (debug) System.err.println("Sum.simplify: combining " + constant1Mult + '*' + constant1OtherThing + " + " + constant2Mult + '*' + constant2OtherThing + " = " + newElem);
                        
                        constants.set(i, newElem);
                        combined = true;
                    }
                    
                    if (combined) {
                        constants.remove(j);
                        j = constants.size();
                        i = -1;
                    }
                }
            }
        }
        
        List<Expr> numbers = new ArrayList<>();
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i).isNumber()) {
                numbers.add(constants.remove(i));
                i--;
            }
        }
        constants.addAll(numbers);
        
        exprs.addAll(constants);
        
        if (exprs.isEmpty()) return Num.make();
        if (exprs.size() == 1) return exprs.get(0);
        
        return this;
    }
    
    public ArrayList<Expr> getExprs() {
        return (ArrayList<Expr>) exprs.clone();
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Sum)) return false;
        
        // if (debug) System.err.println("Sum.equalsExpr: " + dump() + " =? " + expr);
        ArrayList<Expr> otherExprs = ((Operation) expr).getExprs();
        for (Expr expr2 : exprs) {
            // if (debug) System.err.println("Sum.equalsExpr: expr2: " + expr2);
            for (Expr otherExpr2 : otherExprs) {
                // if (debug) System.err.println("Sum.equalsExpr: otherExpr2: " + otherExpr2);
                if (expr2.equalsExpr(otherExpr2)) {
                // if (debug) System.err.println("Sum.equalsExpr: " + expr2 + " == " + otherExpr2);
                    otherExprs.remove(otherExpr2);
                    break;
                }
                // if (debug) System.err.println("Sum.equalsExpr: " + expr2 + " != " + otherExpr2);
                return false;
            }
        }
        
        return true;
    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return make(ArrayLists.copyAll(exprs, subs));
    }
    
    public int sign() {
        return 2;
    }
    
}
