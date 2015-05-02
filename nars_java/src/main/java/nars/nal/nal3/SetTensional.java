package nars.nal.nal3;


import nars.io.Symbols;
import nars.nal.NALOperator;
import nars.nal.term.Term;

public interface SetTensional extends Term {

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    //   @Override
    default public boolean isCommutative() {
        return true;
    }

    abstract public NALOperator operator();

    abstract public int size();

    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of term
     * @return the oldName of the term
     */
    public static CharSequence makeSetName(final char opener, final char closer, final Term... arg) {
        int size = 1 + 1 - 1; //opener + closer - 1 [no preceding separator for first element]

        for (final Term t : arg)
            size += 1 + t.name().length();


        final StringBuilder n = new StringBuilder(size);

        n.append(opener);
        for (int i = 0; i < arg.length; i++) {
            if (i!=0) n.append(Symbols.ARGUMENT_SEPARATOR);
            n.append(arg[i].name());
        }
        n.append(closer);


        return n.toString();
    }

}
