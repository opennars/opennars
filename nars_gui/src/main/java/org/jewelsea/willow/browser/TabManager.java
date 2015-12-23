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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;

import static org.jewelsea.willow.util.ResourceUtil.getString;

/**
 * Manages a set of active browser windows.
 */
public class TabManager {

    public static final double TAB_PANE_WIDTH = 400;

    /**
     * representation of the current browser.
     */
    private final ReadOnlyObjectWrapper<UITab> currentBrowser = new ReadOnlyObjectWrapper<>();

    /**
     * browser tabs.
     */
    public final TabPane tabPane = new TabPane();

    /**
     * button to open a new tab
     */
    private final Button newTabButton = new Button();

    /**
     * a location field in the chrome representing the location of the current tab
     * (can be null if the location is not represented in the chrome but only in the browser in the tab itself).
     */
    private final TextField chromeLocField;

    public TabManager(TextField locField) {
        chromeLocField = locField;

        // create a browser tab pane with a custom tab closing policy which does not allow the last tab to be closed.        
       
        tabPane.setMinWidth(TAB_PANE_WIDTH);
        tabPane.setTabMinWidth(50);
        tabPane.setTabMaxWidth(TAB_PANE_WIDTH);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            ObservableList<Tab> tabs = tabPane.getTabs();
            tabs.get(0).setClosable(tabs.size() > 1);
            for (int i = 1; i < tabs.size(); i++) {
                tabs.get(i).setClosable(true);
            }
            // todo work out a good max width
            // todo file jira setting max width on a initialTab pane is buggy as the close symbol is not usable if you change initialTab from closable to not closable.
            // todo file jira on initialTab pane set policy for closing icon display.
            tabPane.setTabMaxWidth(Math.max(50, TAB_PANE_WIDTH / Math.max(1, tabPane.getTabs().size() * 0.7)));
        });

        // monitor the selected tab in the tab pane so that we can set the TabManager's browser property appropriately.
        
        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) ->
                //if (newTab instanceof BrowserTab) {
                    currentBrowser.set((UITab)newTab)
                /*}
                else {
                    
                }*/
                
        );



        // create a button for opening a new tab.
        newTabButton.setTooltip(new Tooltip(getString("nav-toolbar.createtab.tooltip")));
        /*final ImageView tabGraphic = new ImageView(ResourceUtil.getImage("Plus.png"));
        newTabButton.setGraphic(tabGraphic);*/
        newTabButton.onActionProperty().set(actionEvent -> {
            BrowserTab newTab = new BrowserTab(this);
            newTab.setText(getString("newtab.title"));
            addTab(newTab);
        });
        
        ColorAdjust tabColorAdjust = new ColorAdjust();
        tabColorAdjust.setContrast(-0.7);
//        tabGraphic.setEffect(tabColorAdjust);
//        tabGraphic.setPreserveRatio(true);
//        tabGraphic.setFitHeight(14);
        
        //System.out.println("adding initial tab" + (System.currentTimeMillis() - WebBrowser.start)/1000.0);                
        // add the initialTab to the tabset.
        //addTab(new BrowserTab(c, this));
        

        //System.out.println("TabManager finish" + (System.currentTimeMillis() - WebBrowser.start)/1000.0);        
        
    }

    public UITab getBrowser() {
        return currentBrowser.get();
    }

    public ReadOnlyObjectProperty<UITab> browserProperty() {
        return currentBrowser.getReadOnlyProperty();
    }

    /**
     * @return the tabs which control the active browser window.
     */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * @return a button for opening a new tab.
     */
    public Button getNewTabButton() {
        return newTabButton;
    }

    /**
     * Places a new tab under management.
     * Selects the newly added tab so that it is visible.
     * Updates the chrome's location field to reflect the newly managed and visible tab.
     *
     * @param tab the browser tab to be added to the chrome.
     */
    void addTab(BrowserTab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
        if (chromeLocField != null) {
            chromeLocField.requestFocus();
        }
    }
    public void addTab(Tab tab) {
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().selectLast();
    }    

}
