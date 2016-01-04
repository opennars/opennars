package nars.op.data;

import nars.Global;
import nars.NAR;
import nars.Op;
import nars.nal.nal7.Sequence;
import nars.nar.Default;
import nars.term.Term;
import nars.term.compound.Compound;

import java.util.Map;

import static nars.Op.PRODUCT;

/**
 IO utility operators
 */
public interface StringOps {


    static void initStringOps(NAR n) {

        n.onExecTerm("str_replace", (Term[] X) -> {

            //first argument is substitution map
            Compound<?> substitutions = (Compound<?>) X[0];
            Sequence<?> strings = (Sequence) X[1];

            //convert this set to a Map<Term,Term>
            Map<Term, Term> substs =
                    toKeyValueMap(substitutions);

            StringBuilder sb = new StringBuilder();
            for (Term s : strings) {

                Term replacement = substs.get(s);

                //TODO copy in byte[] without String transforms
                if (replacement != null) {
                    sb.append(replacement);
                } else {
                    sb.append(s);
                }
            }

            return sb.toString();

        });


    }

    /**
     * interprets subterms of a compound term to a set of
     * key,value pairs (Map entries).
     * ie, it translates this SetExt tp a Map<Term,Term> in the
     * following pattern:
     * <p/>
     * { (a,b) }  becomes Map a=b
     * [ (a,b), b:c ] bcomes Map a=b, b=c
     * { (a,b), (b,c), d } bcomes Map a=b, b=c, d=null
     *
     * @return a potentially incomplete map representation of this compound
     */
    static Map<Term, Term> toKeyValueMap(Compound<?> t) {

        Map<Term, Term> result = Global.newHashMap();

        t.forEach(a -> {
            if (a.size() == 2) {
                if ((a.op() == PRODUCT) || (a.op() == Op.INHERIT)) {
                    Compound ii = (Compound) a;
                    result.put(ii.term(0), ii.term(1));
                }
            } else if (a.size() == 1) {
                result.put(a, null);
            }
        });

        return result;
    }


    default void testStrReplace() {
        NAR n = new Default(256,1,1,3);
        n.trace();

        StringOps.initStringOps(n);
        String cmd = "str_replace( {(#number,3)}, (&/, it_, is_, #number), #result);";
        n.input(cmd);

        n.frame(5);

        //expect:  <{it_is_3} --> (/, str_replace, {(#1, 3)}, (&/, it_, is_, #1), _)>. :|: %1.00;0.99% Feedback

    }

}
