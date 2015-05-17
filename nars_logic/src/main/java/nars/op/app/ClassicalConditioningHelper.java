/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.app;

import nars.Global;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.stamp.Stamp;
import nars.nal.term.Term;
import nars.util.event.AbstractReaction;
import nars.util.event.Reaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 *
 * @author tc
 */
public class ClassicalConditioningHelper extends AbstractReaction {

    public boolean EnableAutomaticConditioning=true;
    public Reaction obs;
    public float saved_priority;
    public ArrayList<Task> lastElems=new ArrayList<>();
    public int maxlen=20;
    public int conditionAllNSteps=3;
    public int cnt=0; 
    NAR nar;
    
    public static class Tuple {
        public final ArrayList<Task> x; 
        public final double y; 
        public Tuple(ArrayList<Task> x, double y) { 
          this.x = x; 
          this.y = y; 
        } 
      } 
    
    public ArrayList<Task> cutoutAppend(int ind1,int ind2,ArrayList<Task> first,ArrayList<Task> second) {
        ArrayList<Task> res=new ArrayList<>();
        for(int i=ind1;i<first.size()+ind2;i++) {
            res.add(first.get(i));
        }
        for(Task t : second) {
            res.add(t);
        }
        return res;
    }

    public boolean TaskStringContains(ArrayList<Task> w,ArrayList<Task> content) {
        for(int i=0;i<w.size();i++) {
            boolean same=true;
            for(int j=0;j<content.size();j++) {
                if(i+j>=w.size()) {
                    return false;
                }
                if(!w.get(i+j).sentence.term.equals(content.get(j).sentence.term)) {
                    same=false;
                    break;
                }
            }
            if(content.isEmpty()) {
                return false;
            }
            if(same) {
                return true;
            }
        }
        return false;
    }
    
    public int TaskStringEqualSequentialTermCount(ArrayList<Task> w,ArrayList<Task> content) {
        int cnt2=0;
        for(int i=0;i<w.size();i++) {
            boolean same=true;
            for(int j=0;i+j<w.size() && j<content.size();j++) {
                if(!w.get(i+j).sentence.term.equals(content.get(j).sentence.term)) {
                    same=false;
                    break;
                }
            }
            if(content.isEmpty()) {
                return 0;
            }
            if(same) {
                cnt2++;
            }
        }
        return cnt2;
    }
    
    public void classicalConditioning() {
        ArrayList<Task> st=new ArrayList(lastElems.size());
        Task lastt=null;
        for(Task t: lastElems) {
            st.add(t);
            if(lastt!=null && Math.abs(t.sentence.getOccurrenceTime()-lastt.sentence.getOccurrenceTime())>nar.param.duration.get()*100) {
                st.add(null); //pause
            }
            lastt=t;
        }
        
        HashMap<Term,ArrayList<Task>> theoriess=new HashMap<>();
        
        ArrayList<Task> H=new ArrayList<>(); //temporary, recycled use in loop below
        for(int k=0;k<st.size();k++) {
            for(int i=1;i<st.size();i++) {
                Task ev=st.get(i);
                Task lastev=st.get(i-1);
                Concept c=nar.memory.concept(ev.sentence.term);
                if(c!=null && c.isDesired())//desired
                {
                    H.clear();
                    H.add(lastev);
                    H.add(ev);
                    //System.out.println(lastev.sentence.term);
                   // System.out.println(ev.sentence.term);
                    theoriess.put(ev.sentence.term, H);
                    for(int j=i;j<st.size();j++) {
                        Task ev2=st.get(j);
                        Task lastev2=st.get(j-1); //forward conditioning
                        if(lastev2.sentence.term.equals(lastev.sentence.term) && !ev2.sentence.term.equals(ev.sentence.term) &&
                                theoriess.keySet().contains(ev.sentence.term)) {
                            theoriess.remove(ev.sentence.term); //extinction
                        }
                    }
                }
            }
        }
        
        ArrayList<ArrayList<Task>> theories=new ArrayList<>(theoriess.values());
        
        
        for(int i=0;i<2;i++) {
            ArrayList<ArrayList<Task>> theories2=new ArrayList<>(theories);
            for(ArrayList<Task> A : theories) {
                if (A.size() == 1) continue;
                for(ArrayList<Task> B : theories) {
                    if(B.size()==1) continue;
                    
                    while(A.contains(null)) {
                        A.remove(null);
                    }
                    while(B.contains(null)) {
                        B.remove(null);
                    }
                    boolean caseA=A.get(A.size()-1).equals(B.get(0));
                    boolean caseB=A.size()>2 && B.size()>1 && A.get(A.size()-1).equals(B.get(1)) && Objects.equals(A.get(A.size() - 2), B.get(0));
                    
                    if((A.size()>1 && B.size()>1) && (caseA || caseB)) {
                        ArrayList<Task> compoundT=null;
                        if(caseA) {
                            compoundT=cutoutAppend(0,-1,A,B);   
                        }
                        if(caseB) {
                            compoundT=cutoutAppend(0,-2,A,B);   
                        }
                        theories2.add(compoundT);
                    }
                }
            }
            theories=theories2;
        }
        System.out.println("found theories:");
        ArrayList<ArrayList<Task>> filtered=new ArrayList<>(); //Filtered=[a for a in list(set([a.replace(" ","") for a in theories])) if len(a)>1]
        for(ArrayList<Task> t: theories) {
            if(t.size()>1) {
                //check if its not included:
                boolean included=false;
                for(ArrayList<Task> fil : filtered) {
                    if(fil==null) {
                        continue;
                    }
                    if(fil.size()!=t.size()) {
                        continue;
                    }
                    else {
                        boolean same=true;
                        for(int i=0;i<t.size();i++) {
                            if(!(t.get(i).sentence.term.equals(fil.get(i).sentence.term))) { //we need term equals this is why
                                same=false;
                                break;
                            }
                        }
                        if(same) {
                            included=true;
                        }
                    }
                }
                if(!included) {
                   filtered.add(t);
                }
            }
        }
        ArrayList<Tuple> Ret=new ArrayList<>();
        for(ArrayList<Task> a: filtered) {
            Ret.add(new Tuple(a, TaskStringEqualSequentialTermCount(st, a) * a.size()));
        }
        ArrayList<Tuple> ToRemove=new ArrayList<Tuple>();
        for(Tuple T : Ret) {
            ArrayList<Task> a=T.x;
            double b=T.y;
            for(Tuple D : Ret) {
                ArrayList<Task> c=D.x;
                double d=D.y;
                if(!Objects.equals(a, c) && TaskStringContains(c,a) && d>=b) {
                    //Ret.remove(T);
                    ToRemove.add(T);
                }
            }
        }
        
        Ret.removeAll(ToRemove);
        
        double max=0;
        for(Tuple T : Ret) {
            if(T.y>max) {
                max=T.y;
            }
        }
        
        for(Tuple T : Ret) {
            if(T.y==max) {
                Truth Truth=null;
                Term[] wuh=new Term[T.x.size()-1];
                Term pred=null;
                int i=0;
                for(Task t : T.x) {
                    if(i==T.x.size()-1) {
                        pred=t.sentence.term;
                    } else {
                        wuh[i]=t.sentence.term;
                    }
                    i++;
                    if(Truth==null) {
                        Truth=t.sentence.truth;
                    } else {
                        Truth=TruthFunctions.induction(Truth,t.sentence.truth);
                    }
                }
              //  Concept c=nar.memory.concept(pred);
                //if(c==null || !c.isDesired()) {
               //     return;
               // }
                Conjunction con=(Conjunction) Conjunction.make(wuh, TemporalRules.ORDER_FORWARD);
                Implication res=Implication.make(con, pred,TemporalRules.ORDER_FORWARD);
                Stamp stamp=new Stamp(nar.memory, Stamp.ETERNAL);
                Sentence s=new Sentence(res,Symbols.JUDGMENT,Truth,stamp);

                Task TT= new Task(s, new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY,

                        BudgetFunctions.truthToQuality(Truth)), lastElems.get(lastElems.size() - 1), null);
                nar.input(TT);

                break;
            }
        }
    }

    public void HandleInput(Task task) {
        if(!enabled) {
            return;
        }
        if(!task.isInput()) {
            return;
        }
        if(task.sentence.stamp.getOccurrenceTime()!=Stamp.ETERNAL && task.sentence.punctuation==Symbols.JUDGMENT) {
            lastElems.add(task);
            if(lastElems.size()>maxlen) {
                lastElems.remove(0);
            }
            if(cnt%conditionAllNSteps==0 && EnableAutomaticConditioning) {
                classicalConditioning();
            }
            cnt++;
        }
    }
    
    boolean enabled=false;


    @Override
    public Class[] getEvents() {
        return new Class[] { DirectProcess.class };
    }


//    @Override
//    public boolean setEnabled(NAR n, boolean enabled) {
//
//        this.enabled=enabled;
//        this.nar=n;
//        if(obs==null) {
//            saved_priority= Global.DEFAULT_JUDGMENT_PRIORITY;
//            obs=new Reaction() {
//
//
//            };
//        }
//
//
//        if(enabled) {
//            if (enabled)
//                lastElems.clear();
//            Global.DEFAULT_JUDGMENT_PRIORITY= 0.01f;
//        } else {
//            Global.DEFAULT_JUDGMENT_PRIORITY=saved_priority;
//        }
//
//
//        n.memory.event.set(obs, enabled, DirectProcess.class);
//        return true;
//    }

    @Override
    public void event(Class event, Object[] a) {
        //is not working, keep for later:
        if (event!=DirectProcess.class)
            return;
        Task task = (Task)a[0];
        HandleInput(task);


    }
}
