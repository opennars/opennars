package nars.nal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import javassist.*;
import nars.Global;
import nars.nal.meta.*;
import nars.nal.meta.op.Derive;
import nars.nal.meta.op.MatchTerm;
import nars.term.Term;
import org.magnos.trie.TrieNode;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations
 */
public class TrieDeriver extends Deriver {

    public final ProcTerm<PremiseMatch>[] roots;
    public final TermTrie<Term, PremiseRule> trie;

    /** derivation term graph, gathered for analysis */
    public final HashMultimap<MatchTerm,Derive> derivationLinks = HashMultimap.create();

    public TrieDeriver(String... rule) {
        this(new PremiseRuleSet(Lists.newArrayList(rule)));
    }

    public TrieDeriver(PremiseRuleSet ruleset) {
        super(ruleset);

        this.trie = new TermTrie<Term, PremiseRule>(ruleset.getPremiseRules()) {

            @Override
            public void index(PremiseRule s) {

                if (s == null || s.postconditions == null)
                    return;

                for (PostCondition p : s.postconditions) {

                    PremiseRule existing = trie.put(s.getConditions(p), s);

                    if (existing != null && s != existing && existing.equals(s)) {
                        System.err.println("DUPL: " + existing);
                        System.err.println("      " + existing.getSource());
                        System.err.println("EXST: " + s.getSource());
                        System.err.println();
                    }
                }
            }
        };

        this.roots = getBranches(trie.trie.root).toArray(new ProcTerm[0]);


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

    @Override
    public final void run(PremiseMatch m) {
        for (ProcTerm<PremiseMatch> r : roots)
            r.accept(m);
    }

    /** HACK warning: use of this singular matchParent tracker is not thread-safe. assumes branches will be processed in a linear, depth first order */
    final transient AtomicReference<MatchTerm> matchParent = new AtomicReference<MatchTerm>(null);

    private List<Term> getBranches(TrieNode<List<Term>, PremiseRule> node) {

        List<Term> bb = Global.newArrayList(node.getChildCount());


        node.forEach(n -> {
            List<Term> seq = n.getSequence();

            int from = n.getStart();
            int to = n.getEnd();


            bb.add(branch(
                    compileConditions(seq.subList(from, to), matchParent),
                    new PremiseMatchFork(compileActions(TrieDeriver.this.getBranches(n)).toArray(new ProcTerm[0]))));
        });

        return bb;
    }


    private Collection<BooleanCondition<PremiseMatch>> compileConditions(Collection<Term> t, AtomicReference<MatchTerm> matchParent) {

        return t.stream().filter(x -> {
            if (x instanceof BooleanCondition) {
                if (x instanceof MatchTerm) {
                    matchParent.set((MatchTerm) x);
                }
                return true;
            } if (x instanceof Derive) {
                //link this derivation action to the previous Match,
                //allowing multiple derivations to fold within a Match's actions
                MatchTerm mt = matchParent.get();
                if (mt == null) {
                    throw new RuntimeException("detached Derive action: " + x + " in branch: " + t);
                    //System.err.println("detached Derive action: " + x + " in branch: " + t);
                }
                else {
                    //HACK
                    Derive dx = (Derive) x;
                    mt.derive(dx);
                    derivationLinks.put(mt, dx);
                }
                return false;
            } else {
                throw new RuntimeException("not boolean condition" + x + " in branch: " + t + " (" + x.getClass() + ')');
                //System.out.println("\tnot boolean condition");
                //return false;
            }
        }).map(x -> (BooleanCondition<PremiseMatch>)x).collect(Collectors.toList());
    }



    private static Collection<ProcTerm<PremiseMatch>> compileActions(List<Term> t) {
        //t.forEach(x -> System.out.println(x.getClass() + " " + x));
        return (Collection)t;
    }


    public static ProcTerm<PremiseMatch> branch(
            Collection<BooleanCondition<PremiseMatch>> condition,
            ThenFork<PremiseMatch> conseq) {

        if ((conseq != null) && (conseq.size() > 0)) {
            return new PremiseBranch(condition, conseq);
        } else {
            return new PremiseBranch(condition, Return.the);
        }
    }

    protected void compile(ProcTerm<PremiseMatch> p) throws IOException, CannotCompileException, NotFoundException {
        StringBuilder s = new StringBuilder();

        final String header = "public final static String wtf=" +
                '"' + this + ' ' + new Date() + "\"+\n" +
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
                '\t' + initCode + '\n' +
                '\t' + s + '\n' +
                '}';

        System.out.println(m);


        cc.addMethod(CtNewMethod.make(m, cc));
        cc.writeFile("/tmp");

        //System.out.println(cc.toBytecode());
        System.out.println(cc);
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
