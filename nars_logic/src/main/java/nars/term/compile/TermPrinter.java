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

}
