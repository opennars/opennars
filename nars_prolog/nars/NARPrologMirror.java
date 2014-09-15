package nars;

import java.io.File;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;
import nars.prolog.InvalidTheoryException;
import nars.prolog.SolveInfo;
import nars.prolog.Struct;
import nars.prolog.Var;

/**
 * Causes a NARProlog to mirror certain activity of a NAR
 */
public class NARPrologMirror implements Output {
    private final NAR nar;
    private final NARProlog prolog;

    public NARPrologMirror(NAR nar, NARProlog prolog) {
        this.nar = nar;
        this.prolog = prolog;
        
        nar.addOutput(this);
    }
   
    @Override
    public void output(final Class channel, final Object o) {        
        if ((channel == IN.class)) {
            if (o instanceof Task) {
                Task task = (Task)o;
                Sentence s = task.sentence;
                if (s.isJudgment()) {                    
                    try {
                        Struct th = newJudgmentTheory(s);
                        if (th!=null) {
                            System.out.println("Theory: " + th.toString());
                            SolveInfo si = prolog.addTheory(th);
                            if (si!=null)
                                System.out.println("  Answer: " + si);
                        }
                    } catch (Exception ex) {
                        nar.output(ERR.class, ex.toString());
                    }
                }
                else if (s.isQuestion()) {
                    try {
                        Struct qh = newQuestion(s);
                        if (qh!=null) {
                            //System.out.println("Question: " + qh.toString() + " ?");
                            SolveInfo si = prolog.solve(qh);
                            //System.out.println("  Answer: " + si);
                        }
                    } catch (Exception ex) {
                        nar.output(ERR.class, ex.toString());
                    }
                }

            }
        }
    }
    
    /** creates a theory from a judgment Statement */
    Struct newJudgmentTheory(final Sentence judgment) throws InvalidTheoryException {
        
        //TODO directly construct the theory objects, instead of creating a String
        
        nars.prolog.Term s = pterm(judgment.content);
        if (s instanceof NARStruct) {
            ((NARStruct)s).setSentence(judgment);
        }
        if (s instanceof Struct)
            return (Struct) s;
        return null;
            
    }
    
    Struct newQuestion(final Sentence question) {
        nars.prolog.Term s = pterm(question.content);
        if (s instanceof Struct)
            return (Struct) s;
        return null;
    }

//    public final static BiMap<Class,String> classToProlog = HashBiMap.create();
//    static {
//        classToProlog.put(Inheritance.class, "inheritance");
//        classToProlog.put(Similarity.class, "similarity");
//        classToProlog.put(Implication.class, "similarity");
//        classToProlog.put(Equivalence.class, "similarity");
//    }
    
    //NARS term -> Prolog term
    public nars.prolog.Term pterm(final Term term) {
        
        //CharSequence s = termString(term);
        if (term instanceof Statement) {
            Statement i = (Statement)term;
            String predicate = i.getClass().getSimpleName().toLowerCase();
            return new NARStruct(predicate, pterm(i.getSubject()), pterm(i.getPredicate()));
        }
        else if (term.getClass().equals(Variable.class)) {
            return new Var("V" + term.name().toString());
        }
        else if (term.getClass().equals(Term.class)) {
            return new Struct(term.name().toString());
        }
        
        return null;        
    }
    

    public static class NARStruct extends Struct {
        
        Sentence sentence = null;

        public NARStruct(Sentence sentence, String predicate, nars.prolog.Term[] args) {
            super(predicate, args);
            
            this.sentence = sentence;
        }
        
        public NARStruct(String predicate, nars.prolog.Term... args) {
            this(null, predicate, args);
        }

        public Sentence getSentence() {
            return sentence;
        }

        public void setSentence(Sentence sentence) {
            this.sentence = sentence;
        }
        
        
    }

    
    public static void main(String[] args) throws Exception {
        NAR nar = new DefaultNARBuilder().build();
        new TextOutput(nar, System.out);
        
        NARProlog prolog = new NARProlog(nar);
        
        new NARPrologMirror(nar, prolog);
        
        
        /*
        nar.addInput("<a <-> b>.");        
        nar.addInput("<a <-> b>?");
        nar.finish(3);        
        prolog.solve("similarity(a,D).");
        */
        
        nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
        nar.finish(3);
        
        
    }    

    
    
}
