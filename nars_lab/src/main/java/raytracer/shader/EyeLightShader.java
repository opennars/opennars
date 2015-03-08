package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;

/**
 * 
 * @author Jonas Stahl
 *
 */
public class EyeLightShader implements Shader
{
    /** Haupt-Shader, der die Farbwerte liefert. */
    protected final Shader mainShader;
	
    
    /**
     * Erzeugt einen neuen <code>EyeLightShader</code> zu einem bestimmten Shader.
     * 
     * @param mainShader Haupt-Shader, der die Farbwerte liefert.
     */
    public EyeLightShader(Shader mainShader)
    {
        this.mainShader = mainShader;
    }

    
	@Override
    public ColorEx shade(Intersection intersection)
    {
        // Farbe bestimmen:
        float ratio = (float)(Math.abs(intersection.ray.dir.dot(intersection.getNormal()))/
                (intersection.ray.dir.length()*intersection.getNormal().length()));
        
        float savedWeight = intersection.ray.weight;
        intersection.ray.weight *= ratio;
        ColorEx color = mainShader.shade(intersection);
        intersection.ray.weight = savedWeight;
        
        color.scale(ratio);
		return color;
	}
}