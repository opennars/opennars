/**
 * Ben F Rayfield offers this software opensource GNU GPL 2+
 */
package smartblob.smartblob.ui;


import smartblob.Smartblob;
import smartblob.SmartblobUtil;
import smartblob.blobs.layeredzigzag.CornerData;
import smartblob.blobs.layeredzigzag.LayeredZigzag;
import smartblob.blobs.layeredzigzag.Line;
import smartblob.blobs.layeredzigzag.TriData;
import smartblob.common.CoreUtil;
import smartblob.common.Nanotimer;
import smartblob.realtimeschedulerTodoThreadpool.RealtimeScheduler;
import smartblob.realtimeschedulerTodoThreadpool.Task;
import smartblob.smartblob.physics.ChangeSpeed;
import smartblob.smartblob.physics.GlobalChangeSpeed;
import smartblob.smartblob.physics.SmartblobSim;
import smartblob.smartblob.physics.globalparts.BounceOnSimpleWall;
import smartblob.smartblob.physics.smartblobparts.Push;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

/**
 * Many smartblobs can bounce and reshape and grab eachother as tools on screen
 */
public class SmartblobsPanel extends JComponent implements MouseMotionListener, MouseListener, Task {

    public static final BasicStroke defaultStrokeThin = new BasicStroke(1f);
    public static final BasicStroke defaultStrokeThick = new BasicStroke(2f);

    /**
     * Changes to sim.smartblobs must be synchronized. It is when painted.
     */
    public final SmartblobSim sim;

    public boolean paintPartsRandomly = true;

    protected final Nanotimer timer = new Nanotimer();

    protected double lastTimeMouseMoved = CoreUtil.time();

    //public double simulateThisManySecondsAfterMouseMove = 0;
    //public double simulateThisManySecondsAfterMouseMove = 20;
    //public double simulateThisManySecondsAfterMouseMove = 2e50;

    /**
     * target frame time
     */
    public static final double framePerSec = 80;

    public static final float simTimePerCycle = 0.05f;

    public int subCyclesPerCycle = 500; //increasing increases simulation accuracy


    //public int simCyclesPerDraw = 30;
    //public int simCyclesPerDraw = 100;
    //public int simCyclesPerDraw = 5;

    public boolean drawBoundingRectangles = false;

    public boolean drawBoundingShapes = false;

    public boolean drawOuterTriMouseIsClosestTo = false;

    public int mouseY, mouseX;

    public final boolean mouseButtonDown[] = new boolean[3];


    public long frames = 0, cycles = 0;

    //protected float testPointA[] = new float[2], testPointB[] = new float[2];

    /**
     * Starts self as task. Includes an example smartblob. They can be changed later
     */
    public SmartblobsPanel() {
        this(SmartblobUtil.newSimWithDefaultOptions());


        LayeredZigzag y = SmartblobUtil.simpleSmartblobExample(6,24,75);
            sim.smartblobs.add(y);

        LayeredZigzag z = SmartblobUtil.simpleSmartblobExample(10,12,90);
        for (CornerData cd : z.corners()) {
            cd.x += 200;
            cd.speedX = 100;
            //cd.speedX = 30;
            //cd.speedX = 2;
        }
        //z.updateBoundingRectangle();
            sim.smartblobs.add(z);



        //LayeredZigzag w = SmartblobUtil.simpleSmartblobExample();
        LayeredZigzag w = SmartblobUtil.wavegear(
                null, 250, 500, 75, 90,
                //null, 250, 500, 40, 40,
                //5, 32, 5);
                3, 32, 5);
        //4, 8, 5);

        //Color c = new Color(.5f, .5f, .5f);
        for (int layer = 1; layer < w.layers; layer++) {
            Color c = new Color(0.1f * layer, .7f, 0f);
            for (int p = 0; p < w.layerSize; p++) {
                w.trianglesInward[layer][p].colorOrNull = c;
            }
        }
        w.updateStartDistances();
        w.setTargetDistancesToStartDistances();
        sim.smartblobs.add(w);


    }

    /**
     * Starts self as Task.
     */
    public SmartblobsPanel(SmartblobSim sim) {
        this.sim = sim;
        setIgnoreRepaint(true);
        setDoubleBuffered(true);
        setBackground(Color.black);
        addMouseMotionListener(this);
        addMouseListener(this);
        RealtimeScheduler.start(this);
    }

    //protected LayeredZigzag testBlob = new LayeredZigzag(null, 5, 16, 100, 100, 90);
    //protected LayeredZigzag testBlob = new LayeredZigzag(null, 9, 64, 100, 100, 90);
    //protected LayeredZigzag testBlob = new LayeredZigzag(null, 7, 32, 100, 100, 90);


    public void paint(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

		/*if(testPointA[0] == 0){
            testPointA[0] = getHeight()*CoreUtil.strongRand.nextFloat();
			testPointA[1] = getWidth()*CoreUtil.strongRand.nextFloat();
			testPointB[0] = getHeight()*CoreUtil.strongRand.nextFloat();
			testPointB[1] = getWidth()*CoreUtil.strongRand.nextFloat();
		}*/

        //int w = getWidth(), h = getHeight();
        //testBlob = new LayeredZigzag(null, 5, 16, h/2, w/2, Math.min(w,h)/2);
        //testBlob = new LayeredZigzag(null, 9, 16, h/2, w/2, Math.min(w,h)/2);
        //testBlob = new LayeredZigzag(null, 7, 32, h/2, w/2, Math.min(w,h)/2);

//		Smartblob blobsArray[];
//		synchronized(sim.smartblobs){
//			blobsArray = sim.smartblobs.toArray(new Smartblob[0]);
//		}
        Graphics2D g2 = (Graphics2D) g;
        for (final Smartblob blob : sim.smartblobs) {
            draw(g2, blob);
        }

		/*
		//test nearest point on line math
		float getYX[] = new float[2];
		SmartblobUtil.getClosestPointToInfiniteLine(
			getYX, testPointA[0], testPointA[1], testPointB[0], testPointB[1], mouseY, mouseX);
		g.setColor(Color.pink);
		g.drawLine((int)testPointA[1], (int)testPointA[0], (int)testPointB[1], (int)testPointB[0]);
		g.fillRect((int)getYX[1]-5, (int)getYX[0]-5, 10, 10);
		*/

        g.setColor(Color.white);
        g.drawString("frames: " + frames + ", cycles=" + cycles, 20, 20);
    }

    public static void drawLineWithCurrentSettings(Graphics g, CornerData a, CornerData b) {
        g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
    }

    final static Color boundingShapeColor = new Color(.8f, 0, .8f);

    public void draw(Graphics2D g, Smartblob smartblob) {
        boolean drawShape = drawBoundingShapes || !(smartblob instanceof LayeredZigzag);
        Shape s = null;
        Polygon p = null;
        if (drawShape) {
            s = smartblob.shape();
            if (s instanceof Polygon) { //TODO what to draw here
                p = (Polygon) s;
            } else {
                throw new RuntimeException("TODO use pathiterator of Shape for " + s);
            }
        }
        if (smartblob instanceof LayeredZigzag) {
            draw(g, (LayeredZigzag) smartblob);
        } else {
            g.drawPolygon(p);
        }

        if (drawBoundingShapes) {
            g.setColor(boundingShapeColor);
            g.drawPolygon(p);
        }


        if (drawBoundingRectangles) {
            g.setColor(Color.red);
            Rectangle r = smartblob.boundingRectangle();
            //If rectangle hangs off positive y (bottom) of the panel,
            //panel enlarges and it continues appearing to fall.
            int h = getHeight(), w = getWidth();
            int startY = Math.max(0, r.y);
            int startX = Math.max(0, r.x);
            int endY = Math.min(r.y + r.height - 1, h - 1); //inclusive
            int endX = Math.min(r.x + r.width - 1, w - 1);
            g.drawRect(startX, startY, endX - startX + 1, endY - startY + 1);
            //System.out.println("w="+w+" h="+h+" startY="+startY+" endY="+endY+" r="+r);
            //System.out.println("blob="+smartblob);
        }
    }

    final static Color defaultColor = new Color(.9f, .9f, .9f);
    final static Color defaultColor2 = new Color(0, 0, 1f);

    public void draw(Graphics2D g, LayeredZigzag smartblob) {


        int triX[] = new int[3], triY[] = new int[3]; //filled in from corners float positions

        //g.setColor(Color.blue);
        //g.setColor(new Color(.9f, .9f, .9f));

        //g.setColor(new Color(0,0,1f));
        for (int layer = 1; layer < smartblob.layers; layer++) {
            for (int p = 0; p < smartblob.layerSize; p++) {
				/*Shape triangle = testBlob.triangleShape(layer, p, true);
				if(triangle instanceof Polygon){
					g.fillPolygon((Polygon)triangle);
				}
				*/
                TriData t = smartblob.trianglesInward[layer][p];
                for (int c = 0; c < 3; c++) {
                    CornerData cd = t.adjacentCorners[c];
                    triY[c] = (int) cd.y;
                    triX[c] = (int) cd.x;
                }
                g.setColor(t.colorOrNull == null ? defaultColor : t.colorOrNull);
                g.fillPolygon(triX, triY, 3);
            }
        }
        //g.setColor(new Color(0,0,1f));
        //g.setColor(Color.black);
        //g.setColor(new Color(0, .85f, 0));
        //g.setColor(Color.white);
        //g.setColor(new Color(.9f, .9f, .9f));
        //g.setColor(new Color(.9f, .9f, .9f));
        //g.setColor(new Color(1,0,1f));
        for (int layer = 0; layer < smartblob.layers - 1; layer++) {
            for (int p = 0; p < smartblob.layerSize; p++) {
				/*Shape triangle = testBlob.triangleShape(layer, p, false);
				if(triangle instanceof Polygon){
					g.fillPolygon((Polygon)triangle);
				}*/
                TriData t = smartblob.trianglesOutward[layer][p];
                for (int c = 0; c < 3; c++) {
                    CornerData cd = t.adjacentCorners[c];
                    triY[c] = (int) cd.y;
                    triX[c] = (int) cd.x;
                }
                g.setColor(t.colorOrNull == null ? defaultColor2 : t.colorOrNull);
                g.fillPolygon(triX, triY, 3);
            }
        }

        if (drawOuterTriMouseIsClosestTo) {
            TriData t = smartblob.findCollision(mouseY, mouseX);
            if (t != null) {
                for (int c = 0; c < 3; c++) {
                    CornerData cd = t.adjacentCorners[c];
                    triY[c] = (int) cd.y;
                    triX[c] = (int) cd.x;
                }
                //g.setColor(Color.red);
                //g.fillPolygon(triX, triY, 3);

                float getYX[] = new float[2];
                SmartblobUtil.getClosestPointToInfiniteLine(getYX, t, mouseY, mouseX);
                g.setColor(Color.orange);
                g.fillRect((int) getYX[1] - 3, (int) getYX[0] - 3, 7, 7);
				/*TODO if(mouseButtonDown[0] || mouseButtonDown[2]){
					CornerData cd = t.adjacentCorners[2];
					t.smartblob.onStartUpdateSpeeds();
					float secondsSinceLastDraw = .02;
					cd.speedY -= 10*secondsSinceLastDraw; //TODO do this in nextState
					t.smartblob.onEndUpdateSpeeds();
				}*/
            }
        }


        g.setStroke(defaultStrokeThin);

        g.setColor(Color.green);
        for (Line line : smartblob.allLines()) {
            CornerData a = smartblob.corners[line.cornerLow.layer][line.cornerLow.point];
            CornerData b = smartblob.corners[line.cornerHigh.layer][line.cornerHigh.point];
            drawLineWithCurrentSettings(g, a, b);
        }
        g.setStroke(defaultStrokeThick);

        if (drawBoundingShapes) {
            g.setColor(Color.magenta);
            Shape s = smartblob.shape();
            if (s instanceof Polygon) {
                g.drawPolygon((Polygon) s);
            } else {
                System.out.println("Unknown shape type: " + s.getClass().getName());
            }
        }
		/*TODO when hook in CornerData pointers in LineData for(LineData lineData : testBlob.allLineDatas()){
			drawLineInCurrentColor(g, lineData.adjacentCorners[0], lineData.adjacentCorners[1]);
		}*/
    }

    public void mouseMoved(MouseEvent e) {
        lastTimeMouseMoved = CoreUtil.time();
        mouseY = e.getY();
        mouseX = e.getX();
    }

    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    public void event() {
//        {
//            double now = CoreUtil.time();
//
//            double sinceMouseMove = now - lastTimeMouseMoved;
//
//            if (sinceMouseMove > simulateThisManySecondsAfterMouseMove) {
//                return;
//            }
//        }


        int h = getHeight(), w = getWidth();

        //double secondsSinceLast = timer.secondsSinceLastCall();
        final float dt = simTimePerCycle / subCyclesPerCycle;


        for (GlobalChangeSpeed p : sim.physicsParts) {
            if (p instanceof BounceOnSimpleWall) {
                BounceOnSimpleWall b = (BounceOnSimpleWall) p;
                //the left and top sides of screen stay at 0
                if (b.maxInsteadOfMin) {
                    b.position = b.verticalInsteadOfHorizontal ? h : w;
                }
            }
        }


        //TODO!!! FIXME float sec = Math.min(maxSecondsToSimAtOnce,(float)secondsSinceLast);
        //float sec = simTimeSec; //trying constant update time to see if it improves stability of smartblob bouncing vs sticking together


        //if (drawOuterTriMouseIsClosestTo) {
        if (mouseButtonDown[0]) {
            //Smartblob blobsArray[];


            float a = 5000;
            float px = a * 2f * ((float) Math.random() - 0.5f);
            float py = a * 2f * ((float) Math.random() - 0.5f);

            for (Smartblob blob : sim.smartblobs) {
                if (blob instanceof LayeredZigzag) {
                    LayeredZigzag z = (LayeredZigzag) blob;


                    TriData t = z.findCollision(mouseY, mouseX);
                    if (t != null) {

                        //t.colorOrNull = Color.red;

                        Iterator<ChangeSpeed> iter = z.mutablePhysics().iterator();
                        while (iter.hasNext()) {
                            ChangeSpeed cs = iter.next();
                            if (cs instanceof Push) iter.remove();
                        }

                        CornerData c = t.adjacentCorners[2];
                        ChangeSpeed p = new Push(c,
                                px,
                                py
                        );
                        z.mutablePhysics().add(p);
                    }
                }
            }
        }
        //}


        for (int subcycle = 0; subcycle < subCyclesPerCycle; subcycle++) {
            sim.nextState(dt);
            cycles++;
        }


        frames++;

        //System.out.println("cyc this time "+cyc);
        repaint();

    }

    public double getTargetFPS() {
        return 1.0 / framePerSec;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                mouseButtonDown[0] = true;
                break;
            case MouseEvent.BUTTON2:
                mouseButtonDown[1] = true;
                break;
            case MouseEvent.BUTTON3:
                mouseButtonDown[2] = true;
                break;
        }
    }

    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                mouseButtonDown[0] = false;
                break;
            case MouseEvent.BUTTON2:
                mouseButtonDown[1] = false;
                break;
            case MouseEvent.BUTTON3:
                mouseButtonDown[2] = false;
                break;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}