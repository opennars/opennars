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

package org.jewelsea.willow.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.util.ResourceUtil;

/** A button with an icon */
public class IconButton extends Button {
    public IconButton(String buttonText, String imageLoc, String tooltipText, EventHandler<ActionEvent> actionEventHandler) {
        super(buttonText);

        setTooltip(new Tooltip(tooltipText));
        getStyleClass().add("icon-button");
        setMaxWidth(Double.MAX_VALUE);
        setAlignment(Pos.CENTER_LEFT);

        final ImageView imageView = new ImageView(ResourceUtil.getImage(imageLoc));
        imageView.setFitHeight(16);
        imageView.setPreserveRatio(true);
        setGraphic(imageView);

        setContentDisplay(ContentDisplay.LEFT);
        VBox.setMargin(this, new Insets(0, 5, 0, 5));

        setOnAction(actionEventHandler);
    }
}
