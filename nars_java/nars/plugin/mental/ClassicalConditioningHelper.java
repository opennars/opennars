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
    
    public ArrayList<Task> cutoutAppend(int ind1,int ind2,ArrayList<Task> first,ArrayList<Task> second) {
        ArrayList<Task> res=new ArrayList<Task>();
        for(int i=ind1;i<=first.size()+ind2;i++) {
            res.add(first.get(i));
        }
        for(Task t : second) {
            res.add(t);
        }
        return res;
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
                    ArrayList<Task> H=new ArrayList<Task>();
                    H.add(lastev);
                    H.add(ev);
                    theoriess.put(ev.sentence.term, H);
                    for(int j=i;j<st.size()-1;i++) {
                        Task ev2=st.get(j);
                        Task lastev2=st.get(j-1); //forward conditioning
                        if(lastev2.sentence.term.equals(lastev.sentence.term) && !ev2.sentence.term.equals(ev.sentence.term) &&
                                theoriess.keySet().contains(ev)) {
                            theoriess.remove(theoriess.get(ev)); //extinction
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
    }
   /* Filtered=[a for a in list(set([a.replace(" ","") for a in theories])) if len(a)>1]
    Ret=[(a,float(st.count(a))*float(len(a))) for a in Filtered]
    for (a,b) in Ret:
        for (c,d) in Ret:
            if a!=c and a in c and d>=b:
                if (a,b) in Ret:
                    Ret.remove((a,b))
    Maxc=max([b for (a,b) in Ret] if len(Ret)>0 else [0]) 
    return [(a,b) for (a,b) in Ret if b==Maxc] # 
   */
                


        //ok st is prepared
        /*
from copy import deepcopy

def conditioning2(Sequence):
    st="  "+Sequence+"  "
    dc=set([a for a in st]) #unconditional and conditioned stimulus / events of high desire, currently we use all
    theories={} 
    
    for i in range(len(st)):
        dc2=deepcopy(dc)
        for i in range(1,len(st)-1):
            ev, lastev=st[i], st[i-1]
            if ev in dc:
                theories[ev]=lastev+ev #forward/simultaneous conditioning with blocking and inhibition:
                dc2.add(lastev)  #to allow multiple forward conditionings, this means deriving lastev as a desired subgoal
                for j in range(i,len(st)-1):
                    ev2, lastev2=st[j], st[j-1]
                    if lastev2==lastev and ev2!=ev and ev in theories.keys():
                        del theories[ev] #extinction
        dc=dc2
    
    #higher order conditioning
    theories=theories.values()
    for i in range(2):
        theories2=deepcopy(theories)
        for A in theories:
            for B in theories:
                if len(A)==1 or len(B)==1 :
                    continue
                A=A.replace(" ","")
                B=B.replace(" ","")
                caseA= A[-1]==B[0]
                caseB= len(A)>2 and len(B)>1 and A[-1]==B[1] and A[-2]==B[0]
                if len(A)>1 and len(B)>1 and caseA or caseB:
                    if caseA: compoundT=A[0:-1]+B
                    if caseB: compoundT=A[0:-2]+B
                    theories2=theories2+[compoundT]
        theories=theories2
    print "found theories:" 
    Filtered=[a for a in list(set([a.replace(" ","") for a in theories])) if len(a)>1]
    Ret=[(a,float(st.count(a))*float(len(a))) for a in Filtered]
    for (a,b) in Ret:
        for (c,d) in Ret:
            if a!=c and a in c and d>=b:
                if (a,b) in Ret:
                    Ret.remove((a,b))
    Maxc=max([b for (a,b) in Ret] if len(Ret)>0 else [0]) 
    return [(a,b) for (a,b) in Ret if b==Maxc] # 

a="uab wab kabi"
print "forward conditioning: \n" + a
print conditioning2(a)
print
a="kacf guacf wacfg"
print "higher order conditioning: \n" + a
print conditioning2(a)
print
a="cb cb cb ab"
print "inhibition: \n" + a
print conditioning2(a)
print
a="ab ab ab a"
print "extinction: \n" + a
print conditioning2(a)
print
a="abc abc abc ab"
print "extinction: \n" + a
print conditioning2(a)
print
a="AB AB AB CB CBF CBF CBF"
print "inhibition: \n" + a
print conditioning2(a)
        */

    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {

        this.nar=n;
        if(obs==null) {
            saved_priority=Parameters.DEFAULT_JUDGMENT_PRIORITY;
            obs=new EventObserver() {

                @Override
                public void event(Class event, Object[] a) {
                    if (event!=Events.Perceive.class)
                        return;
                    Task task = (Task)a[0];
                    if(task.sentence.stamp.getOccurrenceTime()!=Stamp.ETERNAL) {
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
        
        n.memory.event.set(obs, enabled, Events.Perceive.class);
        return true;
    }
    
}
