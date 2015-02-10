package nars.analyze.meter;

import nars.core.AbstractPlugin;
import nars.core.Events;
import nars.core.NAR;
import nars.io.condition.OutputCondition;
import nars.io.meter.Metrics;
import nars.io.meter.event.HitMeter;
import nars.logic.entity.Task;
import nars.util.data.CuckooMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Created by me on 2/10/15.
*/
public class CountDerivationCondition extends AbstractPlugin {

    //SM = success method
    final static String meterPrefix = "SM";
    private final Metrics metrics;
    final Map<Task, StackTraceElement[]> stacks = new CuckooMap();
    final List<OutputCondition> successesThisCycle = new ArrayList();

    public CountDerivationCondition(Metrics m) {
        super();
        this.metrics = m;
    }

    @Override
    public Class[] getEvents() {
        return new Class[] { Events.TaskDerive.class, OutputCondition.class, Events.CycleEnd.class };
    }

    @Override public void onEnabled(NAR n) {       }

    @Override public void onDisabled(NAR n) {        }

    @Override
    public void event(Class event, Object[] args) {

        if (event == OutputCondition.class) {

            OutputCondition o = (OutputCondition) args[0];

            if (!o.succeeded) {
                throw new RuntimeException(o + " signaled when it has not succeeded");
            }

            //buffer to calculate at end of cycle when everything is collected
            successesThisCycle.add(o);

        }
        else if (event == Events.TaskDerive.class) {
            Task t = (Task)args[0];
            stacks.put(t, Thread.currentThread().getStackTrace());
        }
        else if (event == Events.CycleEnd.class) {

            /** usually true reason tasks should only be one, because
             * this event will be triggered only the first time it has
             * become successful. */
            for (OutputCondition o : successesThisCycle) {
                for (Task tt : o.getTrueReasons())
                    traceStack(tt);
            }

            //reset everything for next cycle
            stacks.clear();
            successesThisCycle.clear();
        }
    }

    public void traceStack(Task t) {
        StackTraceElement[] s = stacks.get(t);
        if (s == null) {
            //probably a non-derivation condition, ex: immediate reaction to an input event, etc.. or execution
            //throw new RuntimeException("A stackTrace for successful output condition " + t + " was not recorded");
            return;
        }

        int excludeSuffix = 1;
        String startMethod = "reason";

        boolean tracing = false;
        for (int i = 0; i < s.length; i++) {
            StackTraceElement e = s[i];

            String className = e.getClassName();
            String methodName = e.getMethodName();


            if (tracing && className.contains(".ConceptFireTask") && methodName.equals("accept")) {
                tracing = false;
            }

            if (tracing) {
                int cli = className.lastIndexOf(".");
                if (cli!=-1)
                    className = className.substring(cli, className.length()); //class's simpleName

                String sm = meterPrefix + className + '_' + methodName;

                HitMeter m = (HitMeter) metrics.getMeter(sm);
                if (m == null) {
                    metrics.addMeter(m = new HitMeter(sm));
                }
                m.hit();
            }
            else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
                tracing = true; //begins with next stack element
            }
        }
    }
}
