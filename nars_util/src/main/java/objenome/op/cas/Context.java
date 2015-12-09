package objenome.op.cas;

import objenome.op.cas.util.ParseContext;

import java.util.HashMap;

public class Context implements ParseContext {
    
    public HashMap<Character, Var> vars = new HashMap<>();
    
    public HashMap<Expr, Expr> subs = new HashMap<>();
    
    public Context() {
    }
    
    public Var getVar(char varChar) {
        if (vars.containsKey(varChar)) {
            return vars.get(varChar);
        } else {
            Var var = new Var(varChar);
            vars.put(varChar, var);
            return var;
        }
    }
    
}
