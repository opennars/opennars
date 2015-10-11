package nars.guifx.util.paint;


import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Colors taken from https://github.com/mbostock/d3/wiki/Ordinal-Scales#wiki-category10
 */
public enum ColorGroupType {
    TEN("#1f77b4 #ff7f0e #2ca02c #d62728 #9467bd #8c564b #e377c2 #7f7f7f #bcbd22 #17becf"),
    TWENTY("#1f77b4 #aec7e8 #ff7f0e #ffbb78 #2ca02c #98df8a #d62728 #ff9896 #9467bd #c5b0d5 #8c564b #c49c94 #e377c2 #f7b6d2 #7f7f7f #c7c7c7 #bcbd22 #dbdb8d #17becf #9edae5"),
    TWENTYB("#393b79 #5254a3 #6b6ecf #9c9ede #637939 #8ca252 #b5cf6b #cedb9c #8c6d31 #bd9e39 #e7ba52 #e7cb94 #843c39 #ad494a #d6616b #e7969c #7b4173 #a55194 #ce6dbd #de9ed6"),
    TWENTYC("#3182bd #6baed6 #9ecae1 #c6dbef #e6550d #fd8d3c #fdae6b #fdd0a2 #31a354 #74c476 #a1d99b #c7e9c0 #756bb1 #9e9ac8 #bcbddc #dadaeb #636363 #969696 #bdbdbd #d9d9d9");

    private final List<Color> colorList = new LinkedList<>();

    ColorGroupType(String colors) {
        for (String color : colors.split(" ")) {
            colorList.add(Color.web(color));
        }
    }

    public List<Color> getColorList() {
        return Collections.unmodifiableList(colorList);
    }
}
