//package nars.meter.condition;
//
//import nars.Events;
//import nars.NAR;
//import nars.event.NARReaction;
//import nars.task.Task;
//import nars.util.data.map.CuckooMap;
//import nars.util.event.DefaultTopic;
//import nars.util.meter.Metrics;
//import nars.util.meter.event.HitMeter;
//
//import java.util.*;
//
//
//public class CountDerivationCondition extends NARReaction {
//
//    //SM = success method
//    final static String methodInvolvedInSuccessfulDerivation_Prefix = "D";
//    final static String methodInvolvedInDerivation_Prefix = "d";
//    private final DefaultTopic.Subscription cycleEnd;
//
//    boolean includeNonSuccessDerivations = true;
//
//    boolean concatenatePath = false; //whether to store the traced path as an entire sequence, or its individual parts
//
//    private final Metrics metrics;
//    final Map<Task, StackTraceElement[]> derived = new CuckooMap();
//    final List<OutputCondition> successesThisCycle = new ArrayList();
//
//    public CountDerivationCondition(NAR nar, Metrics m) {
//        super(nar, Events.TaskDerive.class, OutputCondition.class);
//        this.cycleEnd = nar.memory.eventCycleEnd.on(memory -> {
//
//            /** usually true rule tasks should only be one, because
//             * this event will be triggered only the first time it has
//             * become successful. */
//            for (OutputCondition o : successesThisCycle) {
//                for (Task tt : o.getTrueReasons()) {
//                    traceStack(tt, true);
//                }
//            }
//
//            /** any successful derivations will be counted again in the
//             * general meters */
//            if (includeNonSuccessDerivations) {
//                for (Task x : derived.keySet()) {
//                    traceStack(x, false);
//                }
//            }
//
//            //reset everything for next cycle
//            derived.clear();
//            successesThisCycle.clear();
//        });
//        this.metrics = m;
//    }
//
//
//    @Override
//    public void event(Class event, Object[] args) {
//
//        if (event == OutputCondition.class) {
//
//            OutputCondition o = (OutputCondition) args[0];
//
//            if (!o.isTrue()) {
//                throw new RuntimeException(o + " signaled when it has not succeeded");
//            }
//
//            //buffer to calculate at end of cycle when everything is collected
//            successesThisCycle.add(o);
//
//        }
//        else if (event == Events.TaskDerive.class) {
//            Task t = (Task)args[0];
//            derived.put(t, Thread.currentThread().getStackTrace());
//        }
//    }
//
//    public void traceStack(Task t, boolean success) {
//        StackTraceElement[] s = derived.get(t);
//        if (s == null) {
//            //probably a non-derivation condition, ex: immediate reaction to an input event, etc.. or execution
//            //throw new RuntimeException("A stackTrace for successful output condition " + t + " was not recorded");
//            return;
//        }
//
//        String prefix;
//        if (success)
//            prefix = methodInvolvedInSuccessfulDerivation_Prefix;
//        else
//            prefix = methodInvolvedInDerivation_Prefix;
//
//        boolean tracing = false;
//        String prevMethodID = null;
//
//        List<String> path = new ArrayList();
//        int i;
//        for (i = 0; i < s.length; i++) {
//            StackTraceElement e = s[i];
//
//            String className = e.getClassName();
//            String methodName = e.getMethodName();
//
//
//            if (tracing) {
//
//                //Filter conditions
//                if (className.contains("reactor."))
//                    continue;
//                if (className.contains("EventEmitter"))
//                    continue;
//                if ((className.equals("NAL") || className.equals("Memory")) && methodName.equals("emit"))
//                    continue;
//
//                int cli = className.lastIndexOf(".") + 1;
//                if (cli!=-1)
//                    className = className.substring(cli, className.length()); //class's simpleName
//
//                String methodID = className + '_' + methodName;
//                String sm = prefix + '_' + methodID;
//
//                traceMethod(sm);
//
//
//
//                if (prevMethodID!=null) {
//                    traceMethodCall(prevMethodID, methodID, success);
//                }
//                path.add(sm);
//
//                prevMethodID = methodID;
//
//
//                //Termination conditions
//                if (className.contains("ConceptFireTask") && methodName.equals("accept"))
//                    break;
//                if (className.contains("ImmediateProcess") && methodName.equals("rule"))
//                    break;
//                if (className.contains("ConceptFire") && methodName.equals("rule"))
//                    break;
//            }
//            else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
//                tracing = true; //begins with next stack element
//            }
//
//        }
//
//        if (!path.isEmpty()) {
//            traceMethodPath(path);
//        }
//
//        if (i >= s.length - 1) {
//            System.out.println("Stack not clipped: " + Arrays.toString(s));
//        }
//
//
//    }
//
//    void hit(String meter) {
//        HitMeter m = (HitMeter) metrics.getMeter(meter);
//        if (m == null) {
//            metrics.add(m = new HitMeter(meter));
//        }
//        m.hit();
//    }
//    private void hit(Collection<String> meter) {
//        if (concatenatePath) {
//            hit(meter.toString());
//        }
//        else {
//            for (String s : meter)
//                hit(s);
//        }
//    }
//
//    private void traceMethod(String sm) {
//        //hit(sm);
//    }
//
//    private void traceMethodPath(Collection<String> pathString) {
//        hit(pathString);
//    }
//
//    protected void traceMethodCall(String prevMethodID, String methodID, boolean success) {
//
//
//    }
//}
