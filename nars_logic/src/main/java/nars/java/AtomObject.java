package nars.java;

import nars.term.atom.StringAtom;


public final class AtomObject<O> extends StringAtom {

    public final O value;

    public AtomObject(String name, O value) {
        super(name);
        this.value = value;
    }

    public O get() {
        return value;
    }
}
