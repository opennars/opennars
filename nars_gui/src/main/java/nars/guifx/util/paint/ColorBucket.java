package nars.guifx.util.paint;

import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;

public class ColorBucket {

    private static final int DEFAULT_SIZE = 10;
    private final Deque<Color> availableColorGroups = new ArrayDeque<>();

    private final ColorGroupType colorGroupType;

    private ColorBucket(ColorGroupType colorGroupType) {
        this.colorGroupType = colorGroupType;
        availableColorGroups.addAll(colorGroupType.getColorList());
    }

    public static ColorBucket createBucket() {
        return createBucket(ColorGroupType.TEN);
    }

    public ColorGroup fetchColorGroup() {
        return fetchColorGroup(DEFAULT_SIZE);
    }

    public static ColorBucket createBucket(ColorGroupType colorGroupType) {
        return new ColorBucket(colorGroupType);
    }

    public ColorGroup fetchColorGroup(int size) {
        if (availableColorGroups.isEmpty()) {
            availableColorGroups.addAll(colorGroupType.getColorList());
        }
        Color mainColor = availableColorGroups.pop();
        return new ColorGroup(mainColor, size);
    }
}
