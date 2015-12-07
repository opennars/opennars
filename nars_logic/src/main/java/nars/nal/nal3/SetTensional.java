package nars.nal.nal3;


import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.term.compound.Compound;

import java.io.IOException;
import java.util.Set;

public interface SetTensional<T extends Term> extends Compound<T> {

    /**
     * Check if the compound is communitative.
     *
     * @return true for communitative
     */

//    default boolean isCommutative() {
//        return true;
//    }

    @Override
    Op op();


    @Override
    T term(int subterm);


//    default byte[] init() {
//
//        //TODO calculate length exactly
//
//
//        final int len = size();
//
//        final char opener, closer;
//        if (this instanceof SetExt) {
//            opener = Op.SET_EXT_OPENER.ch;
//            closer = Symbols.SET_EXT_CLOSER;
//        } else {
//            opener = Op.SET_INT_OPENER.ch;
//            closer = Symbols.SET_INT_CLOSER;
//        }
//
//        //calculate total size
//        int bytes = 2;
//        for (int i = 0; i < len; i++) {
//            T tt = term(i);
//            bytes += tt.bytes().length;
//            if (i != 0) bytes++; //comma
//        }
//
//        ByteBuf b = ByteBuf.create(bytes);
//
//        b.add((byte) opener);
//        for (int i = 0; i < len; i++) {
//            Term tt = term(i);
//            if (i != 0) b.add((byte) Symbols.ARGUMENT_SEPARATOR);
//            b.add(tt.bytes());
//        }
//        b.add((byte) closer);
//
//        return b.toBytes();
//
//    }


    @Override
    default void append(Appendable p, boolean pretty) throws IOException {

        final int len = size();

        //duplicated from above, dont want to store this as a field in the class
        final char opener, closer;
        if (this instanceof SetExt) {
            opener = Op.SET_EXT_OPENER.ch;
            closer = Symbols.SET_EXT_CLOSER;
        } else {
            opener = Op.SET_INT_OPENER.ch;
            closer = Symbols.SET_INT_CLOSER;
        }

        p.append(opener);
        for (int i = 0; i < len; i++) {
            Term tt = term(i);
            if (i != 0) p.append(Symbols.ARGUMENT_SEPARATOR);
            tt.append(p, pretty);
        }
        p.append(closer);
    }





    static Set<Term> subtract(SetTensional a, SetTensional b) {
        Set<Term> set = a.toSet();
        b.forEach(set::remove);
        return set;
    }


//    default boolean showsTermOpenerAndCloser() {
//        return false;
//    }

}
