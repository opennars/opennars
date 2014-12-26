/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.plugin.mental;

import java.util.ArrayList;
import nars.core.EventEmitter;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.entity.Task;

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
    
    public void classicalConditioning() {
        /*
        from copy import deepcopy
@interact
def _(Sequence="kabc kabc abc"):
    st=" "+Sequence+" "
    dc=set([a for a in st]) #unconditional and conditioned stimulus / events of high desire, currently we use all
    theories={} 
    
    for i in range(len(st)):
        dc2=deepcopy(dc)
        for i in range(1,len(st)-1):
            ev, lastev=st[i], st[i-1]
            if ev!=" " and lastev!=" " and ev in dc: 
                theories[ev]=lastev+ev #forward/simultaneous conditioning with blocking and inhibition:
                dc2.add(lastev)  #to allow multiple forward conditionings, this means deriving st[i-1] as a desired subgoal
                for j in range(1,len(st)-1):
                    ev2, lastev2=st[j], st[j-1]
                    if lastev2==lastev and ev2!=ev and ev in theories.keys():
                        del theories[ev] #extinction
        dc=dc2
    
    #higher order conditioning
    theories=theories.values()
    for i in range(len(st)):
        theories2=deepcopy(theories)
        for A in theories:
            for B in theories:
                caseA= A[-1]==B[0]
                caseB= len(A)>2 and A[-1]==B[1] and A[-2]==B[0]
                if caseA or caseB:
                    if caseA: compoundT=A[0:-1]+B
                    if caseB: compoundT=A[0:-2]+B
                    #check for counterexample
                    if st.count(A)>st.count(compoundT) or st.count(B)>st.count(compoundT):
                        continue
                    theories2=theories2+[compoundT]
                    if A in theories2:
                        del theories2[theories2.index(A)]
                    if B in theories2:
                        del theories2[theories2.index(B)]
        theories=theories2
    print "found theories:"             
    show([a for a in theories if st.count(a)==st.count(a[-1]) and st.count(a)==st.count(a[0])])
        //the latter are the temporal hypotheses which enter memory
        */
    }
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {

        if(obs==null) {
            saved_priority=Parameters.DEFAULT_JUDGMENT_PRIORITY;
            obs=new EventObserver() {

                @Override
                public void event(Class event, Object[] a) {
                    if (event!=Events.Perceive.class)
                        return;
                    Task task = (Task)a[0];
                    lastElems.add(task);
                    if(lastElems.size()>maxlen) {
                        lastElems.remove(0);
                    }
                    if(cnt%conditionAllNSteps==0) {
                        classicalConditioning();
                    }
                    cnt++;
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
