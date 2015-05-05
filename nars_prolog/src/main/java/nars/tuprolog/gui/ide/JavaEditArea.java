/*
 * tuProlog - Copyright (C) 2001-2004  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog.gui.ide;

import nars.tuprolog.gui.edit.JEditTextArea;
import nars.tuprolog.gui.edit.SyntaxDocument;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.StringTokenizer;

/**
 * An edit area for the Java 2 platform. Makes use of an advanced Swing text area.
 * 
 * @author    <a href="mailto:giulio.piancastelli@studio.unibo.it">Giulio Piancastelli</a>
 * @version    1.0 - 14-nov-02
 */

@SuppressWarnings("serial")
public class JavaEditArea extends JPanel implements TheoryEditArea, FileEditArea {

    /**
	 * The advanced Swing text area used by this edit area.
	 */
    private JEditTextArea inputTheory;
    /**
	 * The line number corresponding to the caret's current position in the text area.
	 */
    private int caretLine;
    /**
	 * Indicate if the edit area is changed after the last Set Theory operation issued by the editor.
	 */
    private boolean dirty;
    /**
	 * Indicate if the edit area is changed after the last save operation
	 */
    private boolean saved;
    /**
	 * Used for components interested in changes of console's properties.
	 */
    private PropertyChangeSupport propertyChangeSupport;
    /**
	 * Undo Manager for the Document in the JEditTextArea.
	 */
    private UndoManager undoManager;


    public JavaEditArea() {
        PrologTextArea textArea = new PrologTextArea();

        setKeyBindings(textArea);

        inputTheory = new JEditTextArea(textArea);
        inputTheory.setTokenMarker(new PrologTokenMarker());
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        //constraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        constraints.fill = java.awt.GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        //constraints.insets = new java.awt.Insets(0, 0, 10, 0);

        inputTheory.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent event) {
                setCaretLine(inputTheory.getCaretLine() + 1);
            }
        });

        dirty = false;
        saved = true;
        inputTheory.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                changedUpdate(event);
            }
            public void removeUpdate(DocumentEvent event) {
                changedUpdate(event);
            }
            public void changedUpdate(DocumentEvent event) {
                if (!dirty)
                    setDirty(true);
                  if (saved)
                       setSaved(false);
            }

        });

        undoManager = new UndoManager();
        inputTheory.getDocument().addUndoableEditListener(undoManager);

        add(inputTheory, constraints);
        
        propertyChangeSupport = new PropertyChangeSupport(this);
        
    }

    /**
     * Set key bindings for edit area actions. Note that the key bindings must
     * be added to the inputHandler in the PrologTextArea through the facilities
     * offered by this component from the jEdit package.
     */
    private void setKeyBindings(PrologTextArea textArea) {
        // C+Z == Ctrl + Z
        textArea.inputHandler.addKeyBinding("C+Z", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                undoAction();
            }
        });
        // CS+Z = Ctrl + Shift + Z
        textArea.inputHandler.addKeyBinding("CS+Z", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                redoAction();
            }
        });
        // C+A == Ctrl + A
        textArea.inputHandler.addKeyBinding("C+A", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                inputTheory.selectAll();
            }
        });
        // ENTER == Enter
        textArea.inputHandler.addKeyBinding("ENTER", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (inputTheory.isEditable())
                {
                    String line=inputTheory.getLineText(inputTheory.getCaretLine());
                    int breakPoint=inputTheory.getCaretPosition();
                    String padding = "";
                    int paddingLength;
                    StringTokenizer st = new StringTokenizer(line," \t\n");
                    paddingLength = 0;
                    String noPadding = "";
                    if(st.hasMoreTokens())
                    {
                        noPadding=st.nextToken();
                        paddingLength=line.indexOf(noPadding);
                        padding=line.substring(0, paddingLength);
                    }
                    else //if line is empty or if it's made only by ' ', '\t' or '\n'
                    {
                        padding=line;
                        paddingLength=line.length();
                    }
    
                    //String part1 = inputTheory.getText().substring(0, breakPoint);
                    String part2 = inputTheory.getText().substring(breakPoint);
                    /**
                     * JEditTextArea.setText richiama 2 metodi che agiscono sul SyntaxDocument
                     * provocando 2 "entry" nell'elenco undo/redo e quindi non va bene usare
                     * setText, meglio agire direttamente sul SyntaxDocument
                     */
                    //inputTheory.setText(part1+"\n"+padding+part2);
                    
                    SyntaxDocument document = inputTheory.getDocument();
                    try
                    {
                        document.beginStructEdit();
                        /*
                         * There is no way to avoid the double modification... or, if there is, I
                         * wasn't able to find it; I've tried to get the best possible result anyway
                         */
                        document.remove(breakPoint, document.getLength()-breakPoint);
                        document.insertString(breakPoint, '\n' +padding+part2, null);
                        inputTheory.setDocument(document);
                        inputTheory.setCaretPosition(breakPoint+padding.length()+1);
                    }
                    catch(BadLocationException bl)
                    {
                        bl.printStackTrace();
                    }
                    finally
                    {
                        document.endStructEdit();
                    }
                }
            }
        });
    }


    public void setCaretLine(int caretLine) {
        int oldCaretLine = getCaretLine();
        this.caretLine = caretLine;
        propertyChangeSupport.firePropertyChange("caretLine", oldCaretLine, caretLine);
    }


    public int getCaretLine() {
        return caretLine;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setTheory(String theory) {
        inputTheory.setText(theory);

        //to set caret at the begin of the edit area
        inputTheory.setCaretPosition(0);
    }

    public String getTheory() {
        return inputTheory.getText();
    }


    public void setDirty(boolean flag) {
        dirty = flag;
    }


    public boolean isDirty() {
        return dirty;
    }


    public void setSaved(boolean flag) {
        if (inputTheory.isEditable())
        {
            boolean oldSaved = isSaved();
            saved = flag;
            propertyChangeSupport.firePropertyChange("saved", oldSaved, saved);
        }
    }


    public boolean isSaved() {
        return saved;
    }

    /* Managing Undo/Redo actions. */

    public void undoAction() {
        try {
            undoManager.undo();
        } catch (CannotUndoException e) {
            // e.printStackTrace();
        }
    }

    public void redoAction() {
        try {
            undoManager.redo();
        } catch (CannotRedoException e) {
            // e.printStackTrace();
        }
    }

    public void setFontDimension(int dimension)
    {
        Font font = new Font(inputTheory.getPainter().getFont().getName(),inputTheory.getPainter().getFont().getStyle(),dimension);
        inputTheory.getPainter().setFont(font);
    }

    public void setEditable(boolean flag)
    {
        inputTheory.setEditable(flag);
    }
} // end JavaEditArea class