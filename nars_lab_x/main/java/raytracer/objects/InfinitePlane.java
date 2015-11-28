/*
 * InfinitePlane.java                     STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Ray;
import raytracer.basic.Transformation;
import raytracer.shader.Shader;
import raytracer.util.FloatingPoint;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Dieses Objekt stellt eine unendlich gro�e Ebene dar.
 * 
 * @author Mathias Kosch
 * 
 */
public class InfinitePlane extends Shape
{
    /** Ein Punkt auf der Ebene. */
    private Vector3d point = null;
    /** Normalen-Vektor der Ebene. */
    private Vector3d normal = null;
        
    /** Skalarprodukt von POINT und der Normale der Ebene. */
    private double pointDotNormal;
    
    
    /**
     * Erzeugt eine neue unendlich gro�e Ebene.
     * 
     * @param point Ein Punkt auf der Ebene.
     * @param normal Normalen-Vektor der Ebene.
     * @param shader Shader, mit dem die Dreieck gezeichnet wird.
     */
    public InfinitePlane(Vector3d point, Vector3d normal, Shader shader)
    {
        super(shader);
        
    	this.point = new Vector3d(point);
    	this.normal = new Vector3d(normal);
        
        // Skalarprodukte berechnen:
        pointDotNormal = point.dot(normal);
    }
    
    
    @Override
    public InfinitePlane clone()
    throws CloneNotSupportedException 
    {
        InfinitePlane clone = (InfinitePlane)super.clone();
        
        clone.point = new Vector3d(point);
        clone.normal = new Vector3d(normal);

        return clone;
    }
    
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        // Die Kugel wird durch das kartesische Koordinatensystem interpoliert:
        points.add(new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        points.add(new Vector3d(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
        points.add(new Vector3d(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        points.add(new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        points.add(new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        points.add(new Vector3d(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        points.add(new Vector3d(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        points.add(new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    }
    
    
    @Override
    public Vector3d getNormal(Vector3d point)
    {
        return adjustNormal(new Vector3d(normal), point);
    }
    
    
    @Override
    public boolean isFinite()
    {
        return false;
    }
    
    @Override
    public double getCentroid(byte axisId)
    {
        // Unendliche Objekte haben keinen Schwerpunkt:
        throw new UnsupportedOperationException();
    }

    @Override
    public byte compareAxis(byte axisId, double axisValue)
    {
        // Falls der Normalenvektor der unendlichen Ebene nicht linear abh�ngig
        // von dem Normalenvektor der Abene durch den Achsen-Punkt ist, haben
        // beide einen Schnittpunkt.
        double coordinate;
        switch (axisId)
        {
        case 0:
            if ((normal.y != 0.0) || (normal.z != 0.0))
                return (byte) 0;
            coordinate = point.x;
            break;
            
        case 1:
            if ((normal.x != 0.0) || (normal.z != 0.0))
                return (byte) 0;
            coordinate = point.y;
            break;
            
        case 2:
            if ((normal.x != 0.0) || (normal.y != 0.0))
                return (byte) 0;
            coordinate = point.z;
            break;
            
        default:
            throw new IllegalArgumentException();
        }
        
        // Laga des Objekts anhand der Lage eines Punkts auf der Ebene bestimmen:
        if (coordinate < axisValue)
            return (byte) -1;
        if (coordinate > axisValue)
            return (byte) 1;
        return (byte) 0;
    }

    @Override
    public double minAxisValue(byte axisId)
    {
        switch (axisId)
        {
        case 0: return ((normal.y == 0.0) && (normal.z == 0.0)) ?
                point.x : Double.NEGATIVE_INFINITY;
        case 1: return ((normal.x == 0.0) && (normal.z == 0.0)) ?
                point.y : Double.NEGATIVE_INFINITY;
        case 2: return ((normal.x == 0.0) && (normal.y == 0.0)) ?
                point.z : Double.NEGATIVE_INFINITY;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public double maxAxisValue(byte axisId)
    {
        switch (axisId)
        {
        case 0: return ((normal.y == 0.0) && (normal.z == 0.0)) ?
                point.x : Double.NEGATIVE_INFINITY;
        case 1: return ((normal.x == 0.0) && (normal.z == 0.0)) ?
                point.y : Double.NEGATIVE_INFINITY;
        case 2: return ((normal.x == 0.0) && (normal.y == 0.0)) ?
                point.z : Double.NEGATIVE_INFINITY;
        default:
            throw new IllegalArgumentException();
        }
    }
    
    
    @Override
    public void transform(Transformation t)
    {
        // Punkte der Ebene transformieren:
        t.transformPoint(point);
        
        // Vektoren der Ebene transformieren:
        t.transformVector(normal);
        
        // Skalarprodukte neu berechnen:
        pointDotNormal = point.dot(normal);
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
        double t = (pointDotNormal-normal.dot(ray.org))/dot;
        
        // Falls der Schnittpunkt auf der falschen Seite liegt oder
        // zu nahe am Ursprung liegt:
        if ((double) FloatingPoint.compareTolerated(t, 0.0) <= 0.0)
            return false;
        
        // Falls der Strahl bereits ein anderes Objekt schneidet, das weiter
        // vorne liegt:
        if (ray.length <= t)
            return false;
        
        ray.length = t;     // Strahl schneidet die Ebene
        ray.hit = this;     // Geschnittenes Objekt
    	return true;
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return intersect(new Ray(ray));
    }
    
    
    @Override
    public Vector2d getTextureCoords(Vector3d point)
    {
        // Texturen sind derzeit noch nicht implementiert:
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void transformTexture(Transformation t)
    {
        
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        // Anzeige-Funkton derzeit noch nicht implementiert:
        throw new UnsupportedOperationException();
    }
}