package automenta.vivisect.face;

/**
 *
 * @author me
 */



   
import java.util.Random;   
   
public final class ImprovMath   
{   
   
    private ImprovMath()   
    {   
    }   
   
    public static double angleBetween(double d, double d1, double d2, double d3,    
            double d4, double d5)   
    {   
        double d6 = d2 - d;   
        double d7 = d3 - d1;   
        double d8 = d4 - d;   
        double d9 = d5 - d1;   
        return (Math.acos(dot(d6, d7, d8, d9) / (magnitude(d6, d7) * magnitude(d8, d9))) / 3.1415926535897931D) * 180D;   
    }   
   
    public static double bias(double d, double d1)   
    {   
        if(d < 0.001D)   
            return 0.0D;   
        if(d > 0.999D)   
            return 1.0D;   
        if(d1 < 0.001D)   
            return 0.0D;   
        if(d1 > 0.999D)   
            return 1.0D;   
        else   
            return Math.pow(d, Math.log(d1) / LOG_HALF);   
    }   
   
    public static double[] cross(double ad[], double ad1[])   
    {   
        double ad2[] = new double[3];   
        ad2[0] = ad[1] * ad1[2] - ad[2] * ad1[1];   
        ad2[1] = ad[2] * ad1[0] - ad[0] * ad1[2];   
        ad2[2] = ad[0] * ad1[1] - ad[1] * ad1[0];   
        return ad2;   
    }   
   
    public static double dot(double d, double d1)   
    {   
        return d * d1;   
    }   
   
    public static double dot(double d, double d1, double d2, double d3)   
    {   
        return d * d2 + d1 * d3;   
    }   
   
    public static double dot(double d, double d1, double d2, double d3,    
            double d4, double d5)   
    {   
        return d * d3 + d1 * d4 + d2 * d5;   
    }   
   
    public static double dot(double ad[], double ad1[])   
    {   
        double d = 0.0D;   
        int i;   
        if(ad.length <= ad1.length)   
            i = ad.length;   
        else   
            i = ad1.length;   
        for(int j = 0; j < i; j++)   
            d += ad[j] * ad1[j];   
   
        return d;   
    }   
   
    public static String doubleToString(double d, int i)   
    {   
        double d1 = d - (double)(long)d;   
        if(d1 == 0.0D)   
            return String.valueOf((long)d);   
        boolean flag = false;   
        if(d < 0.0D)   
        {   
            flag = true;   
            d = -d;   
        }   
        double d2 = Math.pow(10D, i);   
        d *= d2;   
        if(d - (double)(long)d >= 0.49998999999999999D)   
            d++;   
        d /= d2;   
        String s = String.valueOf(d);   
        int j = s.indexOf('.');   
        if(j != -1)   
        {   
            int k = j + i + 1;   
            if(k < s.length())   
                s = s.substring(0, k);   
        }   
        int l = s.length() - 1;   
        boolean flag1 = false;   
        for(; l > 0 && s.charAt(l) == '0' && s.charAt(l - 1) != '.'; l--)   
            flag1 = true;   
   
        if(flag1)   
            s = s.substring(0, l);   
        return (flag ? "-" : "") + s;   
    }   
   
    public static double gain(double d, double d1)   
    {   
        if(d < 0.001D)   
            return 0.0D;   
        if(d > 0.999D)   
            return 1.0D;   
        d1 = d1 >= 0.001D ? d1 <= 0.999D ? d1 : 0.999D : 0.0001D;   
        double d2 = Math.log(1.0D - d1) / LOG_HALF;   
        if(d < 0.5D)   
            return Math.pow(2D * d, d2) / 2D;   
        else   
            return 1.0D - Math.pow(2D * (1.0D - d), d2) / 2D;   
    }   
   
    public static double[] getEulers(double ad[], int i)   
    {   
        double ad1[] = new double[3];   
        double ad2[] = new double[4];   
        double ad3[][] = new double[3][3];   
        byte byte0 = 0;   
        byte byte1 = 1;   
        byte byte2 = 2;   
        switch(i)   
        {   
        case 0: // '\0'   
            byte0 = 0;   
            byte1 = 1;   
            byte2 = 2;   
            break;   
   
        case 1: // '\001'   
            byte0 = 0;   
            byte1 = 2;   
            byte2 = 1;   
            break;   
   
        case 2: // '\002'   
            byte0 = 1;   
            byte1 = 0;   
            byte2 = 2;   
            break;   
   
        case 3: // '\003'   
            byte0 = 1;   
            byte1 = 2;   
            byte2 = 0;   
            break;   
   
        case 4: // '\004'   
            byte0 = 2;   
            byte1 = 0;   
            byte2 = 1;   
            break;   
   
        case 5: // '\005'   
            byte0 = 2;   
            byte1 = 1;   
            byte2 = 0;   
            break;   
        }   
        ad2[0] = ad[0];   
        ad2[1] = ad[1];   
        ad2[2] = ad[2];   
        ad2[3] = ad[3];   
        if(ad2[0] == 0.0D && ad2[1] == 0.0D && ad2[2] == 0.0D)   
            return ad1;   
        double d1 = ad2[0] * ad2[0] + ad2[1] * ad2[1] + ad2[2] * ad2[2];   
        if(d1 < 0.0001D)   
        {   
            return ad1;   
        } else   
        {   
            double d3 = 1.0D / Math.sqrt(d1);   
            ad2[0] *= d3;   
            ad2[1] *= d3;   
            ad2[2] *= d3;   
            double d = Math.sin(ad2[3] * 0.5D);   
            ad2[0] *= d;   
            ad2[1] *= d;   
            ad2[2] *= d;   
            ad2[3] = Math.cos(ad2[3] * 0.5D);   
            double d2 = ad2[0] * ad2[0] + ad2[1] * ad2[1] + ad2[2] * ad2[2] + ad2[3] * ad2[3];   
            double d4 = 2D / d2;   
            double d5 = ad2[0] * d4;   
            double d6 = ad2[1] * d4;   
            double d7 = ad2[2] * d4;   
            double d8 = ad2[3] * d5;   
            double d9 = ad2[3] * d6;   
            double d10 = ad2[3] * d7;   
            double d11 = ad2[0] * d5;   
            double d12 = ad2[0] * d6;   
            double d13 = ad2[0] * d7;   
            double d14 = ad2[1] * d6;   
            double d15 = ad2[1] * d7;   
            double d16 = ad2[2] * d7;   
            ad3[0][0] = 1.0D - d14 - d16;   
            ad3[0][1] = d12 - d10;   
            ad3[0][2] = d13 + d9;   
            ad3[1][0] = d12 + d10;   
            ad3[1][1] = 1.0D - d11 - d16;   
            ad3[1][2] = d15 - d8;   
            ad3[2][0] = d13 - d9;   
            ad3[2][1] = d15 + d8;   
            ad3[2][2] = 1.0D - d11 - d14;   
            int j = byte1 % 3 <= byte0 % 3 ? -1 : 1;   
            double d17;   
            double d18;   
            ad1[byte0] = Math.atan2(d18 = ad3[byte1][byte2] * (double)(-j), d17 = ad3[byte2][byte2]);   
            ad1[byte1] = Math.atan2(ad3[byte0][byte2] * (double)j, Math.sqrt(d17 * d17 + d18 * d18));   
            ad1[byte2] = Math.atan2(ad3[byte0][byte1] * (double)(-j), ad3[byte0][byte0]);   
            ad1[0] /= 0.017453292519943295D;   
            ad1[1] /= 0.017453292519943295D;   
            ad1[2] /= 0.017453292519943295D;   
            return ad1;   
        }   
    }   
   
    public static double[] getEulers(float af[], int i)   
    {   
        double ad[] = {   
            (double)af[0], (double)af[1], (double)af[2], (double)af[3]   
        };   
        return getEulers(ad, i);   
    }   
   
    public static float[] getQuaternion(double ad[])   
    {   
        return getQuaternion(ad, 0);   
    }   
   
    public static float[] getQuaternion(double ad[], int i)   
    {   
        double ad1[][] = new double[4][4];   
        float af[] = new float[4];   
        return getQuaternion(ad, i, ad1, af);   
    }   
   
    public static float[] getQuaternion(double ad[], int i, double ad1[][], float af[])   
    {   
        double ad2[][] = ad1;   
        double ad3[] = ad2[0];   
        double ad4[] = ad2[1];   
        double ad5[] = ad2[2];   
        double ad6[] = ad2[3];   
        ad3[0] = 1.0D;   
        ad3[1] = 0.0D;   
        ad3[2] = 0.0D;   
        ad3[3] = ad[0] * 0.017453292519943295D;   
        ad4[0] = 0.0D;   
        ad4[1] = 1.0D;   
        ad4[2] = 0.0D;   
        ad4[3] = ad[1] * 0.017453292519943295D;   
        ad5[0] = 0.0D;   
        ad5[1] = 0.0D;   
        ad5[2] = 1.0D;   
        ad5[3] = ad[2] * 0.017453292519943295D;   
        byte byte0;   
        byte byte1;   
        byte byte2;   
        switch(i)   
        {   
        case 1: // '\001'   
            byte0 = 0;   
            byte1 = 2;   
            byte2 = 1;   
            break;   
   
        case 2: // '\002'   
            byte0 = 1;   
            byte1 = 0;   
            byte2 = 2;   
            break;   
   
        case 3: // '\003'   
            byte0 = 1;   
            byte1 = 2;   
            byte2 = 0;   
            break;   
   
        case 4: // '\004'   
            byte0 = 2;   
            byte1 = 0;   
            byte2 = 1;   
            break;   
   
        case 5: // '\005'   
            byte0 = 2;   
            byte1 = 1;   
            byte2 = 0;   
            break;   
   
        default:   
            byte0 = 0;   
            byte1 = 1;   
            byte2 = 2;   
            break;   
        }   
        ad2[byte0] = ad3;   
        ad2[byte1] = ad4;   
        ad2[byte2] = ad5;   
        prepAngles(ad2[0]);   
        prepAngles(ad2[1]);   
        prepAngles(ad2[2]);   
        mult(ad2[2], ad2[1], ad6);   
        mult(ad6, ad2[0], ad2[2]);   
        double ad7[] = ad2[2];   
        double d = ad7[0];   
        double d1 = ad7[1];   
        double d2 = ad7[2];   
        double d3 = ad7[3];   
        double d4 = Math.sqrt(d * d + d1 * d1 + d2 * d2);   
        if(d4 > 0.0D)   
        {   
            af[0] = (float)(d * (1.0D / d4));   
            af[1] = (float)(d1 * (1.0D / d4));   
            af[2] = (float)(d2 * (1.0D / d4));   
            af[3] = 2.0F * (float)Math.acos(d3);   
        } else   
        {   
            af[0] = 0.0F;   
            af[1] = 1.0F;   
            af[2] = 0.0F;   
            af[3] = 0.0F;   
        }   
        return af;   
    }   
   
    private static void init()   
    {   
        Random random1 = new Random();   
        int i;   
        for(i = 0; i < 256; i++)   
        {   
            p[i] = i;   
            double d = (double)(random1.nextLong() & 255L) / 256D;   
            g1[i] = 2D * d - 1.0D;   
            for(int k = 0; k < 2; k++)   
                g2[i][k] = (double)(random1.nextLong() % 512L - 256L) / 256D;   
   
            normalize2(g2[i]);   
            for(int l = 0; l < 3; l++)   
                g3[i][l] = (double)(random1.nextLong() % 512L - 256L) / 256D;   
   
            normalize3(g3[i]);   
        }   
   
        while(--i > 0)    
        {   
            int l1 = p[i];   
            int i1 = (int)(random1.nextLong() & 255L);   
            p[i] = p[i1];   
            p[i1] = l1;   
        }   
        for(int j = 0; j < 258; j++)   
        {   
            p[256 + j] = p[j];   
            g1[256 + j] = g1[j];   
            System.arraycopy(g2[j], 0, g2[256 + j], 0, 2);
            System.arraycopy(g3[j], 0, g3[256 + j], 0, 3);   
   
        }   
   
    }   
   
    public static double lerp(double d, double d1, double d2)   
    {   
        return d1 + d * (d2 - d1);   
    }   
   
    public static double magnitude(double d, double d1)   
    {   
        return Math.sqrt(dot(d, d1, d, d1));   
    }   
   
    public static double magnitude(double ad[])   
    {   
        return Math.sqrt(dot(ad, ad));   
    }   
   
    private static void mult(double ad[], double ad1[], double ad2[])   
    {   
        double d = ad[0];   
        double d1 = ad[1];   
        double d2 = ad[2];   
        double d3 = ad[3];   
        double d4 = ad1[0];   
        double d5 = ad1[1];   
        double d6 = ad1[2];   
        double d7 = ad1[3];   
        double d8 = (d7 * d + d4 * d3 + d5 * d2) - d6 * d1;   
        double d9 = (d7 * d1 + d5 * d3 + d6 * d) - d4 * d2;   
        double d10 = (d7 * d2 + d6 * d3 + d4 * d1) - d5 * d;   
        double d11 = d7 * d3 - d4 * d - d5 * d1 - d6 * d2;   
        d8 *= 1.0D / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);   
        d9 *= 1.0D / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);   
        d10 *= 1.0D / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);   
        d11 *= 1.0D / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);   
        ad2[0] = d8;   
        ad2[1] = d9;   
        ad2[2] = d10;   
        ad2[3] = d11;   
    }   
   
    private static double[] multBy(double ad[], double d)   
    {   
        double ad1[] = ad;   
        ad1[0] *= d;   
        ad1[1] *= d;   
        ad1[2] *= d;   
        return ad1;   
    }   
   
    public static double noise(double d)   
    {   
        double d7 = d;   
        if(start == 1)   
        {   
            start = 0;   
            init();   
        }   
        double d4 = d7 + 4096D;   
        int i = (int)d4 & 0xff;   
        int j = i + 1 & 0xff;   
        double d1 = d4 - (double)(int)d4;   
        double d2 = d1 - 1.0D;   
        double d3 = s_curve(d1);   
        double d5 = d1 * g1[p[i]];   
        double d6 = d2 * g1[p[j]];   
        return lerp(d3, d5, d6);   
    }   
   
    private static double[] norm(double ad[])   
    {   
        double ad1[] = new double[4];   
        ad1[0] = ad[0];   
        ad1[1] = ad[1];   
        ad1[2] = ad[2];   
        ad1[3] = ad[3];   
        double d = Math.sqrt(ad1[0] * ad1[0] + ad1[1] * ad1[1] + ad1[2] * ad1[2]);   
        if(d != 0.0D)   
        {   
            ad1[0] *= d;   
            ad1[1] *= d;   
            ad1[2] *= d;   
        } else   
        {   
            ad1[0] = 0.0D;   
            ad1[1] = 0.0D;   
            ad1[2] = 0.0D;   
        }   
        return ad1;   
    }   
   
    private static void normalize2(double ad[])   
    {   
        double d = Math.sqrt(ad[0] * ad[0] + ad[1] * ad[1]);   
        ad[0] /= d;   
        ad[1] /= d;   
    }   
   
    private static void normalize3(double ad[])   
    {   
        double d = Math.sqrt(ad[0] * ad[0] + ad[1] * ad[1] + ad[2] * ad[2]);   
        ad[0] /= d;   
        ad[1] /= d;   
        ad[2] /= d;   
    }   
   
    private static void prepAngles(double ad[])   
    {   
        norm(ad);   
        multBy(ad, Math.sin(ad[3] / 2D));   
        ad[3] = Math.cos(ad[3] / 2D);   
    }   
   
    public static double random(double d, double d1)   
    {   
        return Math.min(d, d1) + Math.random() * Math.abs(d1 - d);   
    }   
   
    private static double s_curve(double d)   
    {   
        return d * d * (3D - 2D * d);   
    }   
   
    private static int p[] = new int[514];   
    private static double g3[][] = new double[514][3];   
    private static double g2[][] = new double[514][2];   
    private static double g1[] = new double[514];   
    private static int start = 1;   
    private static final double LOG_HALF = Math.log(0.5D);   
    public static final int XYZ = 0;   
    public static final int XZY = 1;   
    public static final int YXZ = 2;   
    public static final int YZX = 3;   
    public static final int ZXY = 4;   
    public static final int ZYX = 5;   
   
    static    
    {   
        init();   
    }   
}  