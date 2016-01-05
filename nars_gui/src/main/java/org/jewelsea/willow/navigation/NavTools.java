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

package org.jewelsea.willow.navigation;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jewelsea.willow.browser.WebBrowser;
import org.jewelsea.willow.util.ResourceUtil;

import static org.jewelsea.willow.util.ResourceUtil.getString;

public enum NavTools {
    ;
    static final int buttonHeight = 20;

    public static Pane createNavPane(WebBrowser chrome) {
        // create a back button.
        Button backButton = new Button();
        backButton.setId("backButton"); // todo I don't like this id set just for lookup - reference would be better
        backButton.setTooltip(new Tooltip(getString("nav-toolbar.back.tooltip")));
                        
        
        //final ImageView backGraphic = new ImageView(ResourceUtil.getImage("239706184.png"));
        //FontAwesomeIcon.setIcon(backButton, FontAwesomeIcon.BACKWARD);
        
  //      final ColorAdjust backColorAdjust = new ColorAdjust();
//        backColorAdjust.setBrightness(-0.1);
//        backColorAdjust.setContrast(-0.1);
        //backGraphic.setEffect(backColorAdjust);                
        //backButton.setGraphic(backGraphic);
        //backGraphic.setPreserveRatio(true);
        //backGraphic.setFitHeight(buttonHeight);        

        backButton.onActionProperty().set(actionEvent -> {
            if (chrome.getBrowser().getHistory()!=null)
                if (chrome.getBrowser().getHistory().canNavBack()) {
                    chrome.getBrowser().go(chrome.getBrowser().getHistory().requestNavBack());
                }
        });
        backButton.setOnMouseReleased(mouseEvent -> {
            if (chrome.getBrowser().getHistory()!=null)
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    chrome.getBrowser().getHistory().showMenu(backButton);
                }
        });

        // create a forward button.
        Button forwardButton = new Button();
        forwardButton.setId("forwardButton"); // todo I don't like this id set just for lookup - reference would be better
        forwardButton.setTranslateX(-2);
        //AwesomeDude.setIcon(forwardButton, AwesomeIcon.FORWARD);
        
        //final ImageView forwardGraphic = new ImageView(ResourceUtil.getImage("1813406178.png"));
        //final ColorAdjust forwardColorAdjust = new ColorAdjust();
//        forwardColorAdjust.setBrightness(-0.1);
//        forwardColorAdjust.setContrast(-0.1);
//        forwardGraphic.setEffect(forwardColorAdjust);
//        forwardGraphic.setPreserveRatio(true);
//        forwardGraphic.setFitHeight(buttonHeight);
//        forwardButton.setGraphic(forwardGraphic);
        forwardButton.setTooltip(new Tooltip(getString("nav-toolbar.forward.tooltip")));
        forwardButton.onActionProperty().set(actionEvent -> {
            if (chrome.getBrowser().getHistory()!=null) 
                if (chrome.getBrowser().getHistory().canNavForward()) {
                    chrome.getBrowser().go(chrome.getBrowser().getHistory().requestNavForward());
                }
        });
        forwardButton.setOnMouseReleased(mouseEvent -> {
            if (chrome.getBrowser().getHistory()!=null)
                if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                    chrome.getBrowser().getHistory().showMenu(backButton);
                }
        });

        // create a navigate button.
        Button navButton = new Button();
        navButton.setTooltip(new Tooltip(getString("nav-toolbar.go.tooltip")));
        ImageView navGraphic = new ImageView(ResourceUtil.getImage("Forward Arrow.png"));
        ColorAdjust navColorAdjust = new ColorAdjust();
        navColorAdjust.setContrast(-0.7);
        navGraphic.setEffect(navColorAdjust);
        navGraphic.setPreserveRatio(true);
        navGraphic.setFitHeight(14);
        navButton.setGraphic(navGraphic);
        navButton.onActionProperty().set(actionEvent ->
                chrome.getBrowser().go(chrome.getBrowser().getLocation())
        );

        // create a button to hide and show the sidebar.
        Button sidebarButton = new Button();
        sidebarButton.setId("sidebarButton");
        ImageView sidebarGraphic = new ImageView(ResourceUtil.getImage("Down Arrow.png"));
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(-0.7);
        sidebarGraphic.setFitHeight(10);
        sidebarGraphic.setPreserveRatio(true);
        sidebarButton.setScaleX(0.8);
        sidebarButton.setScaleY(0.8);
        sidebarGraphic.setEffect(colorAdjust);
        sidebarButton.setGraphic(sidebarGraphic);
        sidebarButton.setTooltip(new Tooltip(getString("nav-toolbar.sidebar-visibility.tooltip")));
        sidebarButton.setStyle("-fx-font-weight: bold;");
        /*sidebarButton.setOnAction(event ->
                SlideAnimator.slide(sidebarButton, chrome.getSidebar().getScroll(), Side.LEFT)
        );*/

        Button fullscreenButton = new Button();
        fullscreenButton.setTooltip(new Tooltip(getString("nav-toolbar.fullscreen.tooltip")));
        ImageView fullscreenGraphic = new ImageView(
                ResourceUtil.getImage("1325834738_gtk-fullscreen.png")
        );
        fullscreenGraphic.setEffect(colorAdjust);
        fullscreenGraphic.setPreserveRatio(true);
        fullscreenGraphic.setFitHeight(14);
        fullscreenButton.setGraphic(fullscreenGraphic);
        fullscreenButton.setOnAction(actionEvent -> {
            Stage stage = (Stage) fullscreenButton.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        });

        // align all of the navigation widgets in a horizontal toolbar.
        HBox navPane = new HBox();
        navPane.setAlignment(Pos.CENTER);
        navPane.getStyleClass().add("toolbar");
        navPane.setSpacing(5);
        navPane.getChildren().addAll(
                sidebarButton,
                backButton,
                forwardButton,
                chrome.getChromeLocField(),
                chrome.getTabManager().getTabPane(),
                chrome.getTabManager().getNewTabButton(),
                navButton,
                fullscreenButton
        );
        navPane.setFillHeight(false);
        Platform.runLater(() -> navPane.setMinHeight(navPane.getHeight()));

        /*final InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(Color.ANTIQUEWHITE);
        navPane.setEffect(innerShadow);*/

        return navPane;
    }
}

// todo while a page is loading we might want to switch the favicon to a loading animation...
// todo may also want to add the following ot the navbar... /* createLoadIndicator(), */ /* browser.favicon, */
// todo add ability to save pdfs and other docs.