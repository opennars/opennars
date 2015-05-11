package nars.prolog;

import nars.NAR;
import org.projog.api.Projog;
import org.projog.core.ProjogSystemProperties;
import org.projog.core.term.*;

import java.io.File;
import java.util.Arrays;

/**
 * Created by me on 5/10/15.
 */
public class NARPrologAgent extends Projog {

    public NARPrologAgent(NAR n) throws InterruptedException {
        super(new ProjogSystemProperties() {
            @Override
            public boolean isRuntimeCompilationEnabled() {
                return false;
            }
        });

        consultFile(new File("/home/me/share/opennars/nars_prolog/src/main/java/nal.pl"));

        consult("believe(inh(A,C)) :- inh(A,B) , inh(B,C).\n");

        System.out.println(query("asserta(inh(a,b)).").get().all());
        System.out.println(query("asserta(inh(b,c)).").get().all());
        query("inh(X,Y).").get().all( result -> {
            System.out.println(result);
        });
        System.out.println(query("?- believe(B).").get().all());

//        query("listing(inh).").get().all(-1 /* sec */, result -> {
//            System.out.println(result.query);
//            System.out.println(result.variables);
//        });

//      //p.consultFile(new File("test.pl"));
//      QueryStatement s1 = query("test(X,Y).");
//      QueryResult r1 = s1.getResult();
//      while (r1.next()) {
//         System.out.println("X = " + r1.getTerm("X") + " Y = " + r1.getTerm("Y"));
//      }
//      QueryResult r2 = s1.getResult();
//      r2.setTerm("X", new Atom("d"));
//      while (r2.next()) {
//         System.out.println("Y = " + r2.getTerm("Y"));
//      }
//
//      QueryStatement s2 = p.query("testRule(X).");
//      QueryResult r3 = s2.getResult();
//      while (r3.next()) {
//         System.out.println("X = " + r3.getTerm("X"));
//      }
//
//      QueryStatement s3 = p.query("test(X, Y), Y<3.");
//      QueryResult r4 = s3.getResult();
//      while (r4.next()) {
//         System.out.println("X = " + r4.getTerm("X") + " Y = " + r4.getTerm("Y"));
//      }

        Thread.sleep(1000);

    }

    public static PAtom atom() {
        return atom("test");
    }

    public static PAtom atom(String name) {
        return new PAtom(name);
    }

    public static PStruct structure() {
        return structure("test", new PTerm[] {atom()});
    }

    public static PStruct structure(String name, PTerm... args) {
        return (PStruct) PStruct.make(name, args);
    }

    public static PList list(PTerm... args) {
        return (PList) ListFactory.createList(args);
    }

    public static IntegerNumber integerNumber() {
        return integerNumber(1);
    }

    public static IntegerNumber integerNumber(long i) {
        return new IntegerNumber(i);
    }

    public static DecimalFraction decimalFraction() {
        return decimalFraction(1.0);
    }

    public static DecimalFraction decimalFraction(double d) {
        return new DecimalFraction(d);
    }

    public static PVar variable() {
        return variable("X");
    }

    public static PVar variable(String name) {
        return new PVar(name);
    }

    public static PTerm[] createArgs(int numberOfArguments) {
        return createArgs(numberOfArguments, atom());
    }

    public static PTerm[] createArgs(int numberOfArguments, PTerm term) {
        PTerm[] args = new PTerm[numberOfArguments];
        Arrays.fill(args, term);
        return args;
    }


}
