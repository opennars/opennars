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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jewelsea.willow.browser.WebBrowser;

public class SideBar {
	private final ScrollPane sideBarScroll;
	private final VBox bar;
	private final VBox progressHolder;

	/**
	 * Create a private contructor so you can only create a sidebar via factory
	 * methods
	 */
	private SideBar(VBox bar, VBox progressHolder) {
		this.bar = bar;
		this.progressHolder = progressHolder;
		sideBarScroll = new ScrollPane(bar);
		sideBarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sideBarScroll.getStyleClass().add("sidebar-scroll");
	}

	/**
	 * Factory method for creating a new sidebar.
	 * 
	 * @param chrome
	 *            the chrome the sidebar will be placed into.
	 * @return the new sidebar.
	 */
	public static SideBar createSidebar(WebBrowser chrome) {
		// layout holder for the sidebar.
		VBox bar = new VBox();
		bar.getStyleClass().add("sidebar-background");

		// create a spacer for the sidebar.
		VBox spacer = new VBox();
		spacer.getStyleClass().add("sidebar-background");
		VBox.setVgrow(spacer, Priority.ALWAYS);
		spacer.setAlignment(Pos.BOTTOM_CENTER);

		// create sidebar panels.
		TitledPane navigationPanel = new NavigationPanel(chrome);
		// final TitledPane developerPanel = new DeveloperPanel(chrome);
		TitledPane demoPanel = new DemoPanel(chrome);
		TitledPane benchPanel = new BenchPanel(chrome);

		// size all of the panes similarly.
		navigationPanel.prefWidthProperty()
				.bind(benchPanel.prefWidthProperty());
		// developerPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());
		demoPanel.prefWidthProperty().bind(benchPanel.prefWidthProperty());

		// put the panes inside the sidebar.
		bar.getChildren().addAll(navigationPanel,
		// developerPanel,
				demoPanel, benchPanel, spacer);

		return new SideBar(bar, spacer);
	}

	/**
	 * Set the load control attached to the sidebar
	 */
	public void setLoadControl(Node loadControl) {
		VBox.setMargin(loadControl, new Insets(5, 5, 10, 5));
		progressHolder.getChildren().clear();
		progressHolder.getChildren().add(loadControl);
	}

	/**
	 * Returns the sidebar display
	 */
	public VBox getBarDisplay() {
		return bar;
	}

	public ScrollPane getScroll() {
		return sideBarScroll;
	}
}

// todo add an autohide to the bar if it hasn't been used for a while.
// todo history in the sidebar should actually be chrome wide rather than
// browser tab specific.
// todo some kind of persistence framework is needed.

// todo file jira ability to set the initial offset of a slider
