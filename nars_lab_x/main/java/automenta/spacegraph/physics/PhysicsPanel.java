///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.physics;
//
//import com.bulletphysics.demos.applet.Cylinder;
//import com.bulletphysics.demos.applet.Disk;
//import com.bulletphysics.demos.applet.Sphere;
//import com.bulletphysics.demos.opengl.IGL;
//import com.sun.opengl.util.Animator;
//import com.sun.opengl.util.BufferUtil;
//import java.awt.BorderLayout;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ComponentListener;
//import java.nio.FloatBuffer;
//import java.util.HashMap;
//import java.util.Map;
//import javax.media.opengl.GL2;
//import javax.media.opengl.GLCapabilities;
//import javax.media.opengl.GLProfile;
//import javax.media.opengl.awt.GLCanvas;
//import javax.media.opengl.glu.GLU;
//import javax.media.opengl.glu.GLUquadric;
//import javax.swing.JPanel;
//import javax.swing.event.AncestorEvent;
//import javax.swing.event.AncestorListener;
//
///**
// * JOGL implementation of JBullet's LWJGLRenderer
// * @author seh
// */
//public class PhysicsPanel extends JPanel implements IGL, ComponentListener {
//
//    ////////////////////////////////////////////////////////////////////////////
//    private static final Cylinder cylinder = new Cylinder();
//    private static final Disk disk = new Disk();
//    private static final Sphere sphere = new Sphere();
//    private static Map<SphereKey, Integer> sphereDisplayLists = new HashMap<SphereKey, Integer>();
//    private static SphereKey sphereKey = new SphereKey();
//    GLUquadric quadric = null;
//    private static Map<CylinderKey, Integer> cylinderDisplayLists = new HashMap<CylinderKey, Integer>();
//    private static CylinderKey cylinderKey = new CylinderKey();
//    private final Animator animator;
//    private boolean running = false;
//    private final GLCanvas canvas;
//    //private final PhysicsController sg;
//    private GL2 gl;
//    private GLU glu;
//    int cylinderSegments = 15;
//    int cylinderLoops = 10;
//
//
//    public PhysicsPanel() {
//        super(new BorderLayout());
//
//        //this.sg = controller;
//
//        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
//        canvas = new GLCanvas(caps);
//        //canvas = new GLJPanel(caps); /** a "lightweight" GLJPanel component for cooperation with other Swing components. */
//
//
//        animator = new Animator(canvas);
//        animator.setRunAsFastAsPossible(false);
//
//        addComponentListener(this);
//
//        addAncestorListener(new AncestorListener() {
//
//            @Override
//            public void ancestorAdded(AncestorEvent event) {
//                startGL();
//            }
//
//            @Override
//            public void ancestorRemoved(AncestorEvent event) {
//                //System.out.println("ancestor removed: " + event);
//                stopGL();
//            }
//
//            @Override
//            public void ancestorMoved(AncestorEvent event) {
//            }
//        });
//
//
//    }
//
//    public void startGL() {
//        if (!running) {
//            System.out.println("STARTING GL");
//
//            running = true;
//            new Thread(new Runnable() {
//
//                public void run() {
//                    add(canvas, BorderLayout.CENTER);
//                    gl = (GL2) canvas.getGL();
//                    glu = new GLU();
//                    updateUI();
//                    animator.start();
//                }
//            }).start();
//        }
//    }
//
//    public void stopGL() {
//        if (running) {
//            System.out.println("STOPPING GL");
//
//            running = false;
//
//            // Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
//            new Thread(new Runnable() {
//
//                public void run() {
//                    animator.stop();
//                    removeAll();
//                }
//            }).start();
//        }
//    }
//
//    @Override
//    public void componentResized(ComponentEvent e) {
//    }
//
//    @Override
//    public void componentMoved(ComponentEvent e) {
//    }
//
//    @Override
//    public void componentShown(ComponentEvent e) {
//    }
//
//    @Override
//    public void componentHidden(ComponentEvent e) {
//    }
//
//    public GLCanvas getCanvas() {
//        return canvas;
//    }
//
//    public Animator getAnimator() {
//        return animator;
//    }
//    private static FloatBuffer floatBuf = BufferUtil.newFloatBuffer(16);
//
//    public void init() {
//        quadric = glu.gluNewQuadric();
//        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL); /* smooth shaded */
//        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
//
////                glu.gluQuadricDrawStyle(qobj, GLU.GLU_FILL); /* flat shaded */
////                glu.gluQuadricNormals(qobj, GLU.GLU_FLAT);
////                gl.glNewList(startList + 1, GL.GL_COMPILE);
////                glu.gluCylinder(qobj, 0.5, 0.3, 1.0, 15, 5);
////                gl.glEndList();
////
////                glu.gluQuadricDrawStyle(qobj, GLU.GLU_LINE); /* all polygons wireframe */
////                glu.gluQuadricNormals(qobj, GLU.GLU_NONE);
////                gl.glNewList(startList + 2, GL.GL_COMPILE);
////                glu.gluDisk(qobj, 0.25, 1.0, 20, 4);
////                gl.glEndList();
////
////                glu.gluQuadricDrawStyle(qobj, GLU.GLU_SILHOUETTE); /* boundary only */
////                glu.gluQuadricNormals(qobj, GLU.GLU_NONE);
////                gl.glNewList(startList + 3, GL.GL_COMPILE);
////                glu.gluPartialDisk(qobj, 0.0, 1.0, 20, 4, 0.0, 225.0);
//
//    }
//
//    public void glLight(int light, int pname, float[] params) {
//        gl.glLightfv(light, pname, FloatBuffer.wrap(params));
//    }
//
//    public void glEnable(int cap) {
//        gl.glEnable(cap);
//    }
//
//    public void glDisable(int cap) {
//        gl.glDisable(cap);
//    }
//
//    public void glShadeModel(int mode) {
//        gl.glShadeModel(mode);
//    }
//
//    public void glDepthFunc(int func) {
//        gl.glDepthFunc(func);
//    }
//
//    public void glClearColor(float red, float green, float blue, float alpha) {
//        gl.glClearColor(red, green, blue, alpha);
//    }
//
//    public void glMatrixMode(int mode) {
//        gl.glMatrixMode(mode);
//    }
//
//    public void glLoadIdentity() {
//        gl.glLoadIdentity();
//    }
//
//    public void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar) {
//        gl.glFrustum(left, right, bottom, top, zNear, zFar);
//    }
//
//    public void gluLookAt(float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz) {
//        glu.gluLookAt(eyex, eyey, eyez, centerx, centery, centerz, upx, upy, upz);
//    }
//
//    public void glViewport(int x, int y, int width, int height) {
//        gl.glViewport(x, y, width, height);
//    }
//
//    public void glPushMatrix() {
//        gl.glPushMatrix();
//    }
//
//    public void glPopMatrix() {
//        gl.glPopMatrix();
//    }
//
//    public void gluOrtho2D(float left, float right, float bottom, float top) {
//        glu.gluOrtho2D(left, right, bottom, top);
//    }
//
//    public void glScalef(float x, float y, float z) {
//        gl.glScalef(x, y, z);
//    }
//
//    public void glTranslatef(float x, float y, float z) {
//        gl.glTranslatef(x, y, z);
//    }
//
//    public void glColor3f(float red, float green, float blue) {
//        gl.glColor3f(red, green, blue);
//    }
//
//    public void glClear(int mask) {
//        gl.glClear(mask);
//    }
//
//    public void glBegin(int mode) {
//        gl.glBegin(mode);
//    }
//
//    public void glEnd() {
//        gl.glEnd();
//    }
//
//    public void glVertex3f(float x, float y, float z) {
//        gl.glVertex3f(x, y, z);
//    }
//
//    public void glLineWidth(float width) {
//        gl.glLineWidth(width);
//    }
//
//    public void glPointSize(float size) {
//        gl.glPointSize(size);
//    }
//
//    public void glNormal3f(float nx, float ny, float nz) {
//        gl.glNormal3f(nx, ny, nz);
//    }
//
//    public void glMultMatrix(float[] m) {
//        floatBuf.clear();
//        floatBuf.put(m).flip();
//        gl.glMultMatrixf(floatBuf);
//    }
//
//    ////////////////////////////////////////////////////////////////////////////
//    public void drawCube(float extent) {
//        extent = extent * 0.5f;
//
//        gl.glBegin(gl.GL_QUADS);
//        gl.glNormal3f(1f, 0f, 0f);
//        gl.glVertex3f(+extent, -extent, +extent);
//        gl.glVertex3f(+extent, -extent, -extent);
//        gl.glVertex3f(+extent, +extent, -extent);
//        gl.glVertex3f(+extent, +extent, +extent);
//        gl.glNormal3f(0f, 1f, 0f);
//        gl.glVertex3f(+extent, +extent, +extent);
//        gl.glVertex3f(+extent, +extent, -extent);
//        gl.glVertex3f(-extent, +extent, -extent);
//        gl.glVertex3f(-extent, +extent, +extent);
//        gl.glNormal3f(0f, 0f, 1f);
//        gl.glVertex3f(+extent, +extent, +extent);
//        gl.glVertex3f(-extent, +extent, +extent);
//        gl.glVertex3f(-extent, -extent, +extent);
//        gl.glVertex3f(+extent, -extent, +extent);
//        gl.glNormal3f(-1f, 0f, 0f);
//        gl.glVertex3f(-extent, -extent, +extent);
//        gl.glVertex3f(-extent, +extent, +extent);
//        gl.glVertex3f(-extent, +extent, -extent);
//        gl.glVertex3f(-extent, -extent, -extent);
//        gl.glNormal3f(0f, -1f, 0f);
//        gl.glVertex3f(-extent, -extent, +extent);
//        gl.glVertex3f(-extent, -extent, -extent);
//        gl.glVertex3f(+extent, -extent, -extent);
//        gl.glVertex3f(+extent, -extent, +extent);
//        gl.glNormal3f(0f, 0f, -1f);
//        gl.glVertex3f(-extent, -extent, -extent);
//        gl.glVertex3f(-extent, +extent, -extent);
//        gl.glVertex3f(+extent, +extent, -extent);
//        gl.glVertex3f(+extent, -extent, -extent);
//        gl.glEnd();
//    }
//
//    private static class SphereKey {
//
//        public float radius;
//
//        public SphereKey() {
//        }
//
//        public SphereKey(SphereKey key) {
//            radius = key.radius;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null || !(obj instanceof SphereKey)) {
//                return false;
//            }
//            SphereKey other = (SphereKey) obj;
//            return radius == other.radius;
//        }
//
//        @Override
//        public int hashCode() {
//            return Float.floatToIntBits(radius);
//        }
//    }
//
//    public void drawSphere(float radius, int slices, int stacks) {
//        sphereKey.radius = radius;
//        Integer glList = sphereDisplayLists.get(sphereKey);
//
//        if (glList == null) {
//            glList = gl.glGenLists(1);
//            gl.glNewList(glList, gl.GL_COMPILE);
//
//
//
//            glu.gluSphere(quadric, radius, 8, 8);
//            gl.glEndList();
//            sphereDisplayLists.put(new SphereKey(sphereKey), glList);
//        }
//
//        gl.glCallList(glList);
//    }
//
//////////////////////////////////////////////////////////////////////////////
//    private static class CylinderKey {
//
//        public float radius;
//        public float halfHeight;
//
//        public CylinderKey() {
//        }
//
//        public CylinderKey(CylinderKey key) {
//            radius = key.radius;
//            halfHeight = key.halfHeight;
//        }
//
//        public void set(float radius, float halfHeight) {
//            this.radius = radius;
//            this.halfHeight = halfHeight;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null || !(obj instanceof CylinderKey)) {
//                return false;
//            }
//            CylinderKey other = (CylinderKey) obj;
//            if (radius != other.radius) {
//                return false;
//            }
//            if (halfHeight != other.halfHeight) {
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 7;
//            hash = 23 * hash + Float.floatToIntBits(radius);
//            hash = 23 * hash + Float.floatToIntBits(halfHeight);
//            return hash;
//        }
//    }
//
//    public void drawCylinder(float radius, float halfHeight, int upAxis) {
//        glPushMatrix();
//
//        switch (upAxis) {
//            case 0:
//                gl.glRotatef(-90f, 0.0f, 1.0f, 0.0f);
//                glTranslatef(0.0f, 0.0f, -halfHeight);
//                break;
//
//            case 1:
//                gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
//                glTranslatef(0.0f, 0.0f, -halfHeight);
//                break;
//
//            case 2:
//                glTranslatef(0.0f, 0.0f, -halfHeight);
//                break;
//
//            default: {
//                assert (false);
//            }
//        }
//
//        // The gluCylinder subroutine draws a cylinder that is oriented along the z axis.
//        // The base of the cylinder is placed at z = 0; the top of the cylinder is placed at z=height.
//        // Like a sphere, the cylinder is subdivided around the z axis into slices and along the z axis into stacks.
//
//        cylinderKey.set(radius, halfHeight);
//        Integer glList = cylinderDisplayLists.get(cylinderKey);
//
//        if (glList == null) {
//            glList = gl.glGenLists(1);
//            gl.glNewList(glList, gl.GL_COMPILE);
//
//            disk.setDrawStyle(glu.GLU_FILL);
//            disk.setNormals(glu.GLU_SMOOTH);
//
//            glu.gluDisk(quadric, 0, radius, cylinderSegments, cylinderLoops);
//            glu.gluCylinder(quadric, radius, radius, 2f * halfHeight, cylinderSegments, cylinderLoops);
//
//            glTranslatef(
//                    0f, 0f, 2f * halfHeight);
//            gl.glRotatef(
//                    -180f, 0f, 1f, 0f);
//            glu.gluDisk(quadric, 0, radius, 15, 10);
//
//            gl.glEndList();
//            cylinderDisplayLists.put(new CylinderKey(cylinderKey), glList);
//        }
//
//        gl.glCallList(glList);
//
//        glPopMatrix();
//
//    }
//
//    ////////////////////////////////////////////////////////////////////////////
//    public void drawString(CharSequence s, int x, int y, float red, float green, float blue) {
////        if (font != null) {
////            FontRender.drawString(font, s, x, y, red, green, blue);
////        }
//        //System.out.println(s);
//    }
//}
