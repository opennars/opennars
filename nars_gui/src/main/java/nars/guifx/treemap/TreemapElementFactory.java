package nars.guifx.treemap;

import javafx.scene.Node;
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

    private final Map<TreemapDtoElement, ColorGroup> colorGroupCache = new HashMap<>();
    private final Map<TreemapDtoElement, Color> colorCache = new HashMap<>();

    public Node createElement(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        ColorGroup realColorGroup = getColorGroup(dtoElement, colorGroup);
        Color color = getColor(dtoElement, realColorGroup);
        if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) {
            SortedSet<Item> items = dtoElement.getItem().getItems();
            double width = dtoElement.getWidth();
            double height = dtoElement.getHeight();
            return new TreemapLayout(width, height, items, realColorGroup, this);
        }
        return new TreemapRectangle(dtoElement, color);
    }

    private Color getColor(TreemapDtoElement dtoElement, ColorGroup realColorGroup) {
        return colorCache.computeIfAbsent(dtoElement, c -> realColorGroup.fetchColor());
        /*
        if (colorCache.containsKey(dtoElement)) {
            return colorCache.get(dtoElement);
        }

        Color color = realColorGroup.fetchColor();
        colorCache.put(dtoElement, color);
        return color;*/
    }

    private ColorGroup getColorGroup(TreemapDtoElement dtoElement, ColorGroup colorGroup) {
        return colorGroupCache.computeIfAbsent(dtoElement, c -> {
            ColorGroup realColorGroup = colorGroup;
            if (dtoElement.isContainer() && !dtoElement.getItem().getItems().isEmpty()) {
                SortedSet<Item> items = dtoElement.getItem().getItems();
                realColorGroup = colorBucket.fetchColorGroup(items.size());
            }
            return realColorGroup;
        });
//        if (colorGroupCache.containsKey(dtoElement)) {
//            return colorGroupCache.get(dtoElement);
//        }
//
//        colorGroupCache.put(dtoElement, realColorGroup);
//        return realColorGroup;
    }

    public TreemapLayout createTreemapLayout(double width, double height, SortedSet<Item> items) {
        return new TreemapLayout(width, height, items, colorBucket.fetchColorGroup(items.size()), this);
    }
}
