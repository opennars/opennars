/*
 * TextureShader.java                     STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.shader;

import raytracer.basic.ColorEx;
import raytracer.basic.Intersection;
import raytracer.util.Texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 
 * 
 * Das Texturbild erh�lt die Koordinaten von <code>(0, 0)</code> bis
 * <code>(1, 1)</code>. Danach wird das Bild wiederholt.
 * 
 * @author Mathias Kosch
 * 
 */
public class TextureShader implements Shader
{
    /** Bilddaten der Textur. */
    BufferedImage texture = null;
    

    public TextureShader(String resourcePath) throws IOException {
        this(ImageIO.read(TextureShader.class.getClassLoader().getResourceAsStream(resourcePath)));
    }
    /**
     * Erzeugt einen neuen <code>TextureShader</code> aus einer Textur.
     * 
     * @param texture Textur-Datei.
     */
    public TextureShader(BufferedImage texture)
    {
        this.texture = texture;
    }
    
    
    @Override
    public ColorEx shade(Intersection intersection)
    {
        // Farbwert der Textur zur�ckgeben:
        return Texture.getTextureColor(texture,
                intersection.shape.getTextureCoords(intersection.getPoint()));
    }
}