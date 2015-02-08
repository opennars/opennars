package nars.scheme;

import nars.util.data.sexpression.IPair;

/**
 * @author Peter Norvig, peter@norvig.com http://www.norvig.com
 *         Copyright 1998 Peter Norvig, see http://www.norvig.com/license.html *
 */

public class Macro extends Closure {

    /**
     * Make a macro from a parameter list, body, and environment. *
     */
    public Macro(Object parms, Object body, SchemeContext env) {
        super(parms, body, env);
    }

    /**
     * Replace the old cons cell with the macro expansion, and return it. *
     */
    public IPair expand(Scheme interpreter, IPair oldPair, Object args) {
        Object expansion = apply(interpreter, args);

        if (expansion instanceof IPair) {
            oldPair.setFirst(((IPair) expansion).first());
            oldPair.setRest(((IPair) expansion).rest());
        } else {
            oldPair.setFirst("begin");
            oldPair.setRest(cons(expansion, null));
        }
        return oldPair;
    }

    /**
     * Macro expand an expression *
     */
    public static Object macroExpand(Scheme interpreter, Object x) {
        if (!(x instanceof IPair)) return x;
        Object fn = interpreter.eval(first(x), interpreter.globalEnvironment);
        if (!(fn instanceof Macro)) return x;
        return ((Macro) fn).expand(interpreter, (IPair) x, rest(x));
    }
}
