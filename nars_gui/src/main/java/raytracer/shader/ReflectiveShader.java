/*
 * ReflectiveShader.java                  STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;

/**
 * Dieser Shader stellt eine anteilige Reflextion des Lichtes auf der
 * Objektoberfl�che dar.
 * 
 * @author Mathias Kosch
 *
 */
public class ReflectiveShader implements Shader
{
    /** Statisches <code>MirrorShader</code>-Objekt zum Berechnen des
     * reflektiven Anteils. */
    protected final static MirrorShader mirrorShader = new MirrorShader();
    
    
    /** Shader, der beide Shader verbindet. */
    protected ConcatShader shader = null;
    
    
    /**
     * Erzeugt einen neuen <code>ReflectiveShader</code> mit einem bestimmten
     * Reflexions-Anteil.
     * 
     * @param reflectionRatio Anteil des reflektierten Strahls.
     * @param mainShader Haupt-Shader, auf den die Reflexion angewendet wird.
     */
    public ReflectiveShader(float reflectionRatio, Shader mainShader)
    {
        shader = new ConcatShader(mainShader, reflectionRatio, mirrorShader);
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        return shader.shade(intersection);
    }
}