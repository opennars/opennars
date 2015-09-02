package nars.struct;

import nars.Op;

import java.nio.ByteBuffer;

/**
 * Created by me on 8/28/15.
 */
public class TermCept extends Fuct  {

    public final TermName name = inner(new TermName());

    public final Unsigned16[] budget = array(new Unsigned16[TermStructTest.OPS]);


    public final TaskStruct[][] beliefs = array(new TaskStruct[TermStructTest.OPS][TermStructTest.BELIEFS]);
    public final TaskStruct[][] goals = array(new TaskStruct[TermStructTest.OPS][TermStructTest.GOALS]);
    public final TaskStruct[][] questions = array(new TaskStruct[TermStructTest.OPS][TermStructTest.QUESTIONS]);


    public TermCept() {
        super();
    }

    public TermCept(ByteBuffer core, int pos) {
        this();
        set(core, pos);
    }

    @Override
    final public ByteBuffer getByteBuffer() {
        return bb;
    }

    /** determines if this termcept is eligible to represent a commutative term,
     *  depending on the ordering and uniqueness of subterms. */
    public boolean canCommutative() {
        return false;
    }

    /** if there are 2 unique subterms,
     *  which pass additional validity tests,
     *  whether this termcept is eligible to represent a statement (relation)
     *  between these 2 terms.
     */
    public boolean canStatement() {
        return false;
    }

    /** whether the particular structure of this compound allows representation of an Operation */
    public boolean canOperation() {
        return false;
    }

    /** if an inheritance and/or similarity and/or reverse-inheritance
     * are present, then the relative balance of their strengths is reduced
     * to a value between -1..+1:
     * -1=<b-->a>   0=<a<->b>   +1=<a-->b>
     */
    public float inheritancePolarity(boolean includePriority, boolean includeBelief, boolean includeDesire) {
        return Float.NaN;
    }

    public void believe(final float f, final float c, final Op o /* long times */) {
        final int tableIndex = 0; /* TODO determine index after possibly flushing and opening a hole */

        beliefs[o.ordinal()][tableIndex]
            .truth(f, c);
    }

    //TODO scalar direction Polarity for all other commutative/non-commutative statements, ex: ==> <=> <==
    //TODO scalar temporal directionality for all temporal compounds, ex: =/> =|> =\>
    //TODO negation polarity which weighs the term with its negated opposite


    public TermCept set(TermCore core, int address) {
        set(core.terms, address * core.s);
        return this;
    }


}
