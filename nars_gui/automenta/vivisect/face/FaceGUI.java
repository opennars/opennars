package automenta.vivisect.face;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import java.util.Vector;

public class FaceGUI extends BaseClass {

    private static final long serialVersionUID = 1L;
    HumanoidFacePanel f;
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
    double spin;
    int noRotateIndex;
    boolean leftArrow;
    boolean rightArrow;
    int prevKey;
    long lastTime;
    int frameCount;
    int dispCount;
    Vector<Color> colorVector;
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
    public final int pupil;
    public final int eyeBall;
    private float eyeballSize = 16;
    private float pupilSize = 8;

    
    public FaceGUI() {
        super();
        firstVertices = true;
        isVectors = false;
        doShade = false;
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

        polygons(.1, 0, 0, "58,59 59,60 60,61");
        polygons(0, 0, 0, "15,16,17");
        polygons(0, 0, 0, "39,41");
        polygons(.56, .56, .56, "11,29,36");
        polygons(.5, .5, .5, "15,29,16");
        polygons(0, 0, 0, "3,49 49,48 48,47 47,46 46,45 1,45,46 1,46,47 50,47,48 48,49,3 30,3,4");
        polygons(0, 0, 0, "34,32 34,1,33,32 1,30,33 45,34 34,45,1 50,1,47 50,48,3 50,3,30 50,30,1");
        polygons(0, 0, 0, "32,51 51,42 32,33,51 51,33,42");
        circles(.56, .56, .56, "53,54");
        polygons(.56, .56, .56, "54,53,55,56,57");
        polygons(.50, .50, .50, "54,53,55,57");
        polygons(0, 0, 0, "18,27 28,20");
        polygons(0, 0, 0, "11,12,13,14");
        eyeBall = circles(.9, .9, .9, "24,25");
        pupil = circles(0, 0, 0, "24,26");
        polygons(.56, .56, .56, "16,29,11");
        polygons(.50, .50, .50, "13,12,9,8 12,11,10,9 5,43 5,4,43");
        polygons(0, 0, 0, "6,7,9,10 7,8,9");
        polygons(.56, .56, .56, "39,40,41 4,41,40 4,5,41 5,37,41 37,19,41 19,39,41 19,37,38,20");
        polygons(.56, .56, .56, "6,10 10,11 11,36 36,15 17,18 20,38 38,37 37,5");
        polygons(.56, .56, .56, "17,16,18 29,15,36");
        polygons(.56, .56, .56, "42,6 42,7,6 42,33,8,7");
        polygons(.56, .56, .56, "30,4,40 30,40,8 13,8,40 8,33,30 11,14,16");
        polygons(.56, .56, .56, "40,16,14 13,40,14 40,39,16 16,39,18 18,39,19");
        polygons(.4, .4, .45, "15,17 18,21 23,20 18,19,22,21 23,22,19,20");
        polylines(0, 0, 0, "11,12,13,14,11");

        flex("sayAh", "19 20 21 22 23 4 5 28 16 37 38 39 41",
                "0,-3,-.6 0,-6,-1.2 0,.3 -1,-2.8,-.6 .6,-5.9,-1.2 1,-1.5,-.3 0,-4,-.8 0,-4.8,-1 .2,-.3 0,-5,-.8 0,-5,-1 0,-1,-.2 0,-5,-1");
        flex("blink", "12 14", "0,-2 0,2");
        flex("brows", "6 7 8 9 10 13 1 2 42", "-1,4 0,1 0,-2.3 0,1 0,4 0,-1 .4,.2 0,-1 -.5,1");
        flex("lookX", "24 25 26", "2.8 3 3");
        assymetric();
        flex("lookY", "24 25 26", "0,2 0,2 0,2");
        flex("lids", "12 14 60", "0,2 0,2 0,3");
        flex("sayOo", "18 19 20 21 22 23 3 4 18 27 28 39 41",
                ".5,.2 4,-.5,2 .5 1.6 4.7,.2,1 1.6 .2 .25,.4 .4 4 4 3 2");
        flex("smile", "19 21 22 23 4 27 28 39 41", "0,4.2 -.8,1 -.8,2.6 -.8,1 0,1 0,1 0,.7 0,3 0,2");
        flex("sneer", "18 21 16 17 29 27 39", "-1.5,2 -1.5,2 -.2,1 -.3 0,.8 0,.6 0,.4");

        map("'", "brows", 1);
        map("_", "brows", 0);
        map("`", "brows", -1);
        map("@", "blink", -.9);
        map("+", "blink", 0);
        map("=", "blink", .5);
        map("-", "blink", 1);
        map("t", "lids", 1);
        map("c", "lids", 0);
        map("b", "lids", -1);

        map("l", "lookX", -1);
        map(".", "lookX", 0);
        map("r", "lookX", 1);
        map("u", "lookY", 1);
        map(":", "lookY", 0);
        map("d", "lookY", -1);
        map("<", "turn", 1);
        map("!", "turn", 0);
        map(">", "turn", -1);
        map("^", "nod", 1);
        map("~", "nod", 0);
        map("v", "nod", -1);
        map("\\", "tilt", -1);
        map("|", "tilt", 0);
        map("/", "tilt", 1);

        map("m", "sayAh", -1);
        map("e", "sayAh", -.5);
        map("o", "sayAh", 0);
        map("O", "sayAh", .5);
        map("P", "sayOo", .8);
        map("p", "sayOo", .4);
        map("a", "sayOo", 0);
        map("w", "sayOo", -.4);
        map("W", "sayOo", -.7);
        map("S", "smile", 1.5);
        map("s", "smile", 1);
        map("n", "smile", 0);
        map("f", "smile", -.7);
        map("i", "sneer", -.5);
        map("x", "sneer", 0);
        map("h", "sneer", .5);
        map("z", "sneer", 1);

    }

    public void setPupil(float radius, float r, float g, float b, float a) {
        colorVector.set(pupil, new Color(r, g, b, a));
        pupilSize = radius;
    }
    public void setEyeball(float radius, float r, float g, float b, float a) {
        colorVector.set(eyeBall, new Color(r, g, b, a));
        eyeballSize = radius;
    }
    
    public void map(String s, String s1, double d) {
        for (int i = 0; i < flexNamesVector.size(); i++) {
            if (((String) flexNamesVector.elementAt(i)).equals(s1)) {
                char c = s.charAt(0);
                keyFlex[c] = i;
                keyValue[c] = d;
                return;
            }
        }

    }

    public void toggleShade() {
        doShade = isVectors ? true : !doShade;
        if (isVectors) {
            toggleVectors();
        }
    }

    public void toggleNoise() {
        addNoise = !addNoise;
    }

    public void toggleVectors() {
        isVectors = !isVectors;
    }

    public void noRotate() {
        noRotateIndex = vertexVector.size() / 3;
    }

    public int polygons(double d, double d1, double d2, String s) {
        return addFaces(0, d, d1, d2, s);
    }

    public void polylines(double d, double d1, double d2, String s) {
        addFaces(1, d, d1, d2, s);
    }

    public int circles(double d, double d1, double d2, String s) {
        return addFaces(2, d, d1, d2, s);
    }

    public void vertices(String s) {
        if (firstVertices) {
            firstVertices = false;
            flex("turn", "", "");
            flex("nod", "", "");
            flex("tilt", "", "");
        }
        if (s != null) {
            StringTokenizer stringtokenizer1;
            for (StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();
                    vertexVector.addElement(new Double(stringtokenizer1.nextToken()))) {
                stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ",");
                vertexVector.addElement(new Double(stringtokenizer1.nextToken()));
                vertexVector.addElement(new Double(stringtokenizer1.nextToken()));
            }

        }
    }

    int type(int i) {
        return ((Integer) typeVector.elementAt(i)).intValue();
    }

    Color color(int i, boolean flag) {
        if (flag) {
            return Color.black;
        } else {
            return (Color) colorVector.elementAt(i);
        }
    }

    Vector shapes(int i) {
        return (Vector) shapesVector.elementAt(i);
    }

    public int addFaces(int i, double d, double d1, double d2, String s) {
        int index = typeVector.size();
        typeVector.addElement(new Integer(i));
        colorVector.addElement(new Color((float) d1, (float) d1, (float) d1));
        shapesVector.addElement(shapeVector = new Vector());
        if (s != null) {
            for (StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();) {
                shapeVector.addElement(face = new Vector());
                for (StringTokenizer stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ","); stringtokenizer1.hasMoreTokens(); face.addElement(new Integer(stringtokenizer1.nextToken())));
            }

        }
        return index;
    }

    public void assymetric() {
        int i = flexVector.size() - 1;
        flexSymmetryVector.setElementAt(new Integer(1), i);
    }

    public void flex(String s, String s1, String s2) {
        flexNamesVector.addElement(s);
        flexSymmetryVector.addElement(new Integer(-1));
        flexVector.addElement(flx = new Vector());
        for (StringTokenizer stringtokenizer = new StringTokenizer(s1); stringtokenizer.hasMoreTokens(); ixyz.addElement(new Integer(stringtokenizer.nextToken()))) {
            flx.addElement(ixyz = new Vector());
        }

        int i = 0;
        for (StringTokenizer stringtokenizer1 = new StringTokenizer(s2); stringtokenizer1.hasMoreTokens();) {
            ixyz = (Vector) flx.elementAt(i++);
            for (StringTokenizer stringtokenizer2 = new StringTokenizer(stringtokenizer1.nextToken(), ","); stringtokenizer2.hasMoreTokens(); ixyz.addElement(new Double(stringtokenizer2.nextToken())));
        }

    }

    public double[][] getTargets() {
        double ad[][] = new double[2][flexTarget[0].length];
        for (int i = 0; i < 2; i++) {
            System.arraycopy(flexTarget[i], 0, ad[i], 0, flexTarget[0].length);

        }

        return ad;
    }

    public void setTargets(double ad[][]) {
        for (int i = 0; i < 2; i++) {
            System.arraycopy(ad[i], 0, flexTarget[i], 0, flexTarget[0].length);

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

        switch (i) {
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
                if (i != prevKey) {
                    setFlex(i);
                    leftArrow = false;
                    rightArrow = false;
                }
                prevKey = i;
                break;
        }
    }
    int ai[] = new int[100];
    int ai1[] = new int[100];
    int ai2[] = new int[100];

    @Override
    public void render(Graphics g) {

        try {
            boolean flag = firstTime;
            float f1 = 0.9019608F;
            super.bgcolor = new Color(50,50,50); //isVectors ? Color.white : new Color(f1, f1, f1);
            if (firstTime) {
                firstTime = false;
                nVertices = vertexVector.size() / 3;
                vertexArray = new double[nVertices][3];
                for (int i = 0; i < nVertices; i++) {
                    for (int k = 0; k < 3; k++) {
                        vertexArray[i][k] = ((Double) vertexVector.elementAt(3 * i + k)).doubleValue();
                    }

                }

                nShapes = shapesVector.size();
                shape = new int[nShapes][][];
                for (int l = 0; l < nShapes; l++) {
                    shapeVector = shapes(l);
                    shape[l] = new int[shapeVector.size()][];
                    for (int j1 = 0; j1 < shape[l].length; j1++) {
                        face = (Vector) shapeVector.elementAt(j1);
                        shape[l][j1] = new int[face.size()];
                        for (int i2 = 0; i2 < shape[l][j1].length; i2++) {
                            shape[l][j1][i2] = ((Integer) face.elementAt(i2)).intValue();
                        }

                    }

                }

                nFlexes = flexVector.size();
                flexData = new double[nFlexes][][];
                flexShape = new int[nFlexes][];
                flexSymmetry = new int[nFlexes];
                flexTarget = new double[2][nFlexes];
                flexValue = new double[2][nFlexes];
                for (int k1 = 0; k1 < nFlexes; k1++) {
                    flx = (Vector) flexVector.elementAt(k1);
                    flexData[k1] = new double[flx.size()][];
                    flexShape[k1] = new int[flx.size()];
                    flexSymmetry[k1] = ((Integer) flexSymmetryVector.elementAt(k1)).intValue();
                    for (int j2 = 0; j2 < flexShape[k1].length; j2++) {
                        ixyz = (Vector) flx.elementAt(j2);
                        flexShape[k1][j2] = ((Integer) ixyz.elementAt(0)).intValue();
                        flexData[k1][j2] = new double[3];
                        for (int k2 = 1; k2 < ixyz.size(); k2++) {
                            flexData[k1][j2][k2 - 1] = ((Double) ixyz.elementAt(k2)).doubleValue();
                        }

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
                for (int l2 = 0; l2 < ad.length && l2 < nFlexes; l2++) {
                    jitterAmpl[l2] = ad[l2];
                    jitterFreq[l2] = ad1[l2];
                }

                pts = new double[2][nVertices][3];
            }
            
            int j = flexValue[0].length;
            for (int i1 = 0; i1 < 2; i1++) {
                for (int l1 = 0; l1 < j; l1++) {
                    if (flexTarget[i1][l1] != flexValue[i1][l1] || addNoise && jitterAmpl[l1] != 0.0D) {
                        flag = true;
                    }
                }

            }

            if (!flag) {
                return;
            }
            double d = 0.94999999999999996D;
            double d1 = 1.0D;
            long l3 = System.currentTimeMillis();
            long l4 = l3 - lastTime;
            double d2 = (d1 * 1000D) / (double) l4;
            double d3 = 1.0D - Math.pow(1.0D - d, 1.0D / d2);
            frameCount++;
            long l5 = l3 / 1000L - lastTime / 1000L;
            if (l5 > 0L) {
                dispCount = (int) ((long) frameCount / l5);
                frameCount = 0;
            }
            lastTime = l3;
            for (int i3 = 0; i3 < 2; i3++) {
                for (int j3 = 0; j3 < j; j3++) {
                    flexValue[i3][j3] += d3 * (flexTarget[i3][j3] - flexValue[i3][j3]);
                    if (addNoise && jitterAmpl[j3] != 0.0D) {
                        flexValue[i3][j3] += jitterAmpl[j3] * ImprovMath.noise(jitterFreq[j3] * t + (double) (10 * j3));
                    }
                    t += 0.0050000000000000001D;
                }

            }

            blinkValue = addNoise ? pulse(t / 2D + ImprovMath.noise(t / 1.5D) + 0.5D * ImprovMath.noise(t / 3D), 0.050000000000000003D) : 0.0D;
            doRender(g, flexValue, blinkValue, isVectors, doShade, shiftAxis);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    void doRender(Graphics g, double ad[][], double d, boolean flag, boolean flag1, boolean flag2) {
        try {
            synchronized (pts) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < vertexArray.length; j++) {
                        pts[i][j][0] = vertexArray[j][0];
                        pts[i][j][1] = vertexArray[j][1];
                        pts[i][j][2] = vertexArray[j][2];
                        if (i == 1) {
                            pts[i][j][0] = -pts[i][j][0];
                        }
                    }

                }

                for (int k = 0; k < nFlexes; k++) {
                    for (int l = 0; l < flexShape[k].length; l++) {
                        int i1 = flexShape[k][l];
                        double d2 = ad[0][k];
                        double d4 = ad[1][k];
                        if (k == 4 && d == 1.0D) {
                            d2 = d4 = 1.0D;
                        }
                        pts[0][i1][0] += d2 * flexData[k][l][0] * (double) flexSymmetry[k];
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
                if (kludge) {
                    kludge = false;
                }
                double d6 = -0.20000000000000001D * d1 - 0.10000000000000001D * spin;
                d6 = (d6 + 3.1415926535897931D + 3141.5926535897929D) % 6.2831853071795862D - 3.1415926535897931D;
                Matrix3D matrix3d = new Matrix3D();
                matrix3d.scale((double) super.height / 110D, (double) (-super.height) / 110D, (double) super.height / 110D);
                matrix3d.translate(35D, -80D, flag2 ? 20 : 5);
                matrix3d.rotateX(-0.20000000000000001D * d3);
                matrix3d.rotateY(d6);
                matrix3d.rotateZ(-0.20000000000000001D * d5);
                matrix3d.translate(0.0D, -30D, flag2 ? -20 : -5);
                Matrix3D matrix3d1 = new Matrix3D();
                matrix3d1.scale((double) super.height / 110D, (double) (-super.height) / 110D, (double) super.height / 110D);
                matrix3d1.translate(35D, -110D, 0.0D);
                Vector3D vector3d = new Vector3D();
                int j1 = d6 >= 0.0D ? 0 : 1;
                for (int k1 = 0; k1 <= 1;) {
                    for (int l1 = 0; l1 < shape.length; l1++) {
                        int i2 = type(l1);
                        g.setColor(color(l1, flag));
                        for (int j2 = 0; j2 < shape[l1].length; j2++) {
                            int k2 = shape[l1][j2].length;
                            if (i2 != 0 || k2 != 2 || k1 != 1) {
                                for (int l2 = 0; l2 < k2; l2++) {
                                    int i3 = shape[l1][j2][l2];
                                    vector3d.set(pts[j1][i3][0], pts[j1][i3][1], pts[j1][i3][2]);
                                    vector3d.transform(i3 >= noRotateIndex ? ((MatrixN) (matrix3d1)) : ((MatrixN) (matrix3d)));
                                    ai[l2] = (int) vector3d.get(0);
                                    ai1[l2] = (int) vector3d.get(1);
                                    if (flag1) {
                                        ai2[l2] = (int) vector3d.get(2);
                                    }
                                }

                                if (i2 == 0 && k2 == 2) {
                                    k2 = 4;
                                    for (int j3 = 0; j3 < 2; j3++) {
                                        int l3 = shape[l1][j2][j3];
                                        vector3d.set(pts[1 - j1][l3][0], pts[1 - j1][l3][1], pts[1 - j1][l3][2]);
                                        vector3d.transform(l3 >= noRotateIndex ? ((MatrixN) (matrix3d1)) : ((MatrixN) (matrix3d)));
                                        ai[3 - j3] = (int) vector3d.get(0);
                                        ai1[3 - j3] = (int) vector3d.get(1);
                                        if (flag1) {
                                            ai2[3 - j3] = (int) vector3d.get(2);
                                        }
                                    }

                                }
                                switch (i2) {
                                    default:
                                        break;

                                    case 2: // '\002'   
                                        /*if (Math.abs(d6) > 2D || d6 > 0.90000000000000002D && j1 == 0 || d6 < -0.90000000000000002D && j1 == 1) {
                                            break;
                                        }*/
                                        int k3 = ai[0] - ai[1];
                                        int i4 = ai1[0] - ai1[1];


                                        //byte byte0 = ((byte) (k3 * k3 + i4 * i4 < 51 ? 6 : 12));
                                        //HACK remove the ear circles, whatever they are
                                        if ((l1!=13) && (l1!=14)) continue;
                                        
                                        float radius = l1 == 14 ? pupilSize : eyeballSize;
                                        
                                        
                                        float rscale = super.height/220f;
                                        int cr = (int)(radius * rscale);
                                                         
                                        int cx = ai[0] - cr/2;
                                        int cy = ai1[0] - cr/2;
                                        if (flag) {
                                            g.drawOval(cx, cy, cr, cr);
                                        } else {
                                            g.fillOval(cx, cy, cr, cr);
                                        }
                                        break;

                                    case 1: // '\001'   
                                        if (!flag && Math.abs(d6) <= 2D && (d6 <= 1.1000000000000001D || j1 != 0) && (d6 >= -1.1000000000000001D || j1 != 1)) {
                                            g.drawPolygon(ai, ai1, k2);
                                        }
                                        break;

                                    case 0: // '\0'   
                                        if (flag && !((Color) colorVector.elementAt(l1)).equals(Color.black) && shape[l1][j2][0] < noRotateIndex) {
                                            ai[k2] = ai[0];
                                            ai1[k2] = ai1[0];
                                            g.drawPolygon(ai, ai1, k2 + 1);
                                        }
                                        if (flag || (area(ai, ai1, k2) > 0) != (j1 == 0)) {
                                            break;
                                        }
                                        if (flag1) {
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
        } catch (Exception e) {

        }
    }

    void setFlex(int i) {
        try {

            if (!leftArrow) {
                flexTarget[0][keyFlex[i]] = keyValue[i];
            }
            if (!rightArrow) {
                flexTarget[1][keyFlex[i]] = keyValue[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int area(int ai[], int ai1[], int i) {
        int j = 0;
        for (int l = 0; l < i; l++) {
            int k = (l + 1) % i;
            j += (ai[l] - ai[k]) * (ai1[l] + ai1[k]);
        }

        return j / 2;
    }

    double pulse(double d, double d1) {
        return (double) (d - (double) (int) d >= d1 ? 0 : 1);
    }

    int getShade(int ai[], int ai1[], int ai2[]) {
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

//package nars.gui.output.face;
//
//import java.awt.Color;
//import java.awt.Event;
//import java.awt.Graphics;
//import java.awt.Image;
//import static java.lang.Thread.sleep;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
///** @author http://cs.nyu.edu/~lhs215/multimedia/face/ */
//public class Face2bApplet extends Animator
//{
//    //---- PUBLIC INTERFACE:
//
//    // MAP A KEY TO A FLEX VALUE
//
//    public void map(String s, String flexName, double f) {
//	for (int i = 0 ; i < flexNamesVector.size() ; i++)
//	    if (((String)flexNamesVector.elementAt(i)).equals(flexName)) {
//		char key = s.charAt(0);
//		keyFlex[(int)key] = i;
//		keyValue[(int)key] = f;
//		return;
//	    }
//    }
//
//    // ALL THIS PARSING STUFF GOES IN THE "MOVING" OBJECT
//
//    public void set(String s) {
//	for (int i = 0 ; i < s.length() ; i++)
//	    if (s.charAt(i) == '[')
//		leftArrow = true;
//	    else if (s.charAt(i) == ']')
//		rightArrow = true;
//	    else if (s.charAt(i) >= '0' && s.charAt(i) <= '9')
//		sleep(100 * (s.charAt(i) - '0'));
//	    else {
//		setFlex((int)s.charAt(i));
//		leftArrow = rightArrow = false;
//	    }
//    }
//    public void toggleShade() {
//	doShade = isVectors ? true : !doShade;
//	if (isVectors)
//	    toggleVectors();
//    }
//    public void toggleNoise() { addNoise = ! addNoise; }
//    public void toggleVectors() { isVectors = ! isVectors; }
//    public void noRotate() { noRotateIndex = vertexVector.size() / 3; }
//
//    public void polygons (double r, double g, double b, String str)
//    { addFaces(POLYGON,  r,g,b, str); }
//    public void polylines(double r, double g, double b, String str)
//    { addFaces(POLYLINE, r,g,b, str); }
//    public void circles  (double r, double g, double b, String str)
//    { addFaces(CIRCLE,   r,g,b, str); }
//
//    // PARSE ITEMS OF THE FORM vertices("x,y,z x,y,z ...")
//
//    public void vertices(String str)
//    {
//	StringTokenizer arg, word;
//
//	if (firstVertices) {
//	    firstVertices = false;
//	    flex("turn","1","","");
//	    flex("nod" ,"1","","");
//	    flex("tilt","1","","");
//	}
//
//	if (str != null)
//	    for (arg = new StringTokenizer(str) ; arg.hasMoreTokens() ; ) {
//		word = new StringTokenizer(arg.nextToken(), ",");
//		vertexVector.addElement(new Double(word.nextToken()));
//		vertexVector.addElement(new Double(word.nextToken()));
//		vertexVector.addElement(new Double(word.nextToken()));
//	    }
//    }
//
//    // PARSE SHAPE SPECIFIERS: addFaces(type, r, g, b, "a,b,c d,e,f,g ...")
//    // WHERE a,b,c,d,e,f... ARE VERTEX INDICES.
//
//    int      type(int i) { return ((Integer)typeVector.elementAt(i)).intValue(); }
//    Color   color(int i, boolean vec) { return vec ? Color.black : (Color)colorVector.elementAt(i); }
//    Vector shapes(int i) { return (Vector)shapesVector.elementAt(i); }
//
//    public void addFaces(int type, double r, double g, double b, String str)
//    {
//	StringTokenizer fs, f;
//
//	typeVector.addElement(new Integer(type));
//	colorVector.addElement(new Color((float)r,(float)g,(float)b));
//	shapesVector.addElement(shapeVector = new Vector());
//	if (str != null)
//	    for (fs = new StringTokenizer(str) ; fs.hasMoreTokens() ; ) {
//		shapeVector.addElement(face = new Vector());
//		for (f=new StringTokenizer(fs.nextToken(),","); f.hasMoreTokens(); )
//		    face.addElement(new Integer(f.nextToken()));
//	    }
//    }
//
//    // SPECIFY THAT THE FLEX WE'VE JUST SPECIFIED IS HORIZONTALLY ASSYMETRIC
//
//    public void assymetric() {
//	int lastFlex = flexVector.size()-1;
//	flexSymmetryVector.setElementAt(new Integer(1), lastFlex);
//    }
//
//    // PARSE FLEX SPECIFIERS OF THE FORM:
//    //    flex("name", "settle_time", "a b c ...", "x,y,z x,y,z x,y,z ...")
//
//    // WHERE a,b,c... ARE VERTEX INDICES, and x,y,z ARE DISPLACEMENTS.
//
//    public void flex(String name, String settle, String id, String xyz) {
//	StringTokenizer s, t;
//
//	flexNamesVector.addElement(name);
//	flexSettleVector.addElement(new Double(settle));
//	flexSymmetryVector.addElement(new Integer(-1));
//	flexVector.addElement(flx = new Vector());
//	for (s = new StringTokenizer(id) ; s.hasMoreTokens() ; ) {
//	    flx.addElement(ixyz = new Vector());
//	    ixyz.addElement(new Integer(s.nextToken()));
//	}
//	int i = 0;
//	for (s = new StringTokenizer(xyz) ; s.hasMoreTokens() ; ) {
//	    ixyz = (Vector)flx.elementAt(i++);
//	    for (t=new StringTokenizer(s.nextToken(), ",") ; t.hasMoreTokens() ; )
//		ixyz.addElement(new Double(t.nextToken()));
//	}
//    }
//
//    // CSP:
//    // getTargets() return current flex targets for Animation applet
//    // setTargets() sets the targets
//    // getSnapshotImage() returns an image of size w by h described
//    //   by the values in flexVals
//    // openAnim() opens the animation applet in a frame
//
//    
//
//    public double[][] getTargets() {
//	double[][] retVal = new double[2][flexTarget[0].length];
//	for(int s = 0; s < 2; s++)
//	    for(int v = 0; v < flexTarget[0].length; v++)
//		retVal[s][v] = flexTarget[s][v];
//	return retVal;
//    }
//
//    public void setTargets(double[][] inVal) {
//	for(int s = 0; s < 2; s++)
//	    for(int v = 0; v < flexTarget[0].length; v++)
//		flexTarget[s][v] = inVal[s][v];
//    }
//
//    public Image getSnapshotImage(double[][] flexVals) {
//	Image temp = createImage(bounds().width, bounds().height);
//	Graphics tempg = temp.getGraphics();
//	doRender(tempg, flexVals, 0, false, false, false);
//
//	tempg.dispose();
//	return temp;
//    }
//
//    public void init() {
//	super.init();
//	setBackground(Color.white);
//	lastTime = System.currentTimeMillis();
//	frameCount = 0;
//	dispCount = 0;
//    }
//
//    GraphAnimFrame f = null;
//
//    public void openAnim() {
//	if(f != null)
//	    if(!f.isShowing()) {
//		f = null;
//	    } else {
//		f.toFront();
//	    }
//	if(f == null) {
//	    f = new GraphAnimFrame("Animator", this);
//	}
//    }
//
//    public void getFaceFrame() {
//	if(f != null)
//	    f.getGraphanim().getFaceFrame();
//    }
//
//    public void startAnim() {
//	if(f != null)
//	    f.getGraphanim().startAnim();
//    }
//
//    // end CSP
//
//    public boolean keyUp(Event e, int key) {
//	switch (key) {
//	case Event.LEFT:
//            leftArrow = true;
//            prevKey = -1;
//            break;
//	case Event.RIGHT:
//            rightArrow = true;
//            prevKey = -1;
//            break;
//	case '{':
//            spin++;
//            break;
//	case '}':
//            spin--;
//            break;
//	case '\t':
//	    toggleNoise();
//	    break;
//	default:
//            if (key != prevKey) {
//		setFlex(key);
//		leftArrow = false;
//		rightArrow = false;
//            }
//            prevKey = key;
//            break;
//	}
//	return true;
//    }
//
//    boolean okToRender = false;
//    public void enableRender() { okToRender = true; }
//
//    //--- RENDERING ROUTINE, CALLED EVERY FRAME
//
//    public void render(Graphics g) {
//
//	//the JavaScript turns on rendering when it's done with init stuff
//	if (!okToRender)
//	    return;
//
//	//firstTime is set elsewhere to true for the first rendering, false after
//	boolean changed = firstTime;
//
//	//float of gray color - don't know why it's not named
//	float c = (float)128 / 255;
//	//background is white for wireframe, gray for rendered
//	bgcolor = isVectors ? Color.white : new Color(c,c,c);
//
//	// INITIALIZATION OF THE "MOVING" OBJECT HAPPENS HERE
//
//	if (firstTime) {
//	    firstTime = false;
//
//	    //set up keymaps, seems to overlap with javascript to do same
//	    createKeyMaps();
//	    //set nVertices to number of vertices - 1/3 of number of coords read
//	    nVertices = vertexVector.size()/3;
//
//	    //set up an array to hold the vertices
//	    vertexArray = new double[nVertices][3];
//	    //load it
//	    for (int v = 0 ; v < nVertices ; v++)
//		for (int j = 0 ; j < 3 ; j++)
//		    vertexArray[v][j] =
//			((Double)vertexVector.elementAt(3*v + j)).doubleValue();
//
//	    //set nShapes to number of shapes read from Javascript
//	    nShapes = shapesVector.size();
//
//	    //set up an array to hold shape data
//	    shape = new int[nShapes][][];
//	    for (int i = 0 ; i < nShapes ; i++) {
//		//put the shape we're looking at in a temporary var
//		//shapes(i) reads the ith shape from shapesVector
//		//create the array for shape i - an array of length
//		//number_of_faces_in_shape, each containing number_of_vertices
//		//_in_face integers identifying the appropriate vertices
//		//not sure what a face with only two vertices means
//		shapeVector = shapes(i);
//		shape[i] = new int[shapeVector.size()][];
//		for (int j = 0 ; j < shape[i].length ; j++) {
//		    face = (Vector)shapeVector.elementAt(j);
//		    shape[i][j] = new int[face.size()];
//		    for (int k = 0 ; k < shape[i][j].length ; k++)
//			shape[i][j][k] = ((Integer)face.elementAt(k)).intValue();
//		}
//	    }
//
//	    nFlexes = flexVector.size();
//
//	    flexData     = new double[nFlexes][][];
//	    flexShape    = new int   [nFlexes][];
//	    flexSymmetry = new int   [nFlexes];
//	    flexTarget   = new double[2][nFlexes];
//	    flexValue    = new double[2][nFlexes];
//	    flexSettle   = new double[nFlexes];
//
//	    for (int f = 0 ; f < nFlexes ; f++) {
//		flx = (Vector)flexVector.elementAt(f);
//		flexData    [f] = new double[flx.size()][];
//		flexShape   [f] = new int[flx.size()];
//		flexSymmetry[f] =
//		    ((Integer)flexSymmetryVector.elementAt(f)).intValue();
//		for (int j = 0 ; j < flexShape[f].length ; j++) {
//		    ixyz = (Vector)flx.elementAt(j);
//		    flexShape[f][j] = ((Integer)ixyz.elementAt(0)).intValue();
//		    flexData [f][j] = new double[3];
//		    for (int k = 1 ; k < ixyz.size() ; k++)
//			flexData[f][j][k-1] =
//			    ((Double)ixyz.elementAt(k)).doubleValue();
//		}
//		flexSettle[f] = ((Double)flexSettleVector.elementAt(f)).doubleValue();
//	    }
//
//	    flexTarget[0][3] = flexTarget[1][3] = -1;
//
//	    double[] jtrAmpl = {.08,.04,0, 0,0,.1,.07,.07,.15};
//	    double[] jtrFreq = {.25,.25,0, 0,0,.5,.5 ,.5 ,.5 };
//
//	    jitterAmpl = new double[nFlexes];
//	    jitterFreq = new double[nFlexes];
// 
//	    for (int f = 0 ; f < jtrAmpl.length && f < nFlexes ; f++) {
//		jitterAmpl[f] = jtrAmpl[f];
//		jitterFreq[f] = jtrFreq[f];
//	    }
//
//	    pts = new double[2][nVertices][3];
//	}
//
//	// FRAME-BY-FRAME STUFF FOR THE "MOVING" OBJECT HAPPENS HERE
//
//	int nFlexes = flexValue[0].length;
//	/* The background is blanked anyway - if we don't draw, the face disappears
//	for (int s = 0 ; s < 2 ; s++)
//	    for (int f = 0 ; f < nFlexes ; f++)
//		if (flexTarget[s][f] != flexValue[s][f] || (addNoise && jitterAmpl[f] != 0))
//		    changed = true;
//	if (! changed)
//	    return;
//	*/
//
//	// CSP: do frame rate stuff
//	// calculate frame rate based on elapsed time since last render
//	// find per-frame movement to accomplish move_p percent of movement
//	// in move_t seconds
//	// changed by lsanders to have separate settling times
//
//	double move_p = 0.95; //move 95% of the way during the settling time
//
//	long currTime = System.currentTimeMillis();
//	double secdiff = (currTime - lastTime)/1000.0;
//
//	/*
//	frameCount++;
//	long secdiff = currTime/1000 - lastTime/1000;
//	if(secdiff > 0) {
//	    dispCount = (int)(frameCount / secdiff);
//	    frameCount = 0;
//	    //	    System.out.println("frames: " + dispCount);
//	}
//
//	*/
//	lastTime = currTime;
//	// end CSP
//
//	double uipf, move_inc;
//	for (int s = 0 ; s < 2 ; s++)
//	    for (int f = 0 ; f < nFlexes ; f++) {
//		uipf = secdiff/flexSettle[f]; //what percentage of settle time has passed
//		move_inc = 1.0 - Math.pow((1.0 - move_p), uipf);
//
//		flexValue[s][f] += move_inc * (flexTarget[s][f] - flexValue[s][f]);
//		if (addNoise && jitterAmpl[f] != 0)
//		    flexValue[s][f] +=
//			jitterAmpl[f] * ImprovMath.noise(jitterFreq[f] * t + 10 * f);
//		t += .005;
//	    }
//
//	blinkValue = //!addNoise ? 0 :
//	    pulse(t/2 + ImprovMath.noise(t/1.5) + .5*ImprovMath.noise(t/3), .05);
//
//	doRender(g, flexValue, blinkValue, isVectors, doShade, shiftAxis);
//
//	//	g.setColor(Color.white);
//	//	g.drawString("frames: " + dispCount + ", inc: " + move_inc, 25, 380);
//    }
//
//    void doRender(Graphics g, double[][] flexValueLocal, double blinkValueLocal,
//		  boolean isVectorsLocal, boolean doShadeLocal, boolean shiftAxisLocal) {
//	synchronized(pts) {
//	    int[] x = new int[100];
//	    int[] y = new int[100];
//	    int[] z = new int[100];
//
//	    // temporary storage for vertices, split out by face side
//	    for (int s = 0 ; s < 2 ; s++)
//		for (int v = 0 ; v < vertexArray.length ; v++) {
//		    pts[s][v][0] = vertexArray[v][0];
//		    pts[s][v][1] = vertexArray[v][1];
//		    pts[s][v][2] = vertexArray[v][2];
//		    if (s == 1)
//			pts[s][v][0] = -pts[s][v][0];
//		}
//
//	    // calculate flex effects on vertices
//	    for (int f = 0 ; f < nFlexes ; f++)
//		for (int j = 0 ; j < flexShape[f].length ; j++) {
//		    int n = flexShape[f][j];
//		    double L = flexValueLocal[0][f];
//		    double R = flexValueLocal[1][f];
//		    if (f == 4 && blinkValueLocal == 1)
//			L = R = 1;
//		    pts[0][n][0] += L * flexData[f][j][0] * flexSymmetry[f];
//		    pts[1][n][0] += R * flexData[f][j][0];
//		    pts[0][n][1] += L * flexData[f][j][1];
//		    pts[1][n][1] += R * flexData[f][j][1];
//		    pts[0][n][2] += L * flexData[f][j][2];
//		    pts[1][n][2] += R * flexData[f][j][2];
//		}
//
//	    // COMPUTE TRANSFORMATION MATRICES
//
//	    double rotY = flexValueLocal[0][0];
//	    double rotX = flexValueLocal[0][1];
//	    double rotZ = flexValueLocal[0][2];
//
//	    // CSP: I don't know why, but weird errors occur if this is not here
//	    if(kludge) {
//		System.out.println("first time");
//		kludge = false;
//	    }
//
//	    double theta = -.2*rotY - .1*spin;
//	    theta = ((theta + Math.PI + 1000 * Math.PI) % (2*Math.PI)) - Math.PI;
//
//	    Matrix3D m1 = new Matrix3D();
//	    m1.scale(height/110.,-height/110.,height/110.);
//	    m1.translate(35,-80,shiftAxisLocal ? 20 : 5);
//	    m1.rotateX(-.2*rotX);
//	    m1.rotateY(theta);
//	    m1.rotateZ(-.2*rotZ);
//	    m1.translate(0,-30,shiftAxisLocal ? -20 : -5);
//
//	    Matrix3D m2 = new Matrix3D();
//	    m2.scale(height/110.,-height/110.,height/110.);
//	    m2.translate(35,-110,0);
//
//	    Vector3D v = new Vector3D();
//
//	    // RENDER THE TWO SIDES OF FACE
//
//	    int s = (theta < 0) ? 1 : 0;      // which side is rendered first depends on head rotation
//	    for (int _s = 0 ; _s <= 1 ; _s++, s = 1-s)
//		for (int i = 0 ; i < shape.length ; i++) {
//
//		    int type = type(i);
//		    g.setColor(color(i, isVectorsLocal));
//
//		    for (int j = 0 ; j < shape[i].length ; j++) {
//
//			int nk = shape[i][j].length;
//			if (type == POLYGON && nk == 2 && _s == 1)
//			    continue;
//
//			for (int k = 0 ; k < nk ; k++) {
//			    int n = shape[i][j][k];
//			    v.set(pts[s][n][0], pts[s][n][1], pts[s][n][2]);
//			    v.transform(n < noRotateIndex ? m1 : m2);
//			    x[k] = (int)v.get(0);
//			    y[k] = (int)v.get(1);
//			    if (doShadeLocal)
//				z[k] = (int)v.get(2);
//			}
//
//			if (type == POLYGON && nk == 2) {
//			    nk = 4;
//			    for (int k = 0 ; k < 2 ; k++) {
//				int n = shape[i][j][k];
//				v.set(pts[1-s][n][0],pts[1-s][n][1],pts[1-s][n][2]);
//				v.transform(n < noRotateIndex ? m1 : m2);
//				x[3-k] = (int)v.get(0);
//				y[3-k] = (int)v.get(1);
//				if (doShadeLocal)
//				    z[3-k] = (int)v.get(2);
//			    }
//			}
//
//			switch (type) {
//			case CIRCLE:
//			    if (Math.abs(theta)>2 || theta>.9 && s==0 || theta<-.9 && s==1)
//				break;
//			    int dx = x[0]-x[1], dy = y[0]-y[1], r = dx*dx + dy*dy >= 51 ? 12 : 6;
//			    if (isVectorsLocal)
//				g.drawOval(x[0]-r, y[0]-r, 2*r, 2*r);
//			    else
//				g.fillOval(x[0]-r, y[0]-r, 2*r, 2*r);
//			    break;
//			case POLYLINE:
//			    if (!isVectorsLocal && !(Math.abs(theta)>2 || theta>1.1 && s==0 || theta<-1.1 && s==1))
//				g.drawPolygon(x, y, nk);
//			    break;
//			case POLYGON:
//			    if (isVectorsLocal && !((Color)colorVector.elementAt(i)).equals(Color.black)
//				&& shape[i][j][0] < noRotateIndex) {
//				x[nk] = x[0];
//				y[nk] = y[0];
//				g.drawPolygon(x, y, nk+1);
//			    }
//			    if (!isVectorsLocal && (area(x, y, nk) > 0) == (s == 0)) {
//				if (doShadeLocal) {
//				    Color color = color(i, isVectorsLocal);
//				    int R = color.getRed(), G = color.getGreen(), B = color.getBlue();
//				    int S = getShade(x,y,z);
//				    R = Math.min(255, S*R >> 8);
//				    G = Math.min(255, S*G >> 8);
//				    B = Math.min(255, S*B >> 8);
//				    g.setColor(new Color(R, G, B));
//				}
//				g.fillPolygon(x, y, nk);
//			    }
//			    break;
//			}
//		    }
//		}
//	}
//    }
//
//    //---- INTERNAL METHODS
//
//    void setFlex(int key) {
//	if (! leftArrow)
//	    flexTarget[0][keyFlex[key]] = keyValue[key];
//	if (! rightArrow)
//	    flexTarget[1][keyFlex[key]] = keyValue[key];
//    }
//
//    int area(int[] x, int[] y, int n) {
//	int area = 0;
//	int j;
//	for (int i = 0 ; i < n ; i++) {
//	    j = (i+1) % n;
//	    area += (x[i] - x[j]) * (y[i] + y[j]);
//	}
//	return area / 2;
//    }
//
//    double pulse(double t,double epsilon) { return (t-(int)t)<epsilon ? 1 : 0; }
//
//    //---- INTERNAL VARIABLES
//
//    // vertices are combined into polygons, polylines, and circles
//    // vertices are grouped together into flexes, and given offsetx which are multiplied by -1 to 1
//
//    boolean firstVertices = true, isVectors = false, addNoise = true, doShade = false, shiftAxis = false;
//
//    int nVertices, nShapes, nFlexes;
//
//    int[]    keyFlex   = new int[256];
//    double[] keyValue = new double[256];
//
//    double[][] flexValue, flexTarget;
//    double[] jitterAmpl, jitterFreq, flexSettle;
//
//    boolean firstTime = true;
//    boolean kludge = true;     // CSP: see kludge comment in doRender()
//
//    final int POLYGON = 0;
//    final int POLYLINE = 1;
//    final int CIRCLE = 2;
//
//    double[][][] flexData;        // array of double[3] offsets for each vertex of each shape
//    int[][]      flexShape;       // array of vertices which make up shapes
//    int          flexSymmetry[];
//    double[][][] pts;
//    int[][][]    shape;           // array of vertices of subparts of shapes
//    double[][]   vertexArray;     // array of double[3] of vertices
//
//    double t = 0, blinkValue = 0;
//    int spin = 0, noRotateIndex = 1000;
//
//    boolean leftArrow = false, rightArrow = false;
//
//    int prevKey = -1;
//
//    long lastTime;
//    int frameCount, dispCount;
//
////---- INTERNAL VECTORS USED WHILE PARSING
//
//    Vector colorVector        = new Vector();
//    Vector flexNamesVector    = new Vector();                      // list of flexes
//    Vector flexSettleVector	= new Vector();				 // list of settle times
//    Vector flexVector         = new Vector(), flx, ixyz;           // Vector of Vectors; inner vector first value is vertex, rest are flex offsets
//    Vector flexSymmetryVector = new Vector();
//    Vector shapesVector       = new Vector(), shapeVector, face;   // list of shapes; Vector of Vector of Vectors
//    Vector typeVector         = new Vector();                      
//    Vector vertexVector       = new Vector();                      // list of vertices; each x, y, z triple is 3 entries; converted to vertexArray
//
//    void createKeyMaps() {
//	map("'","brows",  1); map("_","brows",  0); map("`","brows", -1);
//	map("@","blink",-.9); map("+","blink",  0); map("=","blink", .5);
//	map("-","blink",  1);
//	map("t","lids" ,  1); map("c","lids" ,  0); map("b","lids" , -1);
//
//	map("l","lookX", -1); map(".","lookX",  0); map("r","lookX",  1);
//	map("u","lookY",  1); map(":","lookY",  0); map("d","lookY", -1);
//	map("<","turn" ,  1); map("!","turn" ,  0); map(">","turn" , -1);
//	map("^","nod"  ,  1); map("~","nod"  ,  0); map("v","nod"  , -1);
//	map("\\","tilt", -1); map("|","tilt" ,  0); map("/","tilt" ,  1);
//
//	map("m","sayAh", -1); map("e","sayAh",-.5); map("o","sayAh",  0);
//	map("O","sayAh", .5);
//	map("p","sayOo", .8); map("a","sayOo",  0); map("w","sayOo",-.7);
//	map("s","smile",  1); map("n","smile",  0); map("f","smile",-.7);
//	map("i","sneer",-.5); map("x","sneer",  0); map("h","sneer", .5);
//	map("z","sneer",  1);
//    }
//
//    // 3D Shading
//
//    int getShade(int[] x, int[] y, int[] z) {
//	int ax = x[1] - x[0], ay = y[1] - y[0], az = z[1] - z[0];
//	int bx = x[2] - x[1], by = y[2] - y[1], bz = z[2] - z[1];
//	int cx = ay * bz - az * by, cy = az * bx - ax * bz, cz = ax * by - ay * bx;
//	int N = cx - cy/2 + cz;
//	N = 8 * N * N / (cx*cx + cy*cy + cz*cz);
//	return 192 + 8 * N;
//    }
//
//    // Routines for dianostic printout
//
//    void printVertices() {
//	System.out.println("VERTICES:");
//	for (int v = 0 ; v < nVertices ; v++) {
//	    System.out.print("   " + v + ": ");
//	    for (int j = 0 ; j < 3 ; j++) {
//		System.out.print(vertexArray[v][j]);
//		if (j < 2)
//		    System.out.print(",");
//	    }
//	    System.out.println();
//	}
//	System.out.println();
//    }
//
//    void printFaces() {
//	System.out.println("FACES:");
//	for (int i = 0 ; i < nShapes ; i++) {
//	    System.out.print("   {");
//	    for (int j = 0 ; j < shape[i].length ; j++) {
//		System.out.print(" ");
//		for (int k = 0 ; k < shape[i][j].length ; k++) {
//		    System.out.print(shape[i][j][k]);
//		    if (k < shape[i][j].length-1)
//			System.out.print(",");
//		}
//	    }
//	    System.out.println("}");
//	}
//	System.out.println();
//    }
//
//    void printShapes() {
//	System.out.println("SHAPES:");
//	for (int i = 0 ; i < nShapes ; i++) {
//	    System.out.println();
//	    for (int j = 0 ; j < shape[i].length ; j++) {
//		System.out.print("{");
//		for (int k = 0 ; k < shape[i][j].length ; k++) {
//		    int n = shape[i][j][k];
//		    System.out.print(vertexArray[n][0] + " " +
//				     vertexArray[n][1] + " " +
//				     vertexArray[n][2]);
//		    if (k < shape[i][j].length-1)
//			System.out.print(" ");
//		}
//		System.out.println("}");
//	    }
//	}
//	System.out.println();
//    }
//}
//
