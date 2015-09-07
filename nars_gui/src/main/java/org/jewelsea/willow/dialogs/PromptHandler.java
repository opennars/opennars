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

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.util.ResourceUtil;

import static org.jewelsea.willow.util.ResourceUtil.getString;

public class PromptHandler extends VBox {
    public PromptHandler(
            String message,
            String defaultValue,
            EventHandler<ActionEvent> submitHandler,
            EventHandler<ActionEvent> cancelHandler
    ) {
        super(14);

        // add controls to the popup.
        final Label promptMessage = new Label(message);
        promptMessage.setWrapText(true);
        final ImageView promptImage = new ImageView(ResourceUtil.getImage("help_64.png"));
        promptImage.setFitHeight(32);
        promptImage.setPreserveRatio(true);
        promptMessage.setGraphic(promptImage);
        promptMessage.setPrefWidth(350);
        final TextField inputField = new TextField(defaultValue);
        inputField.setTranslateY(-5);
        Platform.runLater(inputField::selectAll);

        // action button text setup.
        HBox buttonBar = new HBox(20);
        final Button submitButton = new Button(getString("dialog.submit"));
        submitButton.setDefaultButton(true);
        final Button cancelButton = new Button(getString("dialog.cancel"));
        cancelButton.setCancelButton(true);
        ColorAdjust bleach = new ColorAdjust();
        bleach.setSaturation(-0.6);
        cancelButton.setEffect(bleach);
        buttonBar.getChildren().addAll(submitButton, cancelButton);

        // layout the popup.
        setPadding(new Insets(10));
        getStyleClass().add("alert-dialog");
        getChildren().addAll(promptMessage, inputField, buttonBar);

        final DropShadow dropShadow = new DropShadow();
        setEffect(dropShadow);

        // submit and close the popup.
        submitButton.setOnAction(submitHandler);

        // submit and close the popup.
        cancelButton.setOnAction(cancelHandler);
    }
}
