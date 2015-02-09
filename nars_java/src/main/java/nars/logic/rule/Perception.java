package nars.logic.rule;

import com.google.common.collect.Iterators;
import nars.io.InPort;
import nars.logic.entity.Task;
import reactor.function.Consumer;
import reactor.function.Supplier;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Entry point for all new memory percepts
 */
public class Perception  implements Consumer<InPort>, Supplier<Task> {

    //protected Environment env = Environment.initializeIfEmpty().assignErrorJournal();

//    List<Supplier<AbstractTask>> externalInputs = new ArrayList();
//    List<Supplier<AbstractTask>> internalInputs = new ArrayList();
//    private final Supplier<AbstractTask> intIn;
//    private final Supplier<AbstractTask> extIn;

    final Queue<InPort<Task>> in;
    InPort<Task> currentInput;
    final Iterator<InPort<Task>> cycle;


    public Perception() {
        in = new ConcurrentLinkedQueue<>();
        cycle = Iterators.cycle(in);
    }

    @Override
    public void accept(InPort input) {
        in.add(input);
    }

    @Override
    public Task get() {
        while (!in.isEmpty()) {

            currentInput = cycle.next();

            Task t = currentInput.get();
            if (t == null) {
                cycle.remove();
            }
            else {
                return t;
            }

        }
        return null;
    }

    public void reset() {
        currentInput = null;
        for (InPort i : in) {
            i.stop();
        }
        in.clear();
    }

    public boolean isEmpty() {
        return in.isEmpty();
    }


}
