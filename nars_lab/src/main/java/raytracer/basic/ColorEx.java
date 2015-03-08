package raytracer.basic;

import javax.vecmath.Color3f;
import javax.vecmath.Tuple3f;

public class ColorEx extends Color3f
{
    public final static ColorEx BLACK = new ColorEx(0.0f, 0.0f, 0.0f);
    public final static ColorEx BLUE = new ColorEx(0.0f, 0.0f, 1.0f);
    public final static ColorEx CYAN = new ColorEx(0.0f, 1.0f, 1.0f);
    public final static ColorEx DARK_GRAY = new ColorEx(0.25f, 0.25f, 0.25f);
    public final static ColorEx GRAY = new ColorEx(0.5f, 0.5f, 0.5f);
    public final static ColorEx GREEN = new ColorEx(0.0f, 1.0f, 0.0f);
    public final static ColorEx LIGHT_GRAY = new ColorEx(0.75f, 0.75f, 0.75f);
    public final static ColorEx MAGENTA = new ColorEx(1.0f, 0.0f, 1.0f);
    public final static ColorEx ORANGE = new ColorEx(1.0f, 0.75f, 0.0f);
    public final static ColorEx PINK = new ColorEx(1.0f, 0.7f, 0.7f);
    public final static ColorEx RED = new ColorEx(1.0f, 0.0f, 0.0f);
    public final static ColorEx WHITE = new ColorEx(1.0f, 1.0f, 1.0f);
    public final static ColorEx YELLOW = new ColorEx(1.0f, 1.0f, 0.0f);
    

    public ColorEx()
    {
        super();
    }
    
    public ColorEx(float x, float y, float z)
    {
        super(x, y, z);
    }
    
    public ColorEx(float... color)
    {
        super(color);
    }
    
    public ColorEx(java.awt.Color color)
    {
        super(color);
    }
    
    
    public ColorEx(ColorEx color)
    {
        super(color);
    }
    
    
    public void mul(Tuple3f factor)
    {
        x *= factor.x;
        y *= factor.y;
        z *= factor.z;
    }
    
    public void mul(float factor1, Tuple3f factor2)
    {
        x = factor1*factor2.x;
        y = factor1*factor2.y;
        z = factor1*factor2.z;
    }
    
    public void mul(Tuple3f factor1, Tuple3f factor2)
    {
        x = factor1.x*factor2.x;
        y = factor1.y*factor2.y;
        z = factor1.z*factor2.z;
    }
    
    public void mul2Add(Tuple3f factor1, Tuple3f factor2)
    {
        x += factor1.x*factor2.x;
        y += factor1.y*factor2.y;
        z += factor1.z*factor2.z;
    }
    
    public void mul2Add(float factor, Tuple3f factor1, Tuple3f factor2)
    {
        x += factor*factor1.x*factor2.x;
        y += factor*factor1.y*factor2.y;
        z += factor*factor1.z*factor2.z;
    }
    
    public void mul3Add(Tuple3f factor1, Tuple3f factor2, Tuple3f factor3)
    {
        x += factor1.x*factor2.x*factor3.x;
        y += factor1.y*factor2.y*factor3.y;
        z += factor1.z*factor2.z*factor3.z;
    }
    
    public void mul3Add(float factor, Tuple3f factor1, Tuple3f factor2, Tuple3f factor3)
    {
        x += factor*factor1.x*factor2.x*factor3.x;
        y += factor*factor1.y*factor2.y*factor3.y;
        z += factor*factor1.z*factor2.z*factor3.z;
    }
    
    public void mulPow(Tuple3f base, float exponent)
    {
        x *= (float) Math.pow((double) base.x, (double) exponent);
        y *= (float) Math.pow((double) base.y, (double) exponent);
        z *= (float) Math.pow((double) base.z, (double) exponent);
    }
}