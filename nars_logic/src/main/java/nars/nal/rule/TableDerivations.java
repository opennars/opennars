package nars.nal.rule;

import nars.nal.*;
import nars.nal.term.Statement;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.nal1.Negation;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal5.SyllogisticRules;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import static nars.Symbols.VAR_INDEPENDENT;
import static nars.nal.RuleTables.goalFromQuestion;


public class TableDerivations extends ConceptFireTaskTerm {

    private static void perceptionBasedDetachment(final Task task, final NAL nal, final Memory memory) {
        //only if the premise task is a =/>
        
        //the following code is for:
        //<(&/,<a --> b>,<b --> c>,<x --> y>,pick(a)) =/> <goal --> reached>>.
        //(&/,<a --> b>,<b --> c>,<x --> y>). :|:
        // |-
        //<pick(a) =/> <goal --> reached>>. :|:
        //https://groups.google.com/forum/#!topic/open-nars/8VVscfLQ034
        //<(&/,<a --> b>,<$1 --> c>,<x --> y>,pick(a)) =/> <$1 --> reached>>.
        //(&/,<a --> b>,<goal --> c>,<x --> y>). :|:
        //|-
        //<pick(a) =/> <goal --> reached>>.
        if(task.sentence.term instanceof Implication &&
                (((Implication)task.sentence.term).getTemporalOrder()==ORDER_FORWARD ||
                ((Implication)task.sentence.term).getTemporalOrder()==ORDER_CONCURRENT)) {
            Implication imp=(Implication)task.sentence.term;
            if(imp.getSubject() instanceof Conjunction &&
                    ((((Conjunction)imp.getSubject()).getTemporalOrder()==ORDER_FORWARD) ||
                    (((Conjunction)imp.getSubject()).getTemporalOrder()==ORDER_CONCURRENT))) {
                Conjunction conj=(Conjunction)imp.getSubject();
                for(int i=0;i<PerceptionAccel.PERCEPTION_DECISION_ACCEL_SAMPLES;i++) {
                    
                    //prevent duplicate derivations
                    Set<Term> alreadyInducted = new HashSet();
                    
                    Concept next=nal.memory.sampleNextConceptNovel(task.sentence);
                    
                    if (next == null) continue;
                    
                    Term t = next.getTerm();
                    
                    Sentence s=null;
                    if(task.sentence.punctuation==Symbols.JUDGMENT_MARK && !next.beliefs.isEmpty()) {
                        s=next.beliefs.get(0).sentence;
                    }
                    if(task.sentence.punctuation==Symbols.GOAL_MARK && !next.desires.isEmpty()) {
                        s=next.desires.get(0).sentence;
                    }
                    
                    if (s!=null && !alreadyInducted.contains(t) && (t instanceof Conjunction)) {
                        alreadyInducted.add(t);
                        Conjunction conj2=(Conjunction) t; //ok check if it is a right conjunction
                        if(conj.getTemporalOrder()==conj2.getTemporalOrder()) {
                            //conj2 conjunction has to be a minor of conj
                            //the case where its equal is already handled by other inference rule
                            if(conj2.term.length<conj.term.length) {
                                boolean equal=true;
                                HashMap<Term,Term> map=new HashMap<Term,Term>();
                                HashMap<Term,Term> map2=new HashMap<Term,Term>();
                                for(int j=0;j<conj2.term.length;j++) //ok now check if it is really a minor
                                {
                                    if(!Variables.findSubstitute(VAR_INDEPENDENT, conj.term[j], conj2.term[j], map, map2)) {
                                        equal=false;
                                        break;
                                    }
                                }
                                if(equal) {
                                    //ok its a minor, we have to construct the residue implication now
                                    
                                    ///SPECIAL REASONING CONTEXT FOR TEMPORAL INDUCTION
                                    Stamp SVSTamp=nal.getNewStamp();
                                    Sentence SVBelief=nal.getCurrentBelief();
                                    NAL.StampBuilder SVstampBuilder=nal.newStampBuilder;
                                    //now set the current context:
                                    nal.setCurrentBelief(s);
                                    //END
                                    
                                    Term[] residue=new Term[conj.term.length-conj2.term.length];
                                    for(int k=0;k<residue.length;k++) {
                                        residue[k]=conj.term[conj2.term.length+k];
                                    }
                                    Term C=Conjunction.make(residue,conj.getTemporalOrder());
                                    Implication resImp=Implication.make(C, imp.getPredicate(), imp.getTemporalOrder());
                                    if(resImp==null) {
                                        continue;
                                    }
                                    resImp=(Implication) resImp.applySubstitute(map);
                                    //todo add
                                    Stamp st=new Stamp(task.sentence.stamp,nal.memory.time());
                                    boolean eternalBelieve=nal.getCurrentBelief().isEternal(); //https://groups.google.com/forum/#!searchin/open-nars/projection/open-nars/8KnAbKzjp4E/rBc-6V5pem8J
                                    boolean eternalTask=task.sentence.isEternal();
                                    
                                    TruthValue BelieveTruth=nal.getCurrentBelief().truth;
                                    TruthValue TaskTruth=task.sentence.truth;
                                    
                                    if(eternalBelieve && !eternalTask) { //occurence time of task
                                        st.setOccurrenceTime(task.sentence.getOccurenceTime());
                                    }
                                    
                                    if(!eternalBelieve && eternalTask) { //eternalize case
                                        BelieveTruth=TruthFunctions.eternalize(BelieveTruth);
                                    }
                                    
                                    if(!eternalBelieve && !eternalTask) { //project believe to task
                                        BelieveTruth=nal.getCurrentBelief().projectionTruth(task.sentence.getOccurenceTime(), memory.time());
                                    }
                                    
                                    //we also need to add one to stamp... time to think about redoing this for 1.6.3 in a more clever way..
                                    ArrayList<Long> evBase=new ArrayList<Long>();
                                    for(long l: st.evidentialBase) {
                                        if(!evBase.contains(l)) {
                                            evBase.add(l);
                                        }
                                    }
                                    for(long l: nal.getCurrentBelief().stamp.evidentialBase) {
                                        if(!evBase.contains(l)) {
                                            evBase.add(l);
                                        }
                                    }
                                    long[] evB=new long[evBase.size()];
                                    int u=0;
                                    for(long l : evBase) {
                                        evB[i]=l;
                                        u++;
                                    }
                                    
                                    st.evidentialBase=evB;
                                    st.baseLength=evB.length;
                                    TruthValue truth=TruthFunctions.deduction(BelieveTruth, TaskTruth);
                                    
                                    
                                    Sentence S=new Sentence(resImp,s.punctuation,truth,st);
                                    Task Tas=new Task(S,new BudgetValue(BudgetFunctions.forward(truth, nal)));
                                    nal.derivedTask(Tas, false, false, null, null, true);
                                    
                                    //RESTORE CONTEXT
                                    nal.setNewStamp(SVSTamp);
                                    nal.setCurrentBelief(SVBelief);
                                    nal.newStampBuilder=SVstampBuilder; //also restore this one
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean apply(final ConceptProcess f, final TaskLink tLink, final TermLink bLink) {

        memory.emotion.manageBusy(f);
        final Sentence taskSentence = tLink.getSentence();
        final Term taskTerm = tLink.getTerm();
        final Sentence belief = f.getCurrentBelief();
        final Term beliefTerm = bLink.getTerm();
        
        perceptionBasedDetachment(task, nal, memory);
        temporalInduce(nal, task, taskSentence, memory);

        final short tIndex = tLink.getIndex(0);
        short bIndex = bLink.getIndex(0);
        switch (tLink.type) {          // dispatch first by TaskLink type
            case TermLink.SELF:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        RuleTables.compoundAndSelf((Compound) taskTerm, beliefTerm, true, bIndex, f);
                        break;
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndSelf((Compound) beliefTerm, taskTerm, false, bIndex, f);
                        break;
                    case TermLink.COMPONENT_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Statement) {
                                SyllogisticRules.detachment(taskSentence, belief, bIndex, f);
                            }
                        } else {
                            goalFromQuestion(f.getCurrentTask(), taskTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (belief.getTerm() instanceof Statement)
                                SyllogisticRules.detachment(belief, taskSentence, bIndex, f);
                            /*else {
                                new RuntimeException(belief + " not a statement via termlink " + tLink).printStackTrace();
                            }*/
                        }
                        break;
                    case TermLink.COMPONENT_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) taskTerm, bIndex, beliefTerm, tIndex, f);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if ((belief != null) && (taskTerm instanceof Implication) && (beliefTerm instanceof Implication)) {
                            bIndex = bLink.getIndex(1);
                            SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, tIndex, f);
                        }
                        break;
                    default:
                        none(tLink, bLink); break;
                }
                break;
            case TermLink.COMPOUND:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        RuleTables.compoundAndCompound((Compound) taskTerm, (Compound) beliefTerm, bIndex, f);
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        RuleTables.compoundAndStatement((Compound) taskTerm, tIndex, (Statement) beliefTerm, bIndex, beliefTerm, f);
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            if (beliefTerm instanceof Implication) {
                                Term[] u = new Term[] { beliefTerm, taskTerm };
                                if (Variables.unify(VAR_INDEPENDENT, ((Statement) beliefTerm).getSubject(), taskTerm, u, f.memory.random)) {
                                    Sentence<Statement> newBelief = belief.clone(u[0], Statement.class);
                                    if (newBelief!=null) {
                                        Sentence newTaskSentence = taskSentence.clone(u[1]);
                                        if (newTaskSentence!=null) {
                                            RuleTables.detachmentWithVar(newBelief, newTaskSentence, bIndex, f);
                                        }
                                    }
                                } else {
                                    SyllogisticRules.conditionalDedInd((Implication) beliefTerm, bIndex, taskTerm, -1, f);
                                }

                            } else if (beliefTerm instanceof Equivalence) {
                                SyllogisticRules.conditionalAna((Equivalence) beliefTerm, bIndex, taskTerm, -1, f);
                            }
                        }
                        break;
                    default:
                        none(tLink, bLink); break;
                }
                break;
            case TermLink.COMPOUND_STATEMENT:
                switch (bLink.type) {
                    case TermLink.COMPONENT:
                        if (taskTerm instanceof Statement) {
                            RuleTables.componentAndStatement((Compound) f.getCurrentTerm(), bIndex, (Statement) taskTerm, tIndex, f);
                        }
                        break;
                    case TermLink.COMPOUND:
                        if (taskTerm instanceof Statement) {
                            RuleTables.compoundAndStatement((Compound) beliefTerm, bIndex, (Statement) taskTerm, tIndex, beliefTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            RuleTables.syllogisms(tLink, bLink, taskTerm, beliefTerm, f);
                        }
                        break;
                    case TermLink.COMPOUND_CONDITION:
                        if (belief != null) {
                            bIndex = bLink.getIndex(1);
                            if ((taskTerm instanceof Statement) && (beliefTerm instanceof Implication)) {

                                RuleTables.conditionalDedIndWithVar((Implication) beliefTerm, bIndex, (Statement) taskTerm, tIndex, f);
                            }
                        }
                        break;
                    default:
                        none(tLink, bLink); break;
                }
                break;
            case TermLink.COMPOUND_CONDITION:
                switch (bLink.type) {
                    case TermLink.COMPOUND:
                        if (belief != null) {
                            RuleTables.detachmentWithVar(taskSentence, belief, tIndex, f);
                        }
                        break;

                    case TermLink.COMPOUND_STATEMENT:
                        if (belief != null) {
                            if (taskTerm instanceof Implication) // TODO maybe put instanceof test within conditionalDedIndWithVar()
                            {
                                Term subj = ((Statement) taskTerm).getSubject();
                                if (subj instanceof Negation) {
                                    if (taskSentence.isJudgment()) {
                                        RuleTables.componentAndStatement((Compound) subj, bIndex, (Statement) taskTerm, tIndex, f);
                                    } else {
                                        RuleTables.componentAndStatement((Compound) subj, tIndex, (Statement) beliefTerm, bIndex, f);
                                    }
                                } else {
                                    RuleTables.conditionalDedIndWithVar((Implication) taskTerm, tIndex, (Statement) beliefTerm, bIndex, f);
                                }
                            }
                            break;

                        }
                        break;



                    default:
                        none(tLink, bLink); break;
                }
            default:
                none(tLink); break;
        }

        return true;
    }

    private void none(TaskLink tLink, TermLink bLink) {
        //System.err.println(this + " inactivity: " + tLink + "(" + tLink.type + ") &&& " + bLink + " (" + bLink.type + ")");
    }

    private void none(TaskLink tLink) {
        //System.err.println(this + " inactivity: " + tLink+ "(" + tLink.type + ")");
    }

}
