package raytracer.effects;

import javax.vecmath.Vector3d;

/**
 * Effekte auf den Normalenvektoren erzeugen den Eindruck von
 * Oberflächenstrukturen.
 * 
 * @author Mathias Kosch
 *
 */
public interface NormalEffect
{
    /**
     * Verändert einen Normalenvektor anhand eines bestimmten Effekts.
     * 
     * @param normal Normalenvektor, der angepasst wird.
     * @param point Punkt, von dem der 
     * Normalenvektor ausgeht.
     */
    public void adjustNormal(Vector3d normal, Vector3d point);
}