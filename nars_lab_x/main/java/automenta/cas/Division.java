package objenome.op.cas;

import objenome.op.cas.util.ArrayLists;

import java.util.ArrayList;
import java.util.HashMap;

public class Division extends Operation {
    
    private Expr numerator;
    private Expr denom;
    
    private Division(Expr numerator, Expr denom) {
        this.numerator = numerator;
        this.denom = denom;
    }
    
    public static Expr make(Expr numerator, Expr denom) {
        return make(numerator, denom, true);
    }
    
    public static Expr make(Expr numerator, Expr denom, boolean simplify) {
        Division division = new Division(numerator, denom);
        
        if (simplify) return addDenomCondition(division.simplify(), denom);
        return addDenomCondition(division, denom);
    }
    
    public static Expr make(ArrayList<? extends Expr> exprs) {
        // System.err.println(exprs);
        try {
            return make(exprs.get(0), exprs.get(1));
        } catch(Exception e) {
            System.err.println(e);
            throw new RuntimeException();
        }
    }
    
    public static Expr makeDefined(Expr numerator, Expr denom) {
        return new Division(numerator, denom).simplify();
    }
    
    public static Expr makeDefined(ArrayList<? extends Expr> exprs) {
        // System.err.println(exprs);
        try {
            return makeDefined(exprs.get(0), exprs.get(1));
        } catch(Exception e) {
            System.err.println(e);
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    
    private static Expr addDenomCondition(Expr returnExpr, Expr denom) {
        return Conditional.make(NotEqual.make(denom, Num.make(0)), returnExpr);
    }
    
    public Expr deriv(Var respected) {
        return make(Sum.make(Product.make(denom, numerator.deriv(respected)), Product.make(Num.make(-1.0d), Product.make(numerator, denom.deriv(respected)))), Exponent.make(denom, Num.make(2.0d)));
    }
    
    public ArrayList<Expr> getExprs() {
        ArrayList<Expr> arrayList = new ArrayList<>();
        arrayList.add(numerator);
        arrayList.add(denom);
        return arrayList;
    }

    public Expr simplify() {
        Division other = this;
        simplify:
        while (true) {
            Expr conditioned = other.conditioned();
            if (conditioned != null) return conditioned;

            if (other.denom instanceof Num && ((Num) other.denom).val() == 1) return other.numerator; // divide by 1
            if (other.denom instanceof Num && ((Num) other.denom).val() == 0) return new Undef();
            if (other.numerator instanceof Num && ((Num) other.numerator).val() == 0 && other.denom.isConstant())
                return Num.make(); // zero over a non-zero constant
            if (other.numerator instanceof Num && other.denom instanceof Num
                    && ((Num) other.numerator).val() / (((Num) other.numerator).val() / ((Num) other.denom).val()) == ((Num) other.denom).val()
                    && (Num.allowedVal(((Num) other.numerator).val() / ((Num) other.denom).val()) || !((Num) other.numerator).isInt() || !((Num) other.denom).isInt())) { // dividing integers doesn't result in a non-integer
                return Num.make(((Num) other.numerator).val() / ((Num) other.denom).val()); // divide the Numbers
            }
            if (other.numerator.equalsExpr(other.denom)) return Num.make(1); // x/x

            if (other.denom instanceof Division)
                return other.makeDefined(Product.make(numerator, ((Operation) denom).getExpr(1)), ((Operation) denom).getExpr(0)); // flip up denominators in the denominator
            if (other.numerator instanceof Division)
                return other.makeDefined(((Operation) numerator).getExpr(0), Product.make(((Operation) numerator).getExpr(1), denom)); // bring down denominators in the numerator
            // subtract exponents when bases are equal
            if (other.numerator instanceof Exponent && other.denom instanceof Exponent && ((Operation) other.numerator).getExpr(0).equalsExpr(((Operation) other.denom).getExpr(0)))
                return Exponent.make(((Operation) other.numerator).getExpr(0), Sum.make(((Operation) other.numerator).getExpr(1), Product.negative(((Operation) other.denom).getExpr(1))));
            if (other.numerator instanceof Exponent && ((Operation) other.numerator).getExpr(0).equalsExpr(other.denom))
                return Exponent.make(other.denom, Sum.make(((Operation) other.numerator).getExpr(1), Num.make(-1))); // subtract 1 from exponent when denominator exponent is 1
            if (other.denom instanceof Exponent && ((Operation) other.denom).getExpr(0).equalsExpr(other.numerator))
                return Exponent.make(other.numerator, Sum.make(Num.make(1), Product.negative(((Operation) other.denom).getExpr(1)))); // exponent = 1 - (denominator exponent)
            //if (numerator instanceof Number && denom instanceof Exponent) return Product.make(numerator, Exponent.make(denom, Number.make(-1))); // flip up with negative exponent when numerator is Number

            if (other.numerator instanceof Num && other.denom instanceof Num) {
                Num gcd = ((Num) other.numerator).gcd((Num) other.denom);
                // if (debug && !gcd.equalsExpr(Number.make(1))) System.err.println("gcd of (" + numerator + ", " + denom + ") = " + gcd);
                other.numerator = Division.makeDefined(other.numerator, gcd);
                other.denom = Division.makeDefined(other.denom, gcd);
            }

            //if (debug) System.err.println("Division simplify: " + dump());

            ArrayList<Expr> numeratorExprs = new ArrayList<>();
            if (other.numerator instanceof Product) numeratorExprs = ((Operation) other.numerator).getExprs();
            else numeratorExprs.add(other.numerator);
            ArrayList<Expr> denomExprs = new ArrayList<>();
            if (other.denom instanceof Product) denomExprs = ((Operation) other.denom).getExprs();
            else denomExprs.add(other.denom);

            for (int i = 0; i < numeratorExprs.size(); i++) {
                if (numeratorExprs.get(i) instanceof Exponent) {
                    Exponent exponent = (Exponent) numeratorExprs.get(i);
                    // if (debug) System.err.println("yep: " + exponent);
                    Expr exponentExp = exponent.getExpr(1);
                    if ((exponentExp instanceof Num && ((Num) exponentExp).val() < 0) || (exponentExp instanceof Product && ((Operation) exponentExp).getExpr(0) instanceof Num && ((Num) ((Operation) exponentExp).getExpr(0)).val() < 0)) {
                        // if (debug) System.err.println("yep");
                        denomExprs.add(Exponent.make(numeratorExprs.remove(i), Num.make(-1)));
                        i--;
                    }
                }
            }
            for (int i = 0; i < denomExprs.size(); i++) {
                if (denomExprs.get(i) instanceof Exponent) {
                    Exponent exponent = (Exponent) denomExprs.get(i);
                    // if (debug) System.err.println("yep: " + exponent);
                    Expr exponentExp = exponent.getExpr(1);
                    if ((exponentExp instanceof Num && ((Num) exponentExp).val() < 0) || (exponentExp instanceof Product && ((Operation) exponentExp).getExpr(0) instanceof Num && ((Num) ((Operation) exponentExp).getExpr(0)).val() < 0)) {
                        // if (debug) System.err.println("yep");
                        numeratorExprs.add(Exponent.make(denomExprs.remove(i), Num.make(-1)));
                        i--;
                    }
                }
            }

            if (other.numerator instanceof Product || other.denom instanceof Product) {

                for (int i = 0; i < numeratorExprs.size(); i++) {
                    Expr numeratorExpr = numeratorExprs.get(i);
                    for (int j = 0; j < denomExprs.size(); j++) {
                        Expr denomExpr = denomExprs.get(j);
                        // if (debug) System.err.println("Division simplify: dividing (" + numeratorExpr + ")/(" + denomExpr + ")");
                        Expr divided = Division.makeDefined(numeratorExpr, denomExpr);
                        // if (debug) System.err.println("Division simplify: divided: " + divided);
                        if (!(divided instanceof Division)) {
                            numeratorExprs.set(i, divided);
                            denomExprs.remove(j);
                            Division newProduct = new Division(ArrayLists.productArrToExpr(numeratorExprs), ArrayLists.productArrToExpr(denomExprs));
                            if (other.debug)
                                System.err.println("Division.simplify: " + other.dump() + " divided to " + newProduct);
                            other = newProduct;
                            continue simplify;
                        } else if (!((Operation) divided).getExprs().get(0).equals(numeratorExpr)) {
                            numeratorExprs.set(i, ((Operation) divided).getExprs().get(0));
                            other.numerator = (numeratorExprs.size() == 1) ? numeratorExprs.get(0) : Product.make(numeratorExprs);
                            denomExprs.set(j, ((Operation) divided).getExprs().get(1));
                            other.denom = (denomExprs.size() == 1) ? denomExprs.get(0) : Product.make(denomExprs);
                            if (other.debug) System.err.println("Division.simplify: divided to: " + other.dump());
                            continue simplify;
                        }
                    }
                }
            }

            other.numerator = (numeratorExprs.size() == 1) ? numeratorExprs.get(0) : Product.make(numeratorExprs);
            other.denom = (denomExprs.size() == 1) ? denomExprs.get(0) : Product.make(denomExprs);

            if (other.denom.isNegated()) {
                other.numerator = Product.negative(other.numerator);
                other.denom = Product.negative(other.denom);
            }

            return other;
        }
    }
    
    public Integer classOrderPass() {
        if (isFunctionPass()) return classOrder.get(Sin.class);
        // if (numerator instanceof Number && denom instanceof Number && decimalPrinted(((Number) numerator).val(), ((Number) denom).val()) != null) return null;
        // System.err.println(numerator.toString() + " / " + denom.toString());
        
        return super.classOrderPass();
    }
    
    public boolean isFunctionPass() {
        if (isTan() || isCot() || isSec() || isCsc()) return true;
        
        return super.isFunctionPass();
    }
    
    public Integer printLevelLeftPass() {
        if (isFunctionPass()) return classOrderNum - 1;
        if (isNumberPrinted()) return classOrderNum - 1;
        
        return super.printLevelLeftPass();
    }
    
    public Integer printLevelRightPass() {
        if (isFunctionPass()) return classPersonalLevelRight(Sin.class);
        if (isNumberPrinted()) return classOrderNum - 1;
        
        return super.printLevelRightPass();
    }
    
    public boolean firstParenPrint() {
        if (isFunctionPass()) return false;
        if (isNumberPrinted()) return false;
        
        return super.firstParenPrint();
    }
    
    public Double decimalPrinted(double numerator, double denom) {
            double denomVal = denom;
            while (denomVal % 5 == 0) { denomVal/= 5; }
            while (denomVal % 2 == 0) { denomVal/= 2; }
            if (denomVal == 1) {
                return numerator / denom;
            }
            return null;
    }
    
    public boolean isNumberPrinted() {
        return numerator instanceof Num && denom instanceof Num && decimalPrinted(((Num) numerator).val(), ((Num) denom).val()) != null;
    }
    
    public boolean isTan() {
        return numerator instanceof Sin && denom instanceof Cos && ((Operation) numerator).getExpr(0).equalsExpr(((Operation) denom).getExpr(0));
    }
    
    public boolean isCot() {
        return numerator instanceof Cos && denom instanceof Sin && ((Operation) numerator).getExpr(0).equalsExpr(((Operation) denom).getExpr(0));
    }
    
    public boolean isSec() {
        return numerator.equalsExpr(Num.make(1)) && denom instanceof Cos;
    }
    
    public boolean isCsc() {
        return numerator.equalsExpr(Num.make(1)) && denom instanceof Sin;
    }
    
    public Expr printSimplifyPass() {
        // System.err.println("Division.printSimplifyPass(): this: " + this);
        ArrayList<Expr>[] topsbottoms = toTopsBottoms();
        ArrayList<Expr> numeratorExprs = toTopsBottoms()[0];
        ArrayList<Expr> denomExprs = toTopsBottoms()[1];
        
        for (int i = 0; i < denomExprs.size(); i++) {
            Expr botExpr = denomExprs.get(i);
            Expr[] botBasePower = botExpr.toBasePower();
            Expr botBase = botBasePower[0];
            Expr botPower = botBasePower[1];
            // System.err.println("Division.printSimplifyPass(): botExpr: " + botExpr);
            
            boolean combined = false;
            
            for (int j = 0; j < numeratorExprs.size(); j++) {
                Expr topExpr = numeratorExprs.get(j);
                Expr[] topBasePower = topExpr.toBasePower();
                Expr topBase = topBasePower[0];
                Expr topPower = topBasePower[1];
                
                // System.err.println("Division.printSimplifyPass(): topBase: " + topBase);
                if (topBase.isTrig() && botBase.isTrig() && topPower.equalsExpr(botPower) && ((Operation) topBase).getExpr(0).equalsExpr(((Operation) botBase).getExpr(0))) {
                    Expr trigDivision = new Division(topBase, botBase).simplify();
                    trigDivision.printSimplified = true;
                    numeratorExprs.set(j, topPower.equalsExpr(Num.make(1)) ? trigDivision : Exponent.make(trigDivision, topPower, false));
                    combined = true;
                } else if (topExpr instanceof Num && botExpr instanceof Num && decimalPrinted(((Num) topExpr).val(), ((Num) botExpr).val()) != null) {
                    Expr numDivision = new Division(topExpr, botExpr).simplify();
                    numDivision.printSimplified = true;
                    numeratorExprs.set(j, numDivision);
                    combined = true;
                }
                
                if (combined) {
                    denomExprs.remove(i);
                    i--;
                    break;
                }
            }
            
            if (!combined && botBase.isTrig()) {
                // System.err.println("Division.printSimplifyPass(): !combined && " + botBase + " is trig");
                // System.err.println("  before: (Division " + ArrayLists.productArrToExpr(numeratorExprs) + " " + ArrayLists.productArrToExpr(denomExprs) + ")");
                Expr trigFlip = new Division(Num.make(1), botBase).simplify();
                trigFlip.printSimplified = true;
                numeratorExprs.add(botPower.equalsExpr(Num.make(1)) ? trigFlip : Exponent.make(trigFlip, botPower, false));
                if (numeratorExprs.get(0).equalsExpr(Num.make(1))) numeratorExprs.remove(0);
                denomExprs.remove(i);
                i--;
                // System.err.println("  after:  (Division " + ArrayLists.productArrToExpr(numeratorExprs) + " " + ArrayLists.productArrToExpr(denomExprs) + ")");
            }
            
        }
        
        Expr newNumerator = ArrayLists.productArrToExpr(numeratorExprs, false);
        Expr newDenom = ArrayLists.productArrToExpr(denomExprs, false);
        
        Expr printSimplified = newDenom.equalsExpr(Num.make(1)) ? newNumerator
                                                                   : new Division(newNumerator, newDenom);
        if (printSimplified instanceof Division) ((Division) printSimplified).printSimplified = true;
        
        return printSimplified;
    }
    
    public String pretty() {
        if (!printSimplified) return printSimplify().pretty();
        
        if (numerator instanceof Num && denom instanceof Num) {
            Double decimal = decimalPrinted(((Num) numerator).val(), ((Num) denom).val());
            if (decimal != null) {
                return decimal.toString();
            }
        }
        if (isTan()) return "tan" + (((Operation) numerator).getExpr(0).functionalParens()?"(":" ") + ((Operation) numerator).getExpr(0).pretty() + (((Operation) numerator).getExpr(0).functionalParens()?")":"");
        if (isCot()) return "cot" + (((Operation) numerator).getExpr(0).functionalParens()?"(":" ") + ((Operation) numerator).getExpr(0).pretty() + (((Operation) numerator).getExpr(0).functionalParens()?")":"");
        if (isSec()) return "sec" + (((Operation) denom).getExpr(0).functionalParens()?"(":" ") + ((Operation) denom).getExpr(0).pretty() + (((Operation) denom).getExpr(0).functionalParens()?")":"");
        if (isCsc()) return "csc" + (((Operation) denom).getExpr(0).functionalParens()?"(":" ") + ((Operation) denom).getExpr(0).pretty() + (((Operation) denom).getExpr(0).functionalParens()?")":"");

        Integer thisClassOrder = classOrder();
        
        boolean numeratorParens = false;
        if (thisClassOrder > numerator.printLevelRight()) numeratorParens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + numerator + ", printLevelRight=" + numerator.printLevelRight());
        boolean denomParens = false;
        if (thisClassOrder > denom.printLevelLeft()) denomParens = true;
        // if (debug) System.err.println("Division toString(): for expr=" + denom + ", printLevelLeft=" + denom.printLevelLeft());

        String string = "";
        string = string + (numeratorParens ? "(" : "") + numerator.pretty() + (numeratorParens ? ")" : "");
        string = string + "/";
        string = string + (denomParens ? "(" : "") + denom.pretty() + (denomParens ? ")" : "");
        
        return string;
    }
    
    public boolean equalsExpr(Expr expr) {
        if (expr == null) return false;
        if (expr == this) return true;
        if (!(expr instanceof Division)) return false;

        return numerator.equalsExpr(((Operation) expr).getExprs().get(0)) && denom.equalsExpr(((Operation) expr).getExprs().get(1));

    }
    
    public Expr copyPass(HashMap<Expr, Expr> subs) {
        return new Division(numerator.copy(subs), denom.copy(subs)).simplify();
    }
    
    public int sign() {
        if (numerator.sign() == 2 || denom.sign() == 2) return 2;
        return numerator.sign() * denom.sign();
    }
    
}
