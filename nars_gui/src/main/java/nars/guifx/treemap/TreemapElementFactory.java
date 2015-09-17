package nars.guifx.treemap;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import nars.guifx.util.paint.ColorBucket;
import nars.guifx.util.paint.ColorGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapElementFactory {
    private final ColorBucket colorBucket = ColorBucket.createBucket();

    private Map<TreemapDtoElement, ColorGroup> colorGroupCache = new HashMap<>();
    private Map<TreemapDtoElement, Color> colorCache = new HashMap<>();

    public Parent createElement(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        ColorGroup realColorGroup = getColorGroup(dtoElement, colorGroup);
        Color color = getColor(dtoElement, realColorGroup);
        if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) {
            final SortedSet<Item> items = dtoElement.getItem().getItems();
            final double width = dtoElement.getWidth();
            final double height = dtoElement.getHeight();
            return new TreemapLayout(width, height, items, realColorGroup, this);
        }
        return new TreemapRectangle(dtoElement, color);
    }

    private Color getColor(TreemapDtoElement dtoElement, ColorGroup realColorGroup) {
        if (colorCache.containsKey(dtoElement)) {
            return colorCache.get(dtoElement);
        }

        Color color = realColorGroup.fetchColor();
        colorCache.put(dtoElement, color);
        return color;
    }

    private ColorGroup getColorGroup(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        if (colorGroupCache.containsKey(dtoElement)) {
            return colorGroupCache.get(dtoElement);
        }

        ColorGroup realColorGroup = colorGroup;
        if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) {
            final SortedSet<Item> items = dtoElement.getItem().getItems();
            realColorGroup = colorBucket.fetchColorGroup(items.size());
        }
        colorGroupCache.put(dtoElement, realColorGroup);
        return realColorGroup;
    }

    public TreemapLayout createTreemapLayout(double width, double height, SortedSet<Item> items) {
        return new TreemapLayout(width, height, items, colorBucket.fetchColorGroup(items.size()), this);
    }
}
