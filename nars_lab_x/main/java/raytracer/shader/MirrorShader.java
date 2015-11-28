/*
 * MirrorShader.java                      STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.basic.Ray;

import javax.vecmath.Vector3d;

/**
 * Dieser shader simuliert eine totale Spiegelung.
 * 
 * @author Jonas Stahl
 * @author Mathias Kosch
 *
 */
public class MirrorShader implements Shader
{
    /**
     * Erzeugt einen neuen Shader, der eine totale Spiegelung simuliert.
     */
	public MirrorShader()
    {
	}
	
    //TODO: Hintergrundfarbe zur�ckgeben, wenn Ray abgeschnitten
    @Override
    public ColorEx shade(Intersection intersection)
    {
        // Fortf�hrenden Strahl erzeugen:
        Ray ray = Ray.continueRay(intersection.ray);
        if (ray == null)
            return intersection.scene.getBackgroundColor();
        
        Vector3d n = intersection.shape.getNormal(ray.org);
        ray.dir.scaleAdd(-2.0*n.dot(ray.dir)/n.dot(n), n, ray.dir);
        return intersection.scene.trace(ray);
    }
}