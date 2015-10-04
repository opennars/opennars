package nars.io;

import nars.NAR;
import nars.nal.nal7.Sequence;
import nars.nar.Default;
import nars.term.Compound;
import nars.term.Term;

import java.util.Map;

/**
 IO utility operators
 */
public interface IO {


    static void initStringOps(NAR n) {

        n.on("str_replace", (Term[] X) -> {

            //first argument is substitution map
            Compound<?> substitutions = (Compound<?>) X[0];
            Sequence strings = (Sequence) X[1];

            //convert this set to a Map<Term,Term>
            Map<Term, Term> substs = substitutions.
                    toKeyValueMap();

            StringBuilder sb = new StringBuilder();
            for (Term s : strings.term) {

                Term replacement = substs.get(s);

                //TODO copy in byte[] without String transforms
                if (replacement != null) {
                    sb.append(replacement.toString());
                } else {
                    sb.append(s);
                }
            }

            return sb.toString();

        });


    }

    public static void main(String[] args) {

    }

    default void testStrReplace() {
        NAR n = new Default();
        n.stdout();

        IO.initStringOps(n);
        String cmd = "str_replace( {(#number,3)}, (&/, it_, is_, #number), #result);";
        n.input(cmd);

        n.frame(5);

        //expect:  <{it_is_3} --> (/, str_replace, {(#1, 3)}, (&/, it_, is_, #1), _)>. :|: %1.00;0.99% Feedback

    }

}
