package nars.gui.output.face;

import java.awt.Color;   
import java.awt.Graphics;   
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import java.util.Vector;
   
public class FaceGUI extends BaseClass   
{   
    private static final long serialVersionUID = 1L;   
    GraphAppFrame f;   
    boolean firstVertices;   
    boolean isVectors;   
    boolean addNoise;   
    boolean doShade;   
    boolean shiftAxis;   
    int nVertices;   
    int nShapes;   
    int nFlexes;   
    int keyFlex[];   
    double keyValue[];   
    double flexValue[][];   
    double flexTarget[][];   
    double jitterAmpl[];   
    double jitterFreq[];   
    boolean firstTime;   
    boolean kludge;   
    final int POLYGON = 0;   
    final int POLYLINE = 1;   
    final int CIRCLE = 2;   
    double flexData[][][];   
    int flexShape[][];   
    int flexSymmetry[];   
    double pts[][][];   
    int shape[][][];   
    double vertexArray[][];   
    double t;   
    double blinkValue;   
    int spin;   
    int noRotateIndex;   
    boolean leftArrow;   
    boolean rightArrow;   
    int prevKey;   
    long lastTime;   
    int frameCount;   
    int dispCount;   
    Vector colorVector;   
    Vector flexNamesVector;   
    Vector flexVector;   
    Vector flx;   
    Vector ixyz;   
    Vector flexSymmetryVector;   
    Vector shapesVector;   
    Vector shapeVector;   
    Vector face;   
    Vector typeVector;   
    Vector vertexVector;   
    
    public FaceGUI()   
    {   
        super();
        firstVertices = true;   
        isVectors = false;   
        doShade = true;   
        addNoise = true;   
        shiftAxis = false;   
        keyFlex = new int[256];   
        keyValue = new double[256];   
        firstTime = true;   
        kludge = true;   
        noRotateIndex = 1000;   
        leftArrow = false;   
        rightArrow = false;   
        prevKey = -1;   
        colorVector = new Vector();   
        flexNamesVector = new Vector();   
        flexVector = new Vector();   
        flexSymmetryVector = new Vector();   
        shapesVector = new Vector();   
        typeVector = new Vector();   
        vertexVector = new Vector();   
        setBackground(Color.white);   
        lastTime = System.currentTimeMillis();   
        frameCount = 0;   
        dispCount = 0;   
        
	    vertices("14,99,0");

vertices("27,85,0        30,70,0    15,30,-12  19,31.5,10  6.1,16.8,44.5"); //  1- 5 face
vertices("5,70,44        12,72,42   22,68,35    12,70,42  5,68,44.5");    //  6-10 brows

vertices("6,60,43.7      12,62,44   19,60,38    12,58,43  2,45,52");      // 11-15 eyes/nose
vertices("5,44,47        1,42.5,49  4,38,50     8,30.2,46 3,23.5,46");    // 16-20 nose/lips

vertices("2.1,35,48      5,31,47    1.5,29,47   12,60,37  14.1,62.1,37"); // 21-25 mouth/eyeballs
vertices("13.15,61.15,37 7,34,44    7,29.2,42   4,47,48   26.5,55,14");   // 26-30 teeth,nostrils,cheeks,hair

vertices("28,30,0        12,95,27    22,80,24  12,99,13  16,36,33");     // 31-35 forehead,face,headtop
vertices("2,48.5,52.5    5,20.7,46.5 3,23,45  15,36,39  22,51,37");     // 36-40 misc

vertices("13.6,26,38     10,84,34    6,22,20    22,35,15  12,97,-5");     // 41-45
vertices("15,91,-17      18,80,-26   16,60,-29  10,40,-23 30,52,-4");     // 46-50

vertices("16,86,39     20,-40,-23  23.3,51.3,7   26.5,50.7,7 23,59,9"); // 51-55
vertices("26,61,7      27.6,59.5,7");                                     // 56-57
noRotate();
vertices("13,50,0      16,16,0 40,7,0 40,0,0"); // 58-61 neck,shoulders

polygons (.1,0,0, "58,59 59,60 60,61");
polygons (0,0,0, "15,16,17");
polygons(0,0,0, "39,41");
polygons(.93,.56,.56, "11,29,36");
polygons (.66,.36,.36, "15,29,16");
polygons (0,0,0, "3,49 49,48 48,47 47,46 46,45 1,45,46 1,46,47 50,47,48 48,49,3 30,3,4");
polygons (0,0,0, "34,32 34,1,33,32 1,30,33 45,34 34,45,1 50,1,47 50,48,3 50,3,30 50,30,1");
polygons (0,0,0, "32,51 51,42 32,33,51 51,33,42");
circles  (.93,.56,.56, "53,54");
polygons (.93,.56,.56, "54,53,55,56,57");
polygons (.82,.48,.48, "54,53,55,57");
polygons (1,1,1, "18,27 28,20");
polygons (1,1,1, "11,12,13,14");
circles  (.5 ,.15,.06, "24,25");
circles  (0,0,0, "24,26");
polygons(.93,.56,.56, "16,29,11");
polygons (.82,.48,.48, "13,12,9,8 12,11,10,9 5,43 5,4,43");
polygons (0,0,0, "6,7,9,10 7,8,9");
polygons (.93,.56,.56, "39,40,41 4,41,40 4,5,41 5,37,41 37,19,41 19,39,41 19,37,38,20");
polygons (.93,.56,.56, "6,10 10,11 11,36 36,15 17,18 20,38 38,37 37,5");
polygons (.93,.56,.56, "17,16,18 29,15,36");
polygons (.93,.56,.56, "42,6 42,7,6 42,33,8,7");
polygons (.93,.56,.56, "30,4,40 30,40,8 13,8,40 8,33,30 11,14,16");
polygons (.93,.56,.56, "40,16,14 13,40,14 40,39,16 16,39,18 18,39,19");
polygons (.66,.36,.36, "15,17 18,21 23,20 18,19,22,21 23,22,19,20");
polylines(0,0,0, "11,12,13,14,11");

flex("sayAh", "19 20 21 22 23 4 5 28 16 37 38 39 41",
"0,-3,-.6 0,-6,-1.2 0,.3 -1,-2.8,-.6 .6,-5.9,-1.2 1,-1.5,-.3 0,-4,-.8 0,-4.8,-1 .2,-.3 0,-5,-.8 0,-5,-1 0,-1,-.2 0,-5,-1");
flex("blink", "12 14", "0,-2 0,2");
flex("brows", "6 7 8 9 10 13 1 2 42", "-1,4 0,1 0,-2.3 0,1 0,4 0,-1 .4,.2 0,-1 -.5,1");
flex("lookX", "24 25 26", "2.8 3 3"); assymetric();
flex("lookY", "24 25 26", "0,2 0,2 0,2");
flex("lids",  "12 14 60", "0,2 0,2 0,3");
flex("sayOo", "18 19 20 21 22 23 3 4 18 27 28 39 41",
  ".5,.2 4,-.5,2 .5 1.6 4.7,.2,1 1.6 .2 .25,.4 .4 4 4 3 2");
flex("smile", "19 21 22 23 4 27 28 39 41", "0,4.2 -.8,1 -.8,2.6 -.8,1 0,1 0,1 0,.7 0,3 0,2");
flex("sneer", "18 21 16 17 29 27 39", "-1.5,2 -1.5,2 -.2,1 -.3 0,.8 0,.6 0,.4");
      
            map("'","brows",  1); map("_","brows",  0); map("`","brows", -1);
            map("@","blink",-.9); map("+","blink",  0); map("=","blink", .5); map("-","blink", 1 );
            map("t","lids" ,  1); map("c","lids" ,  0); map("b","lids" , -1);

            map("l","lookX", -1); map(".","lookX",  0); map("r","lookX",  1);
            map("u","lookY",  1); map(":","lookY",  0); map("d","lookY", -1);
            map("<","turn" ,  1); map("!","turn" ,  0); map(">","turn" , -1);
            map("^","nod"  ,  1); map("~","nod"  ,  0); map("v","nod"  , -1);
            map("\\","tilt", -1); map("|","tilt" ,  0); map("/","tilt" ,  1);

            map("m","sayAh", -1); map("e","sayAh",-.5); map("o","sayAh",  0); map("O","sayAh", .5);
            map("P","sayOo", .8); map("p","sayOo", .4);
            map("a","sayOo",  0); map("w","sayOo",-.4); map("W","sayOo",-.7);
            map("S","smile",1.5);
            map("s","smile",  1); map("n","smile",  0); map("f","smile",-.7);
            map("i","sneer",-.5); map("x","sneer",  0); map("h","sneer", .5); map("z","sneer", 1 );
        
        
            
    }   
    
    public void map(String s, String s1, double d)   
    {   
        for(int i = 0; i < flexNamesVector.size(); i++)   
            if(((String)flexNamesVector.elementAt(i)).equals(s1))   
            {   
                char c = s.charAt(0);   
                keyFlex[c] = i;   
                keyValue[c] = d;   
                return;   
            }   
   
    }   
   
    public void toggleShade()               
    {   
        doShade = isVectors ? true : !doShade;   
        if(isVectors)   
            toggleVectors();   
    }   
   
    public void toggleNoise()   
    {   
        addNoise = !addNoise;   
    }   
   
    public void toggleVectors()   
    {   
        isVectors = !isVectors;   
    }   
   
    public void noRotate()   
    {   
        noRotateIndex = vertexVector.size() / 3;   
    }   
   
    public void polygons(double d, double d1, double d2, String s)   
    {   
        addFaces(0, d, d1, d2, s);   
    }   
   
    public void polylines(double d, double d1, double d2, String s)   
    {   
        addFaces(1, d, d1, d2, s);   
    }   
   
    public void circles(double d, double d1, double d2, String s)   
    {   
        addFaces(2, d, d1, d2, s);   
    }   
   
    public void vertices(String s)   
    {   
        if(firstVertices)   
        {   
            firstVertices = false;   
            flex("turn", "", "");   
            flex("nod", "", "");   
            flex("tilt", "", "");   
        }   
        if(s != null)   
        {   
            StringTokenizer stringtokenizer1;   
            for(StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();    
                vertexVector.addElement(new Double(stringtokenizer1.nextToken())))   
            {   
                stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ",");   
                vertexVector.addElement(new Double(stringtokenizer1.nextToken()));   
                vertexVector.addElement(new Double(stringtokenizer1.nextToken()));   
            }   
   
        }   
    }   
   
    int type(int i)   
    {   
        return ((Integer)typeVector.elementAt(i)).intValue();   
    }   
   
    Color color(int i, boolean flag)   
    {   
        if(flag)   
            return Color.black;   
        else   
            return (Color)colorVector.elementAt(i);   
    }   
   
    Vector shapes(int i)   
    {   
        return (Vector)shapesVector.elementAt(i);   
    }   
   
    public void addFaces(int i, double d, double d1, double d2,    
            String s)   
    {   
        typeVector.addElement(new Integer(i));   
        colorVector.addElement(new Color((float)d, (float)d1, (float)d2));   
        shapesVector.addElement(shapeVector = new Vector());   
        if(s != null)   
        {   
            for(StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();)   
            {   
                shapeVector.addElement(face = new Vector());   
                for(StringTokenizer stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ","); stringtokenizer1.hasMoreTokens(); face.addElement(new Integer(stringtokenizer1.nextToken())));   
            }   
   
        }   
    }   
   
    public void assymetric()   
    {   
        int i = flexVector.size() - 1;   
        flexSymmetryVector.setElementAt(new Integer(1), i);   
    }   
   
    public void flex(String s, String s1, String s2)   
    {   
        flexNamesVector.addElement(s);   
        flexSymmetryVector.addElement(new Integer(-1));   
        flexVector.addElement(flx = new Vector());   
        for(StringTokenizer stringtokenizer = new StringTokenizer(s1); stringtokenizer.hasMoreTokens(); ixyz.addElement(new Integer(stringtokenizer.nextToken())))   
            flx.addElement(ixyz = new Vector());   
   
        int i = 0;   
        for(StringTokenizer stringtokenizer1 = new StringTokenizer(s2); stringtokenizer1.hasMoreTokens();)   
        {   
            ixyz = (Vector)flx.elementAt(i++);   
            for(StringTokenizer stringtokenizer2 = new StringTokenizer(stringtokenizer1.nextToken(), ","); stringtokenizer2.hasMoreTokens(); ixyz.addElement(new Double(stringtokenizer2.nextToken())));   
        }   
   
    }   
   
    public double[][] getTargets()   
    {   
        double ad[][] = new double[2][flexTarget[0].length];   
        for(int i = 0; i < 2; i++)   
        {   
            for(int j = 0; j < flexTarget[0].length; j++)   
                ad[i][j] = flexTarget[i][j];   
   
        }   
   
        return ad;   
    }   
   
    public void setTargets(double ad[][])   
    {   
        for(int i = 0; i < 2; i++)   
        {   
            for(int j = 0; j < flexTarget[0].length; j++)   
                flexTarget[i][j] = ad[i][j];   
   
        }   
    }   
   
//
//    public void openAnim()
//    {
//        if(f != null)
//            if(!f.isShowing())
//                f = null;
//            else
//                f.toFront();
//        if(f == null)
//            f = new GraphAppFrame("Animator", this, getCodeBase().toString(), null);
//    }   
    
    @Override
    public void keyReleased(KeyEvent e) {
    
        super.keyReleased(e); //To change body of generated methods, choose Tools | Templates.
    }   

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
                
        
        int i = e.getKeyChar();
        System.out.println(i);

        switch(i)
        {
            case '2':
                leftArrow = true;
                prevKey = -1;
                break;
                
            case '3':
                rightArrow = true;
                prevKey = -1;
                break;
                
            case '{': // '{'
                spin++;
                break;
                
            case '}': // '}'
                spin--;
                break;
                
            case '1': // '\t'
                toggleNoise();
                break;
                
            default:
                if(i != prevKey)
                {
                    setFlex(i);
                    leftArrow = false;
                    rightArrow = false;
                }
                prevKey = i;
                break;
        }
    }   
   
    @Override
    public void render(Graphics g)   
    {   
        
        
        try   
        {   
        boolean flag = firstTime;   
        float f1 = 0.5019608F;   
        super.bgcolor = isVectors ? Color.white : new Color(f1, f1, f1);   
        if(firstTime)   
        {   
            firstTime = false;   
            nVertices = vertexVector.size() / 3;   
            vertexArray = new double[nVertices][3];   
            for(int i = 0; i < nVertices; i++)   
            {   
                for(int k = 0; k < 3; k++)   
                    vertexArray[i][k] = ((Double)vertexVector.elementAt(3 * i + k)).doubleValue();   
   
            }   
   
            nShapes = shapesVector.size();   
            shape = new int[nShapes][][];   
            for(int l = 0; l < nShapes; l++)   
            {   
                shapeVector = shapes(l);   
                shape[l] = new int[shapeVector.size()][];   
                for(int j1 = 0; j1 < shape[l].length; j1++)   
                {   
                    face = (Vector)shapeVector.elementAt(j1);   
                    shape[l][j1] = new int[face.size()];   
                    for(int i2 = 0; i2 < shape[l][j1].length; i2++)   
                        shape[l][j1][i2] = ((Integer)face.elementAt(i2)).intValue();   
   
                }   
   
            }   
   
            nFlexes = flexVector.size();   
            flexData = new double[nFlexes][][];   
            flexShape = new int[nFlexes][];   
            flexSymmetry = new int[nFlexes];   
            flexTarget = new double[2][nFlexes];   
            flexValue = new double[2][nFlexes];   
            for(int k1 = 0; k1 < nFlexes; k1++)   
            {   
                flx = (Vector)flexVector.elementAt(k1);   
                flexData[k1] = new double[flx.size()][];   
                flexShape[k1] = new int[flx.size()];   
                flexSymmetry[k1] = ((Integer)flexSymmetryVector.elementAt(k1)).intValue();   
                for(int j2 = 0; j2 < flexShape[k1].length; j2++)   
                {   
                    ixyz = (Vector)flx.elementAt(j2);   
                    flexShape[k1][j2] = ((Integer)ixyz.elementAt(0)).intValue();   
                    flexData[k1][j2] = new double[3];   
                    for(int k2 = 1; k2 < ixyz.size(); k2++)   
                        flexData[k1][j2][k2 - 1] = ((Double)ixyz.elementAt(k2)).doubleValue();   
   
                }   
   
            }   
   
            flexTarget[0][3] = flexTarget[1][3] = -1D;   
            double ad[] = {   
                0.080000000000000002D, 0.040000000000000001D, 0, 0, 0, 0.10000000000000001D, 0.070000000000000007D, 0.070000000000000007D, 0.14999999999999999D   
            };   
            double ad1[] = {   
                0.25D, 0.25D, 0, 0, 0, 0.5D, 0.5D, 0.5D, 0.5D   
            };   
            jitterAmpl = new double[nFlexes];   
            jitterFreq = new double[nFlexes];   
            for(int l2 = 0; l2 < ad.length && l2 < nFlexes; l2++)   
            {   
                jitterAmpl[l2] = ad[l2];   
                jitterFreq[l2] = ad1[l2];   
            }   
   
            pts = new double[2][nVertices][3];   
        }   
        int j = flexValue[0].length;   
        for(int i1 = 0; i1 < 2; i1++)   
        {   
            for(int l1 = 0; l1 < j; l1++)   
                if(flexTarget[i1][l1] != flexValue[i1][l1] || addNoise && jitterAmpl[l1] != 0.0D)   
                    flag = true;   
   
        }   
   
        if(!flag)   
            return;   
        double d = 0.94999999999999996D;   
        double d1 = 1.0D;   
        long l3 = System.currentTimeMillis();   
        long l4 = l3 - lastTime;   
        double d2 = (d1 * 1000D) / (double)l4;   
        double d3 = 1.0D - Math.pow(1.0D - d, 1.0D / d2);   
        frameCount++;   
        long l5 = l3 / 1000L - lastTime / 1000L;   
        if(l5 > 0L)   
        {   
            dispCount = (int)((long)frameCount / l5);   
            frameCount = 0;   
        }   
        lastTime = l3;   
        for(int i3 = 0; i3 < 2; i3++)   
        {   
            for(int j3 = 0; j3 < j; j3++)   
            {   
                flexValue[i3][j3] += d3 * (flexTarget[i3][j3] - flexValue[i3][j3]);   
                if(addNoise && jitterAmpl[j3] != 0.0D)   
                    flexValue[i3][j3] += jitterAmpl[j3] * ImprovMath.noise(jitterFreq[j3] * t + (double)(10 * j3));   
                t += 0.0050000000000000001D;   
            }   
   
        }   
   
        blinkValue = addNoise ? pulse(t / 2D + ImprovMath.noise(t / 1.5D) + 0.5D * ImprovMath.noise(t / 3D), 0.050000000000000003D) : 0.0D;   
        doRender(g, flexValue, blinkValue, isVectors, doShade, shiftAxis);   
        }   
        catch(Exception e)   
        {   
               System.err.println(e);
               e.printStackTrace();
        }   
    }   
   
    void doRender(Graphics g, double ad[][], double d, boolean flag, boolean flag1, boolean flag2)   
    {   
        try   
        {   
        synchronized(pts)   
        {   
            int ai[] = new int[100];   
            int ai1[] = new int[100];   
            int ai2[] = new int[100];   
            for(int i = 0; i < 2; i++)   
            {   
                for(int j = 0; j < vertexArray.length; j++)   
                {   
                    pts[i][j][0] = vertexArray[j][0];   
                    pts[i][j][1] = vertexArray[j][1];   
                    pts[i][j][2] = vertexArray[j][2];   
                    if(i == 1)   
                        pts[i][j][0] = -pts[i][j][0];   
                }   
   
            }   
   
            for(int k = 0; k < nFlexes; k++)   
            {   
                for(int l = 0; l < flexShape[k].length; l++)   
                {   
                    int i1 = flexShape[k][l];   
                    double d2 = ad[0][k];   
                    double d4 = ad[1][k];   
                    if(k == 4 && d == 1.0D)   
                        d2 = d4 = 1.0D;   
                    pts[0][i1][0] += d2 * flexData[k][l][0] * (double)flexSymmetry[k];   
                    pts[1][i1][0] += d4 * flexData[k][l][0];   
                    pts[0][i1][1] += d2 * flexData[k][l][1];   
                    pts[1][i1][1] += d4 * flexData[k][l][1];   
                    pts[0][i1][2] += d2 * flexData[k][l][2];   
                    pts[1][i1][2] += d4 * flexData[k][l][2];   
                }   
   
            }   
   
            double d1 = ad[0][0];   
            double d3 = ad[0][1];   
            double d5 = ad[0][2];   
            if(kludge)   
            {   
                System.out.println("first time");   
                kludge = false;   
            }   
            double d6 = -0.20000000000000001D * d1 - 0.10000000000000001D * (double)spin;   
            d6 = (d6 + 3.1415926535897931D + 3141.5926535897929D) % 6.2831853071795862D - 3.1415926535897931D;   
            Matrix3D matrix3d = new Matrix3D();   
            matrix3d.scale((double)super.height / 110D, (double)(-super.height) / 110D, (double)super.height / 110D);   
            matrix3d.translate(35D, -80D, flag2 ? 20 : 5);   
            matrix3d.rotateX(-0.20000000000000001D * d3);   
            matrix3d.rotateY(d6);   
            matrix3d.rotateZ(-0.20000000000000001D * d5);   
            matrix3d.translate(0.0D, -30D, flag2 ? -20 : -5);   
            Matrix3D matrix3d1 = new Matrix3D();   
            matrix3d1.scale((double)super.height / 110D, (double)(-super.height) / 110D, (double)super.height / 110D);   
            matrix3d1.translate(35D, -110D, 0.0D);   
            Vector3D vector3d = new Vector3D();   
            int j1 = d6 >= 0.0D ? 0 : 1;   
            for(int k1 = 0; k1 <= 1;)   
            {   
                for(int l1 = 0; l1 < shape.length; l1++)   
                {   
                    int i2 = type(l1);   
                    g.setColor(color(l1, flag));   
                    for(int j2 = 0; j2 < shape[l1].length; j2++)   
                    {   
                        int k2 = shape[l1][j2].length;   
                        if(i2 != 0 || k2 != 2 || k1 != 1)   
                        {   
                            for(int l2 = 0; l2 < k2; l2++)   
                            {   
                                int i3 = shape[l1][j2][l2];   
                                vector3d.set(pts[j1][i3][0], pts[j1][i3][1], pts[j1][i3][2]);   
                                vector3d.transform(i3 >= noRotateIndex ? ((MatrixN) (matrix3d1)) : ((MatrixN) (matrix3d)));   
                                ai[l2] = (int)vector3d.get(0);   
                                ai1[l2] = (int)vector3d.get(1);   
                                if(flag1)   
                                    ai2[l2] = (int)vector3d.get(2);   
                            }   
   
                            if(i2 == 0 && k2 == 2)   
                            {   
                                k2 = 4;   
                                for(int j3 = 0; j3 < 2; j3++)   
                                {   
                                    int l3 = shape[l1][j2][j3];   
                                    vector3d.set(pts[1 - j1][l3][0], pts[1 - j1][l3][1], pts[1 - j1][l3][2]);   
                                    vector3d.transform(l3 >= noRotateIndex ? ((MatrixN) (matrix3d1)) : ((MatrixN) (matrix3d)));   
                                    ai[3 - j3] = (int)vector3d.get(0);   
                                    ai1[3 - j3] = (int)vector3d.get(1);   
                                    if(flag1)   
                                        ai2[3 - j3] = (int)vector3d.get(2);   
                                }   
   
                            }   
                            switch(i2)   
                            {   
                            default:   
                                break;   
   
                            case 2: // '\002'   
                                if(Math.abs(d6) > 2D || d6 > 0.90000000000000002D && j1 == 0 || d6 < -0.90000000000000002D && j1 == 1)   
                                    break;   
                                int k3 = ai[0] - ai[1];   
                                int i4 = ai1[0] - ai1[1];   
                                byte byte0 = ((byte)(k3 * k3 + i4 * i4 < 51 ? 6 : 12));   
                                if(flag)   
                                    g.drawOval(ai[0] - byte0, ai1[0] - byte0, 2 * byte0, 2 * byte0);   
                                else   
                                    g.fillOval(ai[0] - byte0, ai1[0] - byte0, 2 * byte0, 2 * byte0);   
                                break;   
   
                            case 1: // '\001'   
                                if(!flag && Math.abs(d6) <= 2D && (d6 <= 1.1000000000000001D || j1 != 0) && (d6 >= -1.1000000000000001D || j1 != 1))   
                                    g.drawPolygon(ai, ai1, k2);   
                                break;   
   
                            case 0: // '\0'   
                                if(flag && !((Color)colorVector.elementAt(l1)).equals(Color.black) && shape[l1][j2][0] < noRotateIndex)   
                                {   
                                    ai[k2] = ai[0];   
                                    ai1[k2] = ai1[0];   
                                    g.drawPolygon(ai, ai1, k2 + 1);   
                                }   
                                if(flag || (area(ai, ai1, k2) > 0) != (j1 == 0))   
                                    break;   
                                if(flag1)   
                                {   
                                    Color color1 = color(l1, flag);   
                                    int j4 = color1.getRed();   
                                    int k4 = color1.getGreen();   
                                    int l4 = color1.getBlue();   
                                    int i5 = getShade(ai, ai1, ai2);   
                                    j4 = Math.min(255, i5 * j4 >> 8);   
                                    k4 = Math.min(255, i5 * k4 >> 8);   
                                    l4 = Math.min(255, i5 * l4 >> 8);   
                                    g.setColor(new Color(j4, k4, l4));   
                                }   
                                
                                g.fillPolygon(ai, ai1, k2);   
                                break;   
                            }   
                        }   
                    }   
   
                }   
   
                k1++;   
                j1 = 1 - j1;   
            }   
   
        }   
        }   
        catch(Exception e)   
        {   
               
        }   
    }   
   
    void setFlex(int i)   
    {   
        try {
        
            if(!leftArrow)   
                flexTarget[0][keyFlex[i]] = keyValue[i];   
            if(!rightArrow)   
                flexTarget[1][keyFlex[i]] = keyValue[i];   
        }
        catch (Exception e) { }
    }   
   
    int area(int ai[], int ai1[], int i)   
    {   
        int j = 0;   
        for(int l = 0; l < i; l++)   
        {   
            int k = (l + 1) % i;   
            j += (ai[l] - ai[k]) * (ai1[l] + ai1[k]);   
        }   
   
        return j / 2;   
    }   
   
    double pulse(double d, double d1)   
    {   
        return (double)(d - (double)(int)d >= d1 ? 0 : 1);   
    }   
   
    int getShade(int ai[], int ai1[], int ai2[])   
    {   
        int i = ai[1] - ai[0];   
        int j = ai1[1] - ai1[0];   
        int k = ai2[1] - ai2[0];   
        int l = ai[2] - ai[1];   
        int i1 = ai1[2] - ai1[1];   
        int j1 = ai2[2] - ai2[1];   
        int k1 = j * j1 - k * i1;   
        int l1 = k * l - i * j1;   
        int i2 = i * i1 - j * l;   
        int j2 = (k1 - l1 / 2) + i2;   
        j2 = (8 * j2 * j2) / (k1 * k1 + l1 * l1 + i2 * i2);   
        return 192 + 8 * j2;   
    }   
}  
