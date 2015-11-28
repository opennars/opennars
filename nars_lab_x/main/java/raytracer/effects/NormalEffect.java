package raytracer.effects;

import javax.vecmath.Vector3d;

/**
 * Effekte auf den Normalenvektoren erzeugen den Eindruck von
 * Oberfl�chenstrukturen.
 * 
 * @author Mathias Kosch
 *
 */
public interface NormalEffect
{
    /**
     * Ver�ndert einen Normalenvektor anhand eines bestimmten Effekts.
     * 
     * @param normal Normalenvektor, der angepasst wird.
     * @param point Punkt, von dem der 
     * Normalenvektor ausgeht.
     */
    void adjustNormal(Vector3d normal, Vector3d point);
}