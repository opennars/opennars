package nars.term;

import nars.Op;
import nars.term.compile.TermIndex;
import nars.term.transform.TermVisitor;
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by me on 11/13/15.
 */
public abstract class AbstractAtomic implements Term, Byted, Externalizable {

    public AbstractAtomic() {

    }

    @Override
    public abstract Op op();

    @Override
    public abstract int structure();


    public final void rehash() {
        /** do nothing */
    }

    @Override public final boolean isCommutative() {
        return false;
    }

    @Override
    public Term term(int n) {
        return null;
    }

    @Override
    public void append(final Appendable w, final boolean pretty) throws IOException {
        Utf8.fromUtf8ToAppendable(bytes(), w);
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        StringBuilder sb = new StringBuilder();
        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
    }

    @Override
    public String toString() {
        return Utf8.fromUtf8toString(bytes());
    }

    @Override
    public final boolean equals(final Object x) {
        if (this == x) return true;

        if (x instanceof AbstractAtomic) {
            AbstractAtomic ax = (AbstractAtomic)x;
            return Byted.equals(this, ax) && (op() == ax.op());
        }
        return false;
    }

    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public final int compareTo(final Object that) {
        if (that==this) return 0;

        Term t = (Term)that;
        int d = Integer.compare(op().ordinal(), t.op().ordinal());
        if (d!=0) return d;

        //if the op is the same, it will be a subclass of atom
        //which should have an ordering determined by its byte[]
        return Byted.compare(this, (AbstractAtomic)that);
    }

    @Override public final int getByteLen() {
        return bytes().length;
    }

    /**
     * Atoms are singular, so it is useless to clone them
     */
    @Override
    public final Term clone() {
        return this;
    }

    @Override
    public final Term cloneDeep() {
        return this;
    }

    @Override
    public final void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);
    }

    @Override
    public final String toString(boolean pretty) {
        return toString();
    }

    @Override public abstract boolean hasVar();

    @Override public abstract int vars();

    @Override public abstract boolean hasVarIndep();

    @Override public abstract boolean hasVarDep();

    @Override public abstract boolean hasVarQuery();

    @Override public abstract int complexity();

    @Override
    public final int size() {
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
        //return 0;
    }

    @Override public final int volume() { return 1; }

    public final boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    @Override public abstract byte[] bytes();

    /** atomic terms contain nothing */
    @Override public final boolean containsTerm(Term target) {
        return false;
    }

    /** atomic terms contain nothing */
    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }

    @Override
    public abstract int varIndep();

    @Override
    public abstract int varDep();

    @Override
    public abstract int varQuery();

    @Override
    public final AbstractAtomic normalized() {
        return this;
    }

    @Override
    public final Term normalized(TermIndex termIndex) {
        //if this is called, this atom will be the unique reference for any subsequent equivalent atomic terms which are normalized
        return this;
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        byte[] name = bytes();
        //out.writeByte((byte)op().ordinal());
        out.writeShort(name.length);
        out.write(bytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int nameLen = in.readShort();
        byte[] name = new byte[nameLen];

        in.read(name);

        setBytes(name);
    }

}
