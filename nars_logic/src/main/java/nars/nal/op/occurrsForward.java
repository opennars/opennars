package nars.nal.op;


import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.$;
import nars.Op;
import nars.nal.PremiseAware;
import nars.nal.PremiseMatch;
import nars.nal.nal7.Sequence;
import nars.process.ConceptProcess;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;

/**
 * occurrsRelative(target, variable, direction)
 * target: pass through
 * direction= +1, -1, 0
 * variable: term to modify occurrence relative to
 */
public class occurrsForward extends ImmediateTermTransform implements PremiseAware {
    static final ObjectIntHashMap<Atom> relationDirection = new ObjectIntHashMap<>(16);

    static {
        Procedure2<Atom, Integer> r = relationDirection::put;

        r.value($.the("\"=/>\""), +1);
        r.value($.the("\"=\\>\""), -1);
        r.value($.the("\"=|>\""), 0);
        r.value($.the("\"==>\""), 0);

        r.value($.the("\"<|>\""), 0);
        r.value($.the("\"</>\""), +1);
        r.value($.the("\"<=>\""), 0);

//        r.value("&/", +1);
//        r.value("&|", 0);
//        //r.value("&&", null);
    }

    //HACK
    @Override public Term function(Compound p, TermIndex i) {
        throw new RuntimeException("should only be called during RuleMatch");
    }

    @Override
    public Term function(Compound p, PremiseMatch r) {
        final Term[] xx = p.terms();

        Term term = xx[0];

        ConceptProcess premise = r.premise;

        if (premise.isEternal()) {
            //continue with derivation but dont apply shift
            return term;
        }


        int direction = relationDirection.getIfAbsent(xx[2], Integer.MAX_VALUE);
        if (direction == Integer.MAX_VALUE)
            throw new RuntimeException("invalid direction term: " + xx[2]);
        boolean positive = positive();


        if (positive) {
            //Term ret = xx[1];
            Termed ret = premise.termLink.get();

            if (ret.op() == Op.IMPLICATION) {
                Term impSubj = Statement.subj(ret);
                if (impSubj.op(Op.SEQUENCE)) {
                    Sequence seq = (Sequence)impSubj;

                    int[] ii = seq.intervals();
                    int iiLen = ii.length;

                    if (iiLen > 0) {
                        r.occurrenceShift.set(
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


        //long durationsOffset = direction; //direction != 0 ? direction : 0;

        long durationsDelta = (positive ? +1 : -1) * direction;

        r.occurrenceAdd(durationsDelta);


        return term;
    }

    protected boolean positive() {
        return true;
    }



    //    ShiftOccurrence(Term arg1, boolean positive, int direction) {
//        super(arg1);
///* doesnt seem to work so my way for now
//        if ((
//            this.direction = relationDirection
//                    .getIfAbsent(operator, Integer.MIN_VALUE)
//        ) == Integer.MIN_VALUE)
//            throw new RuntimeException("unrecognized TimeOffset parameter: " + operator);
//*/
//        this.positive = positive;
//        this.direction = direction;
//        id = getClass().getSimpleName() + ":(" + arg1 + ',' +
//                (positive ? "Pos" : "Neg") +
//                ',' + direction +
//                ')';
//
////        //if(operator.getTemporalOrder()== Temporal.ORDER_FORWARD) {
////        //as long as this TemporalOrder()  check deosnt work we use string comparison:
////        int ret = operator.getTemporalOrder();
////        String str = operator.toString().replace("\"","");
////        if(str.equals("=/>") || str.equals("</>")) {
////           // direction = 1;
////        }
////        else
////        if(str.equals("=\\>")) {
////         //   direction = -1;
////        }
//    }
//
//    @Override public boolean test(RuleMatch m) {
//
//        Premise p = m.premise;
//
//        if (p.isEternal()) {
//            //continue with derivation but dont apply shift
//            return true;
//        }
//
//        if (positive) {
//            Term ret = p.getTermLink().get().term();
//
//            if (ret.op(IMPLICATION)) {
//                Term impSubj = subj(ret);
//                if (impSubj.op(SEQUENCE)) {
//                    Sequence seq = (Sequence)impSubj;
//
//                    int[] ii = seq.intervals();
//                    int iiLen = ii.length;
//
//                    if (iiLen > 0) {
//                        m.occurrenceShift.set(
//                                ii[iiLen - 1]
//                        );
//                        //positive ? interval : -interval);
//                    }
//
//                }
//            }
//        }
//        /* on backward its already handled by shifting
//           (&/,a,/i) backward on i and changing it to a
//         */
//
//
//        return super.test(m);
//    }
//
//    @Override
//    public final boolean test(RuleMatch m, Term arg) {
//
//        /*if (arg.getTemporalOrder() == Temporal.ORDER_NONE) {
//            return true; //and this means its no-order so dont shift
//        }*/
//
//        //NOTE intervals should not appear this point;
//        // add here a general purpose 'interval span' method
//        // that any term will report its total interval.
//        // a sequence will return the sum of its intermvals, for example
//        //
////        if (arg instanceof AbstractInterval) {
////            return ((AbstractInterval) arg).cycles(nal.memory());
////        }
//
//        long offset = direction != 0 ? m.premise.getTask().duration() : 0;
//
//        //if (offset > Stamp.TIMELESS) {
//        //shift since it has an order..
//        m.occurrenceAdd(
//                (positive ? +1 : -1) * offset
//        );
//        //}
//
//        return true;
//    }
//
//
//    static final ObjectIntHashMap<String> relationDirection = new ObjectIntHashMap<>(16);
//    static {
//        Procedure2<String,Integer> r = relationDirection::put;
//
//        r.value("=/>", +1);
//        r.value("=\\>", -1);
//        r.value("=|>", 0);
//        r.value("==>", 0);
//
//        r.value("<|>", 0);
//        r.value("</>", +1);
//        r.value("<=>", 0);
//
////        r.value("&/", +1);
////        r.value("&|", 0);
////        //r.value("&&", null);
//    }

}