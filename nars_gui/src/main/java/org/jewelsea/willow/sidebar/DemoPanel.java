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

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.browser.WebBrowser;
import org.jewelsea.willow.navigation.BookmarkHandler;
import org.jewelsea.willow.widgets.IconButton;

import static org.jewelsea.willow.util.ResourceUtil.getString;

/**
 * Sidebar panel for showing demos
 */
public class DemoPanel extends TitledPane {
    final ContextMenu canvasMenu = new ContextMenu();

    static final String[][] canvasBookmarks = {
            { "3D Bart Simpson", "http://www.zynaps.com/site/experiments/environment.html?mesh=bart.wft" },
            { "Cloth Simulation", "http://andrew-hoyer.com/experiments/cloth/" },
            { "Canvas Cycle", "http://www.effectgames.com/demos/canvascycle/" },
            { "Fractal Graphics", "http://www.kevs3d.co.uk/dev/lsystems/" }
//    "http://mugtug.com/sketchpad/",                 sketchpad gives ES2 Vram Pool errors, so disabled it.
    };

    public DemoPanel(WebBrowser chrome) {
        // create a canvas demos button.
        Button canvasButton = new IconButton(
                getString("demo-panel.canvas-demos"),
                "canvas.jpg",
                getString("demo-panel.canvas-demos.tooltip"),
                null
        );
        canvasButton.setOnAction(actionEvent ->
                canvasMenu.show(canvasButton, Side.BOTTOM, 0, 0)
        );
        for (String[] bookmark : canvasBookmarks) {
            BookmarkHandler.installBookmark(chrome, canvasMenu, bookmark[0], bookmark[1]);
        }

        // create a box for demos.
        VBox demoBox = new VBox();  // todo generalize this title stuff creation for sidebar items.
        demoBox.setSpacing(5);
        demoBox.setStyle("-fx-padding: 5");
        demoBox.getChildren().addAll(canvasButton);

        setText(getString("demo-panel.title"));
        setContent(demoBox);
        getStyleClass().add("sidebar-panel");
        setExpanded(false);
    }
}
