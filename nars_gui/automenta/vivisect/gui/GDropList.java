/*
  Part of the G4P library for Processing 
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

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import processing.core.PApplet;
import processing.event.MouseEvent;

/**
 * A drop down list component. <br>
 * 
 * This replaces the GCombo control in pre V3 editions of this library. <br>
 * 
 * The number of items in the list is not restricted but the user can define 
 * the maximum number of items to be displayed in the drop list. If there are
 * too many items to display a vertical scroll bar is provide to scroll through
 * all the items.
 * 
 * The vertical size of an individual item is calculated from the overall height
 * specified when creating the control. <br>
 *  
 * @author Peter Lager
 *
 */
public class GDropList extends GTextBase {

	static protected int LIST_SURFACE = 1;
	static protected int CLOSED_SURFACE = 2;

	protected static final int FORE_COLOR = 2;
	protected static final int BACK_COLOR = 5;
	protected static final int ITEM_FORE_COLOR = 3;
	protected static final int ITEM_BACK_COLOR = 6;
	protected static final int OVER_ITEM_FORE_COLOR = 15;


	private GScrollbar vsb;
	private GButton showList;

	protected LinkedList<String> itemlist = new LinkedList<String>();
	protected StyledString[] sitems;
	protected StyledString selText;

	protected int selItem = 0;
	protected int startItem = 0;
	protected int lastOverItem = -1;
	protected int currOverItem = lastOverItem;


	protected int dropListMaxSize = 4;
	protected int dropListActualSize = 4;

	protected float itemHeight, buttonWidth;

	protected boolean expanded = false;   // make false in release version

	/**
	 * Create a drop down list component with a list size of 4.
	 * 
	 * After creating the control use setItems to initialise the list. <br>
	 * 
	 * @param theApplet the applet that will display this component.
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GDropList(PApplet theApplet, float p0, float p1, float p2, float p3) {
		this(theApplet, p0, p1, p2, p3, 4);
	}

	/**
	 * Create a drop down list component with a specified list size.
	 * 
	 * After creating the control use setItems to initialise the list. <br>
	 * 
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param dropListMaxSize the maximum number of element to appear in the drop down list 
	 */
	public GDropList(PApplet theApplet, float p0, float p1, float p2, float p3, int dropListMaxSize) {
		super(theApplet, p0, p1, p2, p3);
		children = new LinkedList<GAbstractControl>();
		this.dropListMaxSize = Math.max(dropListMaxSize, 3);
		itemHeight = height / (dropListMaxSize + 1); // make allowance for selected text at top

		G4P.pushStyle();
		G4P.showMessages = false;

		vsb = new GScrollbar(theApplet, 0, 0, height - itemHeight, 10);
		vsb.addEventHandler(this, "vsbEventHandler");
		vsb.setAutoHide(true);
		vsb.setVisible(false);

		buttonWidth = 10;
		showList = new GButton(theApplet, 0, 0, buttonWidth, itemHeight, ":");
		showList.addEventHandler(this, "buttonShowListHandler");

		// Do this before we add the button and scrollbar
		z = Z_SLIPPY_EXPANDS;

		// Add the button and scrollbar
		G4P.control_mode = GControlMode.CORNER;
		addControl(vsb, width, itemHeight + 1, PI/2);
		addControl(showList, width - buttonWidth, 0, 0);

		G4P.popStyle();

		hotspots = new HotSpot[]{
				new HSrect(LIST_SURFACE, 0, itemHeight+1, width - 11, height - itemHeight - 1),	// text list area
				new HSrect(CLOSED_SURFACE, 0, 0, width - buttonWidth, itemHeight)				// selected text display area
		};

		createEventHandler(G4P.sketchApplet, "handleDropListEvents",
				new Class<?>[]{ GDropList.class, GEvent.class }, 
				new String[]{ "list", "event" } 
				);
		registeredMethods = DRAW_METHOD | MOUSE_METHOD;
		cursorOver = HAND;
		G4P.addControl(this);
	}

	/**
	 * Use this to set or change the list of items to appear in the list. If
	 * you enter an invalid selection index then it is forced into
	 * the valid range. <br>
	 * Null and empty values in the list will be ignored. <br>
	 * If the list is null then or empty then then no changes are made. <br>
	 * @param array
	 * @param selected
	 */
	public void setItems(String[] array, int selected){
		if(array == null)
			return;
		// Get rid of null or empty strings
		ArrayList<String> strings = new ArrayList<String>();
		for(String s : array)
			strings.add(s);
		if(cleanupList(strings))
			setItemsImpl(selected);
	}

	public void setItems(List<String> list, int selected){
		if(list == null)
			return;
		if(cleanupList(list))
			setItemsImpl(selected);
	}

	private boolean cleanupList(List<String> list){
		// Get rid of null or empty strings
		Iterator<String> iter = list.iterator();
		while(iter.hasNext()){
			String s = iter.next();
			if(s == null || s.length() == 0)
				iter.remove();
		}
		if(list.size() > 0){
			// We have at least one item for the droplist
			itemlist.clear();
			itemlist.addAll(list);
			return true;
		}
		return false;
	}

	private void setItemsImpl(int selected){
		sitems = new StyledString[itemlist.size()];
		// Create styled strings for display
		for(int i = 0; i < sitems.length; i++)
			sitems[i] = new StyledString(itemlist.get(i));
		// Force selected value into valid range
		selItem = PApplet.constrain(selected, 0, sitems.length - 1);
		startItem = (selItem >= dropListMaxSize) ? selItem - dropListMaxSize + 1 : 0;
		// Make selected item bold
		sitems[selItem].addAttribute(WEIGHT, WEIGHT_BOLD);
		// Create separate styled string for display area
		selText = new StyledString(sitems[selItem].getPlainText());
		dropListActualSize = Math.min(sitems.length, dropListMaxSize);
		if((sitems.length > dropListActualSize)){
			float filler = ((float)dropListMaxSize)/sitems.length;
			float value = ((float)startItem)/sitems.length;
			vsb.setValue(value, filler);
			vsb.setVisible(false); //  make it false
		}
		bufferInvalid = true;
	}

	/**
	 * Remove an item from the list. <br>
	 * If idx is not a valid position in the list then the list is unchanged. 
	 * If idx points to the selected item or an item below it then the 
	 * selected index value is reduced by 1 but the selected text remains the 
	 * same. <br>
	 * If idx points to an item above the selected item then the selected 
	 * item is unchanged. <br>
	 * No event is fired even if the selected index changes. <br>
	 * 
	 * @param idx index of the item to remove
	 * @return true if item is successfully removed else false
	 */
	public boolean removeItem(int idx){
		if(itemlist.size() <= 1 || idx <  0 || idx >= itemlist.size() )
			return false;
		int sel = (idx > selItem) ? selItem : selItem - 1;
		itemlist.remove(idx);
		setItemsImpl(sel);
		return true;
	}
	
	/**
	 * Insert an item at the specified position in the list. <br>
	 * If idx is <0 then the list is unchanged. <br> 
	 * If idx is >= the number of items in the list, it is added to the end. <br> 
	 * If idx points to the selected item or an item below it then the 
	 * selected index value is incremented by 1 but the selected text remains  
	 * the same. <br>
	 * If idx points to an item above the selected item then the selected 
	 * item is unchanged. <br>
	 * No event is fired even if the selected index changes. <br>
	 * 
	 * @param idx index of the item to remove
	 * @param name text of the item to insert (null values ignored)
	 * @return true if item is successfully inserted else false
	 */
	
	public boolean insertItem(int idx, String name){
		if(idx < 0 || name == null || name.length() > 0)
			return false;
		if(idx >= itemlist.size()){
			itemlist.addLast(name);
		}
		else {
			itemlist.add(idx, name);
		}
		int sel = (idx <= selItem) ? selItem + 1: selItem;
		setItemsImpl(sel);
		return true;
	}
	
	/**
	 * Add an item to the end of the list. <br>
	 * No event is fired. <br>
	 * 
	 * @param name text of the item to add (null values ignored)
	 * @return true if add is successfully added else false
	 */
	public boolean addItem(String name){
		if(name == null)
			return false;
		itemlist.addLast(name);
		return true;
	}

	/**
	 * Set the currently selected item from the droplist by index position. <br>
	 * Invalid values are ignored.
	 * 
	 * @param selected
	 */
	public void setSelected(int selected){
		if(selected >=0 && selected < sitems.length){
			selItem = selected;
			startItem = (selItem >= dropListMaxSize) ? selItem - dropListMaxSize + 1 : 0;
			for(StyledString s : sitems)
				s.clearAttributes();
			sitems[selItem].addAttribute(WEIGHT, WEIGHT_BOLD);
			selText = new StyledString(sitems[selItem].getPlainText());		
			bufferInvalid = true;
		}
	}
	
	/**
	 * Get the index position of the selected item
	 */
	public int getSelectedIndex(){
		return selItem;
	}

	/**
	 * Get the text for the selected item
	 */
	public String getSelectedText(){
		return sitems[selItem].getPlainText();
	}

	/**
	 * Sets the local colour scheme for this control
	 */
	public void setLocalColorScheme(int cs){
		super.setLocalColorScheme(cs);
		if(showList != null)
			showList.setLocalColorScheme(localColorScheme);
		if(vsb != null)
			vsb.setLocalColorScheme(localColorScheme);
	}

	/**
	 * Determines if a particular pixel position is over this control taking
	 * into account whether it is collapsed or not.
	 */
	public boolean isOver(float x, float y){
		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);
		return (!expanded)? currSpot == CLOSED_SURFACE : currSpot == CLOSED_SURFACE | currSpot == LIST_SURFACE;
	}

	public void mouseEvent(MouseEvent event){
		if(!visible || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		currSpot = whichHotSpot(ox, oy);

		if(currSpot >= 0 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.CLICK:
			// No need to test for isOver() since if the component has focus
			// and the mouse has not moved since MOUSE_PRESSED otherwise we 
			// would not get the Java MouseEvent.MOUSE_CLICKED event
			if(focusIsWith == this ){
				loseFocus(null);
				vsb.setVisible(false);
				expanded = false;
				bufferInvalid = true;
				// Make sure that we have selected a valid item and that
				// it is not the same as before;
				if(currOverItem >= 0 && currOverItem != selItem){
					setSelected(currOverItem);
					fireEvent(this, GEvent.SELECTED);
				}
				currOverItem = lastOverItem = -1;
			}
			break;
		case MouseEvent.MOVE:
			if(focusIsWith == this){
				if(currSpot == LIST_SURFACE)
					currOverItem = startItem + (int)(oy / itemHeight)-1; 
				//currOverItem = startItem + Math.round(oy / itemHeight) - 1; 
				else
					currOverItem = -1;
				// Only invalidate the buffer if the over item has changed
				if(currOverItem != lastOverItem){
					lastOverItem = currOverItem;
					bufferInvalid = true;
				}
			}
			break;
		}
	}

	public void draw(){
		if(!visible) return;
		updateBuffer();

		winApp.pushStyle();
		winApp.pushMatrix();

		applyTransform();

		winApp.pushMatrix();
		// Move matrix to line up with top-left corner
		winApp.translate(-halfWidth, -halfHeight);
		// Draw buffer
		winApp.imageMode(PApplet.CORNER);
		if(alphaLevel < 255)
			winApp.tint(TINT_FOR_ALPHA, alphaLevel);
		winApp.image(buffer, 0, 0);

		winApp.popMatrix();

		if(children != null){
			for(GAbstractControl c : children)
				c.draw();
		}
		winApp.popMatrix();
		winApp.popStyle();
	}

	protected void updateBuffer(){
		if(bufferInvalid) {
			Graphics2D g2d = buffer.g2;
			bufferInvalid = false;

			buffer.beginDraw();
			buffer.background(buffer.color(255,0));

			buffer.noStroke();
			buffer.fill(palette[BACK_COLOR]);
			buffer.rect(0, 0, width, itemHeight);

			if(expanded){
				buffer.fill(palette[ITEM_BACK_COLOR]);
				buffer.rect(0,itemHeight, width, itemHeight * dropListActualSize);
			}

			float px = TPAD2, py;
			TextLayout line;
			// Get selected text for display
			line = selText.getLines(g2d).getFirst().layout;
			py = (itemHeight + line.getAscent() - line.getDescent())/2;

			g2d.setColor(jpalette[FORE_COLOR]);
			line.draw(g2d, px, py);

			if(expanded){
				g2d.setColor(jpalette[ITEM_FORE_COLOR]);
				for(int i = 0; i < dropListActualSize; i++){
					py += itemHeight;
					if(currOverItem == startItem + i)
						g2d.setColor(jpalette[OVER_ITEM_FORE_COLOR]);
					else
						g2d.setColor(jpalette[ITEM_FORE_COLOR]);

					line = sitems[startItem + i].getLines(g2d).getFirst().layout;				
					line.draw(g2d, px, py);
				}
			}
			buffer.endDraw();
		}
	}

	/**
	 * For most components there is nothing to do when they loose focus.
	 * Override this method in classes that need to do something when
	 * they loose focus eg TextField
	 */
	protected void loseFocus(GAbstractControl grabber){
		if(grabber != vsb){
			expanded = false;
			vsb.setVisible(false);
			bufferInvalid = true;
		}
		if(cursorIsOver == this)
			cursorIsOver = null;
		focusIsWith = grabber;
	}


	/**
	 * This method should <b>not</b> be called by the user. It
	 * is for internal library use only.
	 */
	public void vsbEventHandler(GScrollbar scrollbar, GEvent event){
		int newStartItem = Math.round(vsb.getValue() * sitems.length);
		startItem = newStartItem;
		bufferInvalid = true;
	}

	/**
	 * This method should <b>not</b> be called by the user. It
	 * is for internal library use only.
	 */
	public void buttonShowListHandler(GButton button, GEvent event){
		if(expanded){
			loseFocus(null);
			vsb.setVisible(false);
			expanded = false;
		}
		else {
			takeFocus();
			vsb.setVisible(sitems.length > dropListActualSize);
			expanded = true;
		}
		bufferInvalid = true;
	}

}