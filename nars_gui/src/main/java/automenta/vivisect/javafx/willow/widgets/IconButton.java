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

package automenta.vivisect.javafx.willow.widgets;

import automenta.vivisect.javafx.willow.util.ResourceUtil;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/** A button with an icon */
public class IconButton extends Button {
    public IconButton(String buttonText, String imageLoc, String tooltipText, EventHandler<ActionEvent> actionEventHandler) {
        super(buttonText);

        setTooltip(new Tooltip(tooltipText));
        getStyleClass().add("icon-button");
        setMaxWidth(Double.MAX_VALUE);
        setAlignment(Pos.CENTER_LEFT);

        AwesomeDude.setIcon(this, AwesomeIcon.IMAGE);

        setContentDisplay(ContentDisplay.LEFT);
        VBox.setMargin(this, new Insets(0, 5, 0, 5));

        setOnAction(actionEventHandler);
    }
}
