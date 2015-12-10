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

package org.jewelsea.willow.sidebar;

import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.browser.BrowserTab;
import org.jewelsea.willow.widgets.IconButton;

import static org.jewelsea.willow.util.ResourceUtil.getString;

/**
 * Sidebar panel for development tools
 */
public class DeveloperPanel extends TitledPane {
    @SuppressWarnings("HardcodedFileSeparator")
    public DeveloperPanel(BrowserTab b) {
        // create a firebug button.
        Button firebugButton = new IconButton(
                "Firebug",
                "firebug.png",
                getString("developer-panel.firebug.tooltip"),
                actionEvent -> b.getBrowser().getView().getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}")
        );

        // create a box for development tools.
        VBox developmentBox = new VBox();  // todo generalize this title stuff creation for sidebar items.
        developmentBox.setSpacing(5);
        developmentBox.setStyle("-fx-padding: 5");
        developmentBox.getChildren().addAll(firebugButton);

        setText(getString("developer-panel.title"));
        setContent(developmentBox);
        getStyleClass().add("sidebar-panel");
        setExpanded(false);
    }
}
