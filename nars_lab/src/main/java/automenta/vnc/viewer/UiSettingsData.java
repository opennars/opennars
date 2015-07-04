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

import automenta.vnc.viewer.swing.LocalMouseCursorShape;

import java.io.Serializable;

/**
 * @author dime at tightvnc.com
 */
public class UiSettingsData implements Serializable {
    private static final long serialVersionUID = 1L;
    private double scalePercent;
    private LocalMouseCursorShape mouseCursorShape;
    private boolean fullScreen;


    public UiSettingsData() {
        scalePercent = 100;
        mouseCursorShape = LocalMouseCursorShape.DOT;
        fullScreen = false;
    }

    public UiSettingsData(double scalePercent, LocalMouseCursorShape mouseCursorShape, boolean fullScreen) {
        this.scalePercent = scalePercent;
        this.mouseCursorShape = mouseCursorShape;
        this.fullScreen = fullScreen;
    }

    public UiSettingsData(UiSettingsData other) {
        this(other.getScalePercent(), other.getMouseCursorShape(), other.isFullScreen());
    }

    public double getScalePercent() {
        return scalePercent;
    }

    public boolean setScalePercent(double scalePercent) {
        if (this.scalePercent != scalePercent) {
            this.scalePercent = scalePercent;
            return true;
        }
        return false;
    }


    public LocalMouseCursorShape getMouseCursorShape() {
        return mouseCursorShape;
    }

    public boolean setMouseCursorShape(LocalMouseCursorShape mouseCursorShape) {
        if (this.mouseCursorShape != mouseCursorShape && mouseCursorShape != null) {
            this.mouseCursorShape = mouseCursorShape;
            return true;
        }
        return false;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public boolean setFullScreen(boolean fullScreen) {
        if (this.fullScreen != fullScreen) {
            this.fullScreen = fullScreen;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "UiSettingsData{" +
                "scalePercent=" + scalePercent +
                ", mouseCursorShape=" + mouseCursorShape +
                ", fullScreen=" + fullScreen +
                '}';
    }
}