/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2013 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package automenta.vivisect.gui;

import java.util.ArrayList;

import processing.core.PApplet;

/**
 * This class will be used to safely close windows provided that their actionOnClose 
 * action is G4P.CLOSE_WINODW. <br>
 * This is done here, outside the windows normal Processing event loop to avoid
 * concurrent access errors. <br>
 * This class has to be declared public so it can register the post event, but it should
 * not be used directly. <br>
 * To close a window call the GWinodw close() method.
 *  
 * @author Peter Lager
 *
 */
public class GWindowCloser {

	
	private ArrayList<GWindow> toDisposeOf;
		
		GWindowCloser() {
			toDisposeOf = new ArrayList<GWindow>();
		}
		
		public void addWindow(GWindow gwindow){
			toDisposeOf.add(gwindow);
		}
		
		public void post(){
			// System.out.println("Window to dispose " + toDisposeOf.size());
			if(!toDisposeOf.isEmpty()){
				for(GWindow gwindow : toDisposeOf){
					PApplet wapp = gwindow.papplet;
					GWindowInfo winfo = G4P.windows.get(wapp);
					if(winfo != null){
						// This will the on-close-window event handler to 
						// be called if it exists
						gwindow.onClose();
						winfo.dispose();
						G4P.windows.remove(wapp);
						gwindow.dispose();
					}
				}
				toDisposeOf.clear();
			}
		}
	
	
}
