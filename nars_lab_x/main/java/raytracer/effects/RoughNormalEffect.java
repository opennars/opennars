package raytracer.effects;

import raytracer.util.Noise;

import javax.vecmath.Vector3d;

/**
 * Dieser Effekt erzeugt eine rauhe Oberfl�che f�r eine Bowling-Kugel.
 * 
 * @author Mathias Kosch
 *
 */
public class RoughNormalEffect implements NormalEffect
{
    protected final float factor;
    
    public RoughNormalEffect(float factor)
    {
        this.factor = factor;
    }
    
    
    @Override
    public void adjustNormal(Vector3d normal, Vector3d point)
    {
        double f1 = Noise.noise_3(point.x, point.y, point.z, 1000);
        double f2 = Noise.noise_3(point.y, point.z, point.x, 1000);
        if (f1 < 0.0) f1 = 0.0;
        if (f1 > 1.0) f1 = 1.0;
        if (f2 < 0.0) f2 = 0.0;
        if (f2 > 1.0) f2 = 1.0;

        Vector3d axis1 = new Vector3d(), axis2 = new Vector3d();
        normal.normalize();
        
        // 1. Vektor bestimmen:
        axis1.x = 1.0; axis1.y = 0.0; axis1.z = 0.0;
        axis1.cross(axis1, normal);
        if ((axis1.x == 0.0) && (axis1.y == 0.0) && (axis1.z == 0.0))
        {
            // Beide Vektoren sind linear abh�ngig. Versuche es erneut:
            axis1.z = 1.0;
            axis1.cross(axis1, normal);
        }
        
        // 2. Vektor bestimmen:
        axis2.cross(axis1, normal);
        
        // Normalenvektor variieren:
        normal.scaleAdd((f1*2.0-1.0)* (double) factor /axis1.length(), axis1, normal);
        normal.scaleAdd((f2*2.0-1.0)* (double) factor /axis2.length(), axis2, normal);
    }
}