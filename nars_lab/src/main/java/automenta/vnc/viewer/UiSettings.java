// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package automenta.vnc.viewer;

import automenta.vnc.core.SettingsChangedEvent;
import automenta.vnc.rfb.IChangeSettingsListener;
import automenta.vnc.viewer.swing.LocalMouseCursorShape;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dime at tightvnc.com
 */
public class UiSettings {

    public static final int MIN_SCALE_PERCENT = 10;
	public static final int MAX_SCALE_PERCENT = 500;
    private static final int SCALE_PERCENT_ZOOMING_STEP = 10;
	
	@SuppressWarnings("PointlessBitwiseExpression")
    public static final int CHANGED_SCALE_FACTOR = 1 << 0;
    public static final int CHANGED_MOUSE_CURSOR_SHAPE = 1 << 1;
    public static final int CHANGED_FULL_SCREEN = 1 << 2;

    private final List<IChangeSettingsListener> listeners = new LinkedList<>();
    private int changedSettingsMask = 0;

    private final UiSettingsData uiSettingsData;
    public boolean showControls = true;

    public UiSettings() {
        uiSettingsData = new UiSettingsData();
		changedSettingsMask = 0;
	}

	public UiSettings(UiSettings uiSettings) {
        uiSettingsData = new UiSettingsData(
                uiSettings.getScalePercent(), uiSettings.getMouseCursorShape(), uiSettings.isFullScreen());
        this.changedSettingsMask = uiSettings.changedSettingsMask;
    }

	public double getScaleFactor() {
		return uiSettingsData.getScalePercent() / 100.;
	}

	public void setScalePercent(double scalePercent) {
        if (this.uiSettingsData.setScalePercent(scalePercent)) {
		    changedSettingsMask |= CHANGED_SCALE_FACTOR;
        }
	}

	public void addListener(IChangeSettingsListener listener) {
		listeners.add(listener);
	}

	void fireListeners() {
        if (null == listeners) return;
		final SettingsChangedEvent event = new SettingsChangedEvent(new UiSettings(this));
		changedSettingsMask = 0;
		for (IChangeSettingsListener listener : listeners) {
			listener.settingsChanged(event);
		}
	}

    public void zoomOut() {
	    double oldScaleFactor = uiSettingsData.getScalePercent();
	    double scaleFactor = (int)(this.uiSettingsData.getScalePercent() / SCALE_PERCENT_ZOOMING_STEP) * SCALE_PERCENT_ZOOMING_STEP;
	    if (scaleFactor == oldScaleFactor) {
		    scaleFactor -= SCALE_PERCENT_ZOOMING_STEP;
	    }
	    if (scaleFactor < MIN_SCALE_PERCENT) {
		    scaleFactor = MIN_SCALE_PERCENT;
	    }
	    setScalePercent(scaleFactor);
	    fireListeners();
    }

    public void zoomIn() {
	    double scaleFactor = (int)(this.uiSettingsData.getScalePercent() / SCALE_PERCENT_ZOOMING_STEP) * SCALE_PERCENT_ZOOMING_STEP + SCALE_PERCENT_ZOOMING_STEP;
	    if (scaleFactor > MAX_SCALE_PERCENT) {
		    scaleFactor = MAX_SCALE_PERCENT;
	    }
	    setScalePercent(scaleFactor);
	    fireListeners();
    }

    public void zoomAsIs() {
	    setScalePercent(100);
	    fireListeners();
    }

	public void zoomToFit(int containerWidth, int containerHeight, int fbWidth, int fbHeight) {
		int scalePromille = Math.min(1000 * containerWidth / fbWidth,
				1000 * containerHeight / fbHeight);
		while (fbWidth * scalePromille / 1000. > containerWidth ||
				fbHeight * scalePromille / 1000. > containerHeight) {
			scalePromille -= 1;
		}
		setScalePercent(scalePromille / 10.);
		fireListeners();
	}

	public boolean isChangedMouseCursorShape() {
		return (changedSettingsMask & CHANGED_MOUSE_CURSOR_SHAPE) == CHANGED_MOUSE_CURSOR_SHAPE;
	}

	public static boolean isUiSettingsChangedFired(SettingsChangedEvent event) {
		return event.getSource() instanceof UiSettings;
	}

	public double getScalePercent() {
		return uiSettingsData.getScalePercent();
	}

	public String getScalePercentFormatted() {
		NumberFormat numberFormat = new DecimalFormat("###.#");
		return numberFormat.format(uiSettingsData.getScalePercent());
	}

    public LocalMouseCursorShape getMouseCursorShape() {
        return uiSettingsData.getMouseCursorShape();
    }

    public void setMouseCursorShape(LocalMouseCursorShape mouseCursorShape) {
        if (this.uiSettingsData.setMouseCursorShape(mouseCursorShape)) {
            changedSettingsMask |= CHANGED_MOUSE_CURSOR_SHAPE;
            fireListeners();
        }
    }

    public void copyDataFrom(UiSettingsData other) {
        copyDataFrom(other, 0);
    }
    public void copyDataFrom(UiSettingsData other, int mask) {
        if (null == other) return;
        if ((mask & CHANGED_SCALE_FACTOR) == 0) uiSettingsData.setScalePercent(other.getScalePercent());
        if ((mask & CHANGED_MOUSE_CURSOR_SHAPE) == 0) uiSettingsData.setMouseCursorShape(other.getMouseCursorShape());
        if ((mask & CHANGED_FULL_SCREEN) == 0) uiSettingsData.setFullScreen(other.isFullScreen());
    }

    public void setFullScreen(boolean isFullScreen) {
        if (uiSettingsData.setFullScreen(isFullScreen)) {
            changedSettingsMask |= CHANGED_FULL_SCREEN;
            fireListeners();
        }
    }

    public boolean isFullScreen() {
        return uiSettingsData.isFullScreen();
    }

    public UiSettingsData getData() {
        return uiSettingsData;
    }

    @Override
    public String toString() {
        return "UiSettings{" +
                "scalePercent=" + uiSettingsData.getScalePercent() +
                ", fullScreen=" + uiSettingsData.isFullScreen() +
                ", mouseCursorShape=" + uiSettingsData.getMouseCursorShape() +
                '}';
    }

}
