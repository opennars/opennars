package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;

/**
 * Ein Shader liefert Informationen �ber die Farbe eines Objekts zu einem
 * darauf geschossenen Strahl.
 * 
 * @author Mathias Kosch
 * 
 */
public interface Shader
{
    
    /**
     * Ermittelt den Farbwert zu einem Schnittpunkt mit dem Strahl in einem
     * <code>Intersection</code>-Objekt.
     * 
     * @param intersection Informationen �ber den Schnitt.
     * @return Farbwert am Schnittpunkt mit dem Strahl.
     */
    ColorEx shade(Intersection intersection);
}