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

package automenta.vivisect.javafx.willow.sidebar;

import automenta.vivisect.javafx.WebBrowser;
import automenta.vivisect.javafx.willow.navigation.BookmarkHandler;
import automenta.vivisect.javafx.willow.util.ResourceUtil;
import automenta.vivisect.javafx.willow.widgets.IconButton;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static automenta.vivisect.javafx.willow.util.ResourceUtil.getString;

/**
 * Sidebar panel for showing demos
 */
public class NavigationPanel extends TitledPane {
    final ContextMenu canvasMenu = new ContextMenu();

    static final String[][] defaultBookmarks = {
            { "FX Experience",    "http://fxexperience.com/" },
            { "Jewelsea",         "http://jewelsea.wordpress.com/" },
            { "JavaFX Tutorials", "http://docs.oracle.com/javafx/" },
            { "JavaFX Javadoc",   "http://docs.oracle.com/javafx/2/api/index.html" },
            { "JavaFX Forums",    "https://forums.oracle.com/forums/forum.jspa?forumID=1385&start=0" },
            { "JavaFX StackOverflow", "http://stackoverflow.com/questions/tagged/javafx+javafx-2" }
    };

    public NavigationPanel(final WebBrowser chrome) {
        // create a home button to navigate home.
        final Button homeButton = new IconButton(
                getString("nav-toolbar.home"),
                "Fairytale_folder_home.png",
                getString("nav-toolbar.home.tooltip"),
                actionEvent -> chrome.getBrowser().go(chrome.homeLocationProperty.get())
        );
        homeButton.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        homeButton.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                chrome.homeLocationProperty.set(db.getString());
                success = true;
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

        // create a history button to show the history.
        final Button historyButton = new IconButton(
                getString("nav-panel.history"),
                "History.png",
                getString("nav-panel.history.tooltip"),
                null
        );
        historyButton.setOnAction(e ->
                chrome.getBrowser().getHistory().showMenu(historyButton)
        );

        // create a bookmarksButton.
        final ContextMenu bookmarksMenu = new ContextMenu();
        final Button bookmarksButton = new IconButton(
                getString("nav-panel.bookmarks"),
                "1714696718.png",
                getString("nav-panel.bookmarks.tooltip"),
                null
        );
        bookmarksButton.setOnAction(actionEvent ->
                bookmarksMenu.show(bookmarksButton, Side.BOTTOM, 0, 0)
        );
        bookmarksButton.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        bookmarksButton.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                // add the dragged url to the bookmarks menu (if it wasn't already there).
                final String bookmarkUrl = db.getString();
                if (BookmarkHandler.installBookmark(chrome, bookmarksMenu, bookmarkUrl, bookmarkUrl)) {
                    success = true;
                }
            }
            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });

        // create a slider to manage the fontSize
        final Slider fontSize = new Slider(0.75, 1.515, 1.0);
        fontSize.setTooltip(new Tooltip(getString("nav-panel.fontsize.tooltip")));
        fontSize.setMajorTickUnit(0.25);
        fontSize.setMinorTickCount(0);
        fontSize.setShowTickMarks(true);
        fontSize.setBlockIncrement(0.1);
        //fontSize.valueProperty().addListener((observableValue, oldValue, newValue) ->
                //chrome.getBrowser().getView().setFontScale(newValue.doubleValue())
        //);



        //AwesomeDude.setIcon(fontsizer, AwesomeIcon.TEXT_HEIGHT);


//        // create a reader button.
//        final Button readerButton = new IconButton(
//                getString("nav-panel.read"),
//                "readability.png",
//                getString("nav-panel.read.tooltip"),
//                actionEvent -> {
//                    chrome.getBrowser().getView().getEngine().executeScript(
//                            "window.readabilityUrl='" + chrome.getBrowser().getLocField().getText() + "';var s=document.createElement('script');s.setAttribute('type','text/javascript');s.setAttribute('charset','UTF-8');s.setAttribute('src','http://www.readability.com/bookmarklet/read.js');document.documentElement.appendChild(s);"
//                    );
//                }
//        );
//
        // create a box for displaying navigation options.
        VBox navigationBox = new VBox();
        navigationBox.setSpacing(5);
        navigationBox.setStyle("-fx-padding: 5");
        navigationBox.getChildren().addAll(homeButton, historyButton, bookmarksButton /*readerButton,*/ /*fontsizer*/);
        final TitledPane navPanel = new TitledPane(getString("nav-panel.title"), navigationBox);
        navPanel.getStyleClass().add("sidebar-panel");

        // create an initial set of bookmarks.
        for (String[] bookmark : defaultBookmarks) {
            BookmarkHandler.installBookmark(chrome, bookmarksMenu, bookmark[0], bookmark[1]);
        }

        setText(getString("nav-panel.title"));
        setContent(navigationBox);
        getStyleClass().add("sidebar-panel");
        setExpanded(true);
    }
}
