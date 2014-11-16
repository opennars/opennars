package automenta.vivisect.face;

/**
 *
 * @author me
 */


   
class VectorN    {   
   
    VectorN(int i)   
    {   
        v = new double[i];   
    }   
   
    double distance(VectorN vectorn)   
    {   
        double d2 = 0.0D;   
        for(int i = 0; i < size(); i++)   
        {   
            double d = vectorn.get(0) - get(0);   
            double d1 = vectorn.get(1) - get(1);   
            d2 += d * d + d1 * d1;   
        }   
   
        return Math.sqrt(d2);   
    }   
   
    double get(int i)   
    {   
        return v[i];   
    }   
   
    void set(int i, double d)   
    {   
        v[i] = d;   
    }   
   
    void set(VectorN vectorn)   
    {   
        for(int i = 0; i < size(); i++)   
            set(i, vectorn.get(i));   
   
    }   
   
    int size()   
    {   
        return v.length;   
    }   
   
    public String toString()   
    {   
        String s = "{";   
        for(int i = 0; i < size(); i++)   
            s = s + (i != 0 ? "," : "") + get(i);   
   
        return s + "}";   
    }   
   
    void transform(MatrixN matrixn)   
    {   
        VectorN vectorn = new VectorN(size());   
        for(int i = 0; i < size(); i++)   
        {   
            double d = 0.0D;   
            for(int j = 0; j < size(); j++)   
                d += matrixn.get(i, j) * get(j);   
   
            vectorn.set(i, d);   
        }   
   
        set(vectorn);   
    }   
   
    private double v[];   
}  

class Vector3D extends VectorN   
{   
   
    Vector3D()   
    {   
        super(4);   
    }   
   
    void set(double d, double d1, double d2)   
    {   
        set(d, d1, d2, 1.0D);   
    }   
   
    void set(double d, double d1, double d2, double d3)   
    {   
        set(0, d);   
        set(1, d1);   
        set(2, d2);   
        set(3, d3);   
    }   
}  
   
public class Matrix3D extends MatrixN   
{   
   
    Matrix3D()   
    {   
        super(4);   
        identity();   
    }   
   
    void rotateX(double d)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        double d1 = Math.cos(d);   
        double d2 = Math.sin(d);   
        matrix3d.set(1, 1, d1);   
        matrix3d.set(1, 2, -d2);   
        matrix3d.set(2, 1, d2);   
        matrix3d.set(2, 2, d1);   
        postMultiply(matrix3d);   
    }   
   
    void rotateY(double d)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        double d1 = Math.cos(d);   
        double d2 = Math.sin(d);   
        matrix3d.set(2, 2, d1);   
        matrix3d.set(2, 0, -d2);   
        matrix3d.set(0, 2, d2);   
        matrix3d.set(0, 0, d1);   
        postMultiply(matrix3d);   
    }   
   
    void rotateZ(double d)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        double d1 = Math.cos(d);   
        double d2 = Math.sin(d);   
        matrix3d.set(0, 0, d1);   
        matrix3d.set(0, 1, -d2);   
        matrix3d.set(1, 0, d2);   
        matrix3d.set(1, 1, d1);   
        postMultiply(matrix3d);   
    }   
   
    void scale(double d)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        matrix3d.set(0, 0, d);   
        matrix3d.set(1, 1, d);   
        matrix3d.set(2, 2, d);   
        postMultiply(matrix3d);   
    }   
   
    void scale(double d, double d1, double d2)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        matrix3d.set(0, 0, d);   
        matrix3d.set(1, 1, d1);   
        matrix3d.set(2, 2, d2);   
        postMultiply(matrix3d);   
    }   
   
    void scale(Vector3D vector3d)   
    {   
        scale(vector3d.get(0), vector3d.get(1), vector3d.get(2));   
    }   
   
    void translate(double d, double d1, double d2)   
    {   
        Matrix3D matrix3d = new Matrix3D();   
        matrix3d.set(0, 3, d);   
        matrix3d.set(1, 3, d1);   
        matrix3d.set(2, 3, d2);   
        postMultiply(matrix3d);   
    }   
   
    void translate(Vector3D vector3d)   
    {   
        translate(vector3d.get(0), vector3d.get(1), vector3d.get(2));   
    }   
}  