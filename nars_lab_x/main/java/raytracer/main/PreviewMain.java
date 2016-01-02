package raytracer.main;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import raytracer.basic.BowlingScene;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class PreviewMain
{
    // Verschiebe- und Blickgeschwindigkeit der Maus:
    public final static double MOUSE_TRANSLATION_FACTOR = 5.0;
    public final static double MOUSE_ROTATION_FACTOR = 1.0;
    
    // Verschiebe- und Blickgeschwindigkeit der Tastatur:
    public final static double KEY_TRANSLATION_FACTOR = 1.0;
    public final static double KEY_ROTATION_FACTOR = 10.0;
    public final static double KEY_ANGLE_INCREMENT = 10.0;
        

    
    
    public static void main(String... argv) throws Exception
    {
        //Frame frame = new Frame("Vorschau der Szene");
        /*
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        caps.setStencilBits(1);
        GLJPanel canvas = new GLJPanel(caps);
        */

        //http://jogamp.org/git/?p=jogl-demos.git;a=blob;f=src/demos/es2/RawGL2ES2demo.java;hb=HEAD
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        // We may at this point tweak the caps and request a translucent drawable
        caps.setBackgroundOpaque(false);
        caps.setStencilBits(1);
        caps.setHardwareAccelerated(true);
        GLWindow glWindow = GLWindow.create(caps);



        // Registieren des OpenGL-Objekts:

        glWindow.addGLEventListener(new OpenGLBox());


        // Animator erzeugen:
        final Animator animator = new Animator();
        animator.add(glWindow);
        

        glWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent windowEvent) {
                animator.stop();
                System.exit(0);
            }

            @Override
            public void windowGainedFocus(com.jogamp.newt.event.WindowEvent windowEvent) {
                animator.start();
            }

            @Override
            public void windowLostFocus(com.jogamp.newt.event.WindowEvent windowEvent) {
                animator.stop();
            }
        });

        // Fenster anzeigen:
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        glWindow.setSize(d.width, d.height);
        //glWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
        glWindow.setVisible(true);
    }

    static class OpenGLBox implements GLEventListener, MouseListener, KeyListener {
        // Konstenten zur Umrechnung der Winkelsysteme:
        protected final static double DEC_TO_RAD = Math.PI/180.0;
        protected final static double RAD_TO_DEC = 180.0/Math.PI;

        // Position des Auges:
        protected final Vector3d EYE = new Vector3d(0.0, 2.0, 10.0);
        protected final Vector3d VIEW = new Vector3d(0.0, 0.0, -1.0);
        protected final Vector3d UP = new Vector3d(0.0, 1.0, 0.0);
        protected double ANGLE = 60.0;
        
        protected boolean bDragging, bDisplayed;
        protected int width, height;
        protected int nDragButton, nDisplayList, texnames[];
        protected double matrix[];
        protected Vector3d mouse, last_mouse, saved_mouse;
        protected Vector3d saved_view, saved_up;
        protected Matrix3dEx rotation, saved_rotation;
        protected GL2 gl;
        protected GLU glu;
        protected GLDrawable gld;
        
        // Matrizen zur Koordinaten-Umwandlung:
        protected final double[] mmatrix = new double[16];
        protected final double[] pmatrix = new double[16];
        protected final int[] viewport = new int[4];

        // Objekt initialisieren:
        @Override
        public void init(GLAutoDrawable gld)
        {
            GLWindow j = (GLWindow)gld;
            // Grafikkontexte schnell verf�gbar machen:
            gl = (GL2) gld.getGL();
            glu = GLU.createGLU(gl);
            this.gld = gld;
            
            // Z-Buffer aktivieren:
            gl.glEnable(GL.GL_DEPTH_TEST);
            
            // Vektoren automatisch normalisieren:
            // (In diesem Beispiel ist das zwar nicht notwendig, jedoch ist es so
            //  sauberer, da man gefahrlos die Gr��e der Kugel skalieren kann.)
            gl.glEnable(GL2.GL_NORMALIZE);
            
            // Beleuchtung und Farbe aktivieren:
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            
            // Blending-Funktion zu "Alpha-Blending" setzen:
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            
            // F�rbetechnik setzen:
            gl.glShadeModel(GL2.GL_SMOOTH);
            
            // Backface-Culling aktivieren:
            //gl.glEnable(GL.GL_CULL_FACE);
            gl.glFrontFace(GL.GL_CCW);
            //gl.glCullFace(GL.GL_BACK);
            
            // Ereignisbahandlungs-Routinen f�r Maus und Tastatur hinzuf�gen:
            j.addMouseListener(this);
            //j.addMouseMotionListener(this);
            j.addKeyListener(this);
            
            // Erstellt eine Display-Liste:
            nDisplayList = gl.glGenLists(1);
            if (!gl.glIsList(nDisplayList))
                nDisplayList = -1;      // Fehler bei Erstellung kennzeichnen
            bDisplayed = false; // Display-Liste wurde noch nicht initialisiert
            
            // Matrizen und Vektoren initialisieren:
            rotation = new Matrix3dEx();     // Aktuelle Rotationsmatrix
            matrix = new double[16];
            mouse = new Vector3d();
            last_mouse = new Vector3d(); // Position der Maus bei der letzten Drag&Drop-Operation
            saved_mouse = new Vector3d();// Position der Maus beim Start des Drag&Drop
            saved_rotation = new Matrix3dEx();   // Speichert Werte bei Drag&Drop-Start
            saved_view = new Vector3d();         //
            saved_up = new Vector3d();           //
           
            // Rotationsmatrix initialisieren:
            getLookAtRotationMatrix(rotation);
            matrix[3] = 0.0;
            matrix[7] = 0.0;
            matrix[11] = 0.0;
            matrix[12] = 0.0;
            matrix[13] = 0.0;
            matrix[14] = 0.0;
            matrix[15] = 1.0;
            
            // "Drag & Drop" ist zu Beginn deaktiviert:
            bDragging = false;
            
            initLighting();
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {

        }

        // Beleuchtung initialisieren:
        protected void initLighting()
        {
            // Beleuchtung aktivieren:
            gl.glEnable(GL2.GL_LIGHTING);
            
            // Beleuchtung der R�ckseite aktivieren:
            // (Die Innenseite vom Panzerglas soll auch Licht reflektieren,
            //  weil sie aufgrund der Transparenz sichtbar ist.)
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);
            
            // Weitere Optionen zur Belichtung:
            //
            // Die Option "GL.GL_LIGHT_MODEL_LOCAL_VIEWER" kann dieses Mal nicht
            // benutzt werden, weil der Boden die Szene spiegeln soll. Verwendet
            // man die Option, werden die spekularen Lichtanteile f�r die
            // Szene und f�r die gespiegelte Szene anhand der aktuellen Position
            // des Betrachters berechnet. Der Betrachter befindet sich jedoch
            // nur in einer der beiden Szenen, sodass f�r die anderen (die
            // Spiegelung) die spekularen Lichtanteile falsch berechnet werden.
            // Das macht sich dadurch bemerkbar, dass es keinen flie�enden �bergang
            // der spekularen Glanzpunkte am Boden von der normalen in die gespiegelte
            // Szene gibt.
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, getFloatv4(0.0f, 0.0f, 0.0f, 1.0f));
            //gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
            //gl.glLightModeli(GL.GL_LIGHT_MODEL_COLOR_CONTROL, GL.GL_SEPARATE_SPECULAR_COLOR);
            
            // Lichtquellen initialisieren:
            initLights();
            resetMaterial();
        }
        
        // Lichtquellen initialisieren:
        protected void initLights()
        {
            // D�mpfung des Lichtes setzen:
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 0.4f);
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.05f);
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.005f);

            // Lichtst�rke setzen:
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, getFloatv4(0.0f, 0.0f, 0.0f, 1.0f));
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, getFloatv4(0.5f, 0.5f, 0.5f, 1.0f));
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, getFloatv4(0.5f, 0.5f, 0.5f, 1.0f));
            
            // Leuchtintensive Stelle bei der Mitte des Lichtstrahls:
            //gl.glLightf(GL.GL_LIGHT0, GL.GL_SPOT_EXPONENT, 5.0f);
            
            // �ffnungswinkel des Lichtkegels setzen:
            //gl.glLightf(GL.GL_LIGHT0, GL.GL_SPOT_CUTOFF, 90.0f);
        }
        
        // Zeichenfl�che neu zeichnen:
        @Override
        public void display(GLAutoDrawable drawable)
        {
            // Initialisierungen durchf�hren:
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();

            // Aktuelle Matrizen f�r die Koordinaten-Umwandlung abfragen:
            gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, DoubleBuffer.wrap(mmatrix));
            gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, DoubleBuffer.wrap(pmatrix));
            gl.glGetIntegerv(GL.GL_VIEWPORT, IntBuffer.wrap(viewport));


            // Kameralichtquelle platzieren:
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, getFloatv4(0.0f, 0.0f, 0.0f, 1.0f));
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, getFloatv4(0.0f, 0.0f, -1.0f, 1.0f));
            gl.glEnable(GL2.GL_LIGHT0);
            
            // "gluLookAt" durch eine Verschiebung um "EYE" und eine Rotation
            // um "rotation" nachbauen:
            matrix[0] = rotation.m00;
            matrix[1] = rotation.m10;
            matrix[2] = rotation.m20;
            matrix[4] = rotation.m01;
            matrix[5] = rotation.m11;
            matrix[6] = rotation.m21;
            matrix[8] = rotation.m02;
            matrix[9] = rotation.m12;
            matrix[10] = rotation.m22;
            gl.glMultMatrixd(DoubleBuffer.wrap(matrix));
            gl.glTranslated(-EYE.x, -EYE.y, -EYE.z);
            
            displayStaticScene(drawable);
        }


        // Zeichnet die statische Welt:
        protected void displayStaticScene(GLAutoDrawable drawable)
        {
            if (bDisplayed)
            {
                gl.glCallList(nDisplayList);    // Displaylisten verf�gbar
                return;
            }

            // Benutzt die Display-Liste zum Zeichnen der statischen Szene:
            // Heibei wird die Szene erst erstellt, wenn "display" zum ersten
            // Mal aufgerufen wird und die Screenshots abgeschlossen sind:
            if (nDisplayList >= 0)
                gl.glNewList(nDisplayList, GL2.GL_COMPILE_AND_EXECUTE);
            
            
            
            BowlingScene scene = new BowlingScene();
            scene.display(drawable);
            
            
            
            
            if (nDisplayList >= 0)
            {
                gl.glEndList();
                bDisplayed = true;
            }
        }
        
        // Erstellt eine Rotationsmatrix genauso, wie sie von "gluLookAt" erstellt
        // wird. Die Matrix wird in "matrix" als 3x3-Matrix zur�ckgegeben:
        protected void getLookAtRotationMatrix(Matrix3d matrix)
        {
            Vector3d x = new Vector3d(), y = new Vector3d(), z = new Vector3d();
            
            // Achsen des neuen Systems berechnen:
            x.cross(VIEW, UP);
            y.cross(x, VIEW);
            z.set(VIEW);
            z.scale(-1.0);
            
            // Achsen normalisieren:
            x.normalize();
            y.normalize();
            z.normalize();
            
            // Matrix setzen:
            matrix.setRow(0, x);
            matrix.setRow(1, y);
            matrix.setRow(2, z);
        }

        // Erstellt eine Rotationsmatrix um die Achse "v" mit Winkel "fAngle".
        // Die Matrix wird in "matrix" als 3x3-Matrix zur�ckgegeben:
        // (Zur Berechnung der Matrix wird eine Basistransformation in das
        //  Koordinatensystem mit "v" als y-Achse und zwei zu sich selbst und
        //  "v" orthogonalen Vektoren durchgef�hrt.)
        // (Die unkommentierte Herleitung dieser Matrix befindet sich als
        //  Derive-Worksheet ("Rotation.dfw") bei dieser Abgabe.)
        protected void getRotationMatrix(Matrix3d matrix, double fAngle, Vector3d v)
        {
            double x = v.x, y = v.y, z = v.z;
            double fSin = Math.sin(fAngle*DEC_TO_RAD);
            double fCos = Math.cos(fAngle*DEC_TO_RAD);

            // L�nge des Rotationsvektors (x, y, z) bestimmen:
            double length = Math.sqrt(x * x + y * y + z * z);

            // Rotationsvektor normieren:
            // (Dadurch wird die zu verwendende Formel wesentlich k�rzer.)
            x /= length;
            y /= length;
            z /= length;
            
            // Zeile 1 der Rotationsmatrix bestimmen:
            matrix.m00 = (y*y + z*z)*fCos + x*x;
            matrix.m10 = -(x*y*fCos - z*fSin - x*y);
            matrix.m20 = -(x*z*fCos + y*fSin - x*z);
            
            // Zeile 2 der Rotationsmatrix bestimmen:
            matrix.m01 = -(x*y*fCos + z*fSin - x*y);
            matrix.m11 = (x*x + z*z)*fCos + y*y;
            matrix.m21 = -(y*z*fCos - x*fSin - y*z);

            // Zeile 3 der Rotationsmatrix bestimmen:
            matrix.m02 = -(x*z*fCos - y*fSin - x*z);
            matrix.m12 = -(y*z*fCos + x*fSin - y*z);
            matrix.m22 = (x*x + y*y)*fCos + z*z;
        }

        // Gibt die Bildschirmkoordinate (x, y) als virtuelle Koordinate zur�ck:
        // (Gibt "false" bei einem Fehler zur�ck.)
        protected boolean getVirtualCoordinates(int x, int y, int z, Vector3d v)
        {
            double resultXYZ[] = new double[3];

            // (x, y, z) umwandeln in Weltkoordinaten umwandeln:
            // (Wenn dies erfolgreich ist, wird laut Dokumentation "GL.GL_TRUE" zur�ckgegeben.)
            if (!glu.gluUnProject((double) x, (double) y, (double) z, mmatrix, 0, pmatrix, 0, viewport, 0, resultXYZ, 0))
                return false;

            v.x = resultXYZ[0];
            v.y = resultXYZ[1];
            v.z = resultXYZ[2];

            return true;
        }
        
        // Gr��en�nderung des Zeichenbereichs verarbeiten:
        @Override
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h)
        {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();

            width = w;
            height = h;
            if (w <= h)
            {
                glu.gluPerspective(ANGLE, 1.0, 1.0, 100.0);
                //gl.glFrustum(-1.0, 1.0, -(double)h/w, (double)h/w, 2.0, 1000.0);
            }
            else
            {
                glu.gluPerspective(ANGLE, 1.0, 1.0, 100.0);
                //gl.glFrustum(-(double)w/h, (double)w/h, -1.0, 1.0, 2.0, 1000.0);
            }
        }

        @Override
        public void mouseClicked(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mousePressed(com.jogamp.newt.event.MouseEvent event) {
            // Wenn bereits "Drag & Drop" ausgef�hrt wird, breche ab:
            if (bDragging)
            {
                mouseReleased(event);
                return;
            }
            
            // Virtuelle Mauskoordinaten abfragen:
            if (!getVirtualCoordinates(event.getX(), height-event.getY()-1, 0, last_mouse))
                return;

            // Maustaste speichern:
            nDragButton = event.getButton();
            
            // Vektoren und Matrizen sichern:
            saved_rotation.set(rotation);
            saved_mouse.set(last_mouse);
            saved_view.set(VIEW);
            saved_up.set(UP);
            
            // Drag & Drop starten:
            bDragging = true;
        }

        @Override
        public void mouseReleased(com.jogamp.newt.event.MouseEvent event) {

            bDragging = false;
        }

        @Override
        public void mouseMoved(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseDragged(com.jogamp.newt.event.MouseEvent event) {

            if (!bDragging)
                return;     // Kein Drag&Drop erlaubt
            
            Matrix3d matrix = new Matrix3d();
            Vector3d v = new Vector3d();
            
            // Bestimmt die relativen x-, y- und z-Achsen des Sicht-Koordinatensystems
            // mit dem Auge als Mittelpunkt:
            Vector3d x = new Vector3d(), y = new Vector3d(), z = new Vector3d();
            z.set(VIEW);        z.normalize();
            x.cross(z, UP);     x.normalize();
            y.cross(x, z);      y.normalize();
            
            // Virtuelle Mauskoordinaten abfragen:
            getVirtualCoordinates(event.getX(), height-event.getY()-1, 0, mouse);         
            
            switch (nDragButton)
            {
            case MouseEvent.BUTTON1:        // Ziehen mit linker Maustaste
                
                // VERSCHIEBUNG: Relative z-Achse
                v.set(z);
                v.scale(MOUSE_TRANSLATION_FACTOR*(mouse.y-last_mouse.y));
                EYE.add(v);
                
                // DREHUNG: Relative y-Achse
                getRotationMatrix(matrix, MOUSE_ROTATION_FACTOR*(last_mouse.x-mouse.x)*RAD_TO_DEC, y);
                matrix.transform(VIEW);
                matrix.transform(UP);
                
                break;
                
            case MouseEvent.BUTTON2:        // Ziehen mit mittlerer Maustaste
                
                // VERSCHIEBUNG: Relative x-Achse
                v.set(x);
                v.scale(MOUSE_TRANSLATION_FACTOR*(mouse.x-last_mouse.x));
                EYE.add(v);
                
                // VERSCHIEBUNG: Relative y-Achse
                v.set(y);
                v.scale(MOUSE_TRANSLATION_FACTOR*(mouse.y-last_mouse.y));
                EYE.add(v);
                
                break;
                
            case MouseEvent.BUTTON3:        // TRACKBALL mit rechter Maustaste
                Vector3d u = new Vector3d();
               
                // z-Koordinaten abschneiden, da es sich um virtuelle
                // Mauskoordinaten handelt, die keine Tiefeninformationb esitzen:
                mouse.z = 0.0;
                saved_mouse.z = 0.0;

                // Vektoren nach L�nge 1 abschneiden:
                if (mouse.dot(mouse) > 1.0)
                    mouse.normalize();
                if (saved_mouse.dot(saved_mouse) > 1.0)
                    saved_mouse.normalize();
                
                // z-Koordinaten der beiden Vektoren "u" und "v" auf der
                // 3D-Trackball-Kugel ausrechnen:
                v.x = mouse.x;
                v.y = mouse.y;
                v.z = Math.sqrt(Math.max(0.0, 1.0-mouse.dot(mouse)));
                u.x = saved_mouse.x;
                u.y = saved_mouse.y;
                u.z = Math.sqrt(Math.max(0.0, 1.0-saved_mouse.dot(saved_mouse)));

                // Dreh-Winkel zwischen "u" und "v" berechnen:
                double fAngle = Math.acos(v.dot(u)/(v.length()*u.length()));
                
                // Drehachse ausrechnen, um die gedreht wird. Das ist die Achse,
                // die orthogonal zu "u" und "v" ist:
                v.cross(v, u);

                // Rotationsmatrix f�r Trackball-Transformation erstellen:
                getRotationMatrix(matrix, MOUSE_ROTATION_FACTOR*fAngle*RAD_TO_DEC, v);

                // Vektoren mit dem Traxkball drehen:
                // Zuerst die "LookAt"-Transformation auff�hren, um den Sicht-
                // bereich auf den Ursprung zu transformieren. Nun kann die
                // Trackball-Rotation um die ermittelte Drehachse ausgef�hrt
                // werden. Anschlie�end wird die inverse "LookAt"-Transformation
                // ausgef�hrt, um das Ergebnis wieder an die urspr�ngliche
                // Position zu versetzen.
                // Dieser Aufwand ist n�tig, damit der Trackball unabh�ngig von
                // der aktuellen Position korrekt arbeitet.
                
                // VIEW-Vektor drehen:
                u.set(saved_view);
                saved_rotation.transform(u);
                matrix.transform(u);
                saved_rotation.transformTransposed(u);
                VIEW.set(u);
                
                // UP-Vektor drehen:
                u.set(saved_up);
                saved_rotation.transform(u);
                matrix.transform(u);
                saved_rotation.transformTransposed(u);
                UP.set(u);

                break;
            }
            last_mouse.set(mouse);
            
            // Rotationsmatrix neu bestimmen:
            getLookAtRotationMatrix(rotation);
        }

        @Override
        public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void keyPressed(com.jogamp.newt.event.KeyEvent event) {

            Matrix3d matrix = new Matrix3d();
            Vector3d v = new Vector3d();
            
            // Bestimmt die relativen x-, y- und z-Achsen des Sicht-Koordinatensystems
            // mit dem Auge als Mittelpunkt:
            Vector3d x = new Vector3d(), y = new Vector3d(), z = new Vector3d();
            z.set(VIEW);        z.normalize();
            x.cross(z, UP);     x.normalize();
            y.cross(x, z);      y.normalize();
            
            switch (event.getKeyCode())
            {
            case KeyEvent.VK_UP:            // Nach Vorne laufen
                v.set(z);
                v.scale(KEY_TRANSLATION_FACTOR);
                EYE.add(v);
                break;
                
            case KeyEvent.VK_DOWN:          // Nach Hinten laufen
                v.set(z);
                v.scale(KEY_TRANSLATION_FACTOR);
                EYE.sub(v);
                break;
                
            case KeyEvent.VK_LEFT:          // Nach Links schauen
            case KeyEvent.VK_NUMPAD4:       //
                getRotationMatrix(matrix, KEY_ROTATION_FACTOR, y);
                matrix.transform(VIEW);
                matrix.transform(UP);
                break;
                
            case KeyEvent.VK_RIGHT:         // Nach Rechts schauen
            case KeyEvent.VK_NUMPAD6:       //
                getRotationMatrix(matrix, -KEY_ROTATION_FACTOR, y);
                matrix.transform(VIEW);
                matrix.transform(UP);
                break;
            
            case KeyEvent.VK_NUMPAD8:       // Nach Oben schauen
                getRotationMatrix(matrix, KEY_ROTATION_FACTOR, x);
                matrix.transform(VIEW);
                matrix.transform(UP);
                break;
                
            case KeyEvent.VK_NUMPAD2:       // Nach Unten schauen
                getRotationMatrix(matrix, -KEY_ROTATION_FACTOR, x);
                matrix.transform(VIEW);
                matrix.transform(UP);
                break;
                
            case KeyEvent.VK_PLUS:       // Betrachtungswinkel vergr��ern
                ANGLE += KEY_ANGLE_INCREMENT;
                ((GLJPanel)gld).setSize(width, height);
                break;
                
            case KeyEvent.VK_MINUS:       // Betrachtungswinkel verkleinern
                ANGLE -= KEY_ANGLE_INCREMENT;
                ((GLJPanel)gld).setSize(width, height);
                break;
                
            case KeyEvent.VK_SPACE:       // Kameraposition ausgeben
                System.out.println();
                System.out.println("EYE:   " + EYE);
                System.out.println("VIEW:  " + VIEW);
                System.out.println("UP:    " + UP);
                System.out.println("ANGLE: " + ANGLE);
                break;
            }
            
            // Rotationsmatrix neu bestimmen:
            getLookAtRotationMatrix(rotation);
        }

        @Override
        public void keyReleased(com.jogamp.newt.event.KeyEvent keyEvent) {

        }

        // Setzt die Materialeigenschaften auf die Standardwerte zur�ck:
        protected void resetMaterial()
        {
            setAmbient(0.2f, 0.2f, 0.2f);
            setDiffuse(0.8f, 0.8f, 0.8f);
            setSpecular(0.0f, 0.0f, 0.0f);
            setEmission(0.0f, 0.0f, 0.0f);
            setShininess(0.0f);
        }
        
        // Setzt die Reflektionseigenschaft des Umgebungslichts:
        protected void setAmbient(float red, float green, float blue)
        {
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, getFloatv4(red, green, blue, 1.0f));
        }
        
        // Setzt die Reflektionseigenschaft des diffusen Lichts:
        protected void setDiffuse(float red, float green, float blue)
        {
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, getFloatv4(red, green, blue, 1.0f));
        }
        
        // Setzt die Reflektionseigenschaft des Lichts:
        protected void setSpecular(float red, float green, float blue)
        {
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, getFloatv4(red, green, blue, 1.0f));
        }
        
        // Setzt die Eigenschaft des Selbst-Leuchtens:
        protected void setEmission(float red, float green, float blue)
        {
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, getFloatv4(red, green, blue, 1.0f));
        }
        
        // Gibt an, wie geb�ndelt das Licht reflektiert werden soll:
        protected void setShininess(float shininess)
        {
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
        }
        
        // Gibt das Array "{a, b, c, d}" zur�ck:
        protected static FloatBuffer getFloatv4(float a, float b, float c, float d)
        {
            return FloatBuffer.wrap( new float[] { a, b, c, d } );
        }

        public void displayChanged(GLDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    }
}