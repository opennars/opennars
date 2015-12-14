package nars.java;

import nars.term.atom.StringAtom;

/** refers to a java object instance */
public final class AtomObject<O> extends StringAtom {

    public final O value;

    public AtomObject(String name, O value) {
        super(name);
        this.value = value;
    }

    public O object() {
        return value;
    }
}
