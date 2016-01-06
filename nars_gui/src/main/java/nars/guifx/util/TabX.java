package nars.guifx.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.logging.Logger;

/**
 * Tab that manages the visibilty of its content node
 */
public class TabX extends Tab {

	private static final Logger logger = Logger
			.getLogger(TabX.class.toString());

	public static class TabButton extends Tab {

		static final int spacing = 2;

		final Pane buttons = new HBox(spacing);

		public TabButton(String label, Node content) {
			super(label, content);

			setClosable(false);

			buttons.getStyleClass().add("tabButtons");

			setGraphic(buttons);

		}

		public TabButton button(String label, EventHandler action) {
			Button b = new Button(label);
			b.setOnAction(action);
			buttons.getChildren().add(b);
			buttons.layout();
			return this;
		}
	}

	boolean closed = false;

	public TabX(String name, Node content) {
        super(name,content);
        selectedProperty().addListener(s -> {
            update();
        });

        onClosedProperty().addListener(c -> {
            closed = true;
            update();
        });

        update();
    }
	public TabX closeable(boolean b) {
		setClosable(b);
		return this;
	}

	protected void update() {

		boolean s = !closed && isSelected();

		// logger.severe(getText() + " visible=" + s);

		update(s);

		Node c = getContent();
		if (c != null)
			c.setVisible(s);

	}

	protected void update(boolean visible) {

	}
}
