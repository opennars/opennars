package nars.guifx.treemap;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;

import java.util.SortedSet;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
public class TreemapChart extends Parent {

	// private final ColorBucket colorBucket = ColorBucket.createBucket();
	public final Item root;

	public DoubleProperty width = new SimpleDoubleProperty(640.0);
	public DoubleProperty height = new SimpleDoubleProperty(280.0);

	private final TreemapElementFactory elementFactory = new TreemapElementFactory();
	private final TreemapLayout treemapLayouter;

	public TreemapChart(Item root) {
        this.root = root;
        SortedSet<Item> items = root.getItems();
        treemapLayouter = elementFactory.createTreemapLayout(width.doubleValue(), height.doubleValue(), items);
        ChangeListener<Number> changeListener = (observableValue, number, number2) -> treemapLayouter.update(width.doubleValue(), height.doubleValue(), items);
        width.addListener(changeListener);
        height.addListener(changeListener);
        getChildren().add(treemapLayouter);

    }
	public void update() {
		treemapLayouter.update(width.doubleValue(), height.doubleValue(),
				root.getItems());
		autosize();
	}

	public DoubleProperty getWidth() {
		return width;
	}

	public DoubleProperty getHeight() {
		return height;
	}
}
