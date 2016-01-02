package automenta.spacegraph.demo.spacegraph.old;

//=================================================================================
// Picking 0.2                                                       (Thomas Bladh)
//=================================================================================
// A simple picking example using java/jogl. This is far from a complete solution 
// but it should give you an idea of how to include picking in your assigment 
// solutions.
//
// Notes: * Based on example 13-3 (p 542) in the "OpenGL Programming Guide"
//        * This version should handle overlapping objects correctly.
//---------------------------------------------------------------------------------

import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.sun.prism.impl.BufferUtil;

import java.nio.IntBuffer;


public class DemoPicking
{
  public static void main(String[] args) 
  {
    new DemoPicking();
  }
  	
  DemoPicking()
  {
    Frame frame = new Frame("Picking Example");
    GLCapabilities capabilities = new GLCapabilities(null);
    GLDrawableFactory factory = GLDrawableFactory.getFactory(capabilities.getGLProfile());
    GLCanvas drawable = new GLCanvas(capabilities);
    final Renderer renderer = new Renderer();
    drawable.addGLEventListener(renderer);
    drawable.addMouseListener(renderer);
    drawable.addMouseMotionListener(renderer);
    frame.add(drawable);
    frame.setSize(400, 400);
    final Animator animator = new Animator(drawable);
    frame.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) 
        {
          animator.stop();
          System.exit(0);
        }
      });
    frame.setVisible(true);
    animator.start();	
  }

  static class Renderer implements GLEventListener, MouseListener, MouseMotionListener
  {
    static final int NOTHING = 0, UPDATE = 1, SELECT = 2;
    int cmd = UPDATE;
    int mouse_x, mouse_y;
	
    private GLU glu = new GLU();
    private GLAutoDrawable gldrawable;
		
    public void init(GLAutoDrawable drawable) 
    {
      GL2 gl = drawable.getGL().getGL2();
      this.gldrawable = drawable;
      gl.glEnable(GL2.GL_CULL_FACE);
      gl.glEnable(GL2.GL_DEPTH_TEST);
      gl.glEnable(GL2.GL_NORMALIZE);
      gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }
    	
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
    {
      GL2 gl = drawable.getGL().getGL2();
      float h = (float) height / (float) width;
      gl.glViewport(0, 0, width, height);
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluOrtho2D(0.0f,1.0f,0.0f,1.0f);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void display(GLAutoDrawable drawable) 
    {
      GL2 gl = drawable.getGL().getGL2();
      switch(cmd)
        {
        case UPDATE:
          drawScene(gl);
          break;
        case SELECT:
          int buffsize = 512;
          double x = (double) mouse_x, y = (double) mouse_y;
          int[] viewPort = new int[4];
          IntBuffer selectBuffer = BufferUtil.newIntBuffer(buffsize);
          int hits = 0;
          gl.glGetIntegerv(GL2.GL_VIEWPORT, viewPort, 0);
          gl.glSelectBuffer(buffsize, selectBuffer);
          gl.glRenderMode(GL2.GL_SELECT);
          gl.glInitNames();
          gl.glMatrixMode(GL2.GL_PROJECTION);
          gl.glPushMatrix();
          gl.glLoadIdentity();
          glu.gluPickMatrix(x, (double) viewPort[3] - y, 5.0d, 5.0d, viewPort, 0);
          glu.gluOrtho2D(0.0d, 1.0d, 0.0d, 1.0d);
          drawScene(gl);
          gl.glMatrixMode(GL2.GL_PROJECTION);
          gl.glPopMatrix();
          gl.glFlush();
          hits = gl.glRenderMode(GL2.GL_RENDER);
          processHits(hits, selectBuffer);
          cmd = UPDATE;
          break;
        }
    }

    public void processHits(int hits, IntBuffer buffer)
    {
      System.out.println("---------------------------------");
      System.out.println(" HITS: " + hits);
      int offset = 0;
      int names;
      float z1, z2;
      for (int i=0;i<hits;i++)
        {
          System.out.println("- - - - - - - - - - - -");
          System.out.println(" hit: " + (i + 1));
          names = buffer.get(offset); offset++;
          z1 = (float) buffer.get(offset) / 0x7fffffff; offset++;
          z2 = (float) buffer.get(offset) / 0x7fffffff; offset++;
          System.out.println(" number of names: " + names);
          System.out.println(" z1: " + z1);
          System.out.println(" z2: " + z2);
          System.out.println(" names: ");

          for (int j=0;j<names;j++)
            {
              System.out.print("       " + buffer.get(offset)); 
              if (j==(names-1))
                System.out.println("<-");
              else
                System.out.println();
              offset++;
            }
          System.out.println("- - - - - - - - - - - -");
        }
      System.out.println("---------------------------------");
    }
		
    public int viewPortWidth(GL2 gl)
    {
      int[] viewPort = new int[4];
      gl.glGetIntegerv(GL2.GL_VIEWPORT, viewPort, 0);
      return viewPort[2];
    }

    public int viewPortHeight(GL2 gl)
    {
      int[] viewPort = new int[4];
      gl.glGetIntegerv(GL2.GL_VIEWPORT, viewPort, 0);
      return viewPort[3];
    }

    public void drawScene(GL2 gl)
    {
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

      // Colors
      float red[] =   {1.0f,0.0f,0.0f,1.0f};
      float green[] = {0.0f,1.0f,0.0f,1.0f};
      float blue[] =  {0.0f,0.0f,1.0f,1.0f};
	
      // Red rectangle
      GLRectangleEntity r1 = new GLRectangleEntity(gl, glu);
      r1.x = 0.15f;
      r1.y = 0.25f;
      r1.z = 0.75f;
      r1.w = 0.4f;
      r1.h = 0.4f;
      r1.c = red;
      r1.id = 10;
      r1.draw();

      // Green rectangle
      GLRectangleEntity r2 = new GLRectangleEntity(gl, glu);
      r2.x = 0.35f;
      r2.y = 0.45f;
      r2.z = 0.5f;
      r2.w = 0.4f;
      r2.h = 0.4f;
      r2.c = green;
      r2.id = 20;
      r2.draw();

      // Blue rectangle
      GLRectangleEntity r3 = new GLRectangleEntity(gl, glu);
      r3.x = 0.45f;
      r3.y = 0.15f;
      r3.z = 0.25f;
      r3.w = 0.4f;
      r3.h = 0.4f;
      r3.c = blue;
      r3.id = 30;
      r3.draw();

      gl.glFlush();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
				
    public void mousePressed(MouseEvent e) 
    {
      cmd = SELECT;
      mouse_x = e.getX();
      mouse_y = e.getY();
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    
    public abstract class GLEntity
    {
      float x, y, z;
      float[] c;
      int id = 0;
      boolean outline = false;
      GL2 gl;
      GLU glu;
      public GLEntity(GL2 gl, GLU glu)
      {
        this.gl = gl;
        this.glu = glu;
      }
      public void draw()
      {
        gl.glPushName(id);
        _draw();
      }
      public abstract void _draw();
    }

    public class GLRectangleEntity extends GLEntity
    {
      float w = 0.1f;
      float h = 0.1f;
      public GLRectangleEntity(GL2 gl, GLU glu)
      {
        super(gl, glu);
      }
      public void _draw()
      {
        if (outline)
          gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
        else
          gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);

        gl.glColor4fv(c, 0);
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3f(x, y, z);
        gl.glVertex3f(x + w, y, z);
        gl.glVertex3f(x + w, y + h, z);
        gl.glVertex3f(x, y + h, z);
        gl.glEnd();
      }			
    }
  }
}
