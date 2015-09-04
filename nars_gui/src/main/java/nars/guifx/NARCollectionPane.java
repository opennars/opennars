package nars.guifx;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import nars.Global;
import nars.NARStream;
import nars.guifx.util.DebouncedRunnable;
import org.infinispan.commons.util.concurrent.ConcurrentWeakKeyHashMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/** manages thread interaction with a running NAR for displaying
 * objects collected from the NAR, with appropriate thread-safe
 * and optimal caching and GC of generated GUI elemetns
 *
 * TODO
 *  time throttling
 *  auto throttle by update/render time calculation
 *  item update function (for existing elements to update their values)
 *  parametric filters including automatic control panel generation with sliders, toggles etc
 *
 */
abstract public class NARCollectionPane<R> extends VBox implements DebouncedRunnable {

    final AtomicBoolean canQueue = new AtomicBoolean(true);
    final AtomicLong lastInvocation = new AtomicLong(0);
    private final NARStream narstream;
    private List<Node> toDisplay = Global.newArrayList();

    final Function<R, Node> itemBuilder;

    public NARCollectionPane(NARStream narstream, Function<R, Node> itemBuilder) {
        super();

        this.narstream = narstream;
        this.itemBuilder = itemBuilder;

        narstream.forEachFrame(this);

        run();
    }

    @Override
    public AtomicBoolean canQueue() {
        return canQueue;
    }

    @Override
    public AtomicLong lastInvocation() {
        return lastInvocation;
    }

    @Override
    public void update(long msSinceLast) {
        synchronized (toDisplay) {
            toDisplay.clear();
            collect((r) -> {
                toDisplay.add(item(r));
            });
        }
    }

    final ConcurrentWeakKeyHashMap<R, Node> items = new ConcurrentWeakKeyHashMap<>();

    Node item(R r) {
        return items.computeIfAbsent(r, itemBuilder);
    }

    @Override
    public void run(long msSinceLast) {


        synchronized (toDisplay) {
            getChildren().setAll(toDisplay);
        }

    }

    abstract public void collect(Consumer<R> c);
}
