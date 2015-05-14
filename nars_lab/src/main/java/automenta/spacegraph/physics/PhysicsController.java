//package automenta.spacegraph.physics;
//
//import automenta.spacegraph.control.Repeat;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import javax.media.opengl.GLAutoDrawable;
//import javax.media.opengl.GLEventListener;
//import javax.media.opengl.awt.GLCanvas;
//
//public class PhysicsController implements GLEventListener, MouseMotionListener, MouseListener, KeyListener, MouseWheelListener {
//    private final PhysicsPanel surface;
//    private final GLCanvas canvas;
//    private final PhysicsApp app;
//
//    //see LWJGL.java in JBullet demos
//
//    public PhysicsController(PhysicsPanel surface, PhysicsApp app) {
//        super();
//        this.surface = surface;
//        this.canvas = surface.getCanvas();
//
//        this.app = app;
//
//        keyStates.put(KeyStates.CONTROL, false);
//        keyStates.put(KeyStates.ALT, false);
//
//        canvas.addGLEventListener(this);
//        canvas.addMouseListener(this);
//        canvas.addMouseMotionListener(this);
//        canvas.addMouseWheelListener(this);
//        canvas.addKeyListener(this);
//
//    }
//
//    @Override
//    public void init(GLAutoDrawable glad) {
//        surface.init();
//        app.myinit();
//        app.reshape(glad.getWidth(), glad.getHeight());
//    }
//
//    @Override
//    public void dispose(GLAutoDrawable glad) {
//    }
//
//    @Override
//    public void display(GLAutoDrawable glad) {
//        app.moveAndDisplay();
//    }
//
//    @Override
//    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
//        app.reshape(glad.getWidth(), glad.getHeight());
//    }
//
//    public interface SGListener {
//
//        /** Indicates that the demo wants to be terminated. */
//        public void shutdownDemo();
//
//        /** Indicates that a repaint should be scheduled later. */
//        public void repaint();
//    }
//
//    protected double t = 0;
//    protected List<Repeat> repeats = new LinkedList();
//    protected SGListener sgListener;
//    private boolean doShutdown = true;
//
//    public static enum KeyStates {
//        CONTROL, ALT
//    }
//    public final Map<KeyStates, Boolean> keyStates = new HashMap(2);
//
//    public void setSGListener(SGListener listener) {
//        this.sgListener = listener;
//    }
//
//    // Override this with any other cleanup actions
//    public void shutdownDemo() {
//        // Execute only once
//        boolean shouldDoShutdown = doShutdown;
//        doShutdown = false;
//        if (shouldDoShutdown) {
//            sgListener.shutdownDemo();
//        }
//    }
//
//    public void mouseDragged(MouseEvent e) {
//        app.mouseMotionFunc(e.getX(), e.getY());
//    }
//
//    public void mouseMoved(MouseEvent e) {
//        app.mouseMotionFunc(e.getX(), e.getY());
//    }
//
//    public void mouseClicked(MouseEvent e) {
//    }
//
//    public void mousePressed(MouseEvent e) {
//        app.mouseFunc(e.getButton()-1, 0, e.getX(), e.getY());
//    }
//
//    public void mouseReleased(MouseEvent e) {
//        app.mouseFunc(e.getButton()-1, 1, e.getX(), e.getY());
//    }
//
//    public void mouseEntered(MouseEvent e) {
//    }
//
//    public void mouseExited(MouseEvent e) {
//    }
//
//    public void keyTyped(KeyEvent e) {
//    }
//
//    public void keyPressed(KeyEvent e) {
//        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
//            keyStates.put(KeyStates.CONTROL, true);
//        } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
//            keyStates.put(KeyStates.ALT, true);
//        }
//    }
//
//    public void keyReleased(KeyEvent e) {
//        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
//            keyStates.put(KeyStates.CONTROL, false);
//        } else if (e.getKeyCode() == KeyEvent.VK_ALT) {
//            keyStates.put(KeyStates.ALT, false);
//        }
//    }
//
//    public void mouseWheelMoved(MouseWheelEvent e) {
//    }
//
//    public <R extends Repeat> R add(R r) {
//        repeats.add(r);
//        return r;
//    }
//
//    public boolean remove(Repeat r) {
//        return repeats.remove(r);
//    }
//}
