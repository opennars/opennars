package ca.nengo.ui.lib.world.piccolo.primitive;

import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import org.piccolo2d.extras.nodes.PStyledText;

import javax.swing.text.BadLocationException;

/**
 * TODO not working yet
 */
public class TextEdit extends WorldObjectImpl {
    private PStyledText textNode;



    public TextEdit(String name) {
        super(name);
        init();
    }

    public TextEdit(String name, String abc) {
        this(name);
        setText(abc);
    }


    private void init() {

        textNode = new PStyledText();
        getPNode().addChild(textNode);

    /*
        getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
        final PStyledTextEventHandler textHandler = new PStyledTextEventHandler(getCanvas());
        getCanvas().addInputEventListener(textHandler);*/
    }

/*    public void setFont(Font font) {
        textNode.setFont(font);
    }

    public void setConstrainWidthToTextWidth(boolean constrainWidthToTextWidth) {
        textNode.setConstrainWidthToTextWidth(constrainWidthToTextWidth);
    }*/

    public void setText(String text) {
        try {
            textNode.getDocument().remove(0, getEnd() );
            textNode.getDocument().insertString(0, text, null);
            recomputeLayout();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public int getEnd() {
        return textNode.getDocument().getEndPosition().getOffset();
    }

    public String getText() {
        try {
            return textNode.getDocument().getText(0, getEnd());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void recomputeLayout() {
        textNode.recomputeLayout();
    }

}
