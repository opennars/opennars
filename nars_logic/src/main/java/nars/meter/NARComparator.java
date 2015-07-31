package nars.meter;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.Events;
import nars.NAR;
import nars.NARSeed;
import nars.io.out.Output;
import nars.task.Task;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by me on 7/31/15.
 */
public class NARComparator {

    private final NAR a, b;
    private final Output oa, ob;
    Multimap<NAR,Task> outputs = MultimapBuilder.hashKeys(2).arrayListValues().build();

    public NARComparator(NARSeed sa, NARSeed sb) {
        a = new NAR(sa);
        b = new NAR(sb);

        oa = new Output(a) {
            @Override protected boolean output(Channel channel, Class event, Object... args) {
                NARComparator.this.output(a, channel, event, args);
                return true;
            }
        };
        ob = new Output(b) {
            @Override protected boolean output(Channel channel, Class event, Object... args) {
                NARComparator.this.output(b, channel, event, args);
                return true;
            }
        };

    }

    private void output(NAR n, Output.Channel channel, Class event, Object[] args) {
        if (event == Events.OUT.class) {
            if (args[0] instanceof Task) {
                outputs.put(n, (Task)args[0]);
            }
        }
    }

    public void input(String s) {
        a.input(s);
        b.input(s);
    }


    public Set<Task> getTaskSet(NAR n) {
        return new LinkedHashSet(outputs.get(n));
    }

    public Set<Task> getTaskSetA() { return getTaskSet(a);    }
    public Set<Task> getTaskSetB() { return getTaskSet(b);    }

    public Set<Task> getAMinusB() {
        Set<Task> ta = getTaskSetA();
        ta.removeAll(getTaskSetB());
        return ta;
    }
    public Set<Task> getBMinusA() {
        Set<Task> tb = getTaskSetB();
        tb.removeAll(getTaskSetA());
        return tb;
    }


    public void frame(int frames) {
        a.frame(frames);
        b.frame(frames);
    }

    public boolean areEqual() {
        return getTaskSetA().equals(getTaskSetB());
    }

    public long time() {
        long at = a.time();
        long bt = b.time();
        if (at != bt)
            throw new RuntimeException("time mismatch");
        return at;
    }


}
