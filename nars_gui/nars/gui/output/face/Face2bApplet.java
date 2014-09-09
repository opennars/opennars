package nars.gui.output.face;

import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import static java.lang.Thread.sleep;
import java.util.StringTokenizer;
import java.util.Vector;

/** @author http://cs.nyu.edu/~lhs215/multimedia/face/ */
public class Face2bApplet extends Animator
{
    //---- PUBLIC INTERFACE:

    // MAP A KEY TO A FLEX VALUE

    public void map(String s, String flexName, double f) {
	for (int i = 0 ; i < flexNamesVector.size() ; i++)
	    if (((String)flexNamesVector.elementAt(i)).equals(flexName)) {
		char key = s.charAt(0);
		keyFlex[(int)key] = i;
		keyValue[(int)key] = f;
		return;
	    }
    }

    // ALL THIS PARSING STUFF GOES IN THE "MOVING" OBJECT

    public void set(String s) {
	for (int i = 0 ; i < s.length() ; i++)
	    if (s.charAt(i) == '[')
		leftArrow = true;
	    else if (s.charAt(i) == ']')
		rightArrow = true;
	    else if (s.charAt(i) >= '0' && s.charAt(i) <= '9')
		sleep(100 * (s.charAt(i) - '0'));
	    else {
		setFlex((int)s.charAt(i));
		leftArrow = rightArrow = false;
	    }
    }
    public void toggleShade() {
	doShade = isVectors ? true : !doShade;
	if (isVectors)
	    toggleVectors();
    }
    public void toggleNoise() { addNoise = ! addNoise; }
    public void toggleVectors() { isVectors = ! isVectors; }
    public void noRotate() { noRotateIndex = vertexVector.size() / 3; }

    public void polygons (double r, double g, double b, String str)
    { addFaces(POLYGON,  r,g,b, str); }
    public void polylines(double r, double g, double b, String str)
    { addFaces(POLYLINE, r,g,b, str); }
    public void circles  (double r, double g, double b, String str)
    { addFaces(CIRCLE,   r,g,b, str); }

    // PARSE ITEMS OF THE FORM vertices("x,y,z x,y,z ...")

    public void vertices(String str)
    {
	StringTokenizer arg, word;

	if (firstVertices) {
	    firstVertices = false;
	    flex("turn","1","","");
	    flex("nod" ,"1","","");
	    flex("tilt","1","","");
	}

	if (str != null)
	    for (arg = new StringTokenizer(str) ; arg.hasMoreTokens() ; ) {
		word = new StringTokenizer(arg.nextToken(), ",");
		vertexVector.addElement(new Double(word.nextToken()));
		vertexVector.addElement(new Double(word.nextToken()));
		vertexVector.addElement(new Double(word.nextToken()));
	    }
    }

    // PARSE SHAPE SPECIFIERS: addFaces(type, r, g, b, "a,b,c d,e,f,g ...")
    // WHERE a,b,c,d,e,f... ARE VERTEX INDICES.

    int      type(int i) { return ((Integer)typeVector.elementAt(i)).intValue(); }
    Color   color(int i, boolean vec) { return vec ? Color.black : (Color)colorVector.elementAt(i); }
    Vector shapes(int i) { return (Vector)shapesVector.elementAt(i); }

    public void addFaces(int type, double r, double g, double b, String str)
    {
	StringTokenizer fs, f;

	typeVector.addElement(new Integer(type));
	colorVector.addElement(new Color((float)r,(float)g,(float)b));
	shapesVector.addElement(shapeVector = new Vector());
	if (str != null)
	    for (fs = new StringTokenizer(str) ; fs.hasMoreTokens() ; ) {
		shapeVector.addElement(face = new Vector());
		for (f=new StringTokenizer(fs.nextToken(),","); f.hasMoreTokens(); )
		    face.addElement(new Integer(f.nextToken()));
	    }
    }

    // SPECIFY THAT THE FLEX WE'VE JUST SPECIFIED IS HORIZONTALLY ASSYMETRIC

    public void assymetric() {
	int lastFlex = flexVector.size()-1;
	flexSymmetryVector.setElementAt(new Integer(1), lastFlex);
    }

    // PARSE FLEX SPECIFIERS OF THE FORM:
    //    flex("name", "settle_time", "a b c ...", "x,y,z x,y,z x,y,z ...")

    // WHERE a,b,c... ARE VERTEX INDICES, and x,y,z ARE DISPLACEMENTS.

    public void flex(String name, String settle, String id, String xyz) {
	StringTokenizer s, t;

	flexNamesVector.addElement(name);
	flexSettleVector.addElement(new Double(settle));
	flexSymmetryVector.addElement(new Integer(-1));
	flexVector.addElement(flx = new Vector());
	for (s = new StringTokenizer(id) ; s.hasMoreTokens() ; ) {
	    flx.addElement(ixyz = new Vector());
	    ixyz.addElement(new Integer(s.nextToken()));
	}
	int i = 0;
	for (s = new StringTokenizer(xyz) ; s.hasMoreTokens() ; ) {
	    ixyz = (Vector)flx.elementAt(i++);
	    for (t=new StringTokenizer(s.nextToken(), ",") ; t.hasMoreTokens() ; )
		ixyz.addElement(new Double(t.nextToken()));
	}
    }

    // CSP:
    // getTargets() return current flex targets for Animation applet
    // setTargets() sets the targets
    // getSnapshotImage() returns an image of size w by h described
    //   by the values in flexVals
    // openAnim() opens the animation applet in a frame

    

    public double[][] getTargets() {
	double[][] retVal = new double[2][flexTarget[0].length];
	for(int s = 0; s < 2; s++)
	    for(int v = 0; v < flexTarget[0].length; v++)
		retVal[s][v] = flexTarget[s][v];
	return retVal;
    }

    public void setTargets(double[][] inVal) {
	for(int s = 0; s < 2; s++)
	    for(int v = 0; v < flexTarget[0].length; v++)
		flexTarget[s][v] = inVal[s][v];
    }

    public Image getSnapshotImage(double[][] flexVals) {
	Image temp = createImage(bounds().width, bounds().height);
	Graphics tempg = temp.getGraphics();
	doRender(tempg, flexVals, 0, false, false, false);

	tempg.dispose();
	return temp;
    }

    public void init() {
	super.init();
	setBackground(Color.white);
	lastTime = System.currentTimeMillis();
	frameCount = 0;
	dispCount = 0;
    }

    GraphAnimFrame f = null;

    public void openAnim() {
	if(f != null)
	    if(!f.isShowing()) {
		f = null;
	    } else {
		f.toFront();
	    }
	if(f == null) {
	    f = new GraphAnimFrame("Animator", this);
	}
    }

    public void getFaceFrame() {
	if(f != null)
	    f.getGraphanim().getFaceFrame();
    }

    public void startAnim() {
	if(f != null)
	    f.getGraphanim().startAnim();
    }

    // end CSP

    public boolean keyUp(Event e, int key) {
	switch (key) {
	case Event.LEFT:
            leftArrow = true;
            prevKey = -1;
            break;
	case Event.RIGHT:
            rightArrow = true;
            prevKey = -1;
            break;
	case '{':
            spin++;
            break;
	case '}':
            spin--;
            break;
	case '\t':
	    toggleNoise();
	    break;
	default:
            if (key != prevKey) {
		setFlex(key);
		leftArrow = false;
		rightArrow = false;
            }
            prevKey = key;
            break;
	}
	return true;
    }

    boolean okToRender = false;
    public void enableRender() { okToRender = true; }

    //--- RENDERING ROUTINE, CALLED EVERY FRAME

    public void render(Graphics g) {

	//the JavaScript turns on rendering when it's done with init stuff
	if (!okToRender)
	    return;

	//firstTime is set elsewhere to true for the first rendering, false after
	boolean changed = firstTime;

	//float of gray color - don't know why it's not named
	float c = (float)128 / 255;
	//background is white for wireframe, gray for rendered
	bgcolor = isVectors ? Color.white : new Color(c,c,c);

	// INITIALIZATION OF THE "MOVING" OBJECT HAPPENS HERE

	if (firstTime) {
	    firstTime = false;

	    //set up keymaps, seems to overlap with javascript to do same
	    createKeyMaps();
	    //set nVertices to number of vertices - 1/3 of number of coords read
	    nVertices = vertexVector.size()/3;

	    //set up an array to hold the vertices
	    vertexArray = new double[nVertices][3];
	    //load it
	    for (int v = 0 ; v < nVertices ; v++)
		for (int j = 0 ; j < 3 ; j++)
		    vertexArray[v][j] =
			((Double)vertexVector.elementAt(3*v + j)).doubleValue();

	    //set nShapes to number of shapes read from Javascript
	    nShapes = shapesVector.size();

	    //set up an array to hold shape data
	    shape = new int[nShapes][][];
	    for (int i = 0 ; i < nShapes ; i++) {
		//put the shape we're looking at in a temporary var
		//shapes(i) reads the ith shape from shapesVector
		//create the array for shape i - an array of length
		//number_of_faces_in_shape, each containing number_of_vertices
		//_in_face integers identifying the appropriate vertices
		//not sure what a face with only two vertices means
		shapeVector = shapes(i);
		shape[i] = new int[shapeVector.size()][];
		for (int j = 0 ; j < shape[i].length ; j++) {
		    face = (Vector)shapeVector.elementAt(j);
		    shape[i][j] = new int[face.size()];
		    for (int k = 0 ; k < shape[i][j].length ; k++)
			shape[i][j][k] = ((Integer)face.elementAt(k)).intValue();
		}
	    }

	    nFlexes = flexVector.size();

	    flexData     = new double[nFlexes][][];
	    flexShape    = new int   [nFlexes][];
	    flexSymmetry = new int   [nFlexes];
	    flexTarget   = new double[2][nFlexes];
	    flexValue    = new double[2][nFlexes];
	    flexSettle   = new double[nFlexes];

	    for (int f = 0 ; f < nFlexes ; f++) {
		flx = (Vector)flexVector.elementAt(f);
		flexData    [f] = new double[flx.size()][];
		flexShape   [f] = new int[flx.size()];
		flexSymmetry[f] =
		    ((Integer)flexSymmetryVector.elementAt(f)).intValue();
		for (int j = 0 ; j < flexShape[f].length ; j++) {
		    ixyz = (Vector)flx.elementAt(j);
		    flexShape[f][j] = ((Integer)ixyz.elementAt(0)).intValue();
		    flexData [f][j] = new double[3];
		    for (int k = 1 ; k < ixyz.size() ; k++)
			flexData[f][j][k-1] =
			    ((Double)ixyz.elementAt(k)).doubleValue();
		}
		flexSettle[f] = ((Double)flexSettleVector.elementAt(f)).doubleValue();
	    }

	    flexTarget[0][3] = flexTarget[1][3] = -1;

	    double[] jtrAmpl = {.08,.04,0, 0,0,.1,.07,.07,.15};
	    double[] jtrFreq = {.25,.25,0, 0,0,.5,.5 ,.5 ,.5 };

	    jitterAmpl = new double[nFlexes];
	    jitterFreq = new double[nFlexes];
 
	    for (int f = 0 ; f < jtrAmpl.length && f < nFlexes ; f++) {
		jitterAmpl[f] = jtrAmpl[f];
		jitterFreq[f] = jtrFreq[f];
	    }

	    pts = new double[2][nVertices][3];
	}

	// FRAME-BY-FRAME STUFF FOR THE "MOVING" OBJECT HAPPENS HERE

	int nFlexes = flexValue[0].length;
	/* The background is blanked anyway - if we don't draw, the face disappears
	for (int s = 0 ; s < 2 ; s++)
	    for (int f = 0 ; f < nFlexes ; f++)
		if (flexTarget[s][f] != flexValue[s][f] || (addNoise && jitterAmpl[f] != 0))
		    changed = true;
	if (! changed)
	    return;
	*/

	// CSP: do frame rate stuff
	// calculate frame rate based on elapsed time since last render
	// find per-frame movement to accomplish move_p percent of movement
	// in move_t seconds
	// changed by lsanders to have separate settling times

	double move_p = 0.95; //move 95% of the way during the settling time

	long currTime = System.currentTimeMillis();
	double secdiff = (currTime - lastTime)/1000.0;

	/*
	frameCount++;
	long secdiff = currTime/1000 - lastTime/1000;
	if(secdiff > 0) {
	    dispCount = (int)(frameCount / secdiff);
	    frameCount = 0;
	    //	    System.out.println("frames: " + dispCount);
	}

	*/
	lastTime = currTime;
	// end CSP

	double uipf, move_inc;
	for (int s = 0 ; s < 2 ; s++)
	    for (int f = 0 ; f < nFlexes ; f++) {
		uipf = secdiff/flexSettle[f]; //what percentage of settle time has passed
		move_inc = 1.0 - Math.pow((1.0 - move_p), uipf);

		flexValue[s][f] += move_inc * (flexTarget[s][f] - flexValue[s][f]);
		if (addNoise && jitterAmpl[f] != 0)
		    flexValue[s][f] +=
			jitterAmpl[f] * ImprovMath.noise(jitterFreq[f] * t + 10 * f);
		t += .005;
	    }

	blinkValue = //!addNoise ? 0 :
	    pulse(t/2 + ImprovMath.noise(t/1.5) + .5*ImprovMath.noise(t/3), .05);

	doRender(g, flexValue, blinkValue, isVectors, doShade, shiftAxis);

	//	g.setColor(Color.white);
	//	g.drawString("frames: " + dispCount + ", inc: " + move_inc, 25, 380);
    }

    void doRender(Graphics g, double[][] flexValueLocal, double blinkValueLocal,
		  boolean isVectorsLocal, boolean doShadeLocal, boolean shiftAxisLocal) {
	synchronized(pts) {
	    int[] x = new int[100];
	    int[] y = new int[100];
	    int[] z = new int[100];

	    // temporary storage for vertices, split out by face side
	    for (int s = 0 ; s < 2 ; s++)
		for (int v = 0 ; v < vertexArray.length ; v++) {
		    pts[s][v][0] = vertexArray[v][0];
		    pts[s][v][1] = vertexArray[v][1];
		    pts[s][v][2] = vertexArray[v][2];
		    if (s == 1)
			pts[s][v][0] = -pts[s][v][0];
		}

	    // calculate flex effects on vertices
	    for (int f = 0 ; f < nFlexes ; f++)
		for (int j = 0 ; j < flexShape[f].length ; j++) {
		    int n = flexShape[f][j];
		    double L = flexValueLocal[0][f];
		    double R = flexValueLocal[1][f];
		    if (f == 4 && blinkValueLocal == 1)
			L = R = 1;
		    pts[0][n][0] += L * flexData[f][j][0] * flexSymmetry[f];
		    pts[1][n][0] += R * flexData[f][j][0];
		    pts[0][n][1] += L * flexData[f][j][1];
		    pts[1][n][1] += R * flexData[f][j][1];
		    pts[0][n][2] += L * flexData[f][j][2];
		    pts[1][n][2] += R * flexData[f][j][2];
		}

	    // COMPUTE TRANSFORMATION MATRICES

	    double rotY = flexValueLocal[0][0];
	    double rotX = flexValueLocal[0][1];
	    double rotZ = flexValueLocal[0][2];

	    // CSP: I don't know why, but weird errors occur if this is not here
	    if(kludge) {
		System.out.println("first time");
		kludge = false;
	    }

	    double theta = -.2*rotY - .1*spin;
	    theta = ((theta + Math.PI + 1000 * Math.PI) % (2*Math.PI)) - Math.PI;

	    Matrix3D m1 = new Matrix3D();
	    m1.scale(height/110.,-height/110.,height/110.);
	    m1.translate(35,-80,shiftAxisLocal ? 20 : 5);
	    m1.rotateX(-.2*rotX);
	    m1.rotateY(theta);
	    m1.rotateZ(-.2*rotZ);
	    m1.translate(0,-30,shiftAxisLocal ? -20 : -5);

	    Matrix3D m2 = new Matrix3D();
	    m2.scale(height/110.,-height/110.,height/110.);
	    m2.translate(35,-110,0);

	    Vector3D v = new Vector3D();

	    // RENDER THE TWO SIDES OF FACE

	    int s = (theta < 0) ? 1 : 0;      // which side is rendered first depends on head rotation
	    for (int _s = 0 ; _s <= 1 ; _s++, s = 1-s)
		for (int i = 0 ; i < shape.length ; i++) {

		    int type = type(i);
		    g.setColor(color(i, isVectorsLocal));

		    for (int j = 0 ; j < shape[i].length ; j++) {

			int nk = shape[i][j].length;
			if (type == POLYGON && nk == 2 && _s == 1)
			    continue;

			for (int k = 0 ; k < nk ; k++) {
			    int n = shape[i][j][k];
			    v.set(pts[s][n][0], pts[s][n][1], pts[s][n][2]);
			    v.transform(n < noRotateIndex ? m1 : m2);
			    x[k] = (int)v.get(0);
			    y[k] = (int)v.get(1);
			    if (doShadeLocal)
				z[k] = (int)v.get(2);
			}

			if (type == POLYGON && nk == 2) {
			    nk = 4;
			    for (int k = 0 ; k < 2 ; k++) {
				int n = shape[i][j][k];
				v.set(pts[1-s][n][0],pts[1-s][n][1],pts[1-s][n][2]);
				v.transform(n < noRotateIndex ? m1 : m2);
				x[3-k] = (int)v.get(0);
				y[3-k] = (int)v.get(1);
				if (doShadeLocal)
				    z[3-k] = (int)v.get(2);
			    }
			}

			switch (type) {
			case CIRCLE:
			    if (Math.abs(theta)>2 || theta>.9 && s==0 || theta<-.9 && s==1)
				break;
			    int dx = x[0]-x[1], dy = y[0]-y[1], r = dx*dx + dy*dy >= 51 ? 12 : 6;
			    if (isVectorsLocal)
				g.drawOval(x[0]-r, y[0]-r, 2*r, 2*r);
			    else
				g.fillOval(x[0]-r, y[0]-r, 2*r, 2*r);
			    break;
			case POLYLINE:
			    if (!isVectorsLocal && !(Math.abs(theta)>2 || theta>1.1 && s==0 || theta<-1.1 && s==1))
				g.drawPolygon(x, y, nk);
			    break;
			case POLYGON:
			    if (isVectorsLocal && !((Color)colorVector.elementAt(i)).equals(Color.black)
				&& shape[i][j][0] < noRotateIndex) {
				x[nk] = x[0];
				y[nk] = y[0];
				g.drawPolygon(x, y, nk+1);
			    }
			    if (!isVectorsLocal && (area(x, y, nk) > 0) == (s == 0)) {
				if (doShadeLocal) {
				    Color color = color(i, isVectorsLocal);
				    int R = color.getRed(), G = color.getGreen(), B = color.getBlue();
				    int S = getShade(x,y,z);
				    R = Math.min(255, S*R >> 8);
				    G = Math.min(255, S*G >> 8);
				    B = Math.min(255, S*B >> 8);
				    g.setColor(new Color(R, G, B));
				}
				g.fillPolygon(x, y, nk);
			    }
			    break;
			}
		    }
		}
	}
    }

    //---- INTERNAL METHODS

    void setFlex(int key) {
	if (! leftArrow)
	    flexTarget[0][keyFlex[key]] = keyValue[key];
	if (! rightArrow)
	    flexTarget[1][keyFlex[key]] = keyValue[key];
    }

    int area(int[] x, int[] y, int n) {
	int area = 0;
	int j;
	for (int i = 0 ; i < n ; i++) {
	    j = (i+1) % n;
	    area += (x[i] - x[j]) * (y[i] + y[j]);
	}
	return area / 2;
    }

    double pulse(double t,double epsilon) { return (t-(int)t)<epsilon ? 1 : 0; }

    //---- INTERNAL VARIABLES

    // vertices are combined into polygons, polylines, and circles
    // vertices are grouped together into flexes, and given offsetx which are multiplied by -1 to 1

    boolean firstVertices = true, isVectors = false, addNoise = true, doShade = false, shiftAxis = false;

    int nVertices, nShapes, nFlexes;

    int[]    keyFlex   = new int[256];
    double[] keyValue = new double[256];

    double[][] flexValue, flexTarget;
    double[] jitterAmpl, jitterFreq, flexSettle;

    boolean firstTime = true;
    boolean kludge = true;     // CSP: see kludge comment in doRender()

    final int POLYGON = 0;
    final int POLYLINE = 1;
    final int CIRCLE = 2;

    double[][][] flexData;        // array of double[3] offsets for each vertex of each shape
    int[][]      flexShape;       // array of vertices which make up shapes
    int          flexSymmetry[];
    double[][][] pts;
    int[][][]    shape;           // array of vertices of subparts of shapes
    double[][]   vertexArray;     // array of double[3] of vertices

    double t = 0, blinkValue = 0;
    int spin = 0, noRotateIndex = 1000;

    boolean leftArrow = false, rightArrow = false;

    int prevKey = -1;

    long lastTime;
    int frameCount, dispCount;

//---- INTERNAL VECTORS USED WHILE PARSING

    Vector colorVector        = new Vector();
    Vector flexNamesVector    = new Vector();                      // list of flexes
    Vector flexSettleVector	= new Vector();				 // list of settle times
    Vector flexVector         = new Vector(), flx, ixyz;           // Vector of Vectors; inner vector first value is vertex, rest are flex offsets
    Vector flexSymmetryVector = new Vector();
    Vector shapesVector       = new Vector(), shapeVector, face;   // list of shapes; Vector of Vector of Vectors
    Vector typeVector         = new Vector();                      
    Vector vertexVector       = new Vector();                      // list of vertices; each x, y, z triple is 3 entries; converted to vertexArray

    void createKeyMaps() {
	map("'","brows",  1); map("_","brows",  0); map("`","brows", -1);
	map("@","blink",-.9); map("+","blink",  0); map("=","blink", .5);
	map("-","blink",  1);
	map("t","lids" ,  1); map("c","lids" ,  0); map("b","lids" , -1);

	map("l","lookX", -1); map(".","lookX",  0); map("r","lookX",  1);
	map("u","lookY",  1); map(":","lookY",  0); map("d","lookY", -1);
	map("<","turn" ,  1); map("!","turn" ,  0); map(">","turn" , -1);
	map("^","nod"  ,  1); map("~","nod"  ,  0); map("v","nod"  , -1);
	map("\\","tilt", -1); map("|","tilt" ,  0); map("/","tilt" ,  1);

	map("m","sayAh", -1); map("e","sayAh",-.5); map("o","sayAh",  0);
	map("O","sayAh", .5);
	map("p","sayOo", .8); map("a","sayOo",  0); map("w","sayOo",-.7);
	map("s","smile",  1); map("n","smile",  0); map("f","smile",-.7);
	map("i","sneer",-.5); map("x","sneer",  0); map("h","sneer", .5);
	map("z","sneer",  1);
    }

    // 3D Shading

    int getShade(int[] x, int[] y, int[] z) {
	int ax = x[1] - x[0], ay = y[1] - y[0], az = z[1] - z[0];
	int bx = x[2] - x[1], by = y[2] - y[1], bz = z[2] - z[1];
	int cx = ay * bz - az * by, cy = az * bx - ax * bz, cz = ax * by - ay * bx;
	int N = cx - cy/2 + cz;
	N = 8 * N * N / (cx*cx + cy*cy + cz*cz);
	return 192 + 8 * N;
    }

    // Routines for dianostic printout

    void printVertices() {
	System.out.println("VERTICES:");
	for (int v = 0 ; v < nVertices ; v++) {
	    System.out.print("   " + v + ": ");
	    for (int j = 0 ; j < 3 ; j++) {
		System.out.print(vertexArray[v][j]);
		if (j < 2)
		    System.out.print(",");
	    }
	    System.out.println();
	}
	System.out.println();
    }

    void printFaces() {
	System.out.println("FACES:");
	for (int i = 0 ; i < nShapes ; i++) {
	    System.out.print("   {");
	    for (int j = 0 ; j < shape[i].length ; j++) {
		System.out.print(" ");
		for (int k = 0 ; k < shape[i][j].length ; k++) {
		    System.out.print(shape[i][j][k]);
		    if (k < shape[i][j].length-1)
			System.out.print(",");
		}
	    }
	    System.out.println("}");
	}
	System.out.println();
    }

    void printShapes() {
	System.out.println("SHAPES:");
	for (int i = 0 ; i < nShapes ; i++) {
	    System.out.println();
	    for (int j = 0 ; j < shape[i].length ; j++) {
		System.out.print("{");
		for (int k = 0 ; k < shape[i][j].length ; k++) {
		    int n = shape[i][j][k];
		    System.out.print(vertexArray[n][0] + " " +
				     vertexArray[n][1] + " " +
				     vertexArray[n][2]);
		    if (k < shape[i][j].length-1)
			System.out.print(" ");
		}
		System.out.println("}");
	    }
	}
	System.out.println();
    }
}

