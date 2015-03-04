package raytracer.main;

import javax.vecmath.Matrix3d;
import javax.vecmath.Tuple3d;

public class Matrix3dEx extends Matrix3d
{
    private static final long serialVersionUID = 1L;

    public Matrix3dEx()
    {
        super();
    }
    
    public void transformTransposed(Tuple3d t)
    {
        double x = t.x, y = t.y, z = t.z;
        
        t.x = m00*x+m10*y+m20*z;
        t.y = m01*x+m11*y+m21*z;
        t.z = m02*x+m12*y+m22*z;
    }
}