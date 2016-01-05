//package nars.util.data.buffer;
//
//import com.google.common.collect.Iterators;
//
//import java.util.Iterator;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
///**
// * Entry point for all new memory percepts
// */
//public class RoundRobinBuffer<T>  implements Consumer<Source<T>>,Supplier<T> {
//
//    //protected Environment env = Environment.initializeIfEmpty().assignErrorJournal();
//
////    List<Supplier<AbstractTask>> externalInputs = new ArrayList();
////    List<Supplier<AbstractTask>> internalInputs = new ArrayList();
////    private final Supplier<AbstractTask> intIn;
////    private final Supplier<AbstractTask> extIn;
//
//    final Queue<Source<T>> in;
//
//    final Iterator<Source<T>> cycle;
//
//
//    public RoundRobinBuffer() {
//        in = new ConcurrentLinkedQueue<>();
//        cycle = Iterators.cycle(in);
//    }
//
//    @Override
//    public void accept(Source<T> input) {
//        in.add(input);
//    }
//
//    @Override
//    public T get() {
//        while (!in.isEmpty()) {
//
//            Source<T> currentInput = cycle.next();
//
//            T t = currentInput.get();
//            if ((t == null) || (t == currentInput /* returned itself */)) {
//                cycle.remove();
//                return t;
//            }
//            else {
//                return t;
//            }
//
//        }
//        return null;
//    }
//
//    public void clear() {
//        for (Source i : in) {
//            i.stop();
//        }
//        in.clear();
//    }
//
//    public boolean isEmpty() {
//        return in.isEmpty();
//    }
//
//
// }
