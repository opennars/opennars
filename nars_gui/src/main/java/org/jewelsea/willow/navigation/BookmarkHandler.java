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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.jewelsea.willow.browser.WebBrowser;


/** Creates a named bookmarked url to navigate to. */
public enum BookmarkHandler {
    ;

    /**
     * @param chrome        the browser chrome the bookmark is to control.
     * @param bookmarksMenu the menu into which the bookmark is to be installed.
     * @param bookmarkName  the name of the bookmark.
     * @param bookmarkUrl   the url of the bookmark.
     *
     * @return true if the bookmark was not already installed in the chrome, otherwise false.
     */
    public static boolean installBookmark(
            WebBrowser chrome,
            ContextMenu bookmarksMenu,
            String bookmarkName,
            String bookmarkUrl
    ) {
        for (MenuItem item : bookmarksMenu.getItems()) {
            if (item.getText().equals(bookmarkName)) {
                return false;
            }
        }

        MenuItem menuItem = new MenuItem(bookmarkName);
        menuItem.setOnAction(actionEvent -> chrome.getBrowser().go(bookmarkUrl));
        bookmarksMenu.getItems().add(menuItem);

        return true;
    }

}
