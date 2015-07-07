package nars.meta;

import nars.term.Variable;

/**
 * Pattern variable, which can match any structural component in a Rule
 */
public class PVar extends Variable {

    public PVar(String name) {
        super(name);
    }
}
