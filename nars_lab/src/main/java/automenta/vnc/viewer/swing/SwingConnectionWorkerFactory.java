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

import automenta.vnc.rfb.client.ClientToServerMessage;
import automenta.vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import automenta.vnc.viewer.AbstractConnectionWorkerFactory;
import automenta.vnc.viewer.ConnectionPresenter;
import automenta.vnc.viewer.NetworkConnectionWorker;
import automenta.vnc.viewer.RfbConnectionWorker;

import javax.swing.*;

/**
 * @author dime at tightvnc.com
 */
public class SwingConnectionWorkerFactory extends AbstractConnectionWorkerFactory {

    private final JFrame parentWindow;
    private String predefinedPassword;
    private final ConnectionPresenter presenter;
    private final SwingViewerWindowFactory viewerWindowFactory;

    public SwingConnectionWorkerFactory(JFrame parentWindow, String predefinedPassword, ConnectionPresenter presenter,
                                        SwingViewerWindowFactory viewerWindowFactory) {
        this.parentWindow = parentWindow;
        this.predefinedPassword = predefinedPassword;
        this.presenter = presenter;
        this.viewerWindowFactory = viewerWindowFactory;
    }

    @Override
    public NetworkConnectionWorker createNetworkConnectionWorker() {
        return new SwingNetworkConnectionWorker(parentWindow);
    }

    @Override
    public RfbConnectionWorker createRfbConnectionWorker() {
        SwingRfbConnectionWorker w = new SwingRfbConnectionWorker(predefinedPassword, presenter, parentWindow, viewerWindowFactory) {

            @Override
            protected void frameBufferUpdate(automenta.vnc.drawing.Renderer renderer, FramebufferUpdateRectangle rect) {
                presenter.frameBufferUpdate(renderer, rect);
            }

            @Override
            public void onMessageSend(ClientToServerMessage message) {
                presenter.onMessageSend(message);
            }
        };
        return w;
    }




    @Override
    public void setPredefinedPassword(String predefinedPassword) {
        this.predefinedPassword = predefinedPassword;
    }
}
