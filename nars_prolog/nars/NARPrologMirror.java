package nars;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.AbstractObserver;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.language.CompoundTerm;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Negation;
import nars.language.Similarity;
import nars.language.Statement;
import nars.language.Tense;
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
public class NARPrologMirror extends AbstractObserver {

    private final NAR nar;
    private final NARProlog prolog;
    
    private float trueThreshold = 0.75f;
    private float falseThreshold = 0.25f;
    private float confidenceThreshold = 0.75f;
    private final Set<Term> recent = new HashSet();
    private final Map<Struct,Sentence> beliefs = new HashMap();
    
    /** whether to insert directly into memory, bypassing input ports */
    private boolean inputDirect = true;
    
    private boolean eternalJudgments = true;
    private boolean presentJudgments = false;
    
    public static final Class[] events = { Events.ConceptBeliefAdd.class, Events.ConceptQuestionAdd.class, OUT.class, IN.class };
    
    public NARPrologMirror(NAR nar, NARProlog prolog) {
        super(nar, true, events);
        this.nar = nar;
        this.prolog = prolog;    
        
    }
    
    public NARPrologMirror temporal(boolean eternalJudgments, boolean presentJudgments) {
        this.eternalJudgments = eternalJudgments;
        this.presentJudgments = presentJudgments;
        return this;
    }
    
    boolean validTemporal(Sentence s) {        
        long e = s.getOccurenceTime();
        
        if (eternalJudgments && (e == Stamp.ETERNAL))
            return true;
        
        if (presentJudgments) {
            long now = nar.time();
            long durationCycles = nar.param().duration.get();
            if (now - e < durationCycles)
               return true;
        }
        
        return false;
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
                
                if (!validTemporal(s))
                    return;
                
                if (recent.remove(s.content)) {
                    return;
                }
                recent.add(s.content);
                
                
                
                //only interpret input judgments, or any kind of question
                if (s.isJudgment()) {
                    TruthValue tv = s.truth;
                    if (tv.getConfidence() > confidenceThreshold) {
                        if ((tv.getFrequency() > trueThreshold) || (tv.getFrequency() < falseThreshold)) {
                            try {
                                Struct th = newJudgmentTheory(s);
                                if (th!=null) {
                                    
                                    if (tv.getFrequency() < falseThreshold) {
                                        th = negation(th);
                                    }
                                    
                                    if (beliefs.putIfAbsent(th, s)==null)
                                        System.err.println("Prolog assume: " + th + " | " + s);
                                    
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
                            System.out.println("Prolog question: " + s.toString() + " | " + qh.toString() + " ?");                            
                            prolog.clearTheory();
                                                        
                            Theory theory, axioms;
                            prolog.setTheory(theory = getTheory(beliefs));
                            prolog.addTheory(axioms = getAxioms());
                            
                            System.out.println("  Theory: " + theory);
                            //System.out.println("  Axioms: " + axioms);
                            
                            
                            double maxSolveTimeSeconds = 5.0 / 1e3; //5ms
                            
                            SolveInfo si = prolog.solve(qh, maxSolveTimeSeconds);
                            
                            do {
                                if (si == null) break;
                                
                                if (!si.isSuccess())
                                    break;
                                
                                
                                nars.prolog.Term solution = si.getSolution();
                                if (solution == null)
                                    break;
                                
                                try {
                                    Term n = nterm(solution);
                                    if (n!=null)
                                        answer(task, n);
                                }
                                catch (Exception e) {
                                    //problem generating a result
                                    e.printStackTrace();
                                }
                                
                                
                                if (prolog.hasOpenAlternatives()) {
                                    maxSolveTimeSeconds /= 2d;
                                    si = prolog.solveNext(maxSolveTimeSeconds);
                                }
                            }                            
                            while (prolog.hasOpenAlternatives());
                            
                        }
                    } catch (InvalidTermException nse) {
                        nar.emit(NARPrologMirror.class, s + " : not supported yet");       
                        nse.printStackTrace();;
                    } catch (NoMoreSolutionException nse) {
                        //normal
                    } catch (Exception ex) {                        
                        nar.emit(ERR.class, ex.toString());
                        ex.printStackTrace();
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
        try {
            s = pterm(judgment.content);
        }
        catch (Exception e) {
            e.printStackTrace();;
            return null;
        }
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
    
    public String pescape(String p) {
        return p.replaceAll("\\$", "_d");
    }
            
    //NARS term -> Prolog term
    public nars.prolog.Term pterm(final Term term) {
        
        //CharSequence s = termString(term);
        if (term instanceof Statement) {
            Statement i = (Statement)term;
            String predicate = i.getClass().getSimpleName().toLowerCase();
            nars.prolog.Term subj = pterm(i.getSubject());
            nars.prolog.Term obj = pterm(i.getPredicate());
            if ((subj!=null) && (obj!=null))
                return new Struct(predicate, subj, obj);
        }
        else if (term instanceof Negation) {
            return new Struct("negation", pterm(((Negation)term).term[0]));
        }
        else if (term.getClass().equals(Variable.class)) {
            return new Var("V" + pescape(term.name().toString()));
        }
        else if (term.getClass().equals(Term.class)) {
            return new Struct(pescape(term.name().toString()));
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
                        case "equivalence":
                            return Equivalence.make(a, b);
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
    
    public Task getBeliefTask(Term t, Task parentTask) {
        float freq = 1.0f;
        float conf = Parameters.DEFAULT_JUDGMENT_CONFIDENCE;
        float priority = Parameters.DEFAULT_JUDGMENT_PRIORITY;
        float durability = Parameters.DEFAULT_JUDGMENT_DURABILITY;
        return nar.memory.newTask(t, '.', freq, conf, priority, durability, parentTask,
                this.presentJudgments ? Tense.Present : null);        
    }
    
    /** reflect a result to NARS, and remember it so that it doesn't get reprocessed here later */
    public void answer(Task question, Term t) {
        System.err.println("Prolog answer: " + t);
        
        if (inputDirect) {
            nar.memory.inputTask(getBeliefTask(t, question));
        }
        else {
            //TODO avoid using String input
            nar.addInput(t.toString() + ".");
        }
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
    

    private static Theory axioms;
    
    private Theory getAxioms() {
        if (axioms==null) {
            try {
                axioms = new Theory(
                    "inheritance(A, C):- inheritance(A,B),inheritance(B,C)." + '\n' +
                    "implication(A, C):- implication(A,B),implication(B,C)." + '\n' +
                    "similarity(A, B):- similarity(B,A)." + '\n' +
                    "similarity(A, B):- inheritance(A,B),inheritance(B,A)." + '\n' +
                    "A:- not(not(A))." + '\n'
                );
            } catch (InvalidTheoryException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return axioms;
    }
    
    public Theory getTheory(Map<Struct, Sentence> beliefMap) throws InvalidTheoryException  {
        return new Theory(new Struct(beliefMap.keySet().toArray(new Struct[beliefMap.size()])));
    }

    
    
}

