package nars.term.compile;

import nars.Op;
import nars.Symbols;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.io.IOException;

import static nars.Symbols.*;

/**
 * Created by me on 1/2/16.
 */
public interface TermPrinter {
    static void appendSeparator(Appendable p, boolean pretty) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');
    }

    static void writeCompound1(Op op, Term singleTerm, Appendable writer, boolean pretty) throws IOException {
        writer.append(COMPOUND_TERM_OPENER);
        writer.append(op.str);
        writer.append(ARGUMENT_SEPARATOR);
        singleTerm.append(writer, pretty);
        writer.append(COMPOUND_TERM_CLOSER);
    }

    static void appendCompound(Compound c, Appendable p, boolean pretty) throws IOException {

        p.append(COMPOUND_TERM_OPENER);

        c.op().append(p);

        if (c.size() == 1)
            p.append(ARGUMENT_SEPARATOR);

        c.appendArgs(p, pretty, true);

        appendCloser(p);

    }

    static void appendCloser(Appendable p) throws IOException {
        p.append(COMPOUND_TERM_CLOSER);
    }

    static void setAppend(Compound set, Appendable p, boolean pretty) throws IOException {

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

        p.append(opener);
        for (int i = 0; i < len; i++) {
            Term tt = set.term(i);
            if (i != 0) p.append(Symbols.ARGUMENT_SEPARATOR);
            tt.append(p, pretty);
        }
        p.append(closer);
    }

    static void imageAppend(GenericCompound image, Appendable p, boolean pretty) throws IOException {

        int len = image.size();

        p.append(COMPOUND_TERM_OPENER);
        p.append(image.op().str);

        int relationIndex = image.relation();
        int i;
        for (i = 0; i < len; i++) {
            Term tt = image.term(i);

            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');

            if (i == relationIndex) {
                p.append(Symbols.IMAGE_PLACE_HOLDER);
                p.append(ARGUMENT_SEPARATOR);
                if (pretty) p.append(' ');
            }

            tt.append(p, pretty);
        }
        if (i == relationIndex) {
            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');
            p.append(Symbols.IMAGE_PLACE_HOLDER);
        }

        p.append(COMPOUND_TERM_CLOSER);

    }

    static void productAppend(Compound product, Appendable p, boolean pretty) throws IOException {

        int s = product.size();
        p.append(COMPOUND_TERM_OPENER);
        for (int i = 0; i < s; i++) {
            product.term(i).append(p, pretty);
            if (i < s - 1) {
                p.append(pretty ? ", " : ",");
            }
        }
        p.append(COMPOUND_TERM_CLOSER);
    }
}
