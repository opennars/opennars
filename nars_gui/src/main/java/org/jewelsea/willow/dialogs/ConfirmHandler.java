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

package org.jewelsea.willow.dialogs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.util.ResourceUtil;

import static org.jewelsea.willow.util.ResourceUtil.getString;

public class ConfirmHandler extends VBox {
    public ConfirmHandler(String message, EventHandler<ActionEvent> confirmHandler, EventHandler<ActionEvent> denyHandler) {
        super(14);

        // add controls to the popup.
        Label promptMessage = new Label(message);
        promptMessage.setWrapText(true);
        promptMessage.setPrefWidth(350);

        // action button text setup.
        HBox buttonBar = new HBox(20);

        ImageView confirmImage = new ImageView(ResourceUtil.getImage("select_48.png"));
        confirmImage.setFitHeight(19);
        confirmImage.setPreserveRatio(true);

        Button confirmButton = new Button(getString("dialog.confirm"));
        confirmButton.setGraphic(confirmImage);
        confirmButton.setDefaultButton(true);

        ImageView denyImage = new ImageView(ResourceUtil.getImage("stop_48.png"));
        denyImage.setFitHeight(19);
        denyImage.setPreserveRatio(true);

        Button denyButton = new Button(getString("dialog.deny"));
        denyButton.setGraphic(denyImage);
        denyButton.setCancelButton(true);

        buttonBar.getChildren().addAll(confirmButton, denyButton);

        // layout the popup.
        setPadding(new Insets(10));
        getStyleClass().add("alert-dialog");
        getChildren().addAll(promptMessage, buttonBar);

        DropShadow dropShadow = new DropShadow();
        setEffect(dropShadow);

        // confirm and close the popup.
        confirmButton.setOnAction(confirmHandler);

        // deny and close the popup.
        denyButton.setOnAction(denyHandler);
    }
}
