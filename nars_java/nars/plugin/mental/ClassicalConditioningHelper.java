/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.plugin.mental;

import java.util.ArrayList;
import java.util.HashMap;
import nars.core.EventEmitter;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Term;

/**
 *
 * @author tc
 */
public class ClassicalConditioningHelper implements Plugin {

    public EventEmitter.EventObserver obs;
    public float saved_priority;
    public ArrayList<Task> lastElems=new ArrayList<>();
    public int maxlen=20;
    public int conditionAllNSteps=5;
    public int cnt=0; 
    NAR nar;
    
    public class Tuple { 
        public final ArrayList<Task> x; 
        public final double y; 
        public Tuple(ArrayList<Task> x, double y) { 
          this.x = x; 
          this.y = y; 
        } 
      } 
    
    public ArrayList<Task> cutoutAppend(int ind1,int ind2,ArrayList<Task> first,ArrayList<Task> second) {
        ArrayList<Task> res=new ArrayList<>();
        for(int i=ind1;i<=first.size()+ind2;i++) {
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
            if(lastt!=null && Math.abs(t.sentence.getOccurenceTime()-lastt.sentence.getOccurenceTime())>nar.param.duration.get()*100) {
                st.add(null); //pause
            }
            lastt=t;
        }
        st.add(0, null);
        st.add(null); //empty space
        
        HashMap<Term,ArrayList<Task>> theoriess=new HashMap<>();
        
        for(int k=0;k<st.size();k++) {
            for(int i=1;i<st.size()-1;i++) {
                Task ev=st.get(i);
                Task lastev=st.get(i-1);
                if(true)//desired
                {
                    ArrayList<Task> H=new ArrayList<>();
                    H.add(lastev);
                    H.add(ev);
                    theoriess.put(ev.sentence.term, H);
                    for(int j=i;j<st.size()-1;i++) {
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
        ArrayList<ArrayList<Task>> theories=(ArrayList<ArrayList<Task>>) theoriess.values();
        for(int i=0;i<2;i++) {
            for(ArrayList<Task> A : theories) {
                for(ArrayList<Task> B : theories) {
                    if(A.size()==1 || B.size()==1) {
                        continue;
                    }
                    while(A.contains(null)) {
                        A.remove(null);
                    }
                    while(B.contains(null)) {
                        B.remove(null);
                    }
                    boolean caseA=A.get(A.size()-1)==B.get(0);
                    boolean caseB=A.size()>2 && B.size()>1 && A.get(A.size()-1)==B.get(1) && A.get(A.size()-2)==B.get(0);
                    
                    if((A.size()>1 && B.size()>1) && (caseA || caseB)) {
                        ArrayList<Task> compoundT;
                        if(caseA) {
                            compoundT=cutoutAppend(0,-1,A,B);   
                        }
                        if(caseB) {
                            compoundT=cutoutAppend(0,-2,A,B);   
                        }
                    }
                }
            }
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
            Ret.add(new Tuple(a,TaskStringEqualSequentialTermCount(st,a)*a.size()));
        }
        for(Tuple T : Ret) {
            ArrayList<Task> a=T.x;
            double b=T.y;
            for(Tuple D : Ret) {
                ArrayList<Task> c=D.x;
                double d=D.y;
                if(a!=c && TaskStringContains(c,a) && d>=b) {
                    Ret.remove(T);
                }
            }
        }
        double max=0;
        for(Tuple T : Ret) {
            if(T.y>max) {
                max=T.y;
            }
        }
        
        for(Tuple T : Ret) {
            if(T.y==max) {
                TruthValue Truth=null;
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
                    Conjunction con=(Conjunction) Conjunction.make(wuh, TemporalRules.ORDER_FORWARD);
                    Implication res=Implication.make(con, pred,TemporalRules.ORDER_FORWARD);
                    boolean debugtillhere=true;
                }
                break;
            }
        }
    }
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {

        this.nar=n;
        if(obs==null) {
            saved_priority=Parameters.DEFAULT_JUDGMENT_PRIORITY;
            obs=new EventObserver() {

                @Override
                public void event(Class event, Object[] a) {
                    if (event!=Events.TaskAdd.class)
                        return;
                    Task task = (Task)a[0];
                    if(!task.isInput()) {
                        return;
                    }
                    if(task.sentence.stamp.getOccurrenceTime()!=Stamp.ETERNAL && task.sentence.punctuation==Symbols.JUDGMENT_MARK) {
                        lastElems.add(task);
                        if(lastElems.size()>maxlen) {
                            lastElems.remove(0);
                        }
                        if(cnt%conditionAllNSteps==0) {
                            classicalConditioning();
                        }
                        cnt++;
                    }
                }
            };
        }
        
        if(enabled) {
            Parameters.DEFAULT_JUDGMENT_PRIORITY=(float) 0.01;
        } else {
            Parameters.DEFAULT_JUDGMENT_PRIORITY=saved_priority;
        }
        
        n.memory.event.set(obs, enabled, Events.TaskAdd.class);
        return true;
    }
    
}
