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

import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import java.awt.*;

public class Text extends WorldObjectImpl {
	private PXText textNode;
	


	public Text(String text) {
		super(new PXText(text));
		init();
	}

	public Text() {
		super(new PXText());
		init();
	}

	private void init() {
		setPickable(false);

        textNode = (PXText) getPNode();
	}

	public void setFont(Font font) {
		textNode.setFont(font);
	}

	public void setConstrainWidthToTextWidth(boolean constrainWidthToTextWidth) {
		textNode.setConstrainWidthToTextWidth(constrainWidthToTextWidth);
	}

	public void setText(String text) {
		textNode.setText(text);
	}

	public void setTextPaint(Paint textPaint) {
		textNode.setTextPaint(textPaint);
	}

	public String getText() {
		return textNode.getText();
	}

	public void recomputeLayout() {
		textNode.recomputeLayout();
	}

}
