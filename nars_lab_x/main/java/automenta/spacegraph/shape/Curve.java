/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.shape;


import com.jogamp.opengl.GL2;

/**
 *
 * @author seh
 */
public class Curve extends Spatial implements Drawable {

    private final Rect aRect;
    private final Rect bRect;
    float lineWidth = 2f;
    float lineSteps = 4.0f;
    int bezierPartitions = 4;
    public float[] ctrlPoints;
    private final int degree;
    private float cr, cg, cb;
    //private final TextRect labelRect;

    public Curve(Rect aRect, Rect bRect, int degree, float lineWidth) {
        this(aRect, bRect, degree);
        setLineWidth(lineWidth);
    }

    public Curve(Rect aRect, Rect bRect, int degree) {
        super();
        this.aRect = aRect;
        this.bRect = bRect;
        this.degree = degree;
        cr = cg = cb = 1.0f;
    }

    @Override
    public void draw(GL2 gl) {
        int nbCtrlPoints = 0;
        
        if (degree == 4) {
            nbCtrlPoints = 4;
            final int sizeCtrlPoints = nbCtrlPoints * 3;
            ctrlPoints = new float[] {
                aRect.getCenter().x(), aRect.getCenter().y(), aRect.getCenter().z(),
                aRect.getCenter().x(), aRect.getCenter().y(), bRect.getCenter().z(),
                aRect.getCenter().x(), bRect.getCenter().y(), bRect.getCenter().z(),
                bRect.getCenter().x(), bRect.getCenter().y(), bRect.getCenter().z()
            };
        }
        else if (degree == 2) {
            nbCtrlPoints = 2;
            final int sizeCtrlPoints = nbCtrlPoints * 3;
            ctrlPoints = new float[] {
                aRect.getCenter().x(), aRect.getCenter().y(), aRect.getCenter().z(),
                bRect.getCenter().x(), bRect.getCenter().y(), bRect.getCenter().z()
            };
        }

        gl.glMap1f(gl.GL_MAP1_VERTEX_3,
            0.0f, 1.0f, 3,
            nbCtrlPoints, ctrlPoints, 0);

        gl.glEnable(gl.GL_MAP1_VERTEX_3);
        gl.glEnable(GL2.GL_DEPTH_TEST);


        gl.glPushMatrix();
        transform(gl);

// Draw ctrlPoints.
//        gl.glBegin(gl.GL_POINTS);
//        {
//            for (int i = 0; i < sizeCtrlPoints; i += 3) {
//                gl.glVertex3f(ctrlPoints[i],
//                    ctrlPoints[i + 1],
//                    ctrlPoints[i + 2]);
//            }
//        }
//        gl.glEnd();

        gl.glMapGrid1f(bezierPartitions, 0f, 1f);
        gl.glEvalMesh1(gl.GL_POINT, 0, bezierPartitions);

        gl.glEnable(gl.GL_LINE_SMOOTH);
        gl.glLineWidth(lineWidth);

        gl.glColor3f(cr, cg, cb);
        
        gl.glBegin(gl.GL_LINE_STRIP);
        {
            for (float v = 0; v <= 1; v += (1.0f / lineSteps)) {
                gl.glEvalCoord1f(v);
            }
        }
        gl.glEnd();
        gl.glPopMatrix();

    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setColor(float cr, float cg, float cb) {
        this.cr = cr;
        this.cg = cg;
        this.cb = cb;
    }

    


//    @Override public void draw(GL2 gl) {
//        gl.glColor3f(1f, 1f, 1f);
//        gl.glPushMatrix();
//        transform(gl);
//
//        // Draw ctrlPoints.
//        gl.glBegin(GL.GL_LINES);
//        {
//            gl.glVertex3f(aRect.getCenter().x(), aRect.getCenter().y(), aRect.getCenter().z());
//            gl.glVertex3f(bRect.getCenter().x(), bRect.getCenter().y(), bRect.getCenter().z());
//        }
//        gl.glEnd();
//        gl.glPopMatrix();
//    }
}
