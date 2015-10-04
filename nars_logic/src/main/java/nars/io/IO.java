package nars.io;

import nars.NAR;
import nars.nal.nal3.SetExt;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;
import nars.nar.Default;
import nars.term.Term;

import java.util.Map;
import java.util.function.Function;

/**
 IO utility operators
 */
public interface IO {

    public static class strsubst extends TermFunction {

        @Override
        public Object function(Operation x) {
            return null;
        }
    }

    public static TermFunction operator(String name, Function<Term[],Object> func) {
        return new TermFunction(name) {

            @Override
            public Object function(Operation x) {
                return func.apply(x.term);
            }
        };
    }

    public static void main(String[] args) {
        NAR n = new Default();
        n.on(operator("strsubst", (Term[] X) -> {

            int a = X.length;
            //if (a < 3)
            //  throw new RuntimeException(
            // "ex: strsubst( {$a:3}, (&/, \"hi i \",$a,\" now.\", #result) ");

            //first argument is substitution map
            SetExt<?> substitutions = (SetExt<?>) X[0];
            Sequence strings = (Sequence) X[1];

            //convert this set to a Map<Term,Term>
            Map<Term, Term> substs = substitutions.toInheritanceMap();

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

        }));


        n.stdout();
        n.input("strsubst( {$a:3}, (&/, \"hi i \",$a,\" now.\"), #result);");
        n.frame(5);
    }

}
