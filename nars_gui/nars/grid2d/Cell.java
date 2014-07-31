package nars.grid2d;

public class Cell {

    public float charge = 0;
    public float conductivity = 0.98f;
    public boolean chargeFront = false;
    public float height = 0;

    public Cell() {
        height = 64;
    }
    
    public void setHeightInfinity() {
        height = Float.MAX_VALUE;
    }
    
    public void draw(Grid2DSpace s) {

        //draw ground height
        int r,g,b,a;
        if (height == 0) {
            r = g = b = 0;
            a = 1;
        }
        else if (height == Float.MAX_VALUE) {
            r = g = b = 255;
            a = 255;
        }
        else {
            r = g = b = (int)(128 + height);
            a = 255;
        }
        
        s.fill(r, g, b, a);
        s.rect(0,0,1,1);
        
        if (charge > 0) {
            int points = (int)(charge * 100.0f);
            
            int cr = chargeFront ? 255 : 128;
            int sparkRes = 2;
            float invSparkRes = 1.0f / ((float)sparkRes);
            int ra = nextInt();
            for (int sx = 0; sx < sparkRes; sx++) {
                for (int sy = 0; sy < sparkRes; sy++) {
                    boolean sparkle = (ra & 1) > 0;
                    ra = ra >> 1;
                    if (ra == 0)
                        ra = nextInt();
                    
                    if (sparkle) {
                        s.fill(cr,255,0,(int)(charge*128.0));
                        s.rect( sx * invSparkRes, sy * invSparkRes, invSparkRes, invSparkRes);
                    }
                }
            }
                
            for (int i = 0; i < points; i++) {
                int p = nextInt()%(16*16);
            }
        }
        
        
    }
    
    static long rseed = System.nanoTime();
    
    public static int nextInt()  {
        final int nbits = 32;
        long x = rseed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        rseed = x;
        x &= ((1L << nbits) - 1);
        return (int) x;        
    }

    void setBoundary() {
        setHeightInfinity(); 
    }

    void copyFrom(Cell c) {
        this.height = c.height;
        this.charge = c.charge;
        this.chargeFront = c.chargeFront;
    }

    void setHeight(int h) {
        this.height = h;
    }
            
}
