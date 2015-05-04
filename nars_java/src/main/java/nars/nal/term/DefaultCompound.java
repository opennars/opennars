package nars.nal.term;

import nars.util.data.Utf8;

import java.util.Arrays;

/** implementation of a compound which generates and stores its name as a CharSequence, which is used for equality and hash*/
abstract public class DefaultCompound extends Compound {

    @Deprecated protected String cachedName = null;
    byte[] name = null;
    int hash = 0;

    public DefaultCompound(Term... components) {
        super(components);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;

        if (that == null) return false;

        //if the other class is not a DefaultCompound it is considered non-equal, so the use of implementations must be consistent
        //if (!(that instanceof nars.nal.term.DefaultCompound)) return false;

        if (getClass() != that.getClass()) return false;


        final nars.nal.term.DefaultCompound t = (nars.nal.term.DefaultCompound)that;
        if ((name == null) || (t.name == null)) {
            //check operate first because name() may to avoid potential construction of name()
            if (/*operator()!=t.operator()|| */ getComplexity() != t.getComplexity() )
                return false;
        }
        if (equalsName(t)) {
            share(t);
            return true;
        }
        return false;
    }

    @Override
    public int compareSubterms(Compound otherCompoundOfEqualType) {
        return Integer.compare(name.hashCode(), ((DefaultCompound)otherCompoundOfEqualType).name.hashCode());
    }

    protected void share(DefaultCompound equivalent) {
        super.share(equivalent);
        if (!hasVar()) {
            equivalent.name = this.name; //also share name key
        }
    }

    @Override
    public void invalidate() {
        if (hasVar()) {
            this.name = null; //invalidate name so it will be (re-)created lazily
            for (final Term t : term) {
                if (t instanceof Compound)
                    ((Compound) t).invalidate();
            }
        }
        else {
            setNormalized();
        }
    }


    @Override
    public byte[] name() {
        //new Exception().printStackTrace(); //for debugging when this is called

        if (this.name == null) {
            this.cachedName = makeName().toString();

            name = Utf8.toUtf8(cachedName);
            hash = Arrays.hashCode(name);
        }
        return this.name;
    }

    @Deprecated @Override
    public String nameCached() {
        return this.cachedName;
    }

    @Override
    public int hashCode() {
        if (this.name == null) {
            name();
        }
        return hash;
    }


}
