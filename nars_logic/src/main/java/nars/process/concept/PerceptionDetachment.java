package nars.process.concept;

import nars.Memory;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.process.ConceptProcess;
import nars.task.Task;

/**
 * Created by me on 6/1/15.
 */
public class PerceptionDetachment extends ConceptFireTaskTerm {

   @Override
   public boolean apply(ConceptProcess nal, TaskLink taskLink, TermLink termLink) {
       final Memory memory = nal.memory;
       final Task task = nal.getCurrentTask();

       return true;
   }



    //TODO

//        //only if the premise task is a =/>
//
//        //the following code is for:
//        //<(&/,<a --> b>,<b --> c>,<x --> y>,pick(a)) =/> <goal --> reached>>.
//        //(&/,<a --> b>,<b --> c>,<x --> y>). :|:
//        // |-
//        //<pick(a) =/> <goal --> reached>>. :|:
//        //https://groups.google.com/forum/#!topic/open-nars/8VVscfLQ034
//        //<(&/,<a --> b>,<$1 --> c>,<x --> y>,pick(a)) =/> <$1 --> reached>>.
//        //(&/,<a --> b>,<goal --> c>,<x --> y>). :|:
//        //|-
//        //<pick(a) =/> <goal --> reached>>.
//        if(task.sentence.term instanceof Implication &&
//                (((Implication)task.sentence.term).getTemporalOrder()== ORDER_FORWARD ||
//                        ((Implication)task.sentence.term).getTemporalOrder()==ORDER_CONCURRENT)) {
//            Implication imp=(Implication)task.sentence.term;
//            if(imp.getSubject() instanceof Conjunction &&
//                    ((((Conjunction)imp.getSubject()).getTemporalOrder()==ORDER_FORWARD) ||
//                            (((Conjunction)imp.getSubject()).getTemporalOrder()==ORDER_CONCURRENT))) {
//                Conjunction conj=(Conjunction)imp.getSubject();
//
//                for(int i=0;i< nars.plugin.input.PerceptionAccel.PERCEPTION_DECISION_ACCEL_SAMPLES;i++) {
//
//                    //prevent duplicate derivations
//                    Set<Term> alreadyInducted = new HashSet();
//
//                    Concept next=nal.memory.sampleNextConceptNovel(task.sentence);
//
//                    if (next == null) continue;
//
//                    Term t = next.getTerm();
//
//                    Sentence s=null;
//                    if(task.sentence.punctuation== Symbols.JUDGMENT && next.hasBeliefs()) {
//                        s = next.getStrongestBelief().sentence;
//                    }
//                    if(task.sentence.punctuation==Symbols.GOAL && next.hasGoals()) {
//                        s = next.getStrongestGoal(true,true).sentence;
//                    }
//
//                    if (s!=null && !alreadyInducted.contains(t) && (t instanceof Conjunction)) {
//                        alreadyInducted.add(t);
//                        Conjunction conj2=(Conjunction) t; //ok check if it is a right conjunction
//                        if(conj.getTemporalOrder()==conj2.getTemporalOrder()) {
//                            //conj2 conjunction has to be a minor of conj
//                            //the case where its equal is already handled by other inference rule
//                            if(conj2.term.length<conj.term.length) {
//                                boolean equal=true;
//                                HashMap<Term,Term> map=new HashMap<Term,Term>();
//                                HashMap<Term,Term> map2=new HashMap<Term,Term>();
//                                for(int j=0;j<conj2.term.length;j++) //ok now check if it is really a minor
//                                {
//                                    if(!Variables.findSubstitute(Symbols.VAR_INDEPENDENT, conj.term[j], conj2.term[j], map, map2, memory)) {
//                                        equal=false;
//                                        break;
//                                    }
//                                }
//                                if(equal) {
//                                    //ok its a minor, we have to construct the residue implication now
//
//                                    ///SPECIAL REASONING CONTEXT FOR TEMPORAL INDUCTION
//                                    Stamp SVSTamp=nal.getNewStamp();
//                                    Sentence SVBelief=nal.getCurrentBelief();
//                                    NAL.StampBuilder SVstampBuilder=nal.newStampBuilder;
//                                    //now set the current context:
//                                    nal.setCurrentBelief(s);
//                                    //END
//
//                                    Term[] residue=new Term[conj.term.length-conj2.term.length];
//                                    for(int k=0;k<residue.length;k++) {
//                                        residue[k]=conj.term[conj2.term.length+k];
//                                    }
//                                    Term C=Conjunction.make(residue,conj.getTemporalOrder());
//                                    Implication resImp=Implication.make(C, imp.getPredicate(), imp.getTemporalOrder());
//                                    if(resImp==null) {
//                                        continue;
//                                    }
//                                    resImp=(Implication) resImp.applySubstitute(map);
//                                    //todo add
//                                    Stamp st=new Stamp(task.sentence.stamp,nal.memory.time());
//                                    boolean eternalBelieve=nal.getCurrentBelief().isEternal(); //https://groups.google.com/forum/#!searchin/open-nars/projection/open-nars/8KnAbKzjp4E/rBc-6V5pem8J
//                                    boolean eternalTask=task.sentence.isEternal();
//
//                                    TruthValue BelieveTruth=nal.getCurrentBelief().truth;
//                                    TruthValue TaskTruth=task.sentence.truth;
//
//                                    if(eternalBelieve && !eternalTask) { //occurence time of task
//                                        st.setOccurrenceTime(task.sentence.getOccurenceTime());
//                                    }
//
//                                    if(!eternalBelieve && eternalTask) { //eternalize case
//                                        BelieveTruth= TruthFunctions.eternalize(BelieveTruth);
//                                    }
//
//                                    if(!eternalBelieve && !eternalTask) { //project believe to task
//                                        BelieveTruth=nal.getCurrentBelief().projectionTruth(task.sentence.getOccurenceTime(), memory.time());
//                                    }
//
//                                    //we also need to add one to stamp... time to think about redoing this for 1.6.3 in a more clever way..
//                                    ArrayList<Long> evBase=new ArrayList<Long>();
//                                    for(long l: st.evidentialBase) {
//                                        if(!evBase.contains(l)) {
//                                            evBase.add(l);
//                                        }
//                                    }
//                                    for(long l: nal.getCurrentBelief().stamp.evidentialBase) {
//                                        if(!evBase.contains(l)) {
//                                            evBase.add(l);
//                                        }
//                                    }
//                                    long[] evB=new long[evBase.size()];
//                                    int u=0;
//                                    for(long l : evBase) {
//                                        evB[i]=l;
//                                        u++;
//                                    }
//
//                                    st.evidentialBase=evB;
//                                    st.baseLength=evB.length;
//                                    TruthValue truth=TruthFunctions.deduction(BelieveTruth, TaskTruth);
//
//
//                                    Sentence S=new Sentence(resImp,s.punctuation,truth,st);
//                                    Task Tas=new Task(S,new BudgetValue(BudgetFunctions.forward(truth, nal)));
//                                    nal.derivedTask(Tas, false, false, null, null, true);
//
//                                    //RESTORE CONTEXT
//                                    nal.setNewStamp(SVSTamp);
//                                    nal.setCurrentBelief(SVBelief);
//                                    nal.newStampBuilder=SVstampBuilder; //also restore this one
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//    }



}
