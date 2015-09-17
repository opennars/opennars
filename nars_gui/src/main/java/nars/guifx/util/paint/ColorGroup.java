package nars.guifx.util.paint;

import javafx.scene.paint.Color;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ColorGroup {

    private final Color mainColor;
    private final List<Color> groupColors = new LinkedList<>();
    private final Deque<Color> availableColors = new ArrayDeque<>();

    public ColorGroup(Color mainColor, int size) {
        this.mainColor = mainColor;
        size += 1;
        double rangeStart = 100; //degrees
        double rangeEnd = 250; //degrees
        double step = (rangeEnd - rangeStart)  / size;

        double hue = rangeStart;
        for (int i = 0; i < size; i++) {
            hue += step;
            groupColors.add(mainColor.deriveColor(hue, 0.9, 0.9, 1.0));
        }

        availableColors.addAll(groupColors);
    }

    public Color fetchColor() {
        if (availableColors.isEmpty()) {
            availableColors.addAll(groupColors);
        }
        return availableColors.pop();
    }

    public Color getMainColor() {
        return mainColor;
    }
}
