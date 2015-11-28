/*
 * ColorShader.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;

/**
 * Dieser Shader liefert einen festgelegten (monotonen) Farbwert zur�ck.
 * 
 * @author Jonas Stahl
 * 
 */
public class ColorShader implements Shader
{
	final ColorEx color;
    
    /**
     * Erzeugt einen neuen <code>ColorShader</code> mit einer bestimmten Farbe.
     * 
     * @param color Farbe des Objekts.
     */
	public ColorShader(ColorEx color)
    {
        this.color = new ColorEx(color);
	}
	
    
	@Override
    public ColorEx shade(Intersection intersection)
    {
        return new ColorEx(this.color);
    }
}