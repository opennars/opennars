/*
 * Triangle.java                          STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Ray;
import raytracer.basic.RaytracerConstants;
import raytracer.basic.Transformation;
import raytracer.exception.LinearlyDependentException;
import raytracer.shader.Shader;
import raytracer.util.FloatingPoint;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Objekt stellt ein Dreieck dar.
 * 
 * @author Mathias Kosch
 *
 */
public class Triangle extends Shape
{
    /** Punkt A des Dreiecks. */
    protected Vector3d a = new Vector3d();
    /** Punkt B des Dreiecks. */
    protected Vector3d b = new Vector3d();
    /** Punkt C des Dreiecks. */
    protected Vector3d c = new Vector3d();
    
    /** Normalenvektor der Ebene des Dreiecks. */
    protected Vector3d normal = new Vector3d();
    /** Normalenvektor am Punkt A des Dreiecks, oder <code>null</code>. */
    protected Vector3d normalA = null;
    /** Normalenvektor am Punkt B des Dreiecks, oder <code>null</code>. */
    protected Vector3d normalB = null;
    /** Normalenvektor am Punkt C des Dreiecks, oder <code>null</code>. */
    protected Vector3d normalC = null;
    
    /** Texturkoordnate am Punkt A des Dreiecks. */
    protected Vector2d textureA = new Vector2d();
    /** Texturvektor von Punkt A nach B des Dreiecks. */
    protected Vector2d textureAB = new Vector2d();
    /** Texturvektor von Punkt A nach C des Dreiecks. */
    protected Vector2d textureAC = new Vector2d();
    
    /** Normalenvektor der Ebene BC. */
    private Vector3d normalPlaneA = new Vector3d();
    /** Normalenvektor der Ebene CA. */
    private Vector3d normalPlaneB = new Vector3d();
    /** Normalenvektor der Ebene AB. */
    private Vector3d normalPlaneC = new Vector3d();
    
    /** Skalarprodukt von A und der Normale des Dreiecks. */
    private float aDotNormal;
    /** Skalarprodukt von B und der Normale <code>normalPlaneA</code>. */
    private float bDotNormalPlaneA;
    /** Skalarprodukt von C und der Normale <code>normalPlaneB</code>. */
    private float cDotNormalPlaneB;
    /** Skalarprodukt von A und der Normale <code>normalPlaneC</code>. */
    private float aDotNormalPlaneC;
    /** Skalarprodukt von (A-B) und der Normale <code>normalPlaneA</code>. */
    private float aMinusBDotNormalPlaneA;
    /** Skalarprodukt von (B-C) und der Normale <code>normalPlaneB</code>. */
    private float bMinusCDotNormalPlaneB;
    /** Skalarprodukt von (C-A) und der Normale <code>normalPlaneC</code>. */
    private float cMinusADotNormalPlaneC;
    
    
    /**
     * Erzeugt ein neues Dreieck.
     * 
     * @param a Erster Punkt des Dreiecks.
     * @param b Zweiter Punkt des Dreiecks.
     * @param c Dritter Punkt des Dreiecks.
     * @param shader Shader, mit dem das Dreieck gezeichnet wird.
     */
	public Triangle(Vector3d a, Vector3d b, Vector3d c, Shader shader)
    {
        super(shader);
        
        // Punkte initialisieren:
        this.a.set(a);
        this.b.set(b);
        this.c.set(c);
        
        // Texturkoordinaten initialisieren:
        textureA.x = 0.0; textureA.y = 0.0;
        textureAB.x = 1.0; textureAB.y = 0.0;
        textureAC.x = 0.0; textureAC.y = 1.0;
        
        // Seitenvektoren des Dreiecks berechnen:
        normalPlaneA.sub(this.c, this.b);
        normalPlaneB.sub(this.a, this.c);
        normalPlaneC.sub(this.b, this.a);
        
        // Normalenvektoren des Dreiecks berechnen:
        normal.cross(normalPlaneB, normalPlaneC);
        
        // Sicher stellen, dass keine zwei Seiten des Dreiecks voneinander
        // linear abh�ngig sind:
        if ((normal.x == 0.0) && (normal.y == 0.0) && (normal.z == 0.0))
            throw new LinearlyDependentException();
        
        // H�henvektoren des Dreiecks berechnen:
        normalPlaneA.cross(normal, normalPlaneA);
        normalPlaneB.cross(normal, normalPlaneB);
        normalPlaneC.cross(normal, normalPlaneC);
        
        // Wichtige Werte zwischenspeichern:
        recompute();
	}
    
    protected void recompute()
    {
        // Skalarprodukte berechnen:
        aDotNormal = (float)a.dot(normal);
        bDotNormalPlaneA = (float)b.dot(normalPlaneA);
        cDotNormalPlaneB = (float)c.dot(normalPlaneB);
        aDotNormalPlaneC = (float)a.dot(normalPlaneC);
        aMinusBDotNormalPlaneA = (float)(a.dot(normalPlaneA)- (double) bDotNormalPlaneA);
        bMinusCDotNormalPlaneB = (float)(b.dot(normalPlaneB)- (double) cDotNormalPlaneB);
        cMinusADotNormalPlaneC = (float)(c.dot(normalPlaneC)- (double) aDotNormalPlaneC);
    }
    
    
    @Override
    public Triangle clone()
    throws CloneNotSupportedException 
    {
        Triangle clone = (Triangle)super.clone();
        
        clone.a = new Vector3d(a);
        clone.b = new Vector3d(b);
        clone.c = new Vector3d(c);
        clone.normal = new Vector3d(normal);
        clone.normalA = (normalA == null) ? null : new Vector3d(normalA);
        clone.normalB = (normalB == null) ? null : new Vector3d(normalB);
        clone.normalC = (normalC == null) ? null : new Vector3d(normalC);
        clone.textureA = new Vector2d(textureA);
        clone.textureAB = new Vector2d(textureAB);
        clone.textureAC = new Vector2d(textureAC);
        clone.normalPlaneA = new Vector3d(normalPlaneA);
        clone.normalPlaneB = new Vector3d(normalPlaneB);
        clone.normalPlaneC = new Vector3d(normalPlaneC);

        return clone;
    }


    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        points.add(new Vector3d(a));
        points.add(new Vector3d(b));
        points.add(new Vector3d(c));
    }
    
    
    @Override
    public Vector3d getNormal(Vector3d point)
    {
        if ((normalA == null) || (normalB == null) || (normalC == null))
            return adjustNormal(new Vector3d(normal), point);
        else
        {
            // Normalenvektor aus den Normalenvektoren an den Punkten des Dreiecks
            // mit entsprechender Gewichtung berechnen:
            Vector3d normal = new Vector3d();
            normal.scaleAdd((normalPlaneA.dot(point)- (double) bDotNormalPlaneA)/ (double) aMinusBDotNormalPlaneA, normalA, normal);
            normal.scaleAdd((normalPlaneB.dot(point)- (double) cDotNormalPlaneB)/ (double) bMinusCDotNormalPlaneB, normalB, normal);
            normal.scaleAdd((normalPlaneC.dot(point)- (double) aDotNormalPlaneC)/ (double) cMinusADotNormalPlaneC, normalC, normal);
            return adjustNormal(normal, point);
        }
	}
    
    /**
     * Setzt f�r jeden Punkt dieses Dreiecks einen Normalenvektor.
     * 
     * @param a Normalenvektor an Punkt A.
     * @param b Normalenvektor an Punkt B.
     * @param c Normalenvektor an Punkt C.
     */
    public void setNormals(Vector3d a, Vector3d b, Vector3d c)
    {
        normalA = (a == null) ? null : new Vector3d(a);
        normalB = (b == null) ? null : new Vector3d(b);
        normalC = (c == null) ? null : new Vector3d(c);
        
        // Normalenvektoren normalisieren:
        if (normalA != null)
            normalA.normalize();
        if (normalB != null)
            normalB.normalize();
        if (normalC != null)
            normalC.normalize();
    }
    
    
    @Override
    public boolean isFinite()
    {
        return true;
    }
    
    @Override
    public double getCentroid(byte axisId)
    {
        switch (axisId)
        {
        case 0: return (a.x+b.x+c.x)/3.0;
        case 1: return (a.y+b.y+c.y)/3.0;
        case 2: return (a.z+b.z+c.z)/3.0;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public byte compareAxis(byte axisId, double axisValue)
    {
        // Vorzeichen der Abst�nde der Punkte von der Ebene berechnen:
        byte sign1, sign2, sign3;
        switch (axisId)
        {
        case 0:
            sign1 = (byte)Math.signum(a.x-axisValue);
            sign2 = (byte)Math.signum(b.x-axisValue);
            sign3 = (byte)Math.signum(c.x-axisValue);
            break;
            
        case 1:
            sign1 = (byte)Math.signum(a.y-axisValue);
            sign2 = (byte)Math.signum(b.y-axisValue);
            sign3 = (byte)Math.signum(c.y-axisValue);
            break;
            
        case 2:
            sign1 = (byte)Math.signum(a.z-axisValue);
            sign2 = (byte)Math.signum(b.z-axisValue);
            sign3 = (byte)Math.signum(c.z-axisValue);
            break;

        default:
            throw new IllegalArgumentException();
        }
        
        // Laga des Objekts zur�ckgeben:
        if ((sign1 != sign2) || (sign2 != sign3))
            return (byte) 0;
        return sign1;
    }
    
    @Override
    public double minAxisValue(byte axisId)
    {
        switch (axisId)
        {
        case 0: return Math.min(a.x, Math.min(b.x, c.x));
        case 1: return Math.min(a.y, Math.min(b.y, c.y));
        case 2: return Math.min(a.z, Math.min(b.z, c.z));
        default:
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public double maxAxisValue(byte axisId)
    {
        switch (axisId)
        {
        case 0: return Math.max(a.x, Math.max(b.x, c.x));
        case 1: return Math.max(a.y, Math.max(b.y, c.y));
        case 2: return Math.max(a.z, Math.max(b.z, c.z));
        default:
            throw new IllegalArgumentException();
        }
    }
    

    @Override
    public void transform(Transformation t)
    {
        // Punkte des Dreiecks transformieren:
        t.transformPoint(a);
        t.transformPoint(b);
        t.transformPoint(c);

        // Normalenvektoren transformieren:
        t.transformVector(normal);
        if (normalA != null)
            t.transformVector(normalA);
        if (normalB != null)
            t.transformVector(normalB);
        if (normalC != null)
            t.transformVector(normalC);
        
        // Normalenvektoren der Ebenen transformieren:
        t.transformVector(normalPlaneA);
        t.transformVector(normalPlaneB);
        t.transformVector(normalPlaneC);
        
        // Zwischengespeicherte Werte aktualisieren:
        recompute();
    }


    @Override
    public boolean intersect(Ray ray)
    {
        // Lichter ignorieren, falls erw�nscht:
        if ((isLight) && (ray.ignoreLights))
            return false;
        
        double dot = normal.dot(ray.dir);
        
        // Falls es keinen Schnittpunkt gibt:
        if (dot == 0.0)
            return false;
        
        // Schnittpunkt berechnen:
        double t = ((double) aDotNormal -normal.dot(ray.org))/dot;
        
        // Falls der Schnittpunkt auf der falschen Seite liegt oder
        // zu nahe am Ursprung liegt:
        if ((double) FloatingPoint.compareTolerated(t, 0.0) <= 0.0)
            return false;
        
        // Vorzeichen der Abst�nde vom Schnittpunkt und der Seiten des Dreiecks berechnen:
        float signA = (float)(normalPlaneA.dot(ray.org)+normalPlaneA.dot(ray.dir)*t- (double) bDotNormalPlaneA);
        float signB = (float)(normalPlaneB.dot(ray.org)+normalPlaneB.dot(ray.dir)*t- (double) cDotNormalPlaneB);
        float signC = (float)(normalPlaneC.dot(ray.org)+normalPlaneC.dot(ray.dir)*t- (double) aDotNormalPlaneC);
        
        // Falls eines der Vorzeichen nengativ ist, liegt 'p' nicht im Dreieck.
        // Andernfalls liegt 'p' im Dreieck:
        if (((double) signA < 0.0) || ((double) signB < 0.0) || ((double) signC < 0.0))
            return false;

        // Falls der Strahl bereits ein anderes Objekt schneidet, das weiter
        // vorne liegt:
        if (ray.length <= t)
            return false;
        
        ray.length = t;     // Strahl schneidet das Dreieck
        ray.hit = this;     // Geschnittenes Objekt
        return true;
	}

	@Override
    public boolean occlude(Ray ray)
    {
        return intersect(new Ray(ray));
    }
    
    
    /**
     * Setzt die Texturkoordinaten dieses Dreiecks.
     * 
     * @param a Texturkorodinate f�r den Punkt A.
     * @param b Texturkorodinate f�r den Punkt B.
     * @param c Texturkorodinate f�r den Punkt C.
     */
    public void setTextureCoords(Vector2d a, Vector2d b, Vector2d c)
    {
        textureA.set(a);
        textureAB.sub(b, a);
        textureAC.sub(c, a);
    }
    
    @Override
    public Vector2d getTextureCoords(Vector3d point)
    {
        Vector3d ab = new Vector3d();
        ab.sub(b, a);
        Vector3d ac = new Vector3d();
        ac.sub(c, a);

        // Linearfaktorzerlegung des Punktes:
        double cTy = ac.dot(normalPlaneC);
        double pTy = normalPlaneC.dot(point)-normalPlaneC.dot(a);
        double lambda1 = ((ab.dot(point)-ab.dot(a))*cTy-ac.dot(ab)*pTy)/(ab.dot(ab)*cTy);
        double lambda2 = pTy/cTy;
        
        // Texturkoodinate berechnen und zur�ckgeben:
        Vector2d coords = new Vector2d(textureA);
        coords.scaleAdd(lambda1, textureAB, coords);
        coords.scaleAdd(lambda2, textureAC, coords);
        return coords;
    }
    
    @Override
    public void transformTexture(Transformation t)
    {
        // Texturkoordinaten des Dreiecks transformieren:
        t.transformPoint(textureA);
        t.transformVector(textureAB);
        t.transformVector(textureAC);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        boolean smoothNormals =
                (normalA != null) && (normalB != null) && (normalC != null);
        GL2 gl = (GL2)drawable.getGL();

        // Dreieck zeichnen:
        gl.glBegin((RaytracerConstants.GL_GRID_MODE_ENABLED) ?
                GL.GL_LINE_LOOP : GL.GL_TRIANGLES);
        if (smoothNormals)
            gl.glNormal3d(normalA.x, normalA.y, normalA.z);
        else
            gl.glNormal3d(normal.x, normal.y, normal.z);
        gl.glVertex3d(a.x, a.y, a.z);
        if (smoothNormals)
            gl.glNormal3d(normalB.x, normalB.y, normalB.z);
        gl.glVertex3d(b.x, b.y, b.z);
        if (smoothNormals)
            gl.glNormal3d(normalC.x, normalC.y, normalC.z);
        gl.glVertex3d(c.x, c.y, c.z);
        gl.glEnd();
    }
}