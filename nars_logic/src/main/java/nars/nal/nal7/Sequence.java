package nars.nal.nal7;

import nars.Op;
import nars.Symbols;
import nars.nal.nal5.Conjunction;
import nars.term.Compound;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import static java.lang.System.arraycopy;
import static nars.Op.COMPOUND_TERM_OPENER;
import static nars.Symbols.ARGUMENT_SEPARATOR;

/**
 * Created by me on 7/1/15.
 */
public class Sequence extends Conjunction implements Intermval {

    private final long[] intervals;

    /**
     * creates a normal sequence containing no intervals
     */
    Sequence(Term[] subterms) {
        this(subterms, new long[subterms.length + 1]);
    }

    Sequence(Term[] subterms, long[] intervals) {
        super(subterms, TemporalRules.ORDER_FORWARD);

        if (intervals.length != 1 + subterms.length)
            throw new RuntimeException("invalid intervals length: " + intervals.length + " should equal " + (subterms.length + 1));

        this.intervals = intervals;

        init(subterms);
    }

    @Override
    public nars.nal.nal7.Sequence clone() {
        return new nars.nal.nal7.Sequence(term, intervals);
    }

    @Override
    public Term clone(Term[] t) {
        //for now, require that cloning require same # of terms because intervals will be copied as-is
        if (t.length != length())
            return null;

        return new nars.nal.nal7.Sequence(t, intervals);
    }

    @Override
    public long[] intervals() {
        return intervals;
    }

    @Override
    public Op operator() {
        return Op.SEQUENCE;
    }

    /**
     * the input Terms here is "unnormalized" meaning it may contain
     * Interval whch will need removed as subterms and inserted into the
     * intervals array. the final subterms array for the new Sequence
     * will not contain any Intervals but the data will be separated
     * into the separate intervals array that ensures the terms avoid
     * involving Intervals in equality tests
     *
     * @param a
     * @return
     */
    public static nars.nal.nal7.Sequence makeForward(final Term[] a) {
        //1. count how many intervals so we know how to resize the final arrays
        int intervalsPresent = AbstractInterval.intervalCount(a);

        if (intervalsPresent == 0) return new nars.nal.nal7.Sequence(a);

        Term[] b = new Term[a.length - intervalsPresent];
        long[] i = new long[b.length + 1];

        int p = 0;
        for (final Term x : a) {
            if (x instanceof AbstractInterval) {
                i[p] += ((AbstractInterval) x).cycles(null);
            } else {
                b[p++] = x;
            }
        }

        return new nars.nal.nal7.Sequence(b, i);
    }

    public static Term makeForward(Term term1, Term term2) {
        final Term[] components;

        if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {

            Compound cterm1 = (Compound) term1;

            ArrayList<Term> list = new ArrayList<>(cterm1.length());
            cterm1.addTermsTo(list);

            if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                ((Compound) term2).addTermsTo(list);
            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                list.add(term2);
            }

            components = list.toArray(new Term[list.size()]);

        } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            Compound cterm2 = (Compound) term2;
            components = new Term[term2.length() + 1];
            components[0] = term1;
            arraycopy(cterm2.term, 0, components, 1, cterm2.length());
        } else {
            components = new Term[]{term1, term2};
        }

        return makeForward(components);
    }

    @Override
    public void append(Writer p, boolean pretty) throws IOException {

            boolean opener = appendTermOpener();
            if (opener)
                p.append(COMPOUND_TERM_OPENER.ch);


            final boolean appendedOperator = appendOperator(p);


            int nterms = term.length;

            long[] ii = intervals();
            final int ni = ii.length;

            int subtermsWritten = 0;
            for (int i = 0; i < nterms + 1; i++) {
                if ((subtermsWritten != 0) || (i == 0 && nterms > 1 && appendedOperator)) {
                    p.append(ARGUMENT_SEPARATOR);
                    if (pretty) p.append(' ');
                }

                final long iii = ii[i];

                if (iii!=0) {
                    //insert Interval psuedo-term
                    p.append(Symbols.INTERVAL_PREFIX);
                    p.append(Long.toString(iii));
                    if (i!=ni-1) {
                        p.append(ARGUMENT_SEPARATOR);
                        if (pretty) p.append(' ');
                    }
                    subtermsWritten++;
                }

                if (i < nterms) {
                    term[i].append(p, pretty);
                    subtermsWritten++;
                }
            }


            appendCloser(p);


    }
}
