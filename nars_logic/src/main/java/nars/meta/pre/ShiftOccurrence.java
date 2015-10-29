package nars.meta.pre;

import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.meta.RuleMatch;
import nars.term.Atom;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class ShiftOccurrence extends PreCondition1 {

    final public boolean positive;
    final public int direction;

    public static ShiftOccurrence make(Term arg1, Term operator, boolean positive) {

        int dir = getDirection(operator);
        //if (dir == 0) return null;
        if (dir == Integer.MIN_VALUE)
            return null;

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

        long offset = direction * m.premise.getTask().duration();

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
        Procedure2<String,Integer> r = (k, v) ->
                relationDirection.put(k, v);

        r.value("=/>", +1);
        r.value("=\\>", -1);
        r.value("=|>", 0);
        //r.value("==>", null);

        r.value("<|>", 0);
        r.value("</>", +1);
        //r.value("<=>", null);

        r.value("&/", +1);
        r.value("&|", 0);
        //r.value("&&", null);
    }

    static int getDirection(Term operator) {
        String ss = ((Atom)operator).toStringUnquoted();
        return relationDirection.getIfAbsent(ss, Integer.MIN_VALUE);
    }

    @Override
    public String toString() {
        return super.toString() + '[' +
            (positive ? "Pos" : "Neg") +
            "," + direction +
        ']';
    }
}
