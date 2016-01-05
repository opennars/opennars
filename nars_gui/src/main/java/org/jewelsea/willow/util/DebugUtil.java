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

package org.jewelsea.willow.util;

import javafx.scene.Node;
import javafx.scene.Parent;

/** Some debugging utilties for the JavaFX SceneGraph. */
public enum DebugUtil {
    ;

    /** Debugging routine to dump the scene graph. */
    public static void dump(Node n) {
        dump(n, 0);
    }

    private static void dump(Node n, int depth) {
        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println(n);
        if (n instanceof Parent) {
            for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
                dump(c, depth + 1);
            }
        }
    }

    /** Debugging routine to highlight the borders of nodes. **/
    public static void highlight(Node n) {
        highlight(n, 0);
    }

    private static void highlight(Node n, int depth) {
        n.setStyle("-fx-stroke: red; -fx-stroke-width: 1; -fx-stroke-type: inside;");
        if (n instanceof Parent) {
            for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
                highlight(c, depth + 1);
            }
        }
    }

}
