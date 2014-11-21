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

import java.awt.Font;
import java.awt.font.TextAttribute;
import processing.core.PApplet;


/**
 * Base class for any control that uses styled text.
 * 
 * @author Peter Lager
 *
 */
public abstract class GTextBase extends GControl {

	/** The styled text used by this control */
	public StyledString stext = null;
	
	protected Font localFont = GUI.globalFont;
	

	/**
	 * Constructor
	 * @param theApplet
	 * @param p0
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public GTextBase(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
		buffer.g2.setFont(localFont);
	}

	/**
	 * Used internally to enforce minimum size constraints and to enable 
	 * mimimising the height used by text icon controls (labels, buttons, 
	 * radio button and checkboxes)
	 * 
	 * @param w the new width
	 * @param h the new height
	 */
	protected void resize(int w, int h){
		super.resize(w, h);
		buffer.g2.setFont(localFont);
	}

	/**
	 * Set the text to be displayed.
	 * 
	 * @param text
	 */
	public void setText(String text){
		if(text == null || text.length() == 0 )
			text = " ";
		stext = new StyledString(text, Integer.MAX_VALUE);
		bufferInvalid = true;
	}
	
	/**
	 * Load the styled string to be used by this control.
	 * 
	 * @param fname the name of the file to use
	 * @return true if loaded successfully else false
	 */
	public boolean loadText(String fname){
		StyledString ss = StyledString.load(winApp, fname);
		if(ss != null){
			setStyledText(ss);
			stext.startIdx = stext.endIdx = -1;
			bufferInvalid = true;
			return true;
		}
		return false;
	}
	
	/**
	 * Save the styled text used by this control to file.
	 * 
	 * @param fname the name of the file to use
	 * @return true if saved successfully else false
	 */
	public boolean saveText(String fname){
		if(stext != null){
			stext.startIdx = stext.endIdx = -1;
			StyledString.save(winApp, stext, fname);
			return true;
		}
		return false;
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

	/**
	 * Allows the user to provide their own styled text for this component
	 * @param ss
	 */
	public void setStyledText(StyledString ss){
		if(ss != null) {
			stext = ss;
			stext.setWrapWidth((int)width - TPAD4);
			bufferInvalid = true;
		}
	}
	
	public void forceBufferUpdate(){
		if(stext != null)
			stext.invalidateText();
		bufferInvalid = true;
	}

	/**
	 * Clear <b>all</b> applied styles from the whole text.
	 */
	public void setTextPlain(){
		stext.clearAttributes();
		bufferInvalid = true;
	}
	
	/**
	 * Make the selected characters bold. <br>
	 * Characters affected are >= start and < end
	 * 
	 * @param start the first character to style
	 * @param end the first character not to style
	 */
	public void setTextBold(int start, int end){
		addAttributeImpl(GUI.WEIGHT, GUI.WEIGHT_BOLD, start, end);
	}

	/**
	 * Make all the characters bold.
	 */
	public void setTextBold(){
		addAttributeImpl(GUI.WEIGHT, GUI.WEIGHT_BOLD);
	}

	/**
	 * Make the selected characters italic. <br>
	 * Characters affected are >= start and < end
	 * 
	 * @param start the first character to style
	 * @param end the first character not to style
	 */
	public void setTextItalic(int start, int end){
		addAttributeImpl(GUI.POSTURE, GUI.POSTURE_OBLIQUE, start, end);
	}

	/**
	 * Make all the characters italic.
	 */
	public void setTextItalic(){
		addAttributeImpl(GUI.POSTURE, GUI.POSTURE_OBLIQUE);
	}

	/**
	 * Get the text used for this control.
	 * @return the displayed text without styling
	 */
	public StyledString getStyledText(){
		return stext;
	}
	
	/**
	 * Get the text used for this control.
	 * @return the displayed text without styling
	 */
	public String getText(){
		return stext.getPlainText();
	}
	
	/**
	 * Apply the style to the whole text
	 * 
	 * @param style the style attribute
	 * @param value 'amount' to apply
	 */
	protected void addAttributeImpl(TextAttribute style, Object value){
		stext.addAttribute(style, value);
		bufferInvalid = true;
	}
	
	/**
	 * Apply the style to a portion of the strin
	 * 
	 * @param style the style attribute
	 * @param value 'amount' to apply
	 * @param s first character to be included for styling
	 * @param e the first character not to be included for stylin
	 */
	protected void addAttributeImpl(TextAttribute style, Object value, int s, int e){
		if(s >= e) return;
		if(s < 0) s = 0;
		if(e > stext.length()) e = stext.length();
		stext.addAttribute(style, value, s, e);
		bufferInvalid = true;
	}


}
