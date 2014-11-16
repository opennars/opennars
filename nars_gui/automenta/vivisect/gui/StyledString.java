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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.GraphicAttribute;
import java.awt.font.ImageGraphicAttribute;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import processing.core.PApplet;

/**
 * This class is used to represent text with attributes. <br>
 * It means that you don't have to have the same style of font
 * or even the same font face over the whole length of the text. <br>
 * 
 * Most font features can be modified all except the text background 
 * which is transparent. There is a feature to highlight part of the string
 * by having a different background colour but this is used for highlighting
 * selected text in GTextField and GTextArea components. <br>
 *  
 *  It is also used for all controls that use text.
 * @author Peter Lager
 *
 */
public final class StyledString implements GConstantsInternal, Serializable {

    private static final long serialVersionUID = -8272976313009558508L;
	
    transient private AttributedString styledText = null;
	transient private ImageGraphicAttribute spacer = null;
	transient private LineBreakMeasurer lineMeasurer = null;
	transient private LinkedList<TextLayoutInfo> linesInfo = new LinkedList<TextLayoutInfo>();
	transient private Font font = null;

	private static final char EOL = '\n';
	
	// The plain text to be styled
	private String plainText = "";
	// List of attribute runs to match font
	private LinkedList<AttributeRun> baseStyle = new LinkedList<AttributeRun>();
	// List of attribute runs to be applied over the base style
	private LinkedList<AttributeRun> atrun = new LinkedList<AttributeRun>();

	// The width to break a line
	private int wrapWidth = Integer.MAX_VALUE;
	// Flag to determine whether the text layouts need recalculating
	private boolean invalidLayout = true;
	// Flag to determine whether the actual character string have changed
	private boolean invalidText = true;

	// Base justification
	private boolean justify = false;
	private float justifyRatio = 0.7f;

	// Stats
	private float textHeight = 0;
	private float maxLineLength = 0;
	private float maxLineHeight = 0;
	private int nbrLines;

	// These are only used by GTextField and GTextArea to store the start and end positions
	// for selected text when the string is to be saved.
	int startIdx = -1;
	int endIdx = -1;

	/**
	 * This is assumed to be a single line of text (i.e. no wrap). 
	 * EOL characters will be stripped from the text before use.
	 * 
	 * @param startText
	 */
	public StyledString(String startText){
		plainText = removeSingleSpacingFromPlainText(startText);
		spacer = getParagraghSpacer(1); //  safety
		// Get rid of any EOLs
		styledText = new AttributedString(plainText);
		clearAttributes();
		applyAttributes();
		invalidText = true;
		invalidLayout = true;
	}

	/**
	 * Supports multiple lines of text wrapped on word boundaries. <br>
	 * 
	 * @param startText
	 * @param wrapWidth
	 */
	public StyledString(String startText, int wrapWidth){
		if(wrapWidth > 0 && wrapWidth < Integer.MAX_VALUE)
			this.wrapWidth = wrapWidth;
		plainText = (wrapWidth == Integer.MAX_VALUE) ? removeSingleSpacingFromPlainText(startText) : removeDoubleSpacingFromPlainText(startText);
		spacer = getParagraghSpacer(this.wrapWidth);
		styledText = new AttributedString(plainText);
		styledText = insertParagraphMarkers(plainText, styledText);
		clearAttributes();
		applyAttributes();
		invalidText = true;
		invalidLayout = true;
	}

	/**
	 * Converts this StyledString from multi-line to single-line by replacing all EOL
	 * characters with the space character
	 * for paragraphs
	 * @param ptext
	 * @param as
	 * @return the converted string
	 */
	StyledString convertToSingleLineText(){
		// Make sure we have something to work with.
		if(styledText == null || plainText == null){
			plainText = "";
			styledText = new AttributedString(plainText);
		}
		else {
			// Scan through plain text and for each EOL replace the paragraph spacer from
			// the attributed string (styledText).
			int fromIndex = plainText.indexOf('\n', 0);
			if(fromIndex >= 0){
				while(fromIndex >= 0){
					try { // if text == "\n" then an exception is thrown
						styledText.addAttribute(TextAttribute.CHAR_REPLACEMENT, ' ', fromIndex, fromIndex + 1);
						fromIndex = plainText.indexOf('\n', fromIndex + 1);
					}
					catch(Exception excp){
						break;
					}
				}
				// Finally replace all EOL in the plainText
				plainText = plainText.replace('\n', ' ');
			}
		}
		wrapWidth = Integer.MAX_VALUE;
		return this;
	}

	/**
	 * Get the plain text as a String. Any line breaks will kept and will
	 * be represented by the character 'backslash n' <br>
	 * @return the associated plain text
	 */
	public String getPlainText(){
		return plainText;
	}

	/**
	 * Get the plain text as a String. Any line breaks will kept and will
	 * be represented by the character 'backslash n' <br>
	 * 
	 * @param beginIdx the beginning index inclusive
	 * @param endIdx the ending index exclusive
	 * @return the substring starting at beginIdx to endIdx-1
	 */
	public String getPlainText(int beginIdx, int endIdx){
		if(beginIdx < 0) beginIdx = 0;
		if(endIdx > plainText.length()) endIdx = plainText.length();
		return plainText.substring(beginIdx, endIdx);
	}

	/**
	 * Get a line of text from the plain text. Lines are separated by 
	 * End-of-line (EOL) characters.
	 * 
	 * @param lineNo the line number we want the text for
	 * @return the line of text or an empty string if the line number is invalid.
	 */
	public String getPlainText(int lineNo){
		Point loc = getPlainTextLinePosImpl(lineNo, null);
		return (loc == null) ? "" : plainText.substring(loc.x, loc.y);
	}
	
	
	private Point getPlainTextLinePosImpl(int lineNo, Point loc){
		if(lineNo < 0)
			return null;
		int pos = 0, p0 = 0, count = lineNo;
		// Find start of line
		while(count > 0 && pos < plainText.length()){
			if(plainText.charAt(pos) == EOL)
				count--;
			pos++;
		}
		// If we haven't found start of line then return empty string
		if(count > 0)
			return null;
		p0 = pos;
		while(pos < plainText.length() && plainText.charAt(pos) != EOL)
			pos++;
		if(loc == null)
			loc = new Point();
		else {
			loc.x = p0;
			loc.y = pos;
		}
		return loc;		
	}
	
	/**
	 * Get the plain text as a String array. (splitting on line breaks)
	 * @return the associated plain text as a String array split on line breaks
	 */
	public String[] getPlainTextAsArray(){
		return plainText.split("\n");
	}

	/**
	 * Get the number of characters in this styled string
	 */
	public int length(){
		return plainText.length();
	}

	/**
	 * Text can be either left or fully justified.
	 * @param justify true for full justification
	 */
	public void setJustify(boolean justify){
		if(this.justify != justify){
			this.justify = justify;
			invalidLayout = true;
		}
	}

	/**
	 * Justify only if the line has sufficient text to do so.
	 * 
	 * @param jRatio ratio of text length to visibleWidth 
	 */
	public void setJustifyRatio(float jRatio){
		if(justifyRatio != jRatio){
			justifyRatio = jRatio;
			if(justify)
				invalidLayout = true;
		}
	}

	/**
	 * This class uses transparent images to simulate end/starting positions
	 * for paragraphs
	 * @param ptext
	 * @param as
	 * @return the styled string with paragraph marker images embedded
	 */
	private AttributedString insertParagraphMarkers(String ptext, AttributedString as ){
		if(ptext != null && ptext.length() > 0)
			plainText = ptext;
		int fromIndex = ptext.indexOf('\n', 0);
		while(fromIndex >= 0){
			try { // if text == "\n" then an exception is thrown
				as.addAttribute(TextAttribute.CHAR_REPLACEMENT, spacer, fromIndex, fromIndex + 1);
				fromIndex = ptext.indexOf('\n', fromIndex + 1);
			}
			catch(Exception excp){
				break;
			}
		}
		return as;
	}

	/**
	 * If the text attribute is BACKGROUND or FOREGROUND then if we pass
	 * an integer (e.g. from Processing sketch) then convert it.
	 * 
	 * @param ta the text attribute
	 * @param value the value to use with this text attribute
	 */
	private Object validateTextAttributeColor(TextAttribute ta, Object value){
		if((ta == TextAttribute.BACKGROUND || ta == TextAttribute.FOREGROUND) && !(value instanceof Color))
			return new Color((Integer) value);
		else
			return value;
	}
	
	/**
	 * Add an attribute that affects the whole length of the string.
	 * 
	 * @param type attribute type
	 * @param value attribute value
	 */
	public void addAttribute(Attribute type, Object value){
		addAttribute(type, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Add a text attribute (style) to the specifies range of characters
	 * 
	 * @param type attribute type
	 * @param value attribute value
	 * @param lineNo the line of test affected
	 * @param charStart the first character affected
	 * @param charEnd the character position after the last character affected.
	 */
	public void addAttribute(Attribute type, Object value, int lineNo, int charStart, int charEnd){
		if(lineNo >= 0 && lineNo < linesInfo.size()){
			TextLayoutInfo tli = linesInfo.get(lineNo);
			int lineStartsAt = tli.startCharIndex;
			charEnd = Math.min(charEnd,  tli.nbrChars);
			addAttribute(type, value, lineStartsAt + charStart, lineStartsAt + charEnd);
		}
	}
	
	/**
	 * Add a text attribute (style) to an entire display line
	 * 
	 * @param type attribute type
	 * @param value attribute value
	 * @param lineNo the line of test affected
	 */
	public void addAttribute(Attribute type, Object value, int lineNo){
		if(lineNo >= 0 && lineNo < linesInfo.size()){
			TextLayoutInfo tli = linesInfo.get(lineNo);
			addAttribute(type, value, tli.startCharIndex, tli.startCharIndex + tli.nbrChars);
		}
	}
	
	/**
	 * Set the attribute to be applied to a range of characters starting at
	 * beginIdx and ending with endIdx-1.
	 * 
	 * @param type attribute type
	 * @param value attribute value
	 * @param beginIdx the index of the first character (inclusive)
	 * @param endIdx the index of the last character (exclusive)
	 */
	public void addAttribute(Attribute type, Object value, int beginIdx, int endIdx){
		value = validateTextAttributeColor((TextAttribute) type, value);
		AttributeRun ar = new AttributeRun(type, value, beginIdx, endIdx);
		// If we already have attributes try and rationalize the number by merging
		// runs if possible and removing runs that no longer have a visible effect.
		if(atrun.size() > 0){
			ListIterator<AttributeRun> iter = atrun.listIterator(atrun.size());
			while(iter.hasPrevious()){
				AttributeRun a = iter.previous();
				int action = ar.intersectionWith(a);
				int intersect = action & I_MODES;
				int combiMode = action & COMBI_MODES;
				if(combiMode == MERGE_RUNS){
					switch(intersect){
					case I_TL:
					case I_CL:
						ar.start = a.start;
						iter.remove();
						break;
					case I_TR:
					case I_CR:
						ar.end = a.end;
						iter.remove();
						break;
					}
				}
				else if(combiMode == CLIP_RUN){
					switch(intersect){
					case I_CL:
						a.end = ar.start;
						break;
					case I_CR:
						a.start = ar.end;
						break;
					}
				}
				switch(intersect){
				case I_INSIDE:
					iter.remove();
					break;
				case I_COVERED:
					ar = null;
					break;				
				}
			}
		}
		// If the run is still effective then add it
		if(ar != null)
			atrun.addLast(ar);
		applyAttributes();
		invalidLayout = true;
	}

	/**
	 * Remove text attributes (style) to the specified line and range of characters.
	 * 
	 * @param lineNo the line of test affected
	 * @param beginIdx the index of the first character (inclusive)
	 * @param endIdx the index of the last character (exclusive)
	 */
	public void clearAttributes(int lineNo, int beginIdx, int endIdx){
		if(lineNo >= 0 && lineNo < linesInfo.size()){
			TextLayoutInfo tli = linesInfo.get(lineNo);
			int lineStartsAt = tli.startCharIndex;
			endIdx = Math.min(endIdx,  tli.nbrChars);
			clearAttributes(lineStartsAt + beginIdx, lineStartsAt + endIdx);
		}
	}

	/**
	 * 
	 * @param lineNo
	 */
	public void clearAttributes(int lineNo){
		if(lineNo >= 0 && lineNo < linesInfo.size()){
			TextLayoutInfo tli = linesInfo.get(lineNo);
			clearAttributes(tli.startCharIndex, tli.startCharIndex + tli.nbrChars);
		}
	}

	/**
	 * Remove text attributes (style) to the specified range of characters.
	 * 
	 * @param beginIdx the index of the first character (inclusive)
	 * @param endIdx the index of the last character (exclusive)
	 */
	public void clearAttributes(int beginIdx, int endIdx){
		ListIterator<AttributeRun> iter = atrun.listIterator();
		AttributeRun ar;
		while(iter.hasNext()){
			ar = iter.next();
			// Make sure we have intersection
			if( !(beginIdx >= ar.end && endIdx >= ar.start )){
				// Find the limits to clear
				int s = Math.max(beginIdx, ar.start);
				int e = Math.min(endIdx, ar.end);
				if(ar.start == s && ar.end == e)
					iter.remove();
				else if(ar.start == s) // clear style from beginning
					ar.start = e;
				else if(ar.end == e) // clear style from end
					ar.end = s;
				else {	// Split attribute run
					AttributeRun ar2 = new AttributeRun(ar.atype, ar.value, e, ar.end);
					iter.add(ar2);
					ar.end = s;
				}
			}
		}
		invalidText = true;
	}

	/**
	 * Removes all styling from the string.
	 * 
	 */
	public void clearAttributes(){
		atrun.clear();
		invalidText = true;
	}

	/**
	 * Must call this method to apply
	 */
	private void applyAttributes(){
		if(plainText.length() > 0){
			for(AttributeRun bsar : baseStyle)
				styledText.addAttribute(bsar.atype, bsar.value);
			Iterator<AttributeRun> iter = atrun.iterator();
			AttributeRun ar;

			while(iter.hasNext()){
				ar = iter.next();
				if(ar.end == Integer.MAX_VALUE)
					styledText.addAttribute(ar.atype, ar.value);
				else {
					// If an attribute run fails do not try and fix it - dump it
					try {
						styledText.addAttribute(ar.atype, ar.value, ar.start, ar.end);
					}
					catch(Exception excp){
						System.out.println("Dumping " + ar);
						excp.printStackTrace();
						iter.remove();
					}
				}
			}
		}
		invalidLayout = true;
	}

	/**
	 * Insert some text into the position indicated. <br>
	 * 
	 * @param lineNo a valid line number
	 * @param charStart the position in the line >= 0
	 * @param chars the characters to insert
	 * @param startNewLine prefix the chars with a EOL
	 * @param endNewLine postfix the chars with a EOL
	 * @return the number of characters inserted
	 */
	public int insertCharacters(String chars, int lineNo, int charStart, boolean startNewLine, boolean endNewLine){
		if(lineNo >= 0 && lineNo < linesInfo.size()){
			TextLayoutInfo tli = linesInfo.get(lineNo);
			int insertPos = tli.startCharIndex + Math.min(charStart,  tli.nbrChars);
			return insertCharactersImpl(insertPos, chars, startNewLine, endNewLine);
		}
		return 0;
	}
	
	/**
	 * Insert 1 or more characters into the string. The inserted text will first be made
	 * safe by removing any inappropriate EOL characters. <br>
	 * Do not use this method to insert EOL characters, use the <pre>insertEOL(int)</pre>
	 * method instead.
	 * 
	 * @param insertPos position in string to insert characters
	 * @param chars the characters to insert
	 * @return the number of characters inserted
	 */
	public int insertCharacters(String chars, int insertPos){
		return insertCharactersImpl(insertPos, chars, false, false);
	}

	/**
	 * Insert 1 or more characters into the string. The inserted text will first be made
	 * safe by removing any inappropriate EOL characters. <br>
	 * Do not use this method to insert EOL characters, use the <pre>insertEOL(int)</pre>
	 * method instead.
	 * 
	 * @param insertPos position in string to insert characters
	 * @param chars the characters to insert
	 * @param startNewLine if true insert onto a new line
	 * @return the number of characters inserted
	 */
	public int insertCharacters(String chars, int insertPos, boolean startNewLine, boolean endNewLine){
		return insertCharactersImpl(insertPos, chars, startNewLine, endNewLine);
	}

	/**
	 * Implementation for inserting characters into the plain text.
	 * 
	 * @param insertPos the position to insert the text
	 * @param chars the characters to insert
	 * @param startNewLine inserted text to start on new line
	 * @param endNewLine text after inserted text to start on new line
	 * @return the number of characters inserted including EOLs
	 */
	private int insertCharactersImpl(int insertPos, String chars, boolean startNewLine, boolean endNewLine){
		chars = makeStringSafeForInsert(chars);
		int nbrChars = chars.length();
		int nbrCharsInserted = nbrChars;
		if(nbrChars > 0){
			plainText = plainText.substring(0, insertPos) + chars + plainText.substring(insertPos);
			if(endNewLine && plainText.charAt(insertPos + nbrChars) != '\n')
				nbrCharsInserted += insertEOL(insertPos + nbrChars) ? 1 : 0;
			if(startNewLine && insertPos > 0)
				nbrCharsInserted += insertEOL(insertPos) ? 1 : 0;
			insertParagraphMarkers(plainText, styledText);
			for(AttributeRun ar : atrun){
				if(ar.end < Integer.MAX_VALUE){
					if(ar.end >= insertPos){
						ar.end += nbrChars;
						if(ar.start >= insertPos)
							ar.start += nbrChars;
					}
				}
			}
			invalidText = true;
		}
		return nbrCharsInserted;		
	}

	/**
	 * This is ONLY used when multiple characters are to be inserted. <br>
	 * If it is single line text i.e. no wrapping then it removes all EOLs
	 * If it is multiple line spacing it will reduce all double EOLs to single
	 * EOLs and remove any EOLs at the start or end of the string.
	 * 
	 * @param chars
	 * @return a string that is safe for inserting
	 */
	private String makeStringSafeForInsert(String chars){
		// Get rid of single / double line spacing
		if(chars.length() > 0){
			if(wrapWidth == Integer.MAX_VALUE) // no wrapping remove all
				chars = removeSingleSpacingFromPlainText(chars);
			else {
				chars = removeDoubleSpacingFromPlainText(chars); // wrapping remove double spacing
				// no remove EOL at ends of string
				while(chars.length() > 0 && chars.charAt(0) == '\n')
					chars = chars.substring(1);
				while(chars.length() > 0 && chars.charAt(chars.length() - 1) == '\n')
					chars = chars.substring(0, chars.length() - 1);
			}
		}
		return chars;
	}

	/**
	 * Use this method to insert an EOL character.
	 * @param insertPos index position to insert EOL
	 * @return true if an EOL was inserted into the string
	 */
	public boolean insertEOL(int insertPos){
		if(wrapWidth != Integer.MAX_VALUE){
			if(insertPos == 0)
				return false;
			if(insertPos > 0 && plainText.charAt(insertPos-1) == '\n')
				return false;
			if(insertPos < plainText.length()-1 && plainText.charAt(insertPos+1) == '\n'){
				return false;
			}
			plainText = plainText.substring(0, insertPos) + "\n" + plainText.substring(insertPos);
			insertParagraphMarkers(plainText, styledText);
			for(AttributeRun ar : atrun){
				if(ar.end < Integer.MAX_VALUE){
					if(ar.end >= insertPos){
						ar.end += 1;
						if(ar.start >= insertPos)
							ar.start += 1;
					}
				}
			}
			invalidText = true;
			return true;
		}
		return false;
	}


	/**
	 * Remove a number of characters from the string
	 * 
	 * @param nbrToRemove number of characters to remove
	 * @param fromPos start location for removal
	 * @return true if the deletion was successful else false
	 */
	public boolean deleteCharacters(int fromPos, int nbrToRemove){
		if(fromPos < 0 || fromPos + nbrToRemove > plainText.length())
			return false;
		/*
		 * If the character preceding the selection and the character immediately after the selection
		 * are both EOLs then increment the number of characters to be deleted
		 */
		if(wrapWidth != Integer.MAX_VALUE){
			if(fromPos > 0 && fromPos + nbrToRemove < plainText.length() - 1){
				if(plainText.charAt(fromPos) == '\n' && plainText.charAt(fromPos + nbrToRemove) == '\n'){
					nbrToRemove++;
				}
			}
		}
		if(fromPos != 0)
			plainText = plainText.substring(0, fromPos) + plainText.substring(fromPos + nbrToRemove);
		else
			plainText = plainText.substring(fromPos + nbrToRemove);
		// For wrappable text make sure we have not created
		if(plainText.length() == 0){
			atrun.clear();
			styledText = null;
		}
		else {
			ListIterator<AttributeRun> iter = atrun.listIterator(atrun.size());
			AttributeRun ar;
			while(iter.hasPrevious()){
				ar = iter.previous();
				if(ar.end < Integer.MAX_VALUE){
					// Only need to worry about this if the run ends after the deletion point
					if(ar.end >= fromPos){
						int lastPos = fromPos + nbrToRemove;
						// Deletion removes entire run
						if(fromPos <= ar.start && lastPos >= ar.end){
							iter.remove();
							continue;
						}
						// Deletion fits entirely within the run
						if(fromPos > ar.start && lastPos < ar.end){
							ar.end -= nbrToRemove;
							continue;
						}
						// Now we have overlap either at one end of the run
						// Overlap at start of run?
						if(fromPos <= ar.start){
							ar.start = fromPos;
							ar.end -= nbrToRemove;
							continue;
						}
						// Overlap at end of run?
						if(lastPos >= ar.end){
							ar.end = fromPos;
							continue;
						}
						System.out.println("This run was not modified");
						System.out.println("Run from " + ar.start + " to " + ar.end);
						System.out.println("Delete from " + fromPos + " To " + lastPos + "  (" + nbrToRemove + " to remove)");
					}
				}		
			}
		}
		invalidText = true;
		return true;
	}

	public void setFont(Font a_font){
		if(a_font != null){
			font = a_font;
			baseStyle.clear();
			baseStyle.add(new AttributeRun(TextAttribute.FAMILY, font.getFamily()));
			baseStyle.add(new AttributeRun(TextAttribute.SIZE, font.getSize()));
			baseStyle.add(new AttributeRun(TextAttribute.WIDTH, TextAttribute.WIDTH_REGULAR));
			if(font.isBold())
				baseStyle.add(new AttributeRun(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR));
			else
				baseStyle.add(new AttributeRun(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR));	
			if(font.isItalic())
				baseStyle.add(new AttributeRun(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE));	
			else
				baseStyle.add(new AttributeRun(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR));	
			invalidText = true;
		}
	}

	public void invalidateText(){
		invalidText = true;
	}
	
	/**
	 * Get the text layouts for display if the string has changed since last call
	 * to this method regenerate them.
	 * 
	 * @param g2d Graphics2D display context
	 * @return a list of text layouts for rendering
	 */
	public LinkedList<TextLayoutInfo> getLines(Graphics2D g2d){
		if(font != g2d.getFont()){
			setFont(g2d.getFont());
			invalidText = true;
		}
		if(invalidText){
			styledText = new AttributedString(plainText);
			styledText = insertParagraphMarkers(plainText, styledText);
			setFont(font);
			applyAttributes();
			invalidText = false;
			invalidLayout = true;
		}
		if(invalidLayout){
			linesInfo.clear();
			if(plainText.length() > 0){
				textHeight = 0;
				maxLineLength = 0;
				maxLineHeight = 0;
				nbrLines = 0;
				AttributedCharacterIterator paragraph = styledText.getIterator(null, 0, plainText.length());
				FontRenderContext frc = g2d.getFontRenderContext();
				lineMeasurer = new LineBreakMeasurer(paragraph, frc);
				float yposinpara = 0;
				int charssofar = 0;
				while (lineMeasurer.getPosition() < plainText.length()) {
					TextLayout layout = lineMeasurer.nextLayout(wrapWidth);
					float advance = layout.getVisibleAdvance();
					if(justify){
						if(justify && advance > justifyRatio * wrapWidth){
							// If advance > breakWidth then we have a line break
							float jw = (advance > wrapWidth) ? advance - wrapWidth : wrapWidth;
							layout = layout.getJustifiedLayout(jw);
						}
					}
					// Remember the longest and tallest value for a layout so far.
					float lh = getHeight(layout);
					if(lh > maxLineHeight)
						maxLineHeight = lh;
					textHeight += lh;
					if(advance <= wrapWidth && advance > maxLineLength)
						maxLineLength = advance;

					// Store layout and line info
					linesInfo.add(new TextLayoutInfo(nbrLines, layout, charssofar, layout.getCharacterCount(), yposinpara));
					charssofar += layout.getCharacterCount();
					yposinpara += lh;
					nbrLines++;
				}
			}
			invalidLayout = false;
		}
		return linesInfo;
	}

	/**
	 * Get the number of lines in the layout
	 */
	public int getNbrLines(){
		return nbrLines;
	}

	/**
	 * Return the height of the text line(s)
	 */
	public float getTextAreaHeight(){
		return textHeight;
	}

	/**
	 * Return the length of the longest line.
	 */
	public float getMaxLineLength(){
		return maxLineLength;
	}

	/** 
	 * Get the height of the tallest line
	 */
	public float getMaxLineHeight(){
		return maxLineHeight;
	}

	/**
	 * Get the height of the given TextLayout
	 * @param layout
	 * @return the height of a given text layout
	 */
	private float getHeight(TextLayout layout){
		return layout.getAscent() +layout.getDescent() + layout.getLeading();
	}

	/**
	 * Get the break width used to create the lines.
	 */
	public int getWrapWidth(){
		return wrapWidth;
	}

	/**
	 * Set the maximum width of a line. 
	 * @param wrapWidth
	 */
	public void setWrapWidth(int wrapWidth){
		if(this.wrapWidth != wrapWidth){
			this.wrapWidth = wrapWidth;
			invalidLayout = true;
		}
	}

	/**
	 * Calculate the TLHI for a given pixel position
	 * @param g2d
	 * @param px
	 * @param py
	 * @return
	 */
	TextLayoutHitInfo calculateFromXY(Graphics2D g2d, float px, float py){
		TextHitInfo thi = null;
		TextLayoutInfo tli = null;
		TextLayoutHitInfo tlhi = null;
		if(invalidLayout)
			getLines(g2d);
		if(px < 0) px = 0;
		if(py < 0) py = 0;
		tli = getTLIforYpos(py);
		// Correct py to match layout's upper-left bounds
		py -= tli.yPosInPara;
		// get hit
		thi = tli.layout.hitTestChar(px,py);
		tlhi = new TextLayoutHitInfo(tli, thi);
		return tlhi;
	}

	/**
	 * Get a layout based on line number
	 * @param ln line number 
	 * @return text layout info for the line ln
	 */
	TextLayoutInfo getTLIforLineNo(int ln){
		if(ln >= 0 || ln < linesInfo.size())
			return linesInfo.get(ln);
		else
			return null;
	}

	/**
	 * This will always return a layout provide there is some text.
	 * @param y Must be >= 0
	 * @return the first layout where y is above the upper layout bounds
	 */
	TextLayoutInfo getTLIforYpos(float y){
		TextLayoutInfo tli = null;
		if(!linesInfo.isEmpty()){
			for(int i = linesInfo.size()-1; i >= 0; i--){
				tli = linesInfo.get(i);
				if(tli.yPosInPara <= y)
					break;
			}
		}
		return tli;
	}

	/**
	 * This will always return a layout provided charNo >= 0. <br>
	 * 
	 * If charNo > than the index of the last character in the plain text then this
	 * should be corrected to the last character in the layout by the caller.
	 * 
	 * @param charNo the character position in text (must be >= 0)
	 * @return the first layout where c is greater that the layout's start char index.
	 */
	TextLayoutInfo getTLIforCharNo(int charNo){
		TextLayoutInfo tli = null;
		if(!linesInfo.isEmpty()){
			for(int i = linesInfo.size()-1; i >= 0; i--){
				tli = linesInfo.get(i);
				if(tli.startCharIndex < charNo)
					break;
			}
		}
		return tli;
	}

	/**
	 * For a given line number and character position get the 
	 * corresponding TextLayoutHitInfo object 
	 * @param lineNo line number
	 * @param charNo position in line
	 * @return the corresponding TextLayoutHitInfo object
	 */
	TextLayoutHitInfo getTLHIforCharPosition(int lineNo, int charNo){
		TextLayoutHitInfo tlhi = null;
		TextHitInfo thi = null;
		TextLayoutInfo tli = getTLIforLineNo(lineNo);
		if(tli != null){
			charNo = PApplet.constrain(charNo, 0, tli.nbrChars-2);
			thi = tli.layout.getNextRightHit(charNo);
			if(thi != null)
				tlhi = new TextLayoutHitInfo(tli, thi);
		}
		return tlhi;
	} 

	/**
	 * For a given position in the plain text get the 
	 * corresponding TextLayoutHitInfo object.
	 *  
	 * @param pos position in the plaintext
	 * @return the corresponding TextLayoutHitInfo object
	 */
	TextLayoutHitInfo getTLHIforCharPosition(int pos){
		if(pos < 0 || pos >= plainText.length())
			return null;
		int lineNo = 0, posInLine = pos;
		for(lineNo = linesInfo.size()-1; lineNo >= 0; lineNo--){
			TextLayoutInfo tli = getTLIforLineNo(lineNo);
			posInLine = pos - tli.startCharIndex;
			if(tli.startCharIndex <= pos)
				break;
		}
		return getTLHIforCharPosition(lineNo, posInLine);
	}

	/**
	 * Get the character position in the plain text that is associated
	 * with the given line and character number.
	 * 
	 * @param lineNo the line number (starts with 0)
	 * @param charNo the character position in the line (starts with 0)
	 * @return the position in plain text or -1 if an invalid lineNo
	 */
	int getPos(int lineNo, int charNo){
		TextLayoutInfo tli = getTLIforLineNo(lineNo);
		if(tli != null)
			return tli.startCharIndex + charNo;
		else
			return -1;
	}
	
	/** 
	 * Ensure we do not have blank lines by replacing double EOL characters by 
	 * single EOL until there are only single EOLs. <br>
	 * Using replaceAll on its own will not work because EOL/EOL/EOL would 
	 * become EOL/EOL not the single EOL required.
	 * 
	 */
	private String removeDoubleSpacingFromPlainText(String chars){
		while(chars.indexOf("\n\n") >= 0){
			invalidText = true;
			chars = chars.replaceAll("\n\n", "\n");
		}
		return chars;
	}

	/**
	 * Remove all EOL characters from the string. This is necessary if the string
	 * is for a single line component.
	 * @param chars the string to use
	 * @return the string with all EOLs removed
	 */
	private String removeSingleSpacingFromPlainText(String chars){
		while(chars.indexOf("\n") >= 0){
			invalidText = true;
			chars = chars.replaceAll("\n", "");
		}
		return chars;
	}

	/**
	 * Create a graphic image character to simulate paragraph breaks
	 * 
	 * @param ww
	 * @return a blank image to manage paragraph ends.
	 */
	private ImageGraphicAttribute getParagraghSpacer(int ww){
		if(ww == Integer.MAX_VALUE)
			ww = 1;
		BufferedImage img = new BufferedImage(ww, 10, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(new Color(255, 255, 255, 0));
		g.fillRect(0,0,img.getWidth(), img.getHeight());
		return new ImageGraphicAttribute(img, GraphicAttribute.TOP_ALIGNMENT);
	}


	/* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	 * Serialisation routines to save/restore the StyledString to disc.
	 */

	/**
	 * Save the named StyleString in the named file.
	 * 
	 * @param papp 
	 * @param ss the styled string
	 * @param fname 
	 */
	public static void save(PApplet papp, StyledString ss, String fname){
		OutputStream os;
		ObjectOutputStream oos;
		try {
			os = papp.createOutput(fname);
			oos = new ObjectOutputStream(os);
			oos.writeObject(ss);
			os.close();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load and return a StyledString object from the given file.
	 * 
	 * @param papp
	 * @param fname the filename of the StyledString
	 */
	public static StyledString load(PApplet papp, String fname){
		StyledString ss = null;
		InputStream is;
		ObjectInputStream ios;	
		try {
			is = papp.createInput(fname);
			ios = new ObjectInputStream(is);
			ss = (StyledString) ios.readObject();
			is.close();
			ios.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ss;
	}


	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		// Recreate transient elements
		spacer = getParagraghSpacer(wrapWidth);
		styledText = new AttributedString(plainText);
		styledText = insertParagraphMarkers(plainText, styledText);
		linesInfo = new LinkedList<TextLayoutInfo>();
		applyAttributes();
	}

	/**
	 * For multi-line text, the TextHitInfo class is not enough. We also need 
	 * information about the layout so that the caret(s) can be drawn.
	 * 
	 * @author Peter Lager
	 *
	 */
	static public class TextLayoutHitInfo implements Comparable<TextLayoutHitInfo>{
		public TextLayoutInfo tli;
		public TextHitInfo thi;


		public TextLayoutHitInfo() {
			this.tli = null;
			this.thi = null;
		}

		/**
		 * @param tli
		 */
		public TextLayoutHitInfo(TextLayoutInfo tli) {
			this.tli = tli;
			this.thi = null;
		}

		/**
		 * @param tli
		 * @param thi
		 */
		public TextLayoutHitInfo(TextLayoutInfo tli, TextHitInfo thi) {
			this.tli = tli;
			this.thi = thi;
		}

		/**
		 * Copy constructor
		 * @param tlhi
		 */
		public TextLayoutHitInfo(TextLayoutHitInfo tlhi){
			tli = tlhi.tli;
			thi = tlhi.thi;
		}

		public void copyFrom(TextLayoutHitInfo other){
			this.tli = other.tli;
			this.thi = other.thi;
		}

		public void setInfo(TextLayoutInfo tli, TextHitInfo thi) {
			this.tli = tli;
			this.thi = thi;
		}

		public int compareTo(TextLayoutHitInfo other) {
			int layoutComparison = tli.compareTo(other.tli);
			if(layoutComparison != 0)
				return layoutComparison; // Different layouts so return comparison
			// Same layout SO test hit info
			if(thi.equals(other.thi))
				return 0;
			// Same layout different hit info SO test char index
			if(thi.getCharIndex() != other.thi.getCharIndex()){
				// Different current chars so order on position
				return (thi.getCharIndex() < other.thi.getCharIndex() ? -1 : 1);
			}
			// Same layout same char different edge hit SO test on edge hit
			return (thi.isLeadingEdge() ? -1 : 1);			
		}

		public String toString(){
			StringBuilder s = new StringBuilder(tli.toString());
			s.append("  Hit char = " + thi.getCharIndex());
			return new String(s);			
		}
	}

	/**
	 * Class to hold information about a text layout. This class helps simplify the
	 * algorithms needed for multi-line text.
	 * 
	 * @author Peter Lager
	 *
	 */
	static public class TextLayoutInfo implements Comparable<TextLayoutInfo> {
		public TextLayout layout;		// The matching layout
		public int lineNo;				// The line number
		public int startCharIndex;		// Position of the first char in text
		public int nbrChars;			// Number of chars in this layout
		public float yPosInPara; 		// Top-left corner of bounds

		/**
		 * @param startCharIndex
		 * @param nbrChars
		 * @param yPosInPara
		 */
		public TextLayoutInfo(int lineNo, TextLayout layout, int startCharIndex, int nbrChars, float yPosInPara) {
			this.lineNo = lineNo;
			this.layout  = layout;
			this.startCharIndex = startCharIndex;
			this.nbrChars = nbrChars;
			this.yPosInPara = yPosInPara;
		}

		public int compareTo(TextLayoutInfo other) {
			if(lineNo == other.lineNo)
				return 0;
			return (startCharIndex < other.startCharIndex) ? -1 : 1;
		}

		public String toString(){
			StringBuilder s = new StringBuilder("{ Line no = " + lineNo + "    starts @ char pos " + startCharIndex);
			s.append("  last index " + (startCharIndex+nbrChars+1));
			s.append("  (" + nbrChars +")  ");
			return new String(s);
		}
	}

	/**
	 * Since most of the Java classes associated with AttributedString 
	 * are immutable with virtually no public methods this class represents
	 * an attribute to be applied. <br>
	 * 
	 * This class is only used from within StyledString.
	 * 
	 * @author Peter Lager
	 *
	 */
	private class AttributeRun implements Serializable {

		private static final long serialVersionUID = -8401062069478890163L;

		public Attribute atype;
		public Object value;
		public Integer start;
		public Integer end;


		/**
		 * The attribute and value to be applied over the whole string
		 * @param atype
		 * @param value
		 */
		public AttributeRun(Attribute atype, Object value) {
			this.atype = atype;
			this.value = value;
			this.start = Integer.MIN_VALUE;
			this.end = Integer.MAX_VALUE;
		}

		/**
		 * The attribute and value to be applied over the given range
		 * @param atype
		 * @param value
		 * @param start
		 * @param end
		 */
		public AttributeRun(Attribute atype, Object value, int start, int end) {
			this.atype = atype;
			this.value = value;
			this.start = start;
			this.end = end;
		}

		/**
		 * If possible merge the two runs or crop the prevRun run.
		 * 
		 * If both runs have the same attribute type and the represent
		 * the same location and size in the text then the intersection
		 * mode will be MM_SURROUNDS rather than MM_SURROUNDED because 
		 * 'this' is the attribute being added.
		 * @param m
		 * @param s
		 * @return
		 */
		private int intersectionWith(AttributeRun ar){
			// Different attribute types?
			if(atype != ar.atype)
				return I_NONE;
			// Check for combination mode
			int combi_mode = (value.equals(ar.value)) ? MERGE_RUNS : CLIP_RUN;
			int sdx = 4, edx = 0;
			// Start index
			if(ar.start < start)
				sdx = 0;
			else if(ar.start == start)
				sdx = 1;
			else if (ar.start < end)
				sdx = 2;
			else if(ar.start == end)
				sdx = 3;
			if(sdx < 4){
				if(ar.end > end)
					edx = 4;
				else if(ar.end == end)
					edx = 3;
				else if(ar.end > start)
					edx = 2;
				else if(ar.end == start)
					edx = 1;
			}
			combi_mode |= grid[sdx][edx];
			return combi_mode;
		}

		public String toString(){
			String s = atype.toString() + "  value = " + value.toString() + "  from " + start + "   to " + end;
			return s;
		}

	}  // End of AttributeRun class

}
