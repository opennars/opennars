package nars.guifx.util;

import javafx.scene.layout.Pane;

public class MaximizePane extends Pane {

    @Override
    protected void layoutChildren() {
        getChildren().forEach(child -> {
            child.relocate(getPadding().getLeft(), getPadding().getTop());
            child.resize(getWidth() - getPadding().getLeft() - getPadding().getRight(), getHeight() - getPadding().getTop() - getPadding().getBottom());
        });
    }

    @Override
    protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMinWidth(double height) {
        return 0;
    }

    @Override
    protected double computeMinHeight(double width) {
        return 0;
    }
}