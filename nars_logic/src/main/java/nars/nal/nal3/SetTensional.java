package nars.nal.nal3;


import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;

public interface SetTensional extends Term, Iterable<Term> {

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */

    default public boolean isCommutative() {
        return true;
    }

    abstract public Op operator();




    public Term term(int subterm);




        default public byte[] init() {

            //TODO calculate length exactly


            final int len = length();

            final char opener, closer;
            if (this instanceof SetExt) { opener = Op.SET_EXT_OPENER.ch; closer = Op.SET_EXT_CLOSER.ch;            }
            else { opener = Op.SET_INT_OPENER.ch;  closer = Op.SET_INT_CLOSER.ch; }

            //calculate total size
            int bytes = 2;
            for (int i = 0; i < len; i++) {
                Term tt = term(i);
                bytes += tt.name().bytes().length;
                if (i!=0) bytes++; //comma
            }

            ByteBuf b = ByteBuf.create(bytes);

            b.add((byte) opener);
            for (int i = 0; i < len; i++) {
                Term tt = term(i);
                if (i!=0) b.add((byte) Symbols.ARGUMENT_SEPARATOR);
                b.add(tt.bytes());
            }
            b.add((byte) closer);

            return b.toBytes();

        }


        default public void append(Writer p, boolean pretty) throws IOException {

            final int len = length();

            //duplicated from above, dont want to store this as a field in the class
            final char opener, closer;
            if (this instanceof SetExt) { opener = Op.SET_EXT_OPENER.ch; closer = Op.SET_EXT_CLOSER.ch;            }
            else { opener = Op.SET_INT_OPENER.ch;  closer = Op.SET_INT_CLOSER.ch; }

            p.append(opener);
            for (int i = 0; i < len; i++) {
                Term tt = term(i);
                if (i!=0) p.append(Symbols.ARGUMENT_SEPARATOR);
                tt.append(p, pretty);
            }
            p.append(closer);
        }


    default boolean showsTermOpenerAndCloser() {
        return false;
    }

}
