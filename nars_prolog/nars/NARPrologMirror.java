package nars;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nars.core.Events;
import nars.core.Events.ConceptBeliefAdd;
import nars.core.Events.ConceptBeliefRemove;
import nars.core.Events.Solved;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.AbstractObserver;
import nars.inference.TemporalRules;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.io.Texts;
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
 * Causes a NARProlog to mirror certain activity of a NAR.  It generates
 * prolog terms from NARS beliefs, and answers NARS questions with the results
 * of a prolog solution (converted to NARS terms), which are input to NARS memory
 * with the hope that this is sooner than NARS can solve it by itself. 
 */
public class NARPrologMirror extends AbstractObserver {

    private final NAR nar;
    private final NARProlog prolog;
    
    private float trueThreshold = 0.75f;
    private float falseThreshold = 0.25f;
    private float confidenceThreshold;
    private final Map<Sentence,nars.prolog.Term> beliefs = new HashMap();
        
    private boolean eternalJudgments = true;
    private boolean presentJudgments = false;

    /** how much to scale the memory's duration parameter for this reasoner's "now" duration; default=1.0 */
    float durationMultiplier = 1.0f;
    
    /** how often to remove temporally irrelevant beliefs */
    float durationDivider = 4f;
    
    /** in seconds */
    float maxSolveTime = 5.0f / 1e3f; //5ms
    float baseSolveTime = 1.0f / 1e3f; //1ms

    /** max # answers returned in response to a question */
    int maxAnswers = 3;
    
    boolean reportAssumptions = false;
    boolean reportForgets = reportAssumptions;
    
    
    public static final Class[] telepathicEvents = { Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class, Events.ConceptQuestionAdd.class, IN.class, OUT.class, Solved.class };
    
    public static final Class[] inputOutputEvents = { IN.class, OUT.class };
    private long lastFlush;
    private int durationCycles;
    
    boolean allTerms = false;
    
    public NARPrologMirror(NAR nar, float minConfidence, boolean telepathic) {
        super(nar, true, telepathic ? telepathicEvents : inputOutputEvents );
        this.nar = nar;
        this.confidenceThreshold = minConfidence;
        this.prolog = new NARProlog(nar);                
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
            if (TemporalRules.concurrent(now, e, (int)(durationCycles * durationMultiplier)))
               return true;
        }
        
        return false;
    }
   
    protected boolean forget(Sentence belief) {
        if (beliefs.remove(belief)!=null) {
            if (reportForgets) {
                System.err.println("Prolog forget: " + belief);                
            }
            return true;
        }
        return false;
    }
    
    protected void updateBeliefs() {
        if (presentJudgments) {
            long now = nar.time();
            durationCycles = (nar.param).duration.get();
            if (now - lastFlush > (long)(durationCycles/ durationDivider) ) {
                
                Set<Sentence> toRemove = new HashSet();
                for (Sentence s : beliefs.keySet()) {
                    if (!validTemporal(s)) {
                        toRemove.add(s);
                    }
                }
                for (Sentence s : toRemove) {                    
                    forget(s);
                }
                
                lastFlush = now;
            }                
        }
    }
    
    @Override
    public void event(final Class channel, final Object... arg) {        
        
        if (channel == ConceptBeliefAdd.class) {
            Concept c = (Concept)arg[0];
            Sentence s = (Sentence)arg[1];
            Task task = (Task)arg[2];
            add(s, task);            
        }
        else if (channel == ConceptBeliefRemove.class) {
            Concept c = (Concept)arg[0];
            Sentence s = (Sentence)arg[1];
            Task task = (Task)arg[2];
            remove(s, task);
        }        
        else if (channel == Events.ConceptQuestionAdd.class) {
            Concept c = (Concept)arg[0];            
            Task task = (Task)arg[1];
            add(task.sentence, task);
        }        
        else if ((channel == IN.class) || (channel == OUT.class)) {
            Object o = arg[0];
            if (o instanceof Task) {
                Task task = (Task)o;
                Sentence s = task.sentence;
                
                add(s, task);
            }
        }
    }
    
    protected void remove(Sentence s, Task task) {
        //TODO
    }
    
    protected void add(Sentence s, Task task) {
        
        if (!(s.content instanceof CompoundTerm))
            return;        

        if (!validTemporal(s))
            return;

        updateBeliefs();
        
        //only interpret input judgments, or any kind of question
        if (s.isJudgment()) {

            processBelief(s, task, true);
        }
        else if (s.isQuestion()) {

            //System.err.println("question: " + s);
            
            float priority = task.getPriority();
            float solveTime = ((maxSolveTime-baseSolveTime) * priority) + baseSolveTime;

            if (beliefs.containsKey(s)) {
                //already determined it to be true
                answer(task, s.content, null);
                return;
            }
            
            try {
                Struct qh = newQuestion(s);
                
                if (qh!=null) {
                    //System.out.println("Prolog question: " + s.toString() + " | " + qh.toString() + " ? (" + Texts.n2(priority) + ")");    
                    

                    Theory theory;
                    
                    try {
                        prolog.setTheory(theory = getTheory(beliefs));
                        prolog.addTheory(getAxioms().iterator());
                    }
                    catch (InvalidTheoryException e) {
                        nar.memory.emit(ERR.class, e);
                        return;
                    }

                    //System.out.println("  Theory: " + theory);
                    //System.out.println("  Axioms: " + axioms);
                    

                    SolveInfo si = prolog.solve(qh, solveTime);

                    int answers = 0;
                    
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
                                answer(task, n, solution);
                        }
                        catch (Exception e) {
                            //problem generating a result
                            e.printStackTrace();
                        }


                        if (prolog.hasOpenAlternatives()) {
                            maxSolveTime /= 2d;
                            si = prolog.solveNext(maxSolveTime);
                        }
                    }                            
                    while (prolog.hasOpenAlternatives() && (answers++) < maxAnswers);

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

    protected void processBelief(Sentence s, Task task, boolean addOrRemove) {
            
        TruthValue tv = s.truth;
        if (tv.getConfidence() > confidenceThreshold) {
            if ((tv.getFrequency() > trueThreshold) || (tv.getFrequency() < falseThreshold)) {

                boolean exists = beliefs.containsKey(s.content);
                if ((addOrRemove) && (exists))
                    return;
                else if ((!addOrRemove) && (!exists))
                    return;
                
                try {
                    Struct th = newJudgmentTheory(s);
                    if (th!=null) {

                        if (tv.getFrequency() < falseThreshold) {
                            th = negation(th);
                        }

                        if (addOrRemove) {
                            if (beliefs.putIfAbsent(s, th)==null)
                                if (reportAssumptions)
                                    System.err.println("Prolog assume: " + th + " | " + s);
                        }
                        else {
                            forget(s);
                        }

                    }                                
                } catch (Exception ex) {
                    nar.emit(ERR.class, ex.toString());
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
            nars.prolog.Term np = pterm(((Negation)term).term[0]);
            if (np == null) return null;
            return new Struct("negation", np);
        }
        else if (term.getClass().equals(Variable.class)) {
            return new Var("V" + pescape(term.name().toString()));
        }
        else if (term.getClass().equals(Term.class)) {
            return new Struct(pescape(term.name().toString()));
        }
        else if (term instanceof CompoundTerm) {
            //unhandled type of compound term, store as an atomic string            
            //NOT ready yet
            if (allTerms) {
                return new Struct("_" + pescape(term.name().toString()));
            }
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
                return Term.get(predicate);
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
                            System.err.println("nterm() does not yet support: " + term);
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
    
    public Task getBeliefTask(Sentence question, Term t, Task parentTask) {
        float freq = 1.0f;
        float conf = Parameters.DEFAULT_JUDGMENT_CONFIDENCE;
        float priority = Parameters.DEFAULT_JUDGMENT_PRIORITY;
        float durability = Parameters.DEFAULT_JUDGMENT_DURABILITY;
        return nar.memory.newTask(t, '.', freq, conf, priority, durability, parentTask,
                question.isEternal() ? null : Tense.Present); //TODO may need to adjust tense of answer based on when question was asked
    }
    
    /** reflect a result to NARS, and remember it so that it doesn't get reprocessed here later */
    public void answer(Task question, Term t, nars.prolog.Term pt) {
        //System.err.println("Prolog answer: " + t);
        

        
        Task a = getBeliefTask(question.sentence, t, question);
        nar.memory.inputTask(a);            
        if (pt!=null)
            beliefs.put(question.sentence, pt);
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
    

    private static List<nars.prolog.Term> axioms = null;
    
    private List<? extends nars.prolog.Term> getAxioms() {
        if (axioms==null) {
            try {
                Theory t = new Theory(
                    "inheritance(A, C):- inheritance(A,B),inheritance(B,C)." + '\n' +
                    "implication(A, C):- implication(A,B),implication(B,C)." + '\n' +
                    "similarity(A, B):- similarity(B,A)." + '\n' +
                    "similarity(A, B):- inheritance(A,B),inheritance(B,A)." + '\n' +
                    "A:- not(not(A))." + '\n'
                );
                axioms = Lists.newArrayList( t.iterator(prolog) );
            } catch (InvalidTheoryException ex) {
                ex.printStackTrace();
                System.exit(1);
            }            
        }        
        return axioms;
    }
    
    public Theory getTheory(Map<Sentence, nars.prolog.Term> beliefMap) throws InvalidTheoryException  {
        return new Theory(new Struct(beliefMap.values().toArray(new Struct[beliefMap.size()])));
    }

    
    
}

