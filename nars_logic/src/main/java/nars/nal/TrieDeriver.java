package nars.nal;

import javassist.*;
import nars.nal.meta.ProcTerm;
import nars.nal.meta.RuleTrie;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations
 */
public class TrieDeriver extends Deriver {

    public final ProcTerm<PremiseMatch>[] roots;

    public TrieDeriver(String rule) {
        this(new PremiseRuleSet(Collections.singleton(rule)));
    }

    public TrieDeriver(PremiseRuleSet ruleset) {
        this(new RuleTrie(ruleset).roots);
    }

    public TrieDeriver(ProcTerm<PremiseMatch>[] roots) {
        super();
        this.roots = roots;

        /*
        for (ProcTerm<PremiseMatch> p : roots) {
            try {
                compile(p);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        */
    }



    protected void compile(ProcTerm<PremiseMatch> p) throws IOException, CannotCompileException, NotFoundException {
        StringBuilder s = new StringBuilder();

        final String header = "public final static String wtf=" +
            "\"" + this + " " + new Date() + "\"+\n" +
            "\"COPYRIGHT (C) OPENNARS. ALL RIGHTS RESERVED.\"+\n" +
            "\"THIS SOURCE CODE AND ITS GENERATOR IS PROTECTED BY THE AFFERO GENERAL PUBLIC LICENSE: https://gnu.org/licenses/agpl.html\"+\n" +
            "\"http://github.com/opennars/opennars\";\n";

        //System.out.print(header);
        p.appendJavaProcedure(s);


        ClassPool pool = ClassPool.getDefault();
        pool.importPackage("nars.truth");
        pool.importPackage("nars.nal");

        CtClass cc = pool.makeClass("nars.nal.CompiledDeriver");
        CtClass parent = pool.get("nars.nal.Deriver");

        cc.addField(CtField.make(header, cc));

        cc.setSuperclass(parent);

        //cc.addConstructor(parent.getConstructors()[0]);

        String initCode = "nars.Premise p = m.premise;";

        String m = "public void run(nars.nal.PremiseMatch m) {\n" +
                '\t' + initCode + "\n" +
                '\t' + s + '\n' +
                "}";

        System.out.println(m);


        cc.addMethod(CtNewMethod.make( m, cc ));
        cc.writeFile("/tmp");

        //System.out.println(cc.toBytecode());
        System.out.println(cc);
    }

    @Override public final void run(PremiseMatch m) {

        //int now = m.now();

        for (ProcTerm<PremiseMatch> r : roots) {
            r.accept(m);
        }

        //m.revert(now);

    }

    //final static Logger logger = LoggerFactory.getLogger(TrieDeriver.class);



//    final static void run(RuleMatch m, List<TaskRule> rules, int level, Consumer<Task> t) {
//
//        final int nr = rules.size();
//        for (int i = 0; i < nr; i++) {
//
//            TaskRule r = rules.get(i);
//            if (r.minNAL > level) continue;
//
//            PostCondition[] pc = m.run(r);
//            if (pc != null) {
//                for (PostCondition p : pc) {
//                    if (p.minNAL > level) continue;
//                    ArrayList<Task> Lx = m.apply(p);
//                    if(Lx!=null) {
//                        for (Task x : Lx) {
//                            if (x != null)
//                                t.accept(x);
//                        }
//                    }
//                    /*else
//                        System.out.println("Post exit: " + r + " on " + m.premise);*/
//                }
//            }
//        }
//    }


}
