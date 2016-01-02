package nars.rover.physics.gl;

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import nars.rover.physics.PhysicsController;
import nars.rover.physics.TestbedPanel;
import nars.rover.physics.TestbedState;
import nars.rover.physics.j2d.AWTPanelHelper;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.dynamics.World;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

/**
 *
 */
public abstract class JoglAbstractPanel extends GLCanvas implements TestbedPanel, GLEventListener  {
    private static final long serialVersionUID = 1L;

    public static final int SCREEN_DRAG_BUTTON = 3;

    public static final int INIT_WIDTH = 600;
    public static final int INIT_HEIGHT = 600;

    public final PhysicsController controller;
    private final World world;
    private final DebugDraw debugDraw;
    private Timer timer;
    //LightEngine light = new LightEngine();

    private TestbedState model;

    // model can be null
    // if it is null world and debugDraw can be null, because they are retrived from model
    public JoglAbstractPanel(final World world, DebugDraw debugDraw, final PhysicsController controller, TestbedState model, GLCapabilitiesImmutable config) {
        super(config);
        this.controller = controller;
        setSize(800, 800);
        //(new Dimension(600, 600));
        //setAutoSwapBufferMode(true);
        addGLEventListener(this);
        enableInputMethods(true);

        if( model != null && controller != null ) {
            //AWTPanelHelper.addHelpAndPanelListeners(this, model, controller, SCREEN_DRAG_BUTTON);
            AWTPanelHelper.addHelpAndPanelListeners(this, model, controller, SCREEN_DRAG_BUTTON);
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //setsSize(getWidth(), getHeight());
                //dbImage = null;
            }
        });

        this.world = world;
        this.debugDraw = debugDraw;
        this.model = model;
    }

    @Override
    public void grabFocus() {

    }

    @Override
    public boolean render() {
        return true;
    }

    @Override
    public void paintScreen() {
        display();

    }

    @Override
    public void display(GLAutoDrawable arg0) {
        repainter();
    }
    protected void repainter() {

        GL2 gl = getGL().getGL2();

        //getGL().getGL2().glClearAccum(0,0,0,1f);

        //gl.glClear(GL2.GL_COLOR_BUFFER_BIT);



        gl.glAccum(GL2.GL_RETURN, 0.9f); //adding the current frame to the buffer


        JoglAbstractDraw drawer = ((JoglAbstractDraw)getDebugDraw());

        float time = 0.0f; // what does this?
        if( model != null ) {
            time = model.model.getTime();
        }

        drawer.draw(getWorld(), time);


        //https://www.opengl.org/sdk/docs/man2/xhtml/glAccum.xml

        //light.render(gl, drawer.getViewportTranform());


        gl.glAccum(GL2.GL_LOAD, 0.95f); //Drawing last frame, saved in buffer
        gl.glAccum(GL2.GL_MULT, 0.95f ); //make current frame in buffer dim


        getGL().glFlush();

        repaint();


    }

    private World getWorld() {
        if( model != null ) {
            return model.getCurrTest().getWorld();
        }
        else {
            return world;
        }
    }

    private DebugDraw getDebugDraw() {
        if( model != null ) {
            return model.getDebugDraw();
        }
        else {
            return debugDraw;
        }
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {}

    @Override
    public void init(GLAutoDrawable arg0) {
        GL gl2 = getGL();
        gl2.glLineWidth(1f);

        gl2.glEnable(GL.GL_LINE_SMOOTH);
        gl2.glEnable(GL.GL_LINE_WIDTH);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(gl2.GL_SRC_ALPHA, gl2.GL_ONE_MINUS_SRC_ALPHA);


        //getGL().getGL2().glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        getGL().glClearColor(0.0f, 0.0f, 0.0f, 0.8f);
        getGL().glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);


        //getGL().getGL2().glClearColor(0f, 0f, 0f, 1f);



        new FPSAnimator(this, 25);
    }


    @Override
    public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
        float width = (float)getWidth();
        float height = (float)getHeight();

        GL2 gl2 = arg0.getGL().getGL2();

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0f, width, 0.0f, height);


        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();

        gl2.glViewport(0, 0, getWidth(), getHeight());

        if( controller != null ) {
            controller.updateExtents(arg3 / 2, arg4 / 2);
        }
    }
}
