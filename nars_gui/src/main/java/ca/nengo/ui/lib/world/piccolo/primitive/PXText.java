/*
 * Copyright (c) 2002-@year@, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Piccolo was written at the Human-Computer Interaction Laboratory www.cs.umd.edu/hcil by Jesse Grosjean
 * under the supervision of Ben Bederson. The Piccolo website is www.cs.umd.edu/hcil/piccolo.
 */
package ca.nengo.ui.lib.world.piccolo.primitive;

import automenta.vivisect.Video;
import ca.nengo.ui.lib.NengoStyle;
import org.piccolo2d.util.PPaintContext;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
/**
 * <b>PText</b> is a multi-line text node. The text will flow to base on the
 * width of the node's bounds.
 * <P>
 * 
 * @version 1.1
 * @author Jesse Grosjean
 */
class PXText extends PXNode {

	private static final long serialVersionUID = 1L;

    private static final TextLayout[] EMPTY_TEXT_LAYOUT_ARRAY = new TextLayout[0];

    public static final Font DEFAULT_FONT = Video.monofont;

	public static final double DEFAULT_BLURTEXT_THRESHOLD = 5.5;
	public static final int PROPERTY_CODE_FONT = 1 << 20;
	

	public static final int PROPERTY_CODE_TEXT = 1 << 19;
	/**
	 * The property name that identifies a change of this node's font (see
	 * {@link #getFont getFont}). Both old and new value will be set in any
	 * property change event.
	 */
	public static final String PROPERTY_FONT = "font";

	/**
	 * The property name that identifies a change of this node's text (see
	 * {@link #getText getText}). Both old and new value will be set in any
	 * property change event.
	 */
	public static final String PROPERTY_TEXT = "text";
	private boolean constrainHeightToTextHeight = true;
	private boolean constrainWidthToTextWidth = true;
	private Font font;
	private float justification = javax.swing.JLabel.LEFT_ALIGNMENT;
	private transient TextLayout[] lines;
	private String text;
	private Paint textPaint;
	private static boolean useBlurTextThreshold = true;

    public final ArrayList<Object> linesList = new ArrayList<Object>();

	protected double blurTextThreshold = 5.5;

    public PXText() {
		super();
		setTextPaint(Color.BLACK);
		init();
	}

	public PXText(String aText) {
		this();
		setText(aText);
		init();
	}


    private void init() {
		setBlurTextThreshold(DEFAULT_BLURTEXT_THRESHOLD);
		setConstrainWidthToTextWidth(true);


		setFont(NengoStyle.FONT_NORMAL);
		setTextPaint(NengoStyle.COLOR_FOREGROUND);
	}
	
	static public void setUseBlurTextThreshold(boolean state) {
		useBlurTextThreshold =state;
	}
	
	static public boolean getUseBlurTextThreshold() {
		return useBlurTextThreshold;
	}
	

	// provided in case someone needs to override the way that lines are
	// wrapped.
	protected TextLayout computeNextLayout(LineBreakMeasurer measurer,
			float availibleWidth, int nextLineBreakOffset) {
		return measurer.nextLayout(availibleWidth, nextLineBreakOffset, false);
	}

	protected void internalUpdateBounds(double x, double y, double width, double height) {
		recomputeLayout();
	}

	protected void paint(PPaintContext paintContext) {
		super.paint(paintContext);

		float screenFontSize = getFont().getSize() * (float) paintContext.getScale();
		if (textPaint != null && (!useBlurTextThreshold || (screenFontSize > blurTextThreshold))) {
			float x = (float) getX();
			float y = (float) getY();
			float bottomY = (float) getHeight() + y;


			if (lines == null) {
				recomputeLayout();
				repaint();
				return;
			}


            Graphics2D g2 = paintContext.getGraphics();

            if (!NengoStyle.antialias) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            }
			g2.setPaint(textPaint);

			for (int i = 0; i < lines.length; i++) {
				TextLayout tl = lines[i];
				y += tl.getAscent();

				if (bottomY < y) {
					return;
				}

				float offset = (float) (getWidth() - tl.getAdvance()) * justification;
				tl.draw(g2, x + offset, y);

				y += tl.getDescent() + tl.getLeading();
			}
		}
	}

	/**
	 * Returns a string representing the state of this node. This method is
	 * intended to be used only for debugging purposes, and the content and
	 * format of the returned string may vary between implementations. The
	 * returned string may be empty but may not be <code>null</code>.
	 * 
	 * @return a string representation of this node's state
	 */
	protected String paramString() {
		StringBuilder result = new StringBuilder();

		result.append("text=").append(text == null ? "null" : text);
		result.append(",font=").append(font == null ? "null" : font.toString());
		result.append(',');
		result.append(super.toString());

		return result.toString();
	}

	/**
	 * Returns the font of this PText.
	 * 
	 * @return the font of this PText.
	 */
	public Font getFont() {
		if (font == null) {
			font = DEFAULT_FONT;
		}
		return font;
	}

	/**
	 * Returns the current blur text threshold. When the screen font size will be
	 * below this threshold the text is rendered as blurred/distorted instead of drawing
	 * the text glyphs.
	 */
	public double getBlurTextThreshold() {
		return blurTextThreshold;
	}

	/**
	 * Return the justificaiton of the text in the bounds.
	 * 
	 * @return float
	 */
	public float getJustification() {
		return justification;
	}

	public String getText() {
		return text;
	}

	/**
	 * Get the paint used to paint this nodes text.
	 * 
	 * @return Paint
	 */
	public Paint getTextPaint() {
		return textPaint;
	}

	public boolean isConstrainHeightToTextHeight() {
		return constrainHeightToTextHeight;
	}

	public boolean isConstrainWidthToTextWidth() {
		return constrainWidthToTextWidth;
	}

	/**
	 * Compute the bounds of the text wrapped by this node. The text layout is
	 * wrapped based on the bounds of this node.
	 */
	public void recomputeLayout() {

        linesList.clear();

		float textWidth = 0;
		float textHeight = 0;

		if (text != null && text.length() > 0) {
			AttributedString atString = new AttributedString(text);
			atString.addAttribute(TextAttribute.FONT, getFont());

			AttributedCharacterIterator itr = atString.getIterator();
			LineBreakMeasurer measurer = new LineBreakMeasurer(itr,
					NengoStyle.renderQuality);
			float availableWidth = constrainWidthToTextWidth ? Float.MAX_VALUE
					: Math.max(1, (float) getWidth());

			int nextLineBreakOffset = text.indexOf('\n');
			if (nextLineBreakOffset == -1) {
				nextLineBreakOffset = Integer.MAX_VALUE;
			} else {
				nextLineBreakOffset++;
			}

			while (measurer.getPosition() < itr.getEndIndex()) {
				TextLayout aTextLayout = computeNextLayout(measurer,
						availableWidth, nextLineBreakOffset);

				if (nextLineBreakOffset == measurer.getPosition()) {
					nextLineBreakOffset = text.indexOf('\n', measurer
							.getPosition());
					if (nextLineBreakOffset == -1) {
						nextLineBreakOffset = Integer.MAX_VALUE;
					} else {
						nextLineBreakOffset++;
					}
				}

				linesList.add(aTextLayout);
				textHeight += aTextLayout.getAscent();
				textHeight += aTextLayout.getDescent()
						+ aTextLayout.getLeading();
				textWidth = Math.max(textWidth, aTextLayout.getAdvance());
			}


		}

		lines = linesList.toArray(new TextLayout[linesList.size()]);

		if (constrainWidthToTextWidth || constrainHeightToTextHeight) {
			double newWidth = Math.max(1,getWidth());
			double newHeight = Math.max(1, getHeight());

			if (constrainWidthToTextWidth) {
				newWidth = textWidth;
			}

			if (constrainHeightToTextHeight) {
				newHeight = textHeight;
			}

			super.setBounds(getX(), getY(), newWidth, newHeight);
		}
	}

	/**
	 * Controls whether this node changes its height to fit the height of its
	 * text. If flag is true it does; if flag is false it doesn't
	 */
	public void setConstrainHeightToTextHeight(boolean constrainHeightToTextHeight) {
		if (constrainHeightToTextHeight == this.constrainHeightToTextHeight) return;
        this.constrainHeightToTextHeight = constrainHeightToTextHeight;
		recomputeLayout();
	}

	/**
	 * Controls whether this node changes its width to fit the width of its
	 * text. If flag is true it does; if flag is false it doesn't
	 */
	public void setConstrainWidthToTextWidth(boolean constrainWidthToTextWidth) {
        if (this.constrainWidthToTextWidth == constrainWidthToTextWidth) return;
        this.constrainWidthToTextWidth = constrainWidthToTextWidth;
		recomputeLayout();
	}

	/**
	 * Set the font of this PText. Note that in Piccolo if you want to change
	 * the size of a text object it's often a better idea to scale the PText
	 * node instead of changing the font size to get that same effect. Using
	 * very large font sizes can slow performance.
	 */
	public void setFont(Font aFont) {
        if (this.font!=null && this.font.equals(aFont)) return;
		Font old = font;
		font = aFont;
		lines = null;
		recomputeLayout();
		invalidatePaint();
		firePropertyChange(PROPERTY_CODE_FONT, PROPERTY_FONT, old, font);
	}

	/**
	 * Sets the current blur text threshold.
	 * 
	 * @param threshold
	 *            minimum screen font size.
	 */
	public void setBlurTextThreshold(double threshold) {
        if (threshold == blurTextThreshold) return;
		blurTextThreshold = threshold;
		invalidatePaint();
	}

	/**
	 * Sets the justificaiton of the text in the bounds.
	 * 
	 * @param just
	 */
	public void setJustification(float just) {
        if (justification == just) return;
		justification = just;
		recomputeLayout();
	}

	/**
	 * Set the text for this node. The text will be broken up into multiple
	 * lines based on the size of the text and the bounds width of this node.
	 */
	public void setText(String newText) {
        if (text==null || !text.equals(newText)) {
            String old = text;
            text = newText;
            lines = null;
            recomputeLayout();
            invalidatePaint();
            firePropertyChange(PROPERTY_CODE_TEXT, PROPERTY_TEXT, old, text);
        }
	}

	// ****************************************************************
	// Debugging - methods for debugging
	// ****************************************************************

	/**
	 * Set the paint used to paint this node's text background.
	 * 
	 * @param textPaint
	 */
	public void setTextPaint(Paint textPaint) {
        if (this.textPaint==null || !this.textPaint.equals(textPaint)) {
            this.textPaint = textPaint;
            invalidatePaint();
        }
	}
}