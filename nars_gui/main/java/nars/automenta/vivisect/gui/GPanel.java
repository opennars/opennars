/*
  Part of the GUI library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2012 Peter Lager

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

import automenta.vivisect.gui.HotSpot.HSrect;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.event.MouseEvent;


/**
 * A component that can be used to group GUI components that can be
 * dragged, collapsed (leaves title tab only) and un-collapsed.
 * 
 * When created the Panel is collapsed by default. To open the panel
 * use setCollapsed(true); after creating it. <br>
 * 
 * Once a component has been added the x/y coordinates of the control are 
 * calculated to be the centre of the panel to the centre of the control. This 
 * is to facilitate rotating of controls on panels 
 *  
 * @author Peter Lager
 *
 */
public class GPanel extends GTextBase {

	static protected int COLLAPSED_BAR_SPOT = 1;
	static protected int EXPANDED_BAR_SPOT = 2;
	static protected int SURFACE_SPOT = 0;


	/** Whether the panel is displayed in full or tab only */
	protected boolean tabOnly = false;

	/** The height of the tab calculated from font height + padding */
	protected int tabHeight, tabWidth;

	/** Used to restore position when closing panel */
	protected float dockX, dockY;

	// Defines the area that the panel must fit inside.
	protected float lowX, highX, lowY, highY;
	
	/** true if the panel is being dragged */
	protected boolean beingDragged = false;

	protected boolean draggable = true;
	protected boolean collapsible = true;


	/**
	 * Create a Panel that comprises of 2 parts the tab which is used to 
	 * select and move the panel and the container window below the tab which 
	 * is used to hold other components. <br>
	 * If the panel fits inside the display window then its position will be 
	 * constrained so that it can't be dragged outside the viewable area. 
	 * Otherwise no constraint is applied.
	 * 
	 * @param theApplet the PApplet reference
	 * @param p0 horizontal position
	 * @param p1 vertical position
	 * @param p2 width of the panel
	 * @param p3 height of the panel (excl. tab)
	 */
	public GPanel(PApplet theApplet, float p0, float p1, float p2, float p3) {
		this(theApplet, p0, p1, p2, p3, "Panel");
	}
	
	/**
	 * Create a Panel that comprises of 2 parts the tab which is used to 
	 * select and move the panel and the container window below the tab which 
	 * is used to hold other components. <br>
	 * If the panel fits inside the display window then its position will be 
	 * constrained so that it can't be dragged outside the viewable area. 
	 * Otherwise no constraint is applied.
	 *  
	 * @param theApplet the PApplet reference
	 * @param p0 horizontal position
	 * @param p1 vertical position
	 * @param p2 width of the panel
	 * @param p3 height of the panel (excl. tab)
	 * @param text to appear on tab
	 */
	public GPanel(PApplet theApplet, float p0, float p1, float p2, float p3, String text) {
		super(theApplet, p0, p1, p2, p3);
		// Set the values used to constrain movement of the panel
		if(x < 0 || y < 0 || x + width > winApp.width || y+  height > winApp.height)
			clearDragArea();
		else
			setDragArea();
		// Create the list of children
		children = new LinkedList<GControl>();
		setText(text);
		calcHotSpots();
		constrainPanelPosition();
		opaque = true;
		dockX = x;
		dockY = y;
		z = Z_PANEL;

		createEventHandler(GUI.applet, "handlePanelEvents", 
				new Class<?>[]{ GPanel.class, GEvent.class },
				new String[]{ "panel", "event" } 
		);
		registeredMethods = DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		GUI.addControl(this);
	}

	/**
	 * This needs to be called if the tab text is changed
	 */
	private void calcHotSpots(){
		hotspots = new HotSpot[]{
				new HSrect(COLLAPSED_BAR_SPOT, 0, 0, tabWidth, tabHeight),			// tab text area
				new HSrect(EXPANDED_BAR_SPOT, 0, 0, width, tabHeight),				// tab non-text area
				new HSrect(SURFACE_SPOT, 0, tabHeight, width, height - tabHeight)	// panel content surface
		};
	}

	/**
	 * This panel is being added to another additional changes that need to be made this control
	 * is added to another. <br>
	 * 
	 * In this case we need to set the constraint limits to keep inside the parent.
	 *  
	 * @param p the parent
	 */
	protected void addToParent(GControl p){
		// Will this fit inside the parent panel
		if(width > p.width || height > p.height){ //No
			draggable = false;
		}
		else {
			lowX = -p.width/2;
			highX = p.width/2;
			lowY = -p.height/2;
			highY = p.height/2;
		}
	}
	
	public void setText(String text){
		super.setText(text);
		stext.getLines(buffer.g2);
		tabHeight = (int) (stext.getMaxLineHeight() + 4);
		tabWidth = (int) (stext.getMaxLineLength() + 8);
		calcHotSpots();
		bufferInvalid = true;
	}

	public void setFont(Font font) {
		if(font != null)
			localFont = font;
		tabHeight = (int) (1.2f * localFont.getSize() + 2);
		buffer.g2.setFont(localFont);
		bufferInvalid = true;
		calcHotSpots();
		bufferInvalid = true;
	}

	/**
	 * What to do when the FPanel loses focus.
	 */
	protected void loseFocus(GControl grabber){
		focusIsWith = null;
		beingDragged = false;
	}

	/**
	 * Draw the panel.
	 * If tabOnly == true 
	 * 		then display the tab only
	 * else
	 * 		draw tab and all child (added) components
	 */
	public void draw(){
		if(!visible) return;
		// Update buffer if invalid
		updateBuffer();
		winApp.pushStyle();

		winApp.pushMatrix();
		// Perform the rotation
		applyTransform();
		// If opaque draw the panel tab and back
		if(opaque){
			winApp.pushMatrix();
			// Move matrix to line up with top-left corner
			winApp.translate(-halfWidth, -halfHeight);
			// Draw buffer
			winApp.imageMode(PApplet.CORNER);
			if(alphaLevel < 255)
				winApp.tint(TINT_FOR_ALPHA, alphaLevel);
			winApp.image(buffer, 0, 0);	

			winApp.popMatrix();
		}
		// Draw the children
		if(!tabOnly){
			if(children != null){
				for(GControl c : children)
					c.draw();
			}
		}
		winApp.popMatrix();
		winApp.popStyle();
	}

	protected void updateBuffer(){
		if(bufferInvalid) {
			Graphics2D g2d = buffer.g2;
			buffer.beginDraw();

			buffer.background(buffer.color(255,0));
			buffer.noStroke();
			buffer.fill(palette[4]);
			if(tabOnly){
				buffer.rect(0, 0, tabWidth, tabHeight);	
			}
			else {
				buffer.rect(0, 0, width, tabHeight);
			}
			stext.getLines(g2d);
			g2d.setColor(jpalette[12]);
			TextLayout tl = stext.getTLIforLineNo(0).layout;
			tl.draw(g2d, 4, 2 + tl.getAscent());

			if(!tabOnly){
				buffer.noStroke();
				buffer.fill(palette[5]);
				buffer.rect(0, tabHeight, width, height - tabHeight);
			}
			buffer.endDraw();
		}	
	}

	/**
	 * Determines if a particular pixel position is over the panel taking
	 * into account whether it is collapsed or not.
	 */
	public boolean isOver(float x, float y){
		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		return (tabOnly)? currSpot == COLLAPSED_BAR_SPOT : currSpot == EXPANDED_BAR_SPOT || currSpot == COLLAPSED_BAR_SPOT || currSpot == SURFACE_SPOT;
	}

	/**
	 * All GUI components are registered for mouseEvents
	 */
	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());

		currSpot = whichHotSpot(ox, oy);
		// Is mouse over the panel tab (taking into account extended with when not collapsed)
		boolean mouseOver = (tabOnly)? currSpot == COLLAPSED_BAR_SPOT : currSpot == EXPANDED_BAR_SPOT | currSpot == COLLAPSED_BAR_SPOT;

		if(mouseOver || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(focusIsWith != this && mouseOver &&  z >= focusObjectZ()){
				takeFocus();
				beingDragged = false;
			}
			break;
		case MouseEvent.CLICK:
			if(focusIsWith == this && collapsible){
				tabOnly = !tabOnly;
				// Perform appropriate action depending on collapse state
				setCollapsed(tabOnly);
				if(tabOnly){
					x = dockX;
					y = dockY;
				}
				else {
					dockX = x;
					dockY = y;
					// Open panel move on screen if needed
					if(y + height > winApp.getHeight())
						y = winApp.getHeight() - height;
					if(x + width > winApp.getWidth())
						x = winApp.getWidth() - width;
				}
				// Maintain centre for drawing purposes
				cx = x + width/2;
				cy = y + height/2;
				constrainPanelPosition();
				if(tabOnly)
					fireEvent(this, GEvent.COLLAPSED);
				else
					fireEvent(this, GEvent.EXPANDED);
				beingDragged = false;
				// This component does not keep the focus when clicked
				loseFocus(null);
			}
			break;
		case MouseEvent.RELEASE: // After dragging NOT clicking
			if(focusIsWith == this){
				if(beingDragged){
					// Remember the dock position when the mouse has
					// been released after the panel has been dragged
					dockX = x;
					dockY = y;
					beingDragged = false;
					loseFocus(null);
				}
			}
			break;
		case MouseEvent.DRAG:
			if(focusIsWith == this && draggable ){//&& parent == null){
				// Maintain centre for drawing purposes
				cx += (winApp.mouseX - winApp.pmouseX);
				cy += (winApp.mouseY - winApp.pmouseY);
				//	Update x and y positions
				x = cx - width/2;
				y = cy - height/2;
				constrainPanelPosition();
				beingDragged = true;
				fireEvent(this, GEvent.DRAGGED);
			}
			break;
		}
	}

	/**
	 * Determines whether to show the tab and panel back colour. If the
	 * parameter is the same as the current state then no changes will
	 * be made.  <br>
	 * If the parameter is false then the panel will be <br>
	 * <ul>
	 * <li>expanded</li>
	 * <li>made non-collasible</li>
	 * <li>made unavailable to mouse control (so can't be dragged)</li>
	 * </ul>
	 * If the parameter is true then the panel will remain non-collapsible
	 * and the user must change this if required. <br>
	 * @param opaque
	 */
	public void setOpaque(boolean opaque){
		if(this.opaque == opaque)
			return;  // no change
		if(!opaque){
			setCollapsed(false);
			setCollapsible(false);
		}
		available = opaque;
		this.opaque = opaque;
	}

	/**
	 * This method is used to discover whether the panel is being 
	 * dragged to a new position on the screen.
	 * @return true if being dragged to a new position
	 */
	public boolean isDragging(){
		return beingDragged;
	}

	/**
	 * Sets whether the panel can be dragged by the mouse or not.
	 * @param draggable
	 */
	public void setDraggable(boolean draggable){
		this.draggable = draggable;
	}

	/**
	 * Can we drag this panel with the mouse?
	 * @return true if draggable
	 */
	public boolean isDraggable(){
		return draggable;
	}

	/**
	 * Collapse or open the panel
	 * @param collapse
	 */
	public void setCollapsed(boolean collapse){
		if(collapsible){
			tabOnly = collapse;
			// If we open the panel make sure it fits on the screen but if we collapse
			// the panel disable the panel controls but leave the panel available
			if(tabOnly){
				setAvailable(false);
				available = true; // Needed so we can click on the title bar
			}
			else {
				setAvailable(true);
			}
		}
	}

	/**
	 * Find out if the panel is collapsed
	 * @return true if collapsed
	 */
	public boolean isCollapsed(){
		return tabOnly;
	}

	/**
	 * Determine whether the panel can be collapsed when the title bar is clicked. <br>
	 *  
	 * If this is set to false then the panel will be expanded and it will
	 * not be possible to collapse it until set back to true.
	 * 
	 */
	public void setCollapsible(boolean c){
		collapsible = c;
		if(c == false){
			tabOnly = false;
			setAvailable(true);
		}
	}
	
	/**
	 * Is this panel collapsible
	 */
	public boolean isCollapsible(){
		return collapsible;
	}
	
	public int getTabHeight(){
		return tabHeight;
	}

	/**
	 * Provided the panel is physically small enough this method will set the area 
	 * within which the panel can be dragged and move the panel inside the area if 
	 * not already inside. <br>
	 *  
	 * @param xMin
	 * @param yMin
	 * @param xMax
	 * @param yMax
	 * @return true if the constraint was applied successfully else false
	 */
	public boolean setDragArea(float xMin, float yMin, float xMax, float yMax){
		if(xMax - xMin < width || yMax - yMin < height){
			if(GUI.showMessages)
				System.out.println("The constraint area is too small for this panel - request ignored");
			return false;
		}
		lowX = xMin;
		lowY = yMin;
		highX = xMax;
		highY = yMax;
		constrainPanelPosition();
		return true;
	}
	
	/**
	 * Provided the panel is small enough to fit inside the display area then
	 * the panel will be constrained to fit inside the display area.
	 * 
	 * @return true if the constraint was applied successfully else false
	 */
	public boolean setDragArea(){
		return setDragArea(0, 0, winApp.width, winApp.height);
	}
	
	/**
	 * Remove any drag constraint from this panel.
	 */
	public void clearDragArea(){
		lowX = lowY = -Float.MAX_VALUE;
		highX = highY = Float.MAX_VALUE;		
	}
	
	/**
	 * Ensures that the panel tab and panel body if open does not
	 * extend off the screen.
	 */
	private void constrainPanelPosition(){
		// Calculate the size of the visible part of the panel
		int w = (int) ((tabOnly)? tabWidth : width);
		int h = (int) ((tabOnly)? tabHeight : height);
		// Constrain horizontally
		if(x < lowX) 
			x = lowX;
		else if(x + w > highX) 
			x = (int) (highX - w);
		// Constrain vertically
		if(y < lowY) 
			y = lowY;
		else if(y + h > highY) 
			y = highY - h;
		// Maintain centre for
		cx = x + width/2;
		cy = y + height/2;
	}


	public String toString(){
		return tag + "  [" + x + ", " + y+"]" + "  [" + cx + ", " + cy+"]"+ "  [" + dockX + ", " + dockY+"]";
	}
}
