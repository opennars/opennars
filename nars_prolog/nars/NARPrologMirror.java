package nars;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.language.CompoundTerm;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Negation;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;
import nars.prolog.InvalidTermException;
import nars.prolog.InvalidTheoryException;
import nars.prolog.NoMoreSolutionException;
import nars.prolog.SolveInfo;
import nars.prolog.Struct;
import nars.prolog.Theory;
import nars.prolog.Var;

/**
 * Causes a NARProlog to mirror certain activity of a NAR
 */
public class NARPrologMirror extends Output {
    private final NAR nar;
    private final NARProlog prolog;
    
    private float trueThreshold = 0.75f;
    private float falseThreshold = 0.25f;
    private float confidenceThreshold = 0.7f;
    private final Set<Term> recent = new HashSet();
    private final Map<Struct,Sentence> beliefs = new HashMap();
    

    public NARPrologMirror(NAR nar, NARProlog prolog) {
        super(nar);
        this.nar = nar;
        this.prolog = prolog;    
    }
   
    @Override
    public void event(final Class channel, final Object... arg) {        
        
        if ((channel == IN.class) || (channel == OUT.class)) {
            Object o = arg[0];
            if (o instanceof Task) {
                Task task = (Task)o;
                Sentence s = task.sentence;
                

                if (!(s.content instanceof CompoundTerm))
                    return;
                
                CompoundTerm ct = (CompoundTerm)s.content;
                
                if (ct.hasVar())
                    return;
                
                if (recent.remove(s.content)) {
                    return;
                }
                recent.add(s.content);
                
                
                //only interpret input judgments, or any kind of question
                if (s.isJudgment() && channel == IN.class) {
                    TruthValue tv = s.truth;
                    if (tv.getConfidence() > confidenceThreshold) {
                        if ((tv.getFrequency() > trueThreshold) || (tv.getFrequency() < falseThreshold)) {
                            try {
                                Struct th = newJudgmentTheory(s);
                                if (th!=null) {
                                    
                                    if (tv.getFrequency() < falseThreshold) {
                                        th = negation(th);
                                    }
                                    
                                    System.err.println("ASSUME: " + th + " <== " + s);
                                    beliefs.put(th, s);
                                }
                            } catch (Exception ex) {
                                nar.emit(ERR.class, ex.toString());
                            }
                        }
                        
                        //TODO handle "negative" frequency somewhere below 0.5
                    }
                }
                else if (s.isQuestion()) {
                    
                    
                    try {
                        Struct qh = newQuestion(s);
                        if (qh!=null) {
                            
                            System.out.println("\n\nQUESTION--------------------------------");
                            System.out.println("Question: " + s.toString() + " ==> " + qh.toString() + " ?");                            
                            prolog.clearTheory();
                            
                            Theory theory;
                            prolog.setTheory(theory = getTheory(beliefs));
                            System.out.println("Theory: " + theory);
                            
                            SolveInfo si = prolog.solve(qh);
                            
                            do {
                                if (si == null) break;
                                
                                if (!si.isSuccess())
                                    break;
                                
                                nars.prolog.Term solution = si.getSolution();
                                
                                try {
                                    Term n = nterm(solution);
                                    if (n!=null)
                                        reflect(n);
                                }
                                catch (Exception e) {
                                    //problem generating a result
                                    System.err.println("  nterm/reflect: " + e);
                                }
                                
                                si = prolog.solveNext();
                            }
                            while (prolog.hasOpenAlternatives());
                        }
                    } catch (InvalidTermException nse) {
                        nar.emit(NARPrologMirror.class, s + " : not supported yet");       
                    } catch (NoMoreSolutionException nse) {
                        //normal
                    } catch (Exception ex) {                        
                        nar.emit(ERR.class, ex.toString());
                    }
                }

            }
        }
    }
    
    /** creates a theory from a judgment Statement */
    Struct newJudgmentTheory(final Sentence judgment) throws InvalidTheoryException {
        
        nars.prolog.Term s;
        /*if (judgment.truth!=null) {            
            s = pInfer(pterm(judgment.content), judgment.truth);
        }
        else {*/
            s = pterm(judgment.content);
        //}
        
        return (Struct) s;            
    }
    
    Struct newQuestion(final Sentence question) {
        nars.prolog.Term s = pterm(question.content);
        //TODO not working yet
        return (Struct) s;
    }

    //NOT yet working
    public Struct pInfer(nars.prolog.Term t, TruthValue tv) {
        double freq = tv.getFrequency();
        double conf = tv.getConfidence();
        Struct lt = new Struct(new nars.prolog.Term[] { t, 
            new Struct( new nars.prolog.Term[] { 
                new nars.prolog.Double(freq), 
                new nars.prolog.Double(conf) 
            }) 
        });        
        return new Struct("infer", lt);
    }
    
    public Struct negation(nars.prolog.Term t) {
        return new Struct("negation", t);
    }
    
    //NARS term -> Prolog term
    public nars.prolog.Term pterm(final Term term) {
        
        //CharSequence s = termString(term);
        if (term instanceof Statement) {
            Statement i = (Statement)term;
            String predicate = i.getClass().getSimpleName().toLowerCase();
            return new Struct(predicate, pterm(i.getSubject()), pterm(i.getPredicate()));
        }
        else if (term instanceof Negation) {
            return new Struct("negation", pterm(((Negation)term).term[0]));
        }
        else if (term.getClass().equals(Variable.class)) {
            return new Var("V" + term.name().toString());
        }
        else if (term.getClass().equals(Term.class)) {
            return new Struct(term.name().toString());
        }
        
        return null;        
    }
    
    /** Prolog term --> NARS statement */
    public Term nterm(final nars.prolog.Term term) {
        Memory mem = nar.memory;
        
        if (term instanceof Struct) {
            Struct s = (Struct)term;
            int arity = s.getArity();
            String predicate = s.name().toString();
            if (arity == 0) {
                return new Term(predicate);
            }
            else if (arity == 1) {
                switch (predicate) {
                    case "negation":
                        return Negation.make(nterm(s.getArg(0)));
                }
            }
            else if (arity == 2) {                
                Term a = nterm(s.getArg(0));
                Term b = nterm(s.getArg(1));
                if ((a!=null) && (b!=null)) {
                    switch (predicate) {
                        case "inheritance":
                            return Inheritance.make(a, b);
                        case "similarity":
                            return Similarity.make(a, b);
                        case "implication":
                            return Implication.make(a, b);
                        //TODO more types
                        default:
                            System.err.println("nterm() does not yet support: " + predicate);
                    }
                }
            }
        }
        else if (term instanceof Var) {
            Var v = (Var)term;
            nars.prolog.Term t = v.getTerm();
            if (t!=v) {
                System.out.println("Bound: " + v + " + -> " + t + " " + nterm(t));
                return nterm(t);
            }
            else {
                System.out.println("Unbound: " + v);
                //unbound variable, is there anything we can do with it?
                return null;
            }
        }
        else if (term instanceof nars.prolog.Number) {
            nars.prolog.Number n = (nars.prolog.Number)term;
            return new Term('"' + String.valueOf(n.doubleValue()) + '"');
        }
        
        return null;
    }
    
    /** reflect a result to NARS, and remember it so that it doesn't get reprocessed here later */
    public void reflect(Term t) {
        System.err.println("Answer: " + t);
        
        //TODO avoid using String input
        nar.addInput(t.toString() + ".");
    }

    /*
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
    */
    
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
        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
        //nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
        nar.finish(10);
        
        
    }    

    public static Theory getTheory(Map<Struct, Sentence> beliefs) throws InvalidTheoryException  {
        return new Theory(new Struct(beliefs.keySet().toArray(new Struct[beliefs.size()])));
    }

    
    
}

