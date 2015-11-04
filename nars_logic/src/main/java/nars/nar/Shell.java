package nars.nar;

import nars.java.NALObjects;

/**
 * Shell wraps a Terminal in itself via a NALObject;
 * NAR in the Shell. Useful for metaprogramming
 */
public class Shell extends Terminal {


    public static Shell make() throws Exception {
        Shell s = NALObjects.wrap(new Shell());
        return s;
    }

    /** do not call directly */
    Shell()  {
        super();
    }

//    public static void main(String[] arg) throws Exception {
//
//        Shell n = Shell.make();
//        n.stdout();
//        Task t = n.task("echo(hi);");
//
//        n.frame(1);
//
//        n.input(t);
//
//        Memory m = n.memory();
//        //n.frame(2);
//
//        n.term("a:b");
//
//        new DeductiveChainTest(n,3,2, DeductiveChainTest.sim).run(false);
//
//        n.frame(1);
//
//    }
}
