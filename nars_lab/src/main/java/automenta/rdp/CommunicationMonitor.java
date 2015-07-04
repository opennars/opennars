/* CommunicationMonitor.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Provide a lock for network communications,
 *          used primarily by the Clipboard channel to
 *          prevent sending of mouse/keyboard input when
 *          sending large amounts of clipboard data.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 *          
 */

package automenta.rdp;

public class CommunicationMonitor {

	public static Boolean synch = true;

//	/**
//	 * Identify whether or not communications are locked
//	 *
//	 * @return True if locked
//	 */
//	public static boolean locked() {
//		return locker != null;
//	}

//	/**
//	 * Wait for a lock on communications
//	 *
//	 * @param o
//	 *            Calling object should supply reference to self
//	 */
//	public static void lock(Object o) {
//		if (locker == null)
//			locker = o;
//		else {
//			while (locker != null) {
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					System.err.println("InterruptedException: "
//							+ e.getMessage());
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	/**
//	 * Unlock communications, only permitted if the caller holds the current
//	 * lock
//	 *
//	 * @param o
//	 *            Calling object should supply reference to self
//	 * @return
//	 */
//	public static boolean unlock(Object o) {
//		if (locker == o) {
//			locker = null;
//			return true;
//		}
//		return false;
//	}

}
