package nars.guifx.util;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * Menu button with a Runnable reaction handler
 */
public final class SimpleMenuItem extends MenuItem {

	public SimpleMenuItem(String s, Runnable r) {
		this(null, s, r);
	}

	public SimpleMenuItem(Node i, String s, Runnable r) {
        super(s, i);
        setOnAction(e -> {
            r.run();
        });
    }
}
