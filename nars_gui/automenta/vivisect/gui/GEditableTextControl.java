/*
  Part of the GUI library for Processing 
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

import automenta.vivisect.gui.StyledString.TextLayoutHitInfo;
import automenta.vivisect.gui.StyledString.TextLayoutInfo;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;

import processing.core.PApplet;
import processing.event.KeyEvent;

/**
 * 
 * This class is the basis for the GTextField and GTextArea classes.
 * 
 * @author Peter Lager
 *
 */
public abstract class GEditableTextControl extends GTextBase implements Focusable {

	GTabManager tabManager = null;

	protected StyledString promptText = null;
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
	protected boolean textChanged = false, selectionChanged = false;

	/* Is the component enabled to generate mouse and keyboard events */
	boolean textEditEnabled = true;

	public GEditableTextControl(PApplet theApplet, float p0, float p1, float p2, float p3, int scrollbars) {
		super(theApplet, p0, p1, p2, p3);
		scrollbarPolicy = scrollbars;
		autoHide = ((scrollbars & SCROLLBARS_AUTOHIDE) == SCROLLBARS_AUTOHIDE);
		caretFlasher = new GTimer(theApplet, this, "flashCaret", 400);
		caretFlasher.start();
		opaque = true;
		cursorOver = TEXT;
	}

	public void setTabManager(GTabManager tm){
		tabManager = tm;
	}

	/**
	 * Give up focus but if the text is only made from spaces
	 * then set it to null text. <br>
	 * Fire focus events for the GTextField and GTextArea controls
	 */
	protected void loseFocus(GControl grabber){
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

			calculateCaretPos(endTLHI);
			bufferInvalid = true;
		}
		keepCursorInView = true;
		takeFocus();
	}

	/**
	 * This method is deprecated use the setPromptText(String) method instead
	 * @deprecated
	 */
	@Deprecated
	public void setDefaultText(String dtext){
		setPromptText(dtext);
	}
	
	/**
	 * Set the prompt text for this control. When the text control is empty 
	 * the prompt text (italic) is displayed instead.
	 * .
	 * @param ptext prompt text
	 */
	public void setPromptText(String ptext){
		if(ptext == null || ptext.length() == 0)
			promptText = null;
		else {
			promptText = new StyledString(ptext, wrapWidth);
			promptText.addAttribute(GUI.POSTURE, GUI.POSTURE_OBLIQUE);
		}
		bufferInvalid = true;
	}

	/**
	 * @return the wrapWidth
	 */
	public int getWrapWidth() {
		return wrapWidth;
	}

	/**
	 * @param wrapWidth the wrapWidth to set
	 */
	public void setWrapWidth(int wrapWidth) {
		this.wrapWidth = wrapWidth;
	}

	/**
	 * This method has been deprecated and you should use the getPromptText() method instead.
	 * @deprecated
	 */
	@Deprecated
	public String getDefaultText(){
		return promptText.getPlainText();
	}

	/**
	 * Get the prompt text used in this control.
	 * @return the prompt text without styling
	 */
	public String getPromptText(){
		return promptText.getPlainText();
	}

	/**
	 * Get the text in the control
	 * @return the text without styling
	 */
	public String getText(){
		return stext.getPlainText();
	}

	/**
	 * Get the styled text in the control
	 * @return the text with styling
	 */
	public StyledString getStyledText(){
		return stext;
	}

	/**
	 * Adds the text attribute to a range of characters on a particular line. If charEnd
	 * is past the EOL then the attribute will be applied to the end-of-line.
	 * 
	 * @param attr the text attribute to add
	 * @param value value of the text attribute
	 * @param charStart the position of the first character to apply the attribute
	 * @param charEnd the position after the last character to apply the attribute
	 */
	public void addStyle(TextAttribute attr, Object value, int charStart, int charEnd){
		if(stext != null){
			stext.addAttribute(attr, value, charStart, charEnd);
			bufferInvalid = true;
		}
	}

	/**
	 * Adds the text attribute to a range of characters on a particular line. If charEnd
	 * is past the EOL then the attribute will be applied to the end-of-line.
	 * 
	 * @param attr the text attribute to add
	 * @param value value of the text attribute
	 */
	public void addStyle(TextAttribute attr, Object value){
		if(stext != null){
			stext.addAttribute(attr, value);
			bufferInvalid = true;
		}
	}

	/**
	 * Clears all text attribute from a range of characters starting at position 
	 * charStart and ending with the character preceding charEnd. 
	 * 
	 * 
	 * @param charStart the position of the first character to apply the attribute
	 * @param charEnd the position after the last character to apply the attribute
	 */
	public void clearStyles(int charStart, int charEnd){
		if(stext != null) {
			stext.clearAttributes(charStart, charEnd);
			bufferInvalid = true;
		}
	}

	/**
	 * Clear all styles from the entire text.
	 */
	public void clearStyles(){
		if(stext != null){
			stext.clearAttributes();
			bufferInvalid = true;
		}
	}

	/**
	 * Set the font for this control.
	 * @param font
	 */
	public void setFont(Font font) {
		if(font != null && font != localFont && buffer != null){
			localFont = font;
			buffer.g2.setFont(localFont);
			stext.getLines(buffer.g2);
			ptx = pty = 0;
			setScrollbarValues(ptx, pty);
			bufferInvalid = true;
		}
	}

	//    SELECTED / HIGHLIGHTED TEXT

	/**
	 * Get the text that has been selected (highlighted) by the user. <br>
	 * @return the selected text without styling
	 */
	public String getSelectedText(){
		if(!hasSelection())
			return "";
		TextLayoutHitInfo startSelTLHI;
		TextLayoutHitInfo endSelTLHI;
		if(endTLHI.compareTo(startTLHI) == -1){
			startSelTLHI = endTLHI;
			endSelTLHI = startTLHI;
		}
		else {
			startSelTLHI = startTLHI;
			endSelTLHI = endTLHI;
		}
		int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
		int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
		String s = stext.getPlainText().substring(ss, ee);
		return s;
	}

	/**
	 * If some text has been selected then set the style. If there is no selection then 
	 * the text is unchanged.
	 * 
	 * 
	 * @param style
	 */
	public void setSelectedTextStyle(TextAttribute style, Object value){
		if(!hasSelection())
			return;
		TextLayoutHitInfo startSelTLHI;
		TextLayoutHitInfo endSelTLHI;
		if(endTLHI.compareTo(startTLHI) == -1){
			startSelTLHI = endTLHI;
			endSelTLHI = startTLHI;
		}
		else {
			startSelTLHI = startTLHI;
			endSelTLHI = endTLHI;
		}
		int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
		int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
		stext.addAttribute(style, value, ss, ee);

		// We have modified the text style so the end of the selection may have
		// moved, so it needs to be recalculated. The start will be unaffected.
		stext.getLines(buffer.g2);
		endSelTLHI.tli = stext.getTLIforCharNo(ee);
		int cn = ee - endSelTLHI.tli.startCharIndex;
		if(cn == 0) // start of line
			endSelTLHI.thi = endSelTLHI.tli.layout.getNextLeftHit(1);
		else 
			endSelTLHI.thi = endSelTLHI.tli.layout.getNextRightHit(cn-1);
		bufferInvalid = true;
	}

	/**
	 * Clear any styles applied to the selected text.
	 */
	public void clearSelectionStyle(){
		if(!hasSelection())
			return;
		TextLayoutHitInfo startSelTLHI;
		TextLayoutHitInfo endSelTLHI;
		if(endTLHI.compareTo(startTLHI) == -1){
			startSelTLHI = endTLHI;
			endSelTLHI = startTLHI;
		}
		else {
			startSelTLHI = startTLHI;
			endSelTLHI = endTLHI;
		}
		int ss = startSelTLHI.tli.startCharIndex + startSelTLHI.thi.getInsertionIndex();
		int ee = endSelTLHI.tli.startCharIndex + endSelTLHI.thi.getInsertionIndex();
		stext.clearAttributes(ss, ee);

		// We have modified the text style so the end of the selection may have
		// moved, so it needs to be recalculated. The start will be unaffected.
		stext.getLines(buffer.g2);
		endSelTLHI.tli = stext.getTLIforCharNo(ee);
		int cn = ee - endSelTLHI.tli.startCharIndex;
		if(cn == 0) // start of line
			endSelTLHI.thi = endSelTLHI.tli.layout.getNextLeftHit(1);
		else 
			endSelTLHI.thi = endSelTLHI.tli.layout.getNextRightHit(cn-1);
		bufferInvalid = true;
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

	/**
	 * Find out if some text is selected (highlighted)
	 * @return true if some text is selected else false
	 */
	public boolean hasSelection(){
		return (startTLHI.tli != null && endTLHI.tli != null && startTLHI.compareTo(endTLHI) != 0);	
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
	 * Determines whether the text can be edited using the keyboard or mouse. It
	 * still allows the text to be modified by the sketch code. <br>
	 * If text editing is being disabled and the control has focus then it is forced
	 * to give up that focus. <br>
	 * This might be useful if you want to use a GTextArea control to display large 
	 * amounts of text that needs scrolling (so cannot use a GLabel) but must not 
	 * change e.g. a user instruction guide.
	 * 
	 * @param enableTextEdit false to disable keyboard input
	 */
	public void setTextEditEnabled(boolean enableTextEdit){
		// If we are disabling this then make sure it does not have focus
		if(enableTextEdit == false && focusIsWith == this){
			loseFocus(null);
		}
		enabled = enableTextEdit;
		textEditEnabled = enableTextEdit;
	}

	/**
	 * Is this control keyboard enabled
	 */
	public boolean isTextEditEnabled(){
		return textEditEnabled;
	}
	
	public void keyEvent(KeyEvent e) {
		if(!visible  || !enabled || !textEditEnabled || !available) return;
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

	// Enable polymorphism. 
	protected void keyPressedProcess(int keyCode, char keyChar, boolean shiftDown, boolean ctrlDown) { }

	protected void keyTypedProcess(int keyCode, char keyChar, boolean shiftDown, boolean ctrlDown){ }


	// Only executed if text has changed
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

//			// Is do we have to move cursor to start of next line
//			if(newline) {
//				if(pos >= stext.length()){
//					stext.insertCharacters(pos, " ");
//					stext.getLines(buffer.g2);
//				}
//				moveCaretRight(endTLHI);
//				calculateCaretPos(endTLHI);
//			}
//			// Finish off by ensuring no selection, invalidate buffer etc.
//			startTLHI.copyFrom(endTLHI);
//		}
		bufferInvalid = true;
		return true;
	}

	/**
	 * Do not call this directly. A timer calls this method as and when required.
	 */
	public void flashCaret(GTimer timer){
		showCaret = !showCaret;
	}

	/**
	 * Do not call this method directly, GUI uses it to handle input from
 the horizontal scrollbar.
	 */
	public void hsbEventHandler(GScrollbar scrollbar, GEvent event){
		keepCursorInView = false;
		ptx = hsb.getValue() * (stext.getMaxLineLength() + 4);
		bufferInvalid = true;
	}

	/**
	 * Do not call this method directly, GUI uses it to handle input from
 the vertical scrollbar.
	 */
	public void vsbEventHandler(GScrollbar scrollbar, GEvent event){
		keepCursorInView = false;
		pty = vsb.getValue() * (stext.getTextAreaHeight() + 1.5f * stext.getMaxLineHeight());
		bufferInvalid = true;
	}

	/**
	 * Permanently dispose of this control.
	 */
	public void markForDisposal(){
		if(tabManager != null)
			tabManager.removeControl(this);
		super.markForDisposal();
	}

	/**
	 * Save the styled text used by this control to file. <br>
	 * It will also save the start and end position of any text selection.
	 * 
	 * @param fname the name of the file to use
	 * @return true if saved successfully else false
	 */
	public boolean saveText(String fname){
		if(stext == null)
			return false;
		if(hasSelection()){
			stext.startIdx = startTLHI.tli.startCharIndex + startTLHI.thi.getInsertionIndex();
			stext.endIdx = endTLHI.tli.startCharIndex + endTLHI.thi.getInsertionIndex();
		}
		else {
			stext.startIdx = stext.endIdx = -1;
		}
		StyledString.save(winApp, stext, fname);
		return true;
	}

	/**
	 * Load the styled string to be used by this control. <br>
	 * It will also restore any text selection saved with the text.
	 * 
	 * @param fname the name of the file to use
	 * @return true if loaded successfully else false
	 */
	public boolean loadText(String fname){
		StyledString ss = StyledString.load(winApp, fname);
		if(ss == null)
			return false;
		setStyledText(ss);
		// Now restore any text selection
		if(stext.startIdx >=0){ // we have a selection
			// Selection starts at ...
			startTLHI = new TextLayoutHitInfo();
			startTLHI.tli = stext.getTLIforCharNo(stext.startIdx);
			int pInLayout = stext.startIdx - startTLHI.tli.startCharIndex;
			if(pInLayout == 0)
				startTLHI.thi = startTLHI.tli.layout.getNextLeftHit(1);
			else
				startTLHI.thi = startTLHI.tli.layout.getNextRightHit(pInLayout - 1);
			// Selection ends at ...
			endTLHI = new TextLayoutHitInfo();
			endTLHI.tli = stext.getTLIforCharNo(stext.endIdx);
			pInLayout = stext.endIdx - endTLHI.tli.startCharIndex;

			if(pInLayout == 0)
				endTLHI.thi = endTLHI.tli.layout.getNextLeftHit(1);
			else
				endTLHI.thi = endTLHI.tli.layout.getNextRightHit(pInLayout - 1);
			calculateCaretPos(endTLHI);
		}
		bufferInvalid = true;
		return true;
	}

}
