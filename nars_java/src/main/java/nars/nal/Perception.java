package nars.nal;

import com.google.common.collect.Iterators;
import nars.io.Source;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Entry point for all new memory percepts
 */
public class Perception  implements Consumer<Source>, Supplier<Task> {

    //protected Environment env = Environment.initializeIfEmpty().assignErrorJournal();

//    List<Supplier<AbstractTask>> externalInputs = new ArrayList();
//    List<Supplier<AbstractTask>> internalInputs = new ArrayList();
//    private final Supplier<AbstractTask> intIn;
//    private final Supplier<AbstractTask> extIn;

    final Queue<Source<Task>> in;
    Source<Task> currentInput;
    final Iterator<Source<Task>> cycle;


    public Perception() {
        in = new ConcurrentLinkedQueue<>();
        cycle = Iterators.cycle(in);
    }

    @Override
    public void accept(Source input) {
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
        for (Source i : in) {
            i.stop();
        }
        in.clear();
    }

    public boolean isEmpty() {
        return in.isEmpty();
    }


}
