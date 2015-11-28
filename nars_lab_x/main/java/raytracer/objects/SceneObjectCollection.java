/*
 * SceneObjectCollection.java             STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import javax.vecmath.Vector3d;
import java.util.Collection;

/**
 * Eine <code>SceneObjectCollection</code> ist eine Sammlung vom Szenen-Objekten,
 * die selbst widerum ein Szenen-Objekt darstellt.
 * 
 * @author Mathias Kosch
 *
 */
abstract public class SceneObjectCollection extends SceneObject
{
    @Override
    public SceneObjectCollection clone()
    throws CloneNotSupportedException
    {
        return (SceneObjectCollection)super.clone();
    }
    
    
    @Override
    abstract public void getBoundingPoints(Collection<Vector3d> points);
    
    
    /**
     * F�gt ein Szenen-Objekt zu dieser Samlung hinzu.
     * 
     * @param object Szenen-Objekt, das hinzu gef�gt werden soll.
     */
    abstract public void add(SceneObject object);

    /**
     * F�gt eine Menge von Szenen-Objekten zu dieser Samlung hinzu.
     * 
     * @param objects Menge von Szenen-Objekten, die hinzu gef�gt werden soll.
     */
    abstract public void addAll(Collection<SceneObject> objects);
}