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

package org.jewelsea.willow.browser;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jewelsea.willow.dialogs.DialogFactory;
import org.jewelsea.willow.helpers.FavIconHandler;
import org.jewelsea.willow.helpers.LocationHandler;
import org.jewelsea.willow.navigation.History;

import static org.jewelsea.willow.util.ResourceUtil.copyImageView;
import static org.jewelsea.willow.util.ResourceUtil.getString;

/**
 * A single web browser window to be displayed in a tab.
 */
public class BrowserWindow {
    private final WebView view;
    private final History history = new History(this);
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();
    private final ReadOnlyObjectWrapper<ImageView> favicon = new ReadOnlyObjectWrapper<>();
    private final FavIconHandler favIconHandler = FavIconHandler.getInstance();
    private final DialogFactory dialogFactory ;

    /** the location the browser engine is currently pointing at (or where the user can type in where to go next). */
    private final TextField locField = new TextField();

    public BrowserWindow(WebView view) {
        this.view = view;
        
        dialogFactory = new DialogFactory(view);
        
        // init the location text field.
        HBox.setHgrow(locField, Priority.ALWAYS);
        locField.setPromptText(getString("location.prompt"));
        locField.setTooltip(new Tooltip(getString("location.tooltip")));
        locField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                go(locField.getText());
            }
        });
        locField.focusedProperty().addListener((observableValue4, from, to) -> {
            if (to) {
                Platform.runLater(locField::selectAll);
            }
        });

        // make the location field draggable.
        getLocField().getStyleClass().add("location-field");
        getLocField().setOnDragDetected(mouseEvent -> {
            Dragboard db = getLocField().startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(getLocField().getText());
            db.setContent(content);
        });

        // monitor the web view for when it's location changes, so we can update the history lists and other items correctly.
        WebEngine engine = getView().getEngine();
        engine.locationProperty().addListener((observableValue3, oldLoc1, newLoc) -> {
            getHistory().executeNav(newLoc); // update the history lists.
            getLocField().setText(newLoc);   // update the location field.
            favicon.set(copyImageView(favIconHandler.fetchFavIcon(newLoc)));
        });

        // monitor the web views loading state so we can provide progress feedback.
        Worker<Void> worker = engine.getLoadWorker();
        worker.stateProperty().addListener((observableValue, oldState, newState) -> {
            // todo we actually don't have anything interesting to do at the moment.
        });

        worker.exceptionProperty().addListener((observableValue, oldThrowable, newThrowable) ->
                System.out.println("Browser encountered a load exception: " + newThrowable)
        );

        // create handlers for javascript actions and status changes.
        engine.setPromptHandler(dialogFactory.createPromptHandler());
        engine.setConfirmHandler(dialogFactory.createConfirmHandler());
        engine.setOnAlert(dialogFactory.createAlertHandler());
        engine.setOnStatusChanged(stringWebEvent ->
                status.setValue(stringWebEvent.getData())
        );

        // monitor the location url, and if it is a pdf file, then create a pdf viewer for it.
        getLocField().textProperty().addListener((observableValue, oldLoc, newLoc) ->
                LocationHandler.handleLocation(view, newLoc)
        );

        // add an effect for disabling and enabling the view.
        getView().disabledProperty().addListener(new ChangeListener<Boolean>() {
            final BoxBlur soften = new BoxBlur();
            final ColorAdjust dim = new ColorAdjust();
            {
                dim.setInput(soften);
                dim.setBrightness(-0.5);
            }

            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    getView().setEffect(dim);
                } else {
                    getView().setEffect(null);
                }
            }
        });
    }

    public TextField getLocField() {
        return locField;
    }

    public ReadOnlyStringProperty statusProperty() {
        return status.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<ImageView> faviconProperty() {
        return favicon.getReadOnlyProperty();
    }

    public History getHistory() {
        return history;
    }

    public WebView getView() {
        return view;
    }

    public void go(String loc) {
        // modify the request location, to make it easier on the user for typing.
        if (loc == null) loc = "";
        //noinspection IfStatementWithTooManyBranches
        if (loc.startsWith("google")) { // search google
            loc = "http://www.google.com/search?q=" + loc.substring("google".length()).trim().replaceAll(" ", "+");
        } else if (loc.startsWith("bing")) { // search bing
            loc = "http://www.bing.com/search?q=" + loc.substring("bing".length()).trim().replaceAll(" ", "+");
        } else if (loc.startsWith("yahoo")) { // search yahoo
            loc = "http://search.yahoo.com/search?p=" + loc.substring("yahoo".length()).trim().replaceAll(" ", "+");
        } else if (loc.startsWith("wiki")) {
            loc = "http://en.wikipedia.org/w/index.php?search=" + loc.substring("wiki".length()).trim().replaceAll(" ", "+");
        } else if (loc.startsWith("find")) { // search default (google) due to keyword
            loc = "http://www.google.com/search?q=" + loc.substring("find".length()).trim().replaceAll(" ", "+");
        } else if (loc.startsWith("search")) { // search default (google) due to keyword
            loc = "http://www.google.com/search?q=" + loc.substring("search".length()).trim().replaceAll(" ", "+");
        } else if (loc.contains(" ")) { // search default (google) due to space
            loc = "http://www.google.com/search?q=" + loc.trim().replaceAll(" ", "+");
        } else if (!(loc.startsWith("http://") || loc.startsWith("https://")) && !loc.isEmpty()) {
            loc = "http://" + loc;  // default to http
        }

        // ask the webview to navigate to the given location.
        if (!loc.equals(view.getEngine().getLocation())) {
            if (!loc.isEmpty()) {
                view.getEngine().load(loc);
            } else {
                view.getEngine().loadContent("");
            }
        } else {
            view.getEngine().reload();
        }

        // webview will grab the focus if automatically if it has an html input control to display, but we want it
        // to always grab the focus and kill the focus which was on the input bar, so just set ask the platform to focus
        // the web view later (we do it later, because if we did it now, the default focus handling might kick in and override our request).
        Platform.runLater(view::requestFocus);
    }

}

// todo cleanup the javascript prompt handlers as their code could be collapsed.
// todo log jira issue on browser load work progress not being updated.
// todo add better favicon support (not just for icos, but for pngs, etc.)
// todo how to set the save filename.
// todo file an jira bug request - modifying the list of items in a context menu makes the menus focus model go strange (doesn't appear to...)