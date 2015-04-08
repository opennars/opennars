/*
 * EfficientScene.java                    STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.basic;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.lights.Light;
import raytracer.objects.EfficientCollection;
import raytracer.objects.SceneObject;
import raytracer.objects.SceneObjectCollection;

import java.util.ArrayList;
import java.util.List;


/**
 * Diese Implementierung der Szene ist ineffizient.
 * 
 * @author Mathias Kosch
 * @author Sassan Torabi-Goudarzi
 *
 */
public class EfficientScene extends Scene
{
    /** Objekte dieser Szene. */
    protected final SceneObjectCollection objects = new EfficientCollection();
    /** Lichter dieser Szene. */
    protected final List<Light> lights = new ArrayList<Light>();
    
    
    @Override
    public void add(SceneObject object)
    {
        try
        {
            objects.add(object);
            if (object instanceof Light) {
                if (!lights.contains(object))
                    lights.add((Light) object.clone());
            }
        }
        catch (CloneNotSupportedException e)
        {
            throw new IllegalStateException();
        }
    }


    @Override
    public List<Light> getLights() {
        return lights;
    }

    
    @Override
    public ColorEx trace(Ray ray)
    {
        // Falls der Strahl nicht verfolgt wird, liefere die Hintergrundfarbe:
        if (!followRay(ray))
            return new ColorEx(backgroundColor);
        
        // Falls ein Objekt vom Strahl getroffen wurde:
        if (objects.intersect(ray))
            return ray.hit.getShader().shade(new Intersection(ray,this));
        
        // Kein Objekt wurde geschnitten. Liefere die Hintergrundfarbe:
        return new ColorEx(backgroundColor);
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return objects.occlude(ray);
    }

    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        objects.display(drawable);
    }
}