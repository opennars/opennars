package nars.guifx;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;

/**
 * Created by me on 8/30/15.
 */
public class ComingSoonTab extends Tab {

	public ComingSoonTab(String label, String message) {
		super(label);

		Label l = new Label("TODO: " + message);
		l.setWrapText(true);
		l.getStyleClass().add("ComingSoon");

		setContent(l);
	}
}
