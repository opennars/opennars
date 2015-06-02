package nars.nal.nal3;


import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.util.data.id.DynamicUTF8Identifier;
import nars.util.data.id.UTF8Identifier;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;

import static nars.Symbols.ARGUMENT_SEPARATOR;
import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;

public interface SetTensional extends Term {

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */

    default public boolean isCommutative() {
        return true;
    }

    abstract public NALOperator operator();




    public Term term(int subterm);



    public final static class SetUTF8Identifier extends DynamicUTF8Identifier {
        private final SetTensional compound;

        public SetUTF8Identifier(SetTensional c) {
            this.compound = c;
        }

        @Override
        public byte[] newName() {

            //TODO calculate length exactly


            final int len = compound.length();

            final char opener, closer;
            if (compound instanceof SetExt) { opener = NALOperator.SET_EXT_OPENER.ch; closer = NALOperator.SET_EXT_CLOSER.ch;            }
            else { opener = NALOperator.SET_INT_OPENER.ch;  closer = NALOperator.SET_INT_CLOSER.ch; }

            //calculate total size
            int bytes = 2;
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);
                bytes += tt.name().bytes().length;
                if (i!=0) bytes++; //comma
            }

            ByteBuf b = ByteBuf.create(bytes);

            b.add((byte) opener);
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);
                if (i!=0) b.add((byte)Symbols.ARGUMENT_SEPARATOR);
                b.add(tt.bytes());
            }
            b.add((byte)closer);

            return b.toBytes();

        }

        @Override
        public void write(Writer p, boolean pretty) throws IOException {

            final int len = compound.length();

            //duplicated from above, dont want to store this as a field in the class
            final char opener, closer;
            if (compound instanceof SetExt) { opener = NALOperator.SET_EXT_OPENER.ch; closer = NALOperator.SET_EXT_CLOSER.ch;            }
            else { opener = NALOperator.SET_INT_OPENER.ch;  closer = NALOperator.SET_INT_CLOSER.ch; }

            p.write(opener);
            for (int i = 0; i < len; i++) {
                Term tt = compound.term(i);
                if (i!=0) p.write(Symbols.ARGUMENT_SEPARATOR);
                tt.write(p, pretty);
            }
            p.write(closer);
        }
    }

}
