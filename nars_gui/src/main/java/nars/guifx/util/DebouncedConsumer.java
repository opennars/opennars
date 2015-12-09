package nars.guifx.util;

import javafx.application.Platform;
import nars.NAR;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by me on 8/30/15.
 */
public interface DebouncedConsumer extends Consumer<NAR> {

    @Override
    default void accept(NAR n) {
        if (canQueue().compareAndSet(true, false)) {

            long now = System.currentTimeMillis();
            long prev = lastInvocation().getAndSet(now);
            long delta = now - prev;

            update(delta);

            boolean fx = Platform.isFxApplicationThread();

            Runnable upd = () -> {

                run(delta);

                canQueue().set(true);
            };

            if (fx)
                upd.run();
            else
                Platform.runLater(upd);
        }
    }

    /**
     * gate/lock which this will use to prevent repeats. should be initialized as true
     */
    AtomicBoolean canQueue();

    AtomicLong lastInvocation();

    //min update period?

    /**
     * invoked in the trigger thread
     */
    void update(long msSinceLast);

    void run(long msSinceLast);

}
