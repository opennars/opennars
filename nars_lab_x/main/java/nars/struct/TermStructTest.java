package nars.struct;


import nars.Op;
import nars.term.Atom;

import java.util.Arrays;

/**
 * Created by me on 8/28/15.
 */
public class TermStructTest {


    /** total number of different Op types */
    final static int OPS = Op.NONE.ordinal();
    final static int BELIEFS = 4;
    final static int GOALS = 4;
    final static int QUESTIONS = 2;
    public static int MAX_SUBTERMS = 6;
    public static int MAX_ATOM_LENGTH = 12;

    //    public static class Student extends Fuct {
//        public final Enum32<Gender>       gender = new Enum32<Gender>(Gender.values());
//        public final UTF8String name   = new UTF8String(64);
//        //public final Date                 birth  = inner(new Date());
//        public final Float32[]            grades = array(new Float32[10]);
//        public final Reference32<Student> next   =  new Reference32<Student>();
//    }

    public static void main(String[] args) {

        TermCore core = new TermCore(1024*1024);

        TermSpect a = core.term(Atom.the("a").bytes(), Op.ATOM);

        TermSpect b = core.term(Atom.the("b").bytes(), Op.ATOM);

        TermSpect aIntB = core.term(Atom.the("a_b").bytes(), Op.INHERITANCE);
        aIntB.believe(1f, 0.9f);

        TermSpect aSimB = aIntB.the(Op.SIMILARITY);
        aSimB.believe(1f, 0.9f);

        System.out.println( core.index.size() );

        core.index.entrySet().forEach(e -> {
            System.out.println(Arrays.toString(e.getKey()) + " " + e.getValue());
        });

//
//        final int s = a.size();
//
//        TermCept b = new TermCept(core, s * 1);
//        b.name.set("b");
//
//        TermCept aInhB = new TermCept(core, s * 2);
//        aInhB.name.set(a.pos(), b.pos());
//        aInhB.believe(1.0f, 0.9f, Op.INHERITANCE);
//
//
//
//        core.rewind();
//
//
//        int n;
//        for (n = 0, core.position(n); n < s * 3; n++) {
//            int bb = core.get();
//            System.out.print(Integer.toHexString(bb));
//            System.out.print(' ');
//            if (n % 60 == 0) System.out.println();
//        }
//        System.out.println();

    }

}
