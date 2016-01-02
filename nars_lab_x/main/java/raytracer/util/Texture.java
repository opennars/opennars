package raytracer.util;

import raytracer.basic.ColorEx;
import raytracer.basic.RaytracerConstants;

import javax.vecmath.Vector2d;
import java.awt.image.BufferedImage;

public class Texture
{
    public static ColorEx getTextureColor(BufferedImage texture, Vector2d location)
    {
        // Dimensionen des Textur-Bildes:
        int imageWidth = texture.getWidth();
        int imageHeight = texture.getHeight();
        
        double locationX = (location.x* (double) imageWidth) % (double) imageWidth;
        if (locationX < 0.0)
            locationX += (double) imageWidth;
        double locationY = (location.y* (double) imageHeight) % (double) imageHeight;
        if (locationY < 0.0)
            locationY += (double) imageHeight;
        
        int x = (int)locationX;
        int y = (int)locationY;
        
        if (RaytracerConstants.SMOOTH_TEXTURES)
        {
            float fracX = (float)(locationX- (double) x);
            float fracY = (float)(locationY- (double) y);
            ColorEx color = new ColorEx();
            
            // Farbwert im Smooth-Modus berechnen. Dabei werden alle Pixel in der
            // Umgebung betrachtet und deren Farbwerte anteilig dazu addiert:
            for (byte j = (byte) -1; (int) j <= 1; j++)
            {
                float factorY = 1.0f - Math.abs(fracY - (j + 0.5f));
                if (factorY < 0.0f)
                    continue;
                y = ((int)locationY+ (int) j) % imageHeight;
                if (y < 0)
                    y += imageHeight;
                
                for (byte i = (byte) -1; (int) i <= 1; i++)
                {
                    float factorX = 1.0f - Math.abs(fracX - (i + 0.5f));
                    if (factorX < 0.0f)
                        continue;
                    x = ((int)locationX+ (int) i) % imageWidth;
                    if (x < 0)
                        x += imageWidth;
                    
                    color.scaleAdd(factorX*factorY, new ColorEx(new Color(texture.getRGB(x, imageHeight-y-1))), color);
                }
            }
            
            return color;
        }
        else
            return new ColorEx(new Color(texture.getRGB(x, imageHeight-y-1)));
    }    
}