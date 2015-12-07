package nars.nal.meta.post;

import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition1;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Sequence;
import nars.term.Term;
import nars.term.atom.Atom;

/**
 * Created by me on 8/15/15.
 */
public class ShiftOccurrence extends PreCondition1 {

    final public boolean positive;
    final public int direction;
    private final String id;

    public static ShiftOccurrence make(Term arg1, Term operator, boolean positive) {

        int dir = getDirection(operator);
        //if (dir == 0) return null;
        if (dir == Integer.MIN_VALUE)
            throw new RuntimeException("invalid operator: " + operator);
            //return null;

        return new ShiftOccurrence(arg1, positive, dir);
    }

    ShiftOccurrence(Term arg1, boolean positive, int direction) {
        super(arg1);
/* doesnt seem to work so my way for now
        if ((
            this.direction = relationDirection
                    .getIfAbsent(operator, Integer.MIN_VALUE)
        ) == Integer.MIN_VALUE)
            throw new RuntimeException("unrecognized TimeOffset parameter: " + operator);
*/
        this.positive = positive;
        this.direction = direction;
        this.id = getClass().getSimpleName() + ":(" + arg1 + ',' +
                (positive ? "Pos" : "Neg") +
                ',' + direction +
                ')';

//        //if(operator.getTemporalOrder()== Temporal.ORDER_FORWARD) {
//        //as long as this TemporalOrder()  check deosnt work we use string comparison:
//        int ret = operator.getTemporalOrder();
//        String str = operator.toString().replace("\"","");
//        if(str.equals("=/>") || str.equals("</>")) {
//           // direction = 1;
//        }
//        else
//        if(str.equals("=\\>")) {
//         //   direction = -1;
//        }
    }

    @Override public boolean test(final RuleMatch m) {

        Premise p = m.premise;

        if (p.isEternal()) {
            //continue with derivation but dont apply shift
            return true;
        }

        if (positive) {
            Term ret = p.getTermLink().getTerm();
            if (ret instanceof Implication) {
                Term impSubj = ((Implication) ret).getSubject();
                if (impSubj instanceof Sequence) {
                    Sequence seq = (Sequence)impSubj;

                    int[] ii = seq.intervals();
                    int iiLen = ii.length;

                    if (iiLen > 0) {
                        m.occurrenceShift.set(
                            ii[iiLen - 1]
                        );
                        //positive ? interval : -interval);
                    }

                }
            }
        }
        /* on backward its already handled by shifting
           (&/,a,/i) backward on i and changing it to a
         */


        return super.test(m);
    }

    @Override
    public final boolean test(RuleMatch m, Term arg) {

        /*if (arg.getTemporalOrder() == Temporal.ORDER_NONE) {
            return true; //and this means its no-order so dont shift
        }*/

        //NOTE intervals should not appear this point;
        // add here a general purpose 'interval span' method
        // that any term will report its total interval.
        // a sequence will return the sum of its intermvals, for example
        //
//        if (arg instanceof AbstractInterval) {
//            return ((AbstractInterval) arg).cycles(nal.memory());
//        }

        long offset = direction != 0 ? m.premise.getTask().duration() : 0;

        //if (offset > Stamp.TIMELESS) {
            //shift since it has an order..
            m.occurrenceAdd(
                (positive ? +1 : -1) * offset
            );
        //}

        return true;
    }


    static final ObjectIntHashMap<String> relationDirection = new ObjectIntHashMap<>(16);
    static {
        Procedure2<String,Integer> r = relationDirection::put;

        r.value("=/>", +1);
        r.value("=\\>", -1);
        r.value("=|>", 0);
        r.value("==>", 0);

        r.value("<|>", 0);
        r.value("</>", +1);
        r.value("<=>", 0);

//        r.value("&/", +1);
//        r.value("&|", 0);
//        //r.value("&&", null);
    }

    static int getDirection(Term operator) {
        String ss = ((Atom)operator).toStringUnquoted();
        return relationDirection.getIfAbsent(ss, Integer.MIN_VALUE);
    }

    @Override
    public String toString() {
        return id;
    }
}
