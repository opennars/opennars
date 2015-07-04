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

package automenta.vnc.viewer.swing;

import automenta.vnc.rfb.IRepaintController;
import automenta.vnc.rfb.client.PointerEventMessage;
import automenta.vnc.rfb.protocol.ProtocolContext;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseEventListener extends MouseInputAdapter
implements MouseWheelListener {
	private static final byte BUTTON_LEFT = 1;
	private static final byte BUTTON_MIDDLE = 1 << 1;
	private static final byte BUTTON_RIGHT = 1 << 2;
	private static final byte WHEEL_UP = 1 << 3;
	private static final byte WHEEL_DOWN = 1 << 4;

	private final IRepaintController repaintController;
	private final ProtocolContext context;
	private volatile double scaleFactor;

	public MouseEventListener(IRepaintController repaintController, ProtocolContext context,
			double scaleFactor) {
		this.repaintController = repaintController;
		this.context = context;
		this.scaleFactor = scaleFactor;
	}

	public void processMouseEvent(MouseEvent mouseEvent,
			MouseWheelEvent mouseWheelEvent, boolean moved) {
		byte buttonMask = 0;
		if (null == mouseEvent && mouseWheelEvent != null) {
			mouseEvent = mouseWheelEvent;
		}
		assert mouseEvent != null;
		short x = (short) (mouseEvent.getX() / scaleFactor);
		short y = (short) (mouseEvent.getY() / scaleFactor);
		if (moved) {
			repaintController.updateCursorPosition(x, y);
		}

		int modifiersEx = mouseEvent.getModifiersEx();
		// left
		buttonMask |= (modifiersEx & InputEvent.BUTTON1_DOWN_MASK) != 0 ?
				BUTTON_LEFT : 0;
		// middle
		buttonMask |= (modifiersEx & InputEvent.BUTTON2_DOWN_MASK) != 0 ?
				BUTTON_MIDDLE : 0;
		// right
		buttonMask |= (modifiersEx & InputEvent.BUTTON3_DOWN_MASK) != 0 ?
				BUTTON_RIGHT : 0;

		// wheel
		if (mouseWheelEvent != null) {
			int notches = mouseWheelEvent.getWheelRotation();
			byte wheelMask = notches < 0 ? WHEEL_UP : WHEEL_DOWN;
			// handle more then 1 notches
			notches = Math.abs(notches);
			for (int i=1; i<notches; ++i) {
				context.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
				context.sendMessage(new PointerEventMessage(buttonMask, x, y));
			}
			context.sendMessage(new PointerEventMessage((byte) (buttonMask | wheelMask), x, y));
		}
		context.sendMessage(new PointerEventMessage(buttonMask, x, y));
        onMouseEvent(mouseEvent, mouseWheelEvent, moved);
	}

    protected void onMouseEvent(MouseEvent mouseEvent, MouseWheelEvent mouseWheelEvent, boolean moved) {
        /** for overriding in subclasses */
    }

    @Override
	public void mousePressed(MouseEvent mouseEvent) {
		processMouseEvent(mouseEvent, null, false);
	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent) {
		processMouseEvent(mouseEvent, null, false);
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		processMouseEvent(mouseEvent, null, true);
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		processMouseEvent(mouseEvent, null, true);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent emouseWheelEvent) {
		processMouseEvent(null, emouseWheelEvent, false);
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
}
