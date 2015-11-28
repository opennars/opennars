/*
 * Transformation.java                             STATUS: In Bearbeitung
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.basic;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;

/**
 * Transformationen dienen zum Repositionieren von Punkten.
 * 
 * @author Mathias Kosch
 *
 */
public class Transformation
{
    /** Transformations-Matrix. */
    private final double[][] m = {
            {1.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 1.0},
    };
    
    
    /**
     * Erzeugt eine neue Transformation.
     */
    public Transformation()
    {
    }
    
    /**
     * Erzeugt eine neue Transformation aus einer bereits bestehenden.
     * 
     * @param t Bestehde Transformation, die �bernommen wird.
     */
    public Transformation(Transformation t)
    {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                m[i][j] = t.m[i][j];
    }
    
    
    /**
     * Wendet diese Transformation auf einen Punkt an.
     * 
     * @param point Punkt, auf den diese Transformation angewendet wird.
     */
    public void transformPoint(Tuple2d point)
    {
        double x = point.x, y = point.y;
        double w = m[3][0] * x + m[3][1] * y + m[3][3];
        point.x = (m[0][0]*x+m[0][1]*y+m[0][3])/w;
        point.y = (m[1][0]*x+m[1][1]*y+m[1][3])/w;
    }
    
    /**
     * Wendet diese Transformation auf einen Punkt an.
     * 
     * @param point Punkt, auf den diese Transformation angewendet wird.
     */
    public void transformPoint(Tuple3d point)
    {
        double x = point.x, y = point.y, z = point.z;
        double w = m[3][0] * x + m[3][1] * y + m[3][2] * z + m[3][3];
        point.x = (m[0][0]*x+m[0][1]*y+m[0][2]*z+m[0][3])/w;
        point.y = (m[1][0]*x+m[1][1]*y+m[1][2]*z+m[1][3])/w;
        point.z = (m[2][0]*x+m[2][1]*y+m[2][2]*z+m[2][3])/w;
    }
    
    /**
     * Wendet diese Transformation auf einen Punkt an.
     * 
     * @param point Punkt, auf den diese Transformation angewendet wird.
     */
    public void transformPoint(Tuple3f point)
    {
        double x = (double) point.x, y = (double) point.y, z = (double) point.z;
        double w = m[3][0] * x + m[3][1] * y + m[3][2] * z + m[3][3];
        point.x = (float)((m[0][0]*x+m[0][1]*y+m[0][2]*z+m[0][3])/w);
        point.y = (float)((m[1][0]*x+m[1][1]*y+m[1][2]*z+m[1][3])/w);
        point.z = (float)((m[2][0]*x+m[2][1]*y+m[2][2]*z+m[2][3])/w);
    }
    
    /**
     * Wendet diese Transformation auf einen Vektor an.
     * 
     * @param vector Vektor, auf den diese Transformation angewendet wird.
     */
    public void transformVector(Tuple2d vector)
    {
        double x = vector.x, y = vector.y;
        vector.x = m[0][0]*x+m[0][1]*y;
        vector.y = m[1][0]*x+m[1][1]*y;
    }
    
    /**
     * Wendet diese Transformation auf einen Vektor an.
     * 
     * @param vector Vektor, auf den diese Transformation angewendet wird.
     */
    public void transformVector(Tuple3d vector)
    {
        double x = vector.x, y = vector.y, z = vector.z;
        vector.x = m[0][0]*x+m[0][1]*y+m[0][2]*z;
        vector.y = m[1][0]*x+m[1][1]*y+m[1][2]*z;
        vector.z = m[2][0]*x+m[2][1]*y+m[2][2]*z;
    }
    
    /**
     * Wendet diese Transformation auf einen Vektor an.
     * 
     * @param vector Vektor, auf den diese Transformation angewendet wird.
     */
    public void transformVector(Tuple3f vector)
    {
        double x = (double) vector.x, y = (double) vector.y, z = (double) vector.z;
        vector.x = (float)(m[0][0]*x+m[0][1]*y+m[0][2]*z);
        vector.y = (float)(m[1][0]*x+m[1][1]*y+m[1][2]*z);
        vector.z = (float)(m[2][0]*x+m[2][1]*y+m[2][2]*z);
    }
    
    
    /**
     * Setzt alle Transformationen zur�ck.
     */
    public void reset()
    {
        // Einheitsmatrix zuweisen:
        byte i;
        for (i = (byte) 0; (int) i < 4; i++)
            for (byte j = (byte) 0; (int) j < 4; j++)
                m[((int) i)][((int) j)] = 0.0;
        for (i = (byte) 0; (int) i < 4; i++)
            m[((int) i)][((int) i)] = 1.0;
    }
    
    
    /**
     * Kehrt alle Transformationen dieses Objekts um.
     */
    public static void invert()
    {
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * Kombiniert dise Translation mit einer Verschiebung.
     * 
     * @param x x-Richtung der Verschiebung.
     * @param y y-Richtung der Verschiebung.
     * @param z z-Richtung der Verschiebung.
     */
    public void move(double x, double y, double z)
    {
        // Transformationsmatrix erstellen:
        double t[][] = {
                {1.0, 0.0, 0.0, x},
                {0.0, 1.0, 0.0, y},
                {0.0, 0.0, 1.0, z},
                {0.0, 0.0, 0.0, 1.0},
        };
        concat(t);
    }
    
    /**
     * Kombiniert dise Translation mit einer Skalierung.
     * 
     * @param x Skalierungsfaktor der x-Koordinate.
     * @param y Skalierungsfaktor der y-Koordinate.
     * @param z Skalierungsfaktor der z-Koordinate.
     */
    public void scale(double x, double y, double z)
    {
        // Transformationsmatrix erstellen:
        double t[][] = {
                {x, 0.0, 0.0, 0.0},
                {0.0, y, 0.0, 0.0},
                {0.0, 0.0, z, 0.0},
                {0.0, 0.0, 0.0, 1.0},
        };
        concat(t);
    }
    
    /**
     * Kombiniert dise Translation mit einer Rotation.
     * 
     * @param angle Rotationswinkel in Grad.
     * @param x x-Koordinate der Rotationsachse.
     * @param y y-Koordinate der Rotationsachse.
     * @param z z-Koordinate der Rotationsachse.
     */
    public void rotate(double angle, double x, double y, double z)
    {
        // Rotationsachse normalisieren:
        double length = Math.sqrt(x*x+y*y+z*z);
        x /= length;
        y /= length;
        z /= length;
        
        // Rotationsmstrix erstellen:
        double cos = Math.cos(angle*Math.PI/180.0);
        double sin = Math.sin(angle*Math.PI/180.0);
        double cos1 = 1.0-cos;
        double t[][] = {
                {cos+cos1*x*x, cos1*x*y-sin*z, cos1*x*z+sin*y, 0.0},
                {cos1*x*y+sin*z, cos+cos1*y*y, cos1*y*z-sin*x, 0.0},
                {cos1*x*z-sin*y, cos1*y*z+sin*x, cos+cos1*z*z, 0.0},
                {0.0, 0.0, 0.0, 1.0},
        };
        concat(t);
    }
    
    /**
     * Kombiniert diese Transformation mit einer anderen Transformation.
     * 
     * @param t Transformation, mit der diese Transformation kombiniert wird.
     */
    public void concat(Transformation t)
    {
        concat(t.m);
    }
    
    /**
     * Kombiniert diese Transformation mit einer anderen Transformation.<br>
     * Die Kombination l�uft in Vorw�rts-Richtung ab. Zuerst wird diese
     * Transformation ausgef�hrt und anschlie�end <code>t</code>.
     * 
     * @param t Transformation, mit der diese Transformation kombiniert wird.
     */
    private void concat(final double[][] t)
    {
        int i, j;
        final double m[][] = new double[4][4];
        
        // Ergebnis berechnen:
        for (i = 0; i < 4; i++)
            for (j = 0; j < 4; j++)
            {
                m[i][j] = 0.0;
                for (int k = 0; k < 4; k++)
                    m[i][j] += t[i][k]*this.m[k][j];
            }
        
        // Ergebnis �bernehmen:
        for (i = 0; i < 4; i++)
            for (j = 0; j < 4; j++)
                this.m[i][j] = m[i][j];
    }
}