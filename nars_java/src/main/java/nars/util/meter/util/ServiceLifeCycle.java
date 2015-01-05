package nars.util.meter.util;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * @author The Stajistics Project
 */
public interface ServiceLifeCycle {

    void initialize();

    boolean isRunning();

    void shutdown();

    public static class Support implements Serializable {

        private volatile boolean initalized;
        private volatile boolean shutdown;

        public Support() {
        }

        public synchronized void initialize(final Callable<Void> initCallable) {
            if (!initalized) {
                try {
                    if (initCallable != null) {
                        initCallable.call();
                    }
                    initalized = true;
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        }

        public synchronized void shutdown(final Callable<Void> shutdownCallable) {
            if (!initalized) {
                throw new IllegalStateException("Attempted shutdown when service has not yet started");
            }
            if (!shutdown) {
                shutdown = true;
                try {
                    if (shutdownCallable != null) {
                        shutdownCallable.call();
                    }
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
            }
        }

        public boolean isRunning() {
            return initalized && !shutdown;
        }
    }

}
