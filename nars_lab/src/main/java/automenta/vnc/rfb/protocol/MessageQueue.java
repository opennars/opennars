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

package automenta.vnc.rfb.protocol;

import automenta.vnc.rfb.client.ClientToServerMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author dime at tightvnc.com
 */
public class MessageQueue {
	private final BlockingQueue<ClientToServerMessage> queue;

	public MessageQueue() {
		queue = new LinkedBlockingQueue<>();
	}

	public void put(ClientToServerMessage message) {
		queue.offer(message);
	}

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     * Retrieves and removes the head of this queue, waiting up to the certain wait time if necessary for
     * an element to become available.
     * @return the head of this queue, or null if the specified waiting time elapses before an element is available
     * @throws InterruptedException - if interrupted while waiting
     */
	public ClientToServerMessage get() throws InterruptedException {
		return queue.poll(1, TimeUnit.SECONDS);
	}

}
