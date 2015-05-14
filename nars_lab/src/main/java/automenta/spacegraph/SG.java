package automenta.spacegraph;

import automenta.spacegraph.control.Repeat;
import com.jogamp.opengl.GLEventListener;

import java.awt.event.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class SG implements GLEventListener, MouseMotionListener, MouseListener, KeyListener, MouseWheelListener {

    public interface SGListener {

        /** Indicates that the demo wants to be terminated. */
        public void shutdownDemo();

        /** Indicates that a repaint should be scheduled later. */
        public void repaint();
    }
    
    protected double t = 0;
    protected List<Repeat> repeats = new LinkedList();
    protected SGListener sgListener;
    private boolean doShutdown = true;

    public static enum KeyStates {
        CONTROL, ALT
    }
    public final Map<KeyStates, Boolean> keyStates = new HashMap(2);

    public SG() {
        super();
        keyStates.put(KeyStates.CONTROL, false);
        keyStates.put(KeyStates.ALT, false);
    }

    
    public void setSGListener(SGListener listener) {
        this.sgListener = listener;
    }

    // Override this with any other cleanup actions
    public void shutdownDemo() {
        // Execute only once
        boolean shouldDoShutdown = doShutdown;
        doShutdown = false;
        if (shouldDoShutdown) {
            sgListener.shutdownDemo();
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            keyStates.put(KeyStates.CONTROL, true);
        } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
            keyStates.put(KeyStates.ALT, true);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            keyStates.put(KeyStates.CONTROL, false);
        } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
            keyStates.put(KeyStates.ALT, false);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    public <R extends Repeat> R add(R r) {
        repeats.add(r);
        return r;
    }

    public boolean remove(Repeat r) {
        return repeats.remove(r);
    }
}
