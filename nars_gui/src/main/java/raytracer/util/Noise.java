package raytracer.util;

import javax.vecmath.Vector3d;

/**
 * Portiert von Michael Hemmer.
 * @author Michael Hemmer
 *
 */
public class Noise
{
    protected static double noise(int x)
    {
        x += 684;
        int prime = 7919;
        for (int i = 0; i < 6; i++)
            x = (x*x) % prime;
        return (double) x /((double) prime -1.0);
    }
    
    protected static double noise(int x, int y)
    {
        x += (int) (684 + (noise(y + 452) * 57));
        int prime = 7919;
        for (int i = 0; i < 6; i++)
            x = (x*x) % prime;
        return (double) x /((double) prime -1.0);
    }

    protected static double noise(int x, int y, int z)
    {
        x += (int) (684 + (noise(y + 452) * 57 + (noise(z - 3214) * 97)));
        int prime = 7919;
        for (int i = 0; i < 6; i++)
            x = (x*x) % prime;
        return (double) x /((double) prime -1.0);
    }
    
    protected static double interpolate(double v0, double v1, double x)
    {     
        double t = (Math.cos(x*Math.PI)+1.0)/2.0;
        return v0*t+(1.0-t)*v1;
    }
    
    protected static double dInterpolate(double v0, double v1, double x)
    {     
        double dt= -Math.sin(x*Math.PI)/2.0;
        return v0*dt-dt*v1;
    }

    private static double rest(double x)
    {
        double r = x- (double) (int) x;
        return (r < 0.0) ? r+ 1.0 : r;
    }

    private static int blub_pos(double x, int frequency)
    {
        double r = rest(x)* (double) frequency;
        return (int)r;    
    }
    
    private static double blub_inter(double x, int frequency)
    {
        double r = rest(x)* (double) frequency;
        return r- (double) (int) r;
    }   

    public static double noise_1(double x, int frequency)
    {
        int pos_x = blub_pos(x, frequency);
        double d_x = blub_inter(x, frequency);

        double v0 = noise(pos_x % frequency);
        double v1 = noise(pos_x + 1 % frequency);

        return interpolate(v0,v1,d_x);
    }


    public static double noise_2(double x, double y, int frequency)
    {
        int pos_x = blub_pos(x, frequency);
        double d_x = blub_inter(x, frequency);
        int pos_y = blub_pos(y, frequency);
        double d_y = blub_inter(y, frequency);

        double vv[][] = new double[2][2];
        int i;
        for (i=0;i<2; i++)
        {
            for (int j = 0; j<2; j++)
            {
                vv[i][j] = noise((pos_x+i)%frequency,(pos_y+j)%frequency);   // noise is continouse
            }
        }
        double[] v = new double[2];
        for ( i=0;i<2; i++)
        {
            v[i] =interpolate(vv[i][0],vv[i][1],d_y);
        }
        return interpolate(v[0],v[1],d_x);
    }

    public static double noise_3(double x, double y, double z, int frequency)
    {
        int pos_x = blub_pos(x, frequency);
        double d_x = blub_inter(x, frequency);
        int pos_y = blub_pos(y, frequency);
        double d_y = blub_inter(y, frequency);
        int pos_z = blub_pos(z, frequency);
        double d_z = blub_inter(z, frequency);

        double vvv[][][] = new double[2][2][2];

        int j;
        int i;
        for (i=0;i<2; i++){
            for (j=0;j<2; j++){
                for (int k = 0; k<2; k++){
                    vvv[i][j][k] = noise(((pos_x+i))%frequency,((pos_y+j))%frequency,((pos_z+k))%frequency);    // noise is continouse
                    }
                }
            }

        double[] v = new double[2];
        double[][] vv = new double[2][2];
        for ( i=0;i<2; i++){
            for (j=0;j<2; j++){
                vv[i][j]=interpolate(vvv[i][j][0],vvv[i][j][1],d_z);
                }
            v[i] =interpolate(vv[i][0],vv[i][1],d_y);
            }
        return interpolate(v[0],v[1],d_x);
    }

        /*! return value in [0,1]
        *  computes a ostensible random C^1 function
        *  Hz ist the frequency of the function
        *  it also computes the gradient vector of the funtion
        *  gradient = Vec3f(Dnoise/dx,Dnoise/dy,Dnoise/dz);
    */
    public static double noise_1(double x, int frequency, Vector3d gradient)
    { 
        // this is very efficient programming :-) 
        double r = noise_3(x, 0.0, 0.0, frequency, gradient);
        gradient.y= 0.0;
        gradient.z= 0.0;
        return r;
    }
        
    public static double noise_2(double x, double y, int frequency, Vector3d gradient)
    {
        double r = noise_3(x, y, 0.0, frequency, gradient);
        gradient.z= 0.0;
        return r;
    }
    
    public static double noise_3(double x, double y, double z, int frequency, Vector3d gradient)
    {

        int pos_x = blub_pos(x, frequency);
        double d_x = blub_inter(x, frequency);
        int pos_y = blub_pos(y, frequency);
        double d_y = blub_inter(y, frequency);
        int pos_z = blub_pos(z, frequency);
        double d_z = blub_inter(z, frequency);

        double vvv[][][] = new double[2][2][2];


        int i,j;
        for ( i=0;i<2; i++){
            for ( j=0;j<2; j++){
                for (int k = 0; k<2; k++){
                    vvv[i][j][k] = noise(((pos_x+i))%frequency,((pos_y+j))%frequency,((pos_z+k))%frequency);    // noise is continouse
                    }
                }
            }

        double[] v = new double[2];
        double[][] vv = new double[2][2];
        for ( i=0;i<2; i++){
            for ( j=0;j<2; j++){
                vv[i][j]=interpolate(vvv[i][j][0],vvv[i][j][1],d_z);
                }
            v[i] =interpolate(vv[i][0],vv[i][1],d_y);
            }
        gradient.x=dInterpolate(v[0],v[1],d_x);
        
        for ( i=0;i<2; i++){
            for ( j=0;j<2; j++){
                vv[i][j]=interpolate(vvv[i][j][0],vvv[i][j][1],d_z);
                }
            v[i] =dInterpolate(vv[i][0],vv[i][1],d_y);
            }
        gradient.y=interpolate(v[0],v[1],d_x);
        
        for ( i=0;i<2; i++){
            for ( j=0;j<2; j++){
                vv[i][j]=dInterpolate(vvv[i][j][0],vvv[i][j][1],d_z);
                }
            v[i] =interpolate(vv[i][0],vv[i][1],d_y);
            }
        gradient.z=interpolate(v[0],v[1],d_x);
        
        for ( i=0;i<2; i++){
            for ( j=0;j<2; j++){
                vv[i][j]=interpolate(vvv[i][j][0],vvv[i][j][1],d_z);
                }
            v[i] =interpolate(vv[i][0],vv[i][1],d_y);
            }
        return interpolate(v[0],v[1],d_x);
    }

    public static double perlin_noise_1(double x, int s, int[] w, int hz)
    {
        double N= 0.0; //return value
        int W=0;    //sum of w
        for(int i = 0; i < s ; i++)
        {
            N+=w[i]*noise_1(x,hz);
            hz*=2;
            W+=w[i];
        }
        return N/ (double) W;
    }
        
    public static double perlin_noise_2(double x, double y, int s, int[] w, int hz)
    {
        //  std::cout << "H"<<std::endl;
        
        double N= 0.0; //return value
        int W=0;    //sum of w
        for(int i=0 ; i < s ; i++){
            
            N+=w[i]*noise_2(x,y,hz);
            hz*=2;
            W+=w[i];
            }
        
        return N/ (double) W;
    }
    
    public static double perlin_noise_3(double x, double y, double z, int s, int[] w, int hz)
    {
        double N= 0.0; //return value
        int W=0;    //sum of w
        for (int i=0; i < s ; i++)
        {
            N+=w[i]*noise_3(x,y,z,hz);
            hz*=2;
            W+=w[i];
        }
        return N/ (double) W;
    }

    public static double perlin_noise_1(double x, int s, int hz){
        double N= 0.0; //return value
        double W= 0.0; //sum of w
        double w = Math.pow(2.0, (double) s);
        for (int i=0; i < s ; i++)
        {
            N+=w*noise_1(x,hz);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        return N/W;
    }
        
    public static double perlin_noise_2(double x, double y, int s, int hz)
    {
        double N= 0.0; //return value
        double W= 0.0; //sum of w
        double w = Math.pow(2.0, (double) s);
        for(int i=0; i < s ; i++)
        {
            N+=w*noise_2(x,y,hz);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        return N/W;
    }
    
    public static double perlin_noise_3(double x, double y, double z, int s, int hz)
    {
        double N= 0.0; //return value
        double W= 0.0; //sum of w
        double w = Math.pow(2.0, (double) s);
        for (int i=0; i < s ; i++)
        {
            N+=w*noise_3(x,y,z,hz);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        return N/W;
    }

    public static double perlin_noise_1(double x, int s, int hz, Vector3d gradient)
    {
        double w = Math.pow(2.0, (double) s);
        Vector3d g = new Vector3d();
        
        gradient.x = 0.0; gradient.y = 0.0; gradient.z = 0.0;
        double W = 0; //sum of w
        double N = 0; //return value
        for (int i=0; i < s ; i++)
        {
            N+=w*noise_1(x,hz,g);
            gradient.scaleAdd(w, g, gradient);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        gradient.scale(1.0/W);
        return N/W;
    }
        
    public static double perlin_noise_2(double x, double y, int s, int hz, Vector3d gradient)
    {
        double w = Math.pow(2.0, (double) s);
        Vector3d g = new Vector3d();
        
        gradient.x = 0.0; gradient.y = 0.0; gradient.z = 0.0;
        double W = 0; //sum of w
        double N = 0; //return value
        for(int i=0; i < s ; i++)
        {
            N+=w*noise_2(x,y,hz,g);
            gradient.scaleAdd(w, g, gradient);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        gradient.scale(1.0/W);
        return N/W;
    }
    
    public static double perlin_noise_3(double x, double y, double z, int s, int hz, Vector3d gradient)
    {
        double w = Math.pow(2.0, (double) s);
        Vector3d g = new Vector3d();
        
        gradient.x = 0.0; gradient.y = 0.0; gradient.z = 0.0;
        double W = 0; //sum of w
        double N = 0; //return value
        for (int i=0; i < s ; i++)
        {
            N+=w*noise_3(x,y+noise(2*hz),z+noise(hz),hz,g);
            gradient.scaleAdd(w, g, gradient);
            hz*=2;
            W+=w;
            w/= 2.0;
        }
        gradient.scale(1.0/W);
        return N/W;
    }
}