/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author seh
 */
package automenta.spacegraph.video;

import automenta.spacegraph.Surface;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class SGPanel extends JPanel implements ComponentListener {

    private final Animator animator;
    private boolean running = false;
    private final GLCanvas canvas;
    private final Surface sg;

    public SGPanel(Surface sg) {
        super(new BorderLayout());

        this.sg = sg;
        
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        canvas = new GLCanvas(caps);
        //canvas = new GLJPanel(caps); /** a "lightweight" GLJPanel component for cooperation with other Swing components. */


        canvas.addGLEventListener(sg);

        canvas.addMouseListener(sg);
        canvas.addMouseMotionListener(sg);
        canvas.addMouseWheelListener(sg);
        canvas.addKeyListener(sg);

        animator = new Animator(canvas);
        animator.setRunAsFastAsPossible(false);
        
        

        addComponentListener(this);

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                //System.out.println("ancestor added: " + event);
                startGL();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                //System.out.println("ancestor removed: " + event);
                stopGL();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }

        });


    }


    public void startGL() {
         if (!running) {
            //System.out.println("STARTING GL");
             
            running = true;
            new Thread(new Runnable() {
                public void run() {
                    add(canvas, BorderLayout.CENTER);
                    updateUI();
                    animator.start();
                }
            }).start();
         }
    }

    public void stopGL() {
        if (running) {
            //System.out.println("STOPPING GL");

            running = false;

            // Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
            new Thread(new Runnable() {
                public void run() {
                    animator.stop();
                    removeAll();
                }
            }).start();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public Surface getSurface() {
        return sg;
    }

    public GLCanvas getCanvas() {
        return canvas;
    }

    public Animator getAnimator() {
        return animator;
    }

    

}
