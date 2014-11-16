/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2014 Peter Lager

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
import automenta.vivisect.gui.StyledString.TextLayoutHitInfo;
import automenta.vivisect.gui.StyledString.TextLayoutInfo;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * The password field component. <br>
 * 
 * This control allows the user to secretly enter a password and supports an
 * optional horizontal scrollbar. <br>
 * 
 * Each key typed will display a <b>#</b> character, but the user can specify another character. <br>
 * 
 * Unlike a text field it does not support text selection or the copying and pasting of 
 * text via the clipboard. <br>
 * 
 * Fires CHANGED, ENTERED, LOST_FOCUS, GETS_FOCUS events.<br>
 * The focus events are only fired if the control is added to a GTabManager object. <br>
 * 
 * @author Peter Lager
 *
 */
public final class GPassword extends GAbstractControl implements Focusable{

	protected TextLayoutHitInfo cursorTLHI = new TextLayoutHitInfo();

	private static char cover = '#';

	private StyledString stext = new StyledString(" ");
	private StyledString hidden = new StyledString(" ");
	private int maxWordLength = 10;
	private int wordLength = 0;
	
	GTabManager tabManager = null;

	// The width to break a line
	protected int wrapWidth = Integer.MAX_VALUE;

	// The typing area
	protected float tx,ty,th,tw;
	// Offset to display area
	protected float ptx, pty;
	// Caret position
	protected float caretX, caretY;

	protected boolean keepCursorInView = false;

	protected GeneralPath gpTextDisplayArea;

	// Used for identifying selection and cursor position
	protected TextLayoutHitInfo startTLHI = new TextLayoutHitInfo();
	protected TextLayoutHitInfo endTLHI = new TextLayoutHitInfo();

	// The scrollbars available
	protected final int scrollbarPolicy;
	protected boolean autoHide = false;
	protected GScrollbar hsb, vsb;

	protected GTimer caretFlasher;
	protected boolean showCaret = false;

	// Stuff to manage text selections
	protected int endChar = -1, startChar = -1, pos = endChar, nbr = 0, adjust = 0;
	protected boolean textChanged = false;

	protected Font localFont = G4P.globalFont;
	
	/**
	 * Create a password field without a scrollbar.
	 * 
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GPassword(PApplet theApplet, float p0, float p1, float p2, float p3) {
		this(theApplet, p0, p1, p2, p3, SCROLLBARS_NONE);
	}

	/**
	 * Create a password field with the given scrollbar policy. <br>
	 * This policy can be one of these <br>
	 * <ul>
	 * <li>SCROLLBARS_NONE</li>
	 * <li>SCROLLBARS_HORIZONTAL_ONLY</li>
	 * </ul>
	 * If you want the scrollbar to auto hide then perform a logical or with 
	 * <ul>
	 * <li>SCROLLBARS_AUTOHIDE</li>
	 * </ul>
	 * e.g. SCROLLBARS_HORIZONTAL_ONLY | SCROLLBARS_AUTOHIDE
	 * <br>
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param sbPolicy
	 */
	public GPassword(PApplet theApplet, float p0, float p1, float p2, float p3, int sbPolicy) {
		super(theApplet, p0, p1, p2, p3);
		scrollbarPolicy = sbPolicy;
		autoHide = ((sbPolicy & SCROLLBARS_AUTOHIDE) == SCROLLBARS_AUTOHIDE);
		caretFlasher = new GTimer(theApplet, this, "flashCaret", 400);
		caretFlasher.start();
		opaque = true;
		cursorOver = TEXT;
		
		setVisibleChar(cover);
		children = new LinkedList<GAbstractControl>();
		tx = ty = 2;
		tw = width - 2 * 2;
		th = height - ((scrollbarPolicy & SCROLLBAR_HORIZONTAL) != 0 ? 11 : 0);
		gpTextDisplayArea = new GeneralPath();
		gpTextDisplayArea.moveTo( 0,  0);
		gpTextDisplayArea.lineTo( 0, th);
		gpTextDisplayArea.lineTo(tw, th);
		gpTextDisplayArea.lineTo(tw,  0);
		gpTextDisplayArea.closePath();

//		// The image buffer is just for the typing area
//		buffer = (PGraphicsJava2D) winApp.createGraphics((int)width, (int)height, PApplet.JAVA2D);
//		buffer.rectMode(PApplet.CORNER);
//		buffer.g2.setFont(localFont);
		hotspots = new HotSpot[]{
				new HSrect(1, tx, ty, tw, th),			// typing area
				new HSrect(9, 0, 0, width, height)		// control surface
		};

		G4P.pushStyle();
		G4P.showMessages = false;

		z = Z_STICKY;

		G4P.control_mode = GControlMode.CORNER;
		if((scrollbarPolicy & SCROLLBAR_HORIZONTAL) != 0){
			hsb = new GScrollbar(theApplet, 0, 0, tw, 10);
			addControl(hsb, tx, ty + th + 2, 0);
			hsb.addEventHandler(this, "hsbEventHandler");
			hsb.setAutoHide(autoHide);
		}
		G4P.popStyle();
		//		z = Z_STICKY;
		createEventHandler(G4P.sketchApplet, "handlePasswordEvents", 
				new Class<?>[]{ GPassword.class, GEvent.class }, 
				new String[]{ "pwordControl", "event" } 
				);
		registeredMethods = PRE_METHOD | DRAW_METHOD | MOUSE_METHOD | KEY_METHOD;
		G4P.addControl(this);
	}

	/**
	 * Set the character that will be displayed instead of the actual character
	 * entered by the user. <br>
	 * Default value is '#'
	 */
	public void setVisibleChar(char c){
		int ascii = (int) c;
		if((ascii >= 33 && ascii <= 255 && ascii != 127) || ascii == 8364)
			cover = c;
	}
	
	/**
	 * Get the current password (hidden) value of this field.
	 * @return actual password text
	 */
	public String getPassword(){
		String password = hidden.getPlainText();
		return password.equals(" ") ? "" : password;
	}
	
	/**
	 * Get the current length of the password entered.
	 */
	public int getWordLength(){
		return wordLength;
	}
	
	/**
	 * Sets the max length of the password. This method is ignored if the control
	 * already holds some user input. <br>
	 * The default value is 10.
	 * @param ml the new max length (must be >= 1)
	 */
	public void setMaxWordLength(int ml){
		if(wordLength == 0 && ml >= 1)
			maxWordLength = ml;
	}
	
	/** 
	 * Set the font to be used in this control
	 * 
	 * @param font AWT font to use
	 */
	public void setFont(Font font) {
		if(font != null && font != localFont && buffer != null){
			localFont = font;
			buffer.g2.setFont(localFont);
			bufferInvalid = true;
		}
	}

	public PGraphics getSnapshot(){
		updateBuffer();
		PGraphicsJava2D snap = (PGraphicsJava2D) winApp.createGraphics(buffer.width, buffer.height, PApplet.JAVA2D);
		snap.beginDraw();
		snap.image(buffer,0,0);
		if(hsb != null){
			snap.pushMatrix();
			snap.translate(hsb.getX(), hsb.getY());
			snap.image(hsb.getBuffer(), 0, 0);
			snap.popMatrix();
		}
		snap.endDraw();
		return snap;
	}

	public void pre(){
		if(keepCursorInView){
			boolean horzScroll = false;
			float max_ptx = caretX - tw + 2;
			if(endTLHI != null){
				if(ptx > caretX){ 								// Scroll to the left (text moves right)
					ptx -= HORZ_SCROLL_RATE;
					if(ptx < 0) ptx = 0;
					horzScroll = true;
				}
				else if(ptx < max_ptx){ 						// Scroll to the right (text moves left)?
					ptx += HORZ_SCROLL_RATE;
					if(ptx > max_ptx) ptx = max_ptx;
					horzScroll = true;
				}
				// Ensure that we show as much text as possible keeping the caret in view
				// This is particularly important when deleting from the end of the text
				if(ptx > 0 && endTLHI.tli.layout.getAdvance() - ptx < tw - 2){
					ptx = Math.max(0, endTLHI.tli.layout.getAdvance() - tw - 2);
					horzScroll = true;
				}
				if(horzScroll && hsb != null)
					hsb.setValue(ptx / (stext.getMaxLineLength() + 4));
			}
			// If we have scrolled invalidate the buffer otherwise forget it
			if(horzScroll)
				bufferInvalid = true;
			else
				keepCursorInView = false;
		}
	}

	/**
	 * Do not call this directly. A timer calls this method as and when required.
	 */
	public void flashCaret(GTimer timer){
		showCaret = !showCaret;
	}

	public void mouseEvent(MouseEvent event){
		if(!visible  || !enabled || !available) return;

		calcTransformedOrigin(winApp.getCursorX(), winApp.getCursorY());
		ox -= tx; oy -= ty; // Remove translation

		currSpot = whichHotSpot(ox, oy);

		if(currSpot == 1 || focusIsWith == this)
			cursorIsOver = this;
		else if(cursorIsOver == this)
			cursorIsOver = null;

		switch(event.getAction()){
		case MouseEvent.PRESS:
			if(currSpot == 1){
				if(focusIsWith != this && z >= focusObjectZ()){
					keepCursorInView = true;
					takeFocus();
				}
				dragging = false;
				if(stext == null || stext.length() == 0){
					stext = new StyledString(" ", wrapWidth);
					stext.getLines(buffer.g2);
				}
				endTLHI = stext.calculateFromXY(buffer.g2, ox + ptx, oy + pty);
				startTLHI = new TextLayoutHitInfo(endTLHI);
				calculateCaretPos(endTLHI);
				bufferInvalid = true;
			}
			else { // Not over this control so if we have focus loose it
				if(focusIsWith == this)
					loseFocus(null);
			}		
			break;
		case MouseEvent.RELEASE:
			dragging = false;
			bufferInvalid = true;
			break;
		}
	}
	
	public void keyEvent(KeyEvent e) {
		if(!visible  || !enabled || !available) return;
		if(focusIsWith == this && endTLHI != null){
			char keyChar = e.getKey();
			int keyCode = e.getKeyCode();
			int keyID = e.getAction();
			boolean shiftDown = e.isShiftDown();
			boolean ctrlDown = e.isControlDown();

			textChanged = false;
			keepCursorInView = true;

			int startPos = pos, startNbr = nbr;

			// Get selection details
			endChar = endTLHI.tli.startCharIndex + endTLHI.thi.getInsertionIndex();
			startChar = (startTLHI != null) ? startTLHI.tli.startCharIndex + startTLHI.thi.getInsertionIndex() : endChar;
			pos = endChar;
			nbr = 0;
			adjust = 0;
			if(endChar != startChar){ // Have we some text selected?
				if(startChar < endChar){ // Forward selection
					pos = startChar; nbr = endChar - pos;
				}
				else if(startChar > endChar){ // Backward selection
					pos = endChar;	nbr = startChar - pos;
				}
			}
			if(startPos >= 0){
				if(startPos != pos || startNbr != nbr)
					fireEvent(this, GEvent.SELECTION_CHANGED);
			}
			// Select either keyPressedProcess or keyTypeProcess. These two methods are overridden in child classes
			if(keyID == KeyEvent.PRESS) {
				keyPressedProcess(keyCode, keyChar, shiftDown, ctrlDown);
				setScrollbarValues(ptx, pty);
			}
			else if(keyID == KeyEvent.TYPE ){ // && e.getKey()  != KeyEvent.CHAR_UNDEFINED && !ctrlDown){
				keyTypedProcess(keyCode, keyChar, shiftDown, ctrlDown);
				setScrollbarValues(ptx, pty);
			}
			if(textChanged){
				changeText();
				fireEvent(this, GEvent.CHANGED);
			}
		}
	}

	protected void keyPressedProcess(int keyCode, char keyChar, boolean X, boolean ctrlDown){
		boolean cursorMoved = true;
		switch(keyCode){
		case LEFT:
			moveCaretLeft(endTLHI);
			break;
		case RIGHT:
			moveCaretRight(endTLHI);
			break;
		case GConstants.HOME:
			moveCaretStartOfLine(endTLHI);
			break;
		case GConstants.END:
			moveCaretEndOfLine(endTLHI);
			break;
		default:
			cursorMoved = false;
		}
		if(cursorMoved){
			calculateCaretPos(endTLHI);
			startTLHI.copyFrom(endTLHI);
		}
	}

	protected void keyTypedProcess(int keyCode, char keyChar, boolean shiftDown, boolean ctrlDown){
		int ascii = (int)keyChar;
		
		//if((wordLength < maxWordLength && ascii >= 32 && ascii <= 255 && ascii != 127) || ascii == 8364){
		if(wordLength < maxWordLength && isDisplayable(ascii)){
			stext.insertCharacters( "" + cover, pos);
			hidden.insertCharacters("" + keyChar, pos);
			wordLength++;
			adjust = 1; textChanged = true;
		}
		else if(keyChar == BACKSPACE){
			if(stext.deleteCharacters(pos - 1, 1)){
				hidden.deleteCharacters(pos - 1, 1);
				wordLength = --wordLength < 0 ? 0: wordLength;
				adjust = -1; textChanged = true;
			}
		}
		else if(keyChar == DELETE){
			if(stext.deleteCharacters(pos, 1)){
				hidden.deleteCharacters(pos, 1);
				wordLength--;
				adjust = 0; textChanged = true;
			}
		}
		else if(keyChar == ENTER || keyChar == RETURN) {
			fireEvent(this, GEvent.ENTERED);
			// If we have a tab manager and can tab forward then do so
			if(tabManager != null && tabManager.nextControl(this)){
				startTLHI.copyFrom(endTLHI);
				return;
			}
		}
		else if(keyChar == TAB){
			// If possible move to next text control
			if(tabManager != null){
				boolean result = (shiftDown) ? tabManager.prevControl(this) : tabManager.nextControl(this);
				if(result){
					startTLHI.copyFrom(endTLHI);
					return;
				}
			}
		}
		// If we have emptied the text then recreate a one character string (space)
		if(stext.length() == 0){
			stext.insertCharacters(" ", 0);
			hidden.insertCharacters(" ", 0);
			adjust++; textChanged = true;
		}
	}

	protected boolean changeText(){
		TextLayoutInfo tli;
		TextHitInfo thi = null, thiRight = null;

		pos += adjust;
		// Force layouts to be updated
		stext.getLines(buffer.g2);

		// Try to get text layout info for the current position
		tli = stext.getTLIforCharNo(pos);
		if(tli == null){
			// If unable to get a layout for pos then reset everything
			endTLHI = null;
			startTLHI = null;
			ptx = pty = 0;
			caretX = caretY = 0;
			return false;
		}
		// We have a text layout so we can do something
		// First find the position in line
		int posInLine = pos - tli.startCharIndex;

		// Get some hit info so we can see what is happening
		try{
			thiRight = tli.layout.getNextRightHit(posInLine);
		}
		catch(Exception excp){
			thiRight = null;
		}

		if(posInLine <= 0){					// At start of line
			thi = tli.layout.getNextLeftHit(thiRight);				
		}
		else if(posInLine >= tli.nbrChars){	// End of line
			thi = tli.layout.getNextRightHit(tli.nbrChars - 1);
		}
		else {								// Character in line;
			thi = tli.layout.getNextLeftHit(thiRight);	
		}

		endTLHI.setInfo(tli, thi);
		// Cursor at end of paragraph graphic
		calculateCaretPos(endTLHI);
		bufferInvalid = true;
		
		startTLHI.copyFrom(endTLHI);
		return true;
	}

	/**
	 * Used internally to set the scrollbar values as the text changes.
	 * 
	 * @param sx
	 * @param sy
	 */
	void setScrollbarValues(float sx, float sy){
		if(vsb != null){
			float sTextHeight = stext.getTextAreaHeight();
			if(sTextHeight < th)
				vsb.setValue(0.0f, 1.0f);
			else 
				vsb.setValue(sy/sTextHeight, th/sTextHeight);
		}
		// If needed update the horizontal scrollbar
		if(hsb != null){
			float sTextWidth = stext.getMaxLineLength();
			if(stext.getMaxLineLength() < tw)
				hsb.setValue(0,1);
			else
				hsb.setValue(sx/sTextWidth, tw/sTextWidth);
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

		// Draw caret if text display area
		if(focusIsWith == this && showCaret && endTLHI.tli != null){
			float[] cinfo = endTLHI.tli.layout.getCaretInfo(endTLHI.thi);
			float x_left =  - ptx + cinfo[0];
			float y_top = - pty + endTLHI.tli.yPosInPara; 
			float y_bot = y_top - cinfo[3] + cinfo[5];
			if(x_left >= 0 && x_left <= tw && y_top >= 0 && y_bot <= th){
				winApp.strokeWeight(1.9f);
				winApp.stroke(palette[15]);
				winApp.line(tx+x_left, ty+Math.max(0, y_top), tx+x_left, ty+Math.min(th, y_bot));
			}
		}

		winApp.popMatrix();

		if(children != null){
			for(GAbstractControl c : children)
				c.draw();
		}
		winApp.popMatrix();
		winApp.popStyle();
	}

	/**
	 * If the buffer is invalid then redraw it.
	 * @TODO need to use palette for colours
	 */
	protected void updateBuffer(){
		if(bufferInvalid) {
			Graphics2D g2d = buffer.g2;
			// Get the latest lines of text
			LinkedList<TextLayoutInfo> lines = stext.getLines(g2d);	
			bufferInvalid = false;

			buffer.beginDraw();
			// Whole control surface if opaque
			if(opaque)
				buffer.background(palette[6]);
			else
				buffer.background(buffer.color(255,0));

			// Now move to top left corner of text display area
			buffer.translate(tx,ty); 

			// Typing area surface
			buffer.noStroke();
			buffer.fill(palette[7]);
			buffer.rect(-1,-1,tw+2,th+2);

			g2d.setClip(gpTextDisplayArea);
			buffer.translate(-ptx, -pty);
			// Translate in preparation for display selection and text

			// Display selection and text
			for(TextLayoutInfo lineInfo : lines){
				TextLayout layout = lineInfo.layout;
				buffer.translate(0, layout.getAscent());
				// Draw text
				g2d.setColor(jpalette[2]);
				lineInfo.layout.draw(g2d, 0, 0);
				buffer.translate(0, layout.getDescent() + layout.getLeading());
			}
			g2d.setClip(null);
			buffer.endDraw();
		}
	}

	/**
	 * Give up focus but if the text is only made from spaces
	 * then set it to null text. <br>
	 * Fire focus events for the GTextField and GTextArea controls
	 */
	protected void loseFocus(GAbstractControl grabber){
		// If this control has focus then Fire a lost focus event
		if(focusIsWith == this)
			fireEvent(this, GEvent.LOST_FOCUS);
		// Process mouse-over cursor 
		if(cursorIsOver == this)
			cursorIsOver = null;
		focusIsWith = grabber;
		// If only blank text clear it out allowing default text (if any) to be displayed
		if(stext.length() > 0){
			int tl = stext.getPlainText().trim().length();
			if(tl == 0)
				stext = new StyledString("", wrapWidth);
		}
		keepCursorInView = true;
		bufferInvalid = true;
	}

	/**
	 * Give the focus to this component but only after allowing the 
	 * current component with focus to release it gracefully. <br>
	 * Always cancel the keyFocusIsWith irrespective of the component
	 * type. 
	 * Fire focus events for the GTextField and GTextArea controls
	 */
	protected void takeFocus(){
		// If focus is not yet with this control fire a gets focus event
		if(focusIsWith != this){
			// If the focus is with another control then tell
			// that control to lose focus
			if(focusIsWith != null)
				focusIsWith.loseFocus(this);
			fireEvent(this, GEvent.GETS_FOCUS);
		}
		focusIsWith = this;
	}

	/**
	 * Determines whether this component is to have focus or not. <br>
	 */
	public void setFocus(boolean focus){
		if(!focus){
			loseFocus(null);
			return;
		}
		// Make sure we have some text
		if(focusIsWith != this){
			dragging = false;
			if(stext == null || stext.length() == 0)
				stext = new StyledString(" ", wrapWidth);
			LinkedList<TextLayoutInfo> lines = stext.getLines(buffer.g2);
			startTLHI = new TextLayoutHitInfo(lines.getFirst(), null);
			startTLHI.thi = startTLHI.tli.layout.getNextLeftHit(1);

			endTLHI = new TextLayoutHitInfo(lines.getLast(), null);
			int lastChar = endTLHI.tli.layout.getCharacterCount();
			endTLHI.thi = startTLHI.tli.layout.getNextRightHit(lastChar-1);
			startTLHI.copyFrom(endTLHI);
			
			calculateCaretPos(endTLHI);
			bufferInvalid = true;
		}
		keepCursorInView = true;
		takeFocus();
	}

	
	/**
	 * Calculate the caret (text insertion point)
	 * 
	 * @param tlhi
	 */
	protected void calculateCaretPos(TextLayoutHitInfo tlhi){
		float temp[] = tlhi.tli.layout.getCaretInfo(tlhi.thi);
		caretX = temp[0];		
		caretY = tlhi.tli.yPosInPara;
	}

	/**
	 * Move caret to home position
	 * @param currPos the current position of the caret
	 * @return true if caret moved else false
	 */
	protected boolean moveCaretStartOfLine(TextLayoutHitInfo currPos){
		if(currPos.thi.getCharIndex() == 0)
			return false; // already at start of line
		currPos.thi = currPos.tli.layout.getNextLeftHit(1);
		return true;
	}

	/**
	 * Move caret to the end of the line that has the current caret position
	 * @param currPos the current position of the caret
	 * @return true if caret moved else false
	 */
	protected boolean moveCaretEndOfLine(TextLayoutHitInfo currPos){
		if(currPos.thi.getCharIndex() == currPos.tli.nbrChars - 1)
			return false; // already at end of line
		currPos.thi = currPos.tli.layout.getNextRightHit(currPos.tli.nbrChars - 1);
		return true;
	}

	/**
	 * Move caret left by one character.
	 * @param currPos the current position of the caret
	 * @return true if caret moved else false
	 */
	protected boolean moveCaretLeft(TextLayoutHitInfo currPos){
		TextHitInfo nthi = currPos.tli.layout.getNextLeftHit(currPos.thi);
		if(nthi == null){ 
			return false;
		}
		else {
			// Move the caret to the left of current position
			currPos.thi = nthi;
		}
		return true;
	}

	/**
	 * Move caret right by one character.
	 * @param currPos the current position of the caret
	 * @return true if caret moved else false
	 */
	protected boolean moveCaretRight(TextLayoutHitInfo currPos){
		TextHitInfo nthi = currPos.tli.layout.getNextRightHit(currPos.thi);
		if(nthi == null){ 
			return false;
		}
		else {
			currPos.thi = nthi;
		}
		return true;
	}

	public void setJustify(boolean justify){
		stext.setJustify(justify);
		bufferInvalid = true;
	}

	/**
	 * Sets the local colour scheme for this control
	 */
	public void setLocalColorScheme(int cs){
		super.setLocalColorScheme(cs);
		if(hsb != null)
			hsb.setLocalColorScheme(localColorScheme);
		if(vsb != null)
			vsb.setLocalColorScheme(localColorScheme);
	}

	@Override
	public void setTabManager(GTabManager tm){
		tabManager = tm;
	}


}
