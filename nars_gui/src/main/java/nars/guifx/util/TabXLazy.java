package nars.guifx.util;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/30/15.
 */
public class TabXLazy extends TabX implements Runnable {

    static final ExecutorService exe = Executors.newCachedThreadPool();

    private final Supplier<Node> contentBuilder;
    private Node map = null;

    public TabXLazy(String label, Supplier<Node> contentBuilder) {
        super(label, new Label(" "));
        this.contentBuilder = contentBuilder;
    }

    @Override
    protected void update(boolean visible) {
        if (visible) {
            if (map == null) {

                exe.submit(() -> {

                    Node g = contentBuilder.get();
                    map = g;

                    runLater(TabXLazy.this);
                });

            }
        }
        else {

        }
    }

    @Override
    public void run() {
        setContent( map );
    }

}
