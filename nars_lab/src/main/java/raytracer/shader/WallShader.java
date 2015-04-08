/*
 * ColorShader.java                       STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.util.Noise;

import javax.vecmath.Vector3d;

/**
 * Dieser Shader liefert einen festgelegten (monotonen) Farbwert zur�ck.
 * 
 * @author Mathias Kosch
 * 
 */
public class WallShader implements Shader
{
	final ColorEx color;
    final float factor = 0.4f;
    
    
    /**
     * Erzeugt einen neuen <code>ColorShader</code> mit einer bestimmten Farbe.
     * 
     * @param color Farbe des Objekts.
     */
	public WallShader(ColorEx color)
    {
        this.color = new ColorEx(color);
	}
	
    
	@Override
    public ColorEx shade(Intersection intersection)
    {
        Vector3d point = intersection.getPoint();
        float f1 = (float)Noise.perlin_noise_3(point.x*1.03, point.y*0.93, point.z, 7, 3);
        float f2 = (float)Noise.perlin_noise_3(point.x*1.9, point.y*1.07, point.z*0.9, 7, 4);
        float f3 = (float)Noise.perlin_noise_3(point.x*1.1, point.y*1.3, point.z*0.85, 9, 10);

        ColorEx color = new ColorEx(this.color);
        
        
        double factor = (double) (f1 * f2 * f3);
        
        
        color.x -= (float) ((1.0f - factor) * this.factor);
        color.y -= (float) ((1.0f - factor) * this.factor);
        color.z -= (float) ((1.0f - factor) * this.factor);
        
        color.clampMin(0.0f);
        color.clampMax(1.0f);
        return color;
    }
}