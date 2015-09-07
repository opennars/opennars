/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow.util;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.util.Duration;

/**
 * Animates sliding a node on and off screen.
 */
public class SlideAnimator {

    public static void slide(Button controlButton, Control node, Side direction) {
        final DoubleProperty startWidth = new SimpleDoubleProperty();

        // hide sidebar.
        final Animation hideSidebar = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth.get() * (1.0 - frac);
                node.setPrefWidth(curWidth);   // todo resize a spacing underlay to allow the scene to adjust.
                node.setTranslateX(-startWidth.get() + curWidth);
            }
        };
        hideSidebar.onFinishedProperty().set(actionEvent ->
                node.setVisible(false)
        );

        // show node.
        final Animation showSidebar = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curWidth = startWidth.get() * frac;
                node.setPrefWidth(curWidth);
                node.setTranslateX(-startWidth.get() + curWidth);
            }
        };

        controlButton.setOnAction(actionEvent -> {
            node.setMinWidth(Control.USE_PREF_SIZE);

            if (    Animation.Status.STOPPED.equals(showSidebar.getStatus())
                    && Animation.Status.STOPPED.equals(hideSidebar.statusProperty().get())
                    ) {
                if (node.isVisible()) {
                    startWidth.set(node.getWidth());
                    hideSidebar.play();
                } else {
                    node.setVisible(true);
                    showSidebar.play();
                }
            }
        });
    }
}

// todo java 8 has a weird background issue on resize - file bug, got around it by placing the sidebar content in a scrollpane and scrolling the scrollpane.
