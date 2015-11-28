package nars.struct;

/**
 * Created by me on 8/28/15.
 */
public class TermName extends Union {
    public final UTF8String literal = new UTF8String(TermStructTest.MAX_ATOM_LENGTH);
    //public final Unsigned16[] sub = array(new Unsigned16[TermStructTest.MAX_SUBTERMS]);


    final int SUBTERM_EMPTY = -1;

    public TermName set(final String l) {
        literal.set(l);
        return this;
    }
    public TermName set(final byte[] l) {
        literal.set(l);
        return this;
    }

//    public TermName set(final int... subterms) {
//
//        final int l = subterms.length;
//        if (l > sub.length)
//            throw new RuntimeException("subterm overflow");
//
//        for (int i = 0; i < sub.length; i++) {
//            sub[i].set(
//                    (i < l) ? subterms[i] : SUBTERM_EMPTY
//            );
//        }
//
//        return this;
//    }


}
