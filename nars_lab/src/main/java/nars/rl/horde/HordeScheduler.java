package nars.rl.horde;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HordeScheduler implements Serializable {
    private static final long serialVersionUID = 6003588160245867945L;

    protected class Updater implements Runnable, Serializable {
        private static final long serialVersionUID = 3170029744578080040L;
        private final int offset;

        Updater(int offset) {
            this.offset = offset;
        }

        @Override
        public void run() {
            int currentPosition = offset;
            while (currentPosition < context.nbElements()) {
                if (throwable != null)
                    return;
                try {
                    context.updateElement(currentPosition);
                } catch (Throwable throwable) {
                    HordeScheduler.this.throwable = throwable;
                    return;
                }
                currentPosition += nbThread;
            }
        }
    }

    public interface Context {
        int nbElements();

        void updateElement(int index);
    }


    transient private ExecutorService executor = null;
    private final Updater[] updaters;
    Context context;
    transient private Future<?>[] futurs;
    transient Throwable throwable = null;
    protected final int nbThread;


    public HordeScheduler(int nbThread) {
        this.nbThread = nbThread;
        updaters = new Updater[nbThread];
        for (int i = 0; i < updaters.length; i++)
            updaters[i] = new Updater(i);
    }

    private void initialize() {
        futurs = new Future<?>[nbThread];
        executor = Executors.newFixedThreadPool(nbThread);
    }

    public void update(Context context) {
        this.context = context;
        if (executor == null)
            initialize();
        throwable = null;
        for (int i = 0; i < updaters.length; i++)
            futurs[i] = executor.submit(updaters[i]);
        try {
            for (Future<?> futur : futurs)
                futur.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        this.context = null;
        if (throwable != null)
            throw new RuntimeException(throwable);
    }
}
