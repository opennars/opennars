package nars.nal.nal3;


import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.term.compound.Compound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public interface SetTensional {


    Logger logger = LoggerFactory.getLogger(SetTensional.class);

    static void append(Compound set, Appendable p, boolean pretty) {

        int len = set.size();

        //duplicated from above, dont want to store this as a field in the class
        char opener, closer;
        if (set.op(Op.SET_EXT)) {
            opener = Op.SET_EXT_OPENER.ch;
            closer = Symbols.SET_EXT_CLOSER;
        } else {
            opener = Op.SET_INT_OPENER.ch;
            closer = Symbols.SET_INT_CLOSER;
        }

        try {
            p.append(opener);
            for (int i = 0; i < len; i++) {
                Term tt = set.term(i);
                if (i != 0) p.append(Symbols.ARGUMENT_SEPARATOR);
                tt.append(p, pretty);
            }
            p.append(closer);
        } catch (IOException e) {
            logger.error("append", e);
        }
    }


    static Set<Term> subtract(Compound a, Compound b) {
        Set<Term> set = a.toSet();
        b.forEach(set::remove);
        return set;
    }


//    default boolean showsTermOpenerAndCloser() {
//        return false;
//    }

    /**
     * Check if the compound is communitative.
     *
     * @return true for communitative
     */

//    default boolean isCommutative() {
//        return true;
//    }



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


}
