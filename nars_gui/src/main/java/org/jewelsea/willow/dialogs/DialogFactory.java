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

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.util.Callback;

public class DialogFactory {
    private final WebView webView;

    public DialogFactory(WebView webView) {
        this.webView = webView;
    }

    public EventHandler<WebEvent<String>> createAlertHandler() {
        return stringWebEvent -> {
            AlertHandler alertHandler = new AlertHandler(
                    stringWebEvent.getData(),
                    event -> {
                        webView.setDisable(false);
                        removeViewOverlay();
                    }
            );
            overlayView(alertHandler);

            // todo block until the user accepts the alert.
        };
    }

    public Callback<String, Boolean> createConfirmHandler() {
        return message -> {
            ConfirmHandler confirmHandler = new ConfirmHandler(
                    message,
                    event -> {
                        webView.setDisable(false);
                        removeViewOverlay();
                    },
                    event -> {
                        webView.setDisable(false);
                        removeViewOverlay();
                    }
            );
            overlayView(confirmHandler);

            // todo block until the user confirms or denies the action.
            return true;
        };
    }

    public Callback<PromptData, String> createPromptHandler() {
        return promptData -> {
            PromptHandler promptHandler = new PromptHandler(
                    promptData.getMessage(),
                    promptData.getDefaultValue(),
                    event -> {
                        webView.setDisable(false);
                        removeViewOverlay();
                    },
                    event -> {
                        webView.setDisable(false);
                        removeViewOverlay();
                    }
            );
            overlayView(promptHandler);

            // todo block until the user confirms or denies the action.
            return promptData.getDefaultValue();
        };
    }

    /**
     * Overlay a dialog on top of the WebView.
     *
     * @param dialogNode the dialog to overlay on top of the view.
     */
    private void overlayView(Node dialogNode) {
        // if the view is already overlaid we will just ignore this overlay call silently . . . todo probably not the best thing to do, but ok for now.
        if (!(webView.getParent() instanceof BorderPane)) return;

        // record the view's parent.
        BorderPane viewParent = (BorderPane) webView.getParent();

        // create an overlayPane layering the popup on top of the webview
        StackPane overlayPane = new StackPane();
        overlayPane.getChildren().addAll(webView, new Group(dialogNode));
        webView.setDisable(true);

        // overlay the popup on the webview.
        viewParent.setCenter(overlayPane);
    }

    /**
     * Removes an existing dialog overlaying a WebView.
     */
    private void removeViewOverlay() {
        BorderPane viewParent = (BorderPane) webView.getParent().getParent();
        viewParent.setCenter(webView);
    }
}
