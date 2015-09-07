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

import javafx.concurrent.Worker;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A control to monitor worker progress.
 *
 * Used for showing progress of WebView loading.
 */
public class LoadingProgressDisplay extends ProgressBar {

    /**
     * Creates a UI display monitor for provided worker.
     *
     * Assumes the worker is reporting progress as work done on a scale from 0 to 100 (other values indicate indeterminate progress).
     *
     * @param worker the worker whose progress is to be monitored and displayed.
     */
    public LoadingProgressDisplay(Worker worker) {
        setMaxWidth(Double.MAX_VALUE);

        ColorAdjust bleach = new ColorAdjust();
        bleach.setSaturation(-0.6);
        setEffect(bleach);

        HBox.setHgrow(this, Priority.ALWAYS);

        visibleProperty().bind(worker.runningProperty());

        // as the webview load progresses update progress.
        worker.workDoneProperty().addListener((observableValue, oldNumber, newNumber) -> {
            if (newNumber == null) newNumber = -1.0;
            final double newValue = newNumber.doubleValue();
            if (newValue < 0.0 || newValue > 100.0) {
                setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            }
            setProgress(newValue / 100.0);
        });
    }

}
