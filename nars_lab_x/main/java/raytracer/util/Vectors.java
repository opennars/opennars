package raytracer.util;

import javax.vecmath.Vector3d;

public class Vectors
{
    /**
     * Ermittelt die Entfernung zweier Vektoren.
     * 
     * @param v1 Vektor 1.
     * @param v2 Vektor 2.
     * @return Entfernung zwischen <code>v1</code> und <code>v2</code>.
     */
    public static double distance(Vector3d v1, Vector3d v2)
    {
        return Math.sqrt((v2.x-v1.x)*(v2.x-v1.x)+(v2.y-v1.y)*(v2.y-v1.y)+(v2.z-v1.z)*(v2.z-v1.z));
    }
    
    /**
     * Ermittelt die Entfernung zweier Vektoren zum Quadrat.
     * 
     * @param v1 Vektor 1.
     * @param v2 Vektor 2.
     * @return Entfernung zwischen <code>v1</code> und <code>v2</code> zum
     *         Quadrat.
     */
    public static double distanceSquared(Vector3d v1, Vector3d v2)
    {
        return (v2.x-v1.x)*(v2.x-v1.x)+(v2.y-v1.y)*(v2.y-v1.y)+(v2.z-v1.z)*(v2.z-v1.z);
    }
}