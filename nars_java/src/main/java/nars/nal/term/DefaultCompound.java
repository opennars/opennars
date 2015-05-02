package nars.nal.term;

/** implementation of a compound which generates and stores its name as a CharSequence, which is used for equality and hash*/
abstract public class DefaultCompound extends Compound {

    @Deprecated protected CharSequence name = null;

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
        if (name().equals(t.name())) {
            share(t);
            return true;
        }
        return false;
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
    public CharSequence name() {
        //new Exception().printStackTrace(); //for debugging when this is called

        if (this.name == null) {
            this.name = makeName();
        }
        return this.name;
    }

    @Override
    public CharSequence nameCached() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }


}
