package nars.guifx.treemap;

import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapRectangle extends Parent {

	// private static final Logger LOG =
	// Logger.getLogger(TreemapRectangle.class.getName());
	// private Random random = new Random();
	private final Color rectangleColor;
	private final Rectangle rectangle = new Rectangle();

	public TreemapRectangle(TreemapDtoElement child, Color color) {

		rectangle.setHeight(child.getHeight());
		rectangle.setWidth(child.getWidth());

		/*
		 * (rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, new
		 * HoverOnEventHandler());
		 * rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, new
		 * HoverOffEventHandler());
		 */
		rectangleColor = color;
		rectangle.setFill(rectangleColor);

		getChildren().setAll(rectangle, new Text(child.getLabel()));

		/*
		 * runLater(() -> { //Tooltip.install(rectangle, new
		 * NodeTooltip(child)); });
		 */

	}

	private static class NodeTooltip extends Tooltip {

		public NodeTooltip(TreemapDtoElement child) {
			super(child.getLabel());
		}
	}

	/*
	 * private class HoverOnEventHandler implements EventHandler<Event> {
	 * 
	 * @Override public void handle(Event t) { Color brighter =
	 * rectangleColor.brighter();
	 * TreemapRectangle.this.rectangle.setFill(brighter); } }
	 * 
	 * private class HoverOffEventHandler implements EventHandler<Event> {
	 * 
	 * @Override public void handle(Event t) {
	 * TreemapRectangle.this.rectangle.setFill(rectangleColor); } }
	 */
}