//package nars.prolog;
//
//import nars.NAR;
//import org.projog.api.Projog;
//import org.projog.core.KB;
//import org.projog.core.Operands;
//import org.projog.core.PredicateKey;
//import org.projog.core.ProjogSystemProperties;
//import org.projog.core.parser.SentenceParser;
//import org.projog.core.term.*;
//import org.projog.core.udp.ClauseModel;
//import org.projog.core.udp.StaticUserDefinedPredicateFactory;
//import org.projog.core.udp.UserDefinedPredicateFactory;
//
//import java.util.Arrays;
//
//import static org.projog.core.KnowledgeBaseServiceLocator.getServiceLocator;
//
///**
// * Created by me on 5/10/15.
// */
//public class NARPrologAgent extends Projog {
//
//    private static UserDefinedPredicateFactory getActualPredicateFactory(KB kb, PTerm... clauses) {
//        StaticUserDefinedPredicateFactory f = null;
//        for (PTerm clause : clauses) {
//            if (f == null) {
//                PredicateKey key = PredicateKey.createForTerm(clause);
//                f = new StaticUserDefinedPredicateFactory(key);
//                f.setKB(kb);
//            }
//            ClauseModel clauseModel = ClauseModel.createClauseModel(clause);
//            f.addLast(clauseModel);
//        }
//        return f; //.getActualPredicateFactory();
//    }
//    public static Operands getOperands(KB kb) {
//        return getServiceLocator(kb).getInstance(Operands.class);
//    }
//
//    private static PTerm[] toTerms(KB kb, String... clausesSyntax) {
//        PTerm[] clauses = new PTerm[clausesSyntax.length];
//        for (int i = 0; i < clauses.length; i++) {
//            clauses[i] = parseSentence(clausesSyntax[i], kb);
//        }
//        return clauses;
//    }
//    public static SentenceParser createSentenceParser(String prologSyntax, KB kb) {
//        return SentenceParser.getInstance(prologSyntax, getOperands(kb));
//    }
//
//    public static PTerm parseSentence(String prologSyntax, KB kb) {
//        SentenceParser sp = createSentenceParser(prologSyntax, kb);
//        return sp.parseSentence();
//    }
//
//
//    public NARPrologAgent(NAR n) throws InterruptedException {
//        super(new ProjogSystemProperties() {
//            @Override
//            public boolean isRuntimeCompilationEnabled() {
//                return false;
//            }
//        });
//
//        //consultFile(new File("/home/me/share/opennars/nars_prolog/src/main/java/nal.pl"));
//
//        kb.addDefined( getActualPredicateFactory(kb,
//                toTerms(kb,
//                        "inh(a,b).",
//                        "inh(b,c)."
//                )) );
//        kb.addDefined( getActualPredicateFactory(kb,
//                toTerms(kb,
//                        "inh(A,C) :- inh(A,B), inh(B,C)."
//                        //"concatenate([],L,L).", "concatenate([X|L1],L2,[X|L3]) :- concatenate(L1,L2,L3).",
//                        //"p(X,Y,Z) :- repeat(3), X<Y.", "p(X,Y,Z) :- X is Y+Z.", "p(X,Y,Z) :- X=a."
//                )) );
//
//        query("listing(inh).").get().all( result -> {
//            System.out.println(result);
//        });
//        query("inh(a,c).").get().all( result -> {
//            System.out.println(result);
//        });
//        //System.out.println(query("?- believe(B).").get().all());
//
////        query("listing(inh).").get().all(-1 /* sec */, result -> {
////            System.out.println(result.query);
////            System.out.println(result.variables);
////        });
//
////      //p.consultFile(new File("test.pl"));
////      QueryStatement s1 = query("test(X,Y).");
////      QueryResult r1 = s1.getResult();
////      while (r1.next()) {
////         System.out.println("X = " + r1.getTerm("X") + " Y = " + r1.getTerm("Y"));
////      }
////      QueryResult r2 = s1.getResult();
////      r2.setTerm("X", new Atom("d"));
////      while (r2.next()) {
////         System.out.println("Y = " + r2.getTerm("Y"));
////      }
////
////      QueryStatement s2 = p.query("testRule(X).");
////      QueryResult r3 = s2.getResult();
////      while (r3.next()) {
////         System.out.println("X = " + r3.getTerm("X"));
////      }
////
////      QueryStatement s3 = p.query("test(X, Y), Y<3.");
////      QueryResult r4 = s3.getResult();
////      while (r4.next()) {
////         System.out.println("X = " + r4.getTerm("X") + " Y = " + r4.getTerm("Y"));
////      }
//
//        Thread.sleep(1000);
//
//    }
//
//    public static PAtom atom() {
//        return atom("test");
//    }
//
//    public static PAtom atom(String name) {
//        return new PAtom(name);
//    }
//
//    public static PStruct structure() {
//        return structure("test", new PTerm[] {atom()});
//    }
//
//    public static PStruct structure(String name, PTerm... args) {
//        return (PStruct) PStruct.make(name, args);
//    }
//
//    public static PList list(PTerm... args) {
//        return (PList) ListFactory.createList(args);
//    }
//
//    public static IntegerNumber integerNumber() {
//        return integerNumber(1);
//    }
//
//    public static IntegerNumber integerNumber(long i) {
//        return new IntegerNumber(i);
//    }
//
//    public static DecimalFraction decimalFraction() {
//        return decimalFraction(1.0);
//    }
//
//    public static DecimalFraction decimalFraction(double d) {
//        return new DecimalFraction(d);
//    }
//
//    public static PVar variable() {
//        return variable("X");
//    }
//
//    public static PVar variable(String name) {
//        return new PVar(name);
//    }
//
//    public static PTerm[] createArgs(int numberOfArguments) {
//        return createArgs(numberOfArguments, atom());
//    }
//
//    public static PTerm[] createArgs(int numberOfArguments, PTerm term) {
//        PTerm[] args = new PTerm[numberOfArguments];
//        Arrays.fill(args, term);
//        return args;
//    }
//
//
// }
