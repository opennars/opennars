package nars.guifx.space;

import javafx.scene.Node;
import javafx.scene.control.Label;
import nars.guifx.TabX;

import java.util.function.Supplier;

/**
 * Created by me on 8/30/15.
 */
public class TabXLazy extends TabX {


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
                setContent( map = contentBuilder.get() );
            }
        }
        else {

        }
    }
}
