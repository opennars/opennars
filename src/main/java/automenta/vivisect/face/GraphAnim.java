package automenta.vivisect.face;

//package nars.gui.output.face;
//
///* General Notes:  Chris Poultney, 2/1/2000
//   This applet is designed to work with Ken Perlin's responsive face applet, currently called
//   Face2bApplet.  It can run in two ways:  1) as an applet, included directly in an HTML page
//   within an <applet> tag, or 2) as a frame, by declaring a new GraphAnimFrame from within a
//   running applet (or presumably a frame).
//
//   The applet's UI is broken into three major parts:  The snapshot area, the animation area,
//   and the file area.  The snapshot area includes the snapshot icon bar and snapshot display
//   area, located on the left side of the applet.  The animation area includes the animation
//   icon bar, timeline, and keyframe display, located in the upper right.  The file area
//   consists of a card layout located in the bottom right.  The areas which need to be painted
//   by the applet are the snapshot display, timeline, and keyframe display.
// */
//
//import java.applet.Applet;
//import java.awt.BorderLayout;
//import java.awt.Button;
//import java.awt.CardLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Event;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.FontMetrics;
//import java.awt.Graphics;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Image;
//import java.awt.Insets;
//import java.awt.Label;
//import java.awt.MediaTracker;
//import java.awt.Panel;
//import java.awt.Point;
//import java.awt.Polygon;
//import java.awt.Scrollbar;
//import java.awt.TextArea;
//import java.awt.TextField;
//import java.awt.Toolkit;
//import java.awt.image.ImageObserver;
//import java.awt.image.MemoryImageSource;
//import java.awt.image.PixelGrabber;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.URLEncoder;
//import java.util.NoSuchElementException;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//public class GraphAnim extends Applet {
//    public static final int SNAPHEIGHT = 40;                     // height of snapshot in snap and anim components
//    public static final int TOPSTART = 0;                        // top row within applet
//    public static final int SNAPCOMPWIDTH = 100;                 // width of snapshot component
//    public static final int ICONCOMPHEIGHT = 32;                 // height of icon bars
//    public static final int ANIMTIMECOMPHEIGHT = 20;             // height of timeline
//    public static final int ANIMFACECOMPHEIGHT = SNAPHEIGHT;     // height of keyframe display
//    public static final int ANIMSCRHEIGHT = 15;                  // height of animation scroll bar
//    public static final double TIMEMULTDEFAULT = 0.5;            // default "time multiple" - one keyframe takes TIMEMULTDEFAULT seconds on timeline display
//    public static final int WIDTHDEFAULT = 600;
//    public static final int HEIGHTDEFAULT = 400;
//    public static final int ANIMSECONDS = 60;                    // seconds of animation to provide interface for
//
//    // these numbers are determined in init() and reshape() fns
//    public int ANIMCOMPWIDTH;                                    // width of animation (timeline, keyframe) components, also used for animation icon bar and file panel
//    public int SNAPCOMPHEIGHT;                                   // height of snapshot component
//    public int FILECOMPHEIGHT;                                   // height of file component
//    public int ANIMCOMPHEIGHT;                                   // height of animation components
//
//    // color & font defs
//    public static final Color SNAPBACK = Color.lightGray;
//    public static final Color SELECTBOX = Color.black;
//    public static final Color NUMBACK = Color.gray;
//    public static final Color NUMCOLOR = Color.black;
//    public static final Color ANIMBACK = Color.lightGray;
//    public static final Color ANIMBOX = Color.black;
//    public static final Color ICONBACK = new Color(204, 204, 204);
//    public static final Color ARROWCOLOR = Color.darkGray;
//    public static final Font TIMEFONT = new Font("Helvetica", Font.BOLD, 24);
//    public static final Font PALETTEFONT = new Font("Helvetica", Font.BOLD, 24);
//
//    // flags passed to draw() routine to control drawing of different parts of applet
//    public static final int DRAWSNAP = 1;
//    public static final int DRAWNUM = 1<<1;
//    public static final int DRAWANIM = 2<<2;
//    public static final int DRAWALL = DRAWSNAP | DRAWNUM | DRAWANIM;
//
//    // constants denoting the section of the applet in which a drag operation started
//    public static final int DRAGNONE = 0;
//    public static final int DRAGSNAP = 1;
//    public static final int DRAGANIM = 2;
//
//    protected int snapwidth;                                // width of snapshot in snap and anim components
//    protected int snapcols, snaprows;                       // # of snaps in col, row of snap component
//    protected int maxsnaps;                                 // max # of snaps that can fit in snapshot display
//    protected int framewidth;                               // # of pixels in a second on anim timeline
//    protected int animwidth;                                // # of pixels required for ANIMSECONDS
//    protected double timemult;                              // # of seconds corresponding to snapwidth pixels on anim timeline
//    protected double timediv;
//    protected int xOffset, yOffset;                         // offset with snap of mouse click
//    protected boolean dragdraw;                             // has any drawing been done for drag yet?
//    protected int dragMode;                                 // mouse drag stuff
//    protected int oldx, oldy;                               // x, y positions of last MOUSE_DRAG or MOUSE_UP
//    protected boolean oldinanim;                            // did last drag operation start in keyframe display area?
//    protected boolean wasinanim;                            // did any drag operation enter keyframe display area?
//    protected boolean deleteFrame;                          // should the current keyframe be deleted in do_drag?
//    protected int scrPos;                                   // scrollbar position
//    protected int fullAnimWidth;                            // logical width of scrollable animation area
//
//    protected Face2bApplet faceApplet;
//    protected Vector snaps, frames;                         // lists of snapshots and animation frames
//    protected int currentSnap, currentFrame;                // currently selected snapshot and frame (-1 for none)
//    protected FaceFrame currentFace;                        // useful for drag operations
//    protected boolean doRollOver;
//    protected int rollSnap, rollFrame;                      // current snapshot/frame under mouse (-1 for none)
//    protected double[][] preRollTargets;                    // face setting before rollover
//
//    protected AnimThread athread;                           // thread class for playing animations
//
//    // servlet save/load stuff
//    protected String faceTitle, faceStory;                  // user-supplied title, description for animation
//    protected String servletURL;                            // servlet URL for animation save/load
//    protected Vector seqnos;                                // unique ids of saved animations returned by servlet
//
//    // applet/frame version compatibility vars
//    protected String codeBase = null;                       // URL of codebase, supplied by frame parent or applet calls
//    protected boolean isFrame;
//    protected boolean standalone;                           // set param to true if you want to view as applet in appletviewer
//    protected boolean initdone;
//
//    // GUI stuff
//    Panel snapControlPan, animControlPan;                   // snap and animation section icon bars
//    DrawPanel snapFacePan;                                  // snapshot display panel
//    Panel animPan;                                          // parent panel for animation time and face panels
//    DrawPanel animTimePan, animFacePan;                     // animation timeline and face panels
//    Panel filePan;                                          // remaining area of screen used for file ops & messaging
//    Panel infoPan, loadPan, savePan, msgPan;                // panels displayed in filePan (card layout)
//    Panel saveControlPan;                                   // holds save/cancel buttons for save panel
//    MessagePanel message;
//    GraphicButton snap, delete;                             // snap icons
//    GraphicButton play, stop, clear, save, load, help;      // anim icons
//    OnOffGraphicButton rollOverBut;                         // rollover on/off toggle button
//    Button loadBut, cancelLoadBut, saveBut, cancelSaveBut;  // save/load control buttons
//    Button contBut;                                         // message panel continue button
//    Label titleLab, titleLabDisp, storyLab, storyLabDisp;   // title, story labels for save panel and info panel
//    java.awt.List loadList;                                 // filename list for load
//    TextField titleText, titleTextDisp;                     // title entry/display
//    TextArea storyText, storyTextDisp;                      // story entry/display
//    CardLayout fileCard;                                    // filePan multi-function layout
//    Scrollbar animScr;                                      // anim timeline, face scroll
//
//    GridBagLayout gb;
//    GridBagConstraints gbc;
//
//    Image playIm, pauseIm;                                  // in case pause fn is added and shared with play button
//    Polygon timeArrow, paletteArrow;                        // background arrows for palette & timeline
//
//    // Instantiated as applet
//    public GraphAnim() {
//	super();
//	isFrame = false;
//    }
//
//    // Instantiated as frame
//    public GraphAnim(Face2bApplet fa, String cb, String su) {
//	this(fa, cb, su, false);
//    }
//
//    // Instantiated as standalone frame
//    public GraphAnim(Face2bApplet fa, String cb, String su, boolean sa) {
//	super();
//	codeBase = cb;
//	faceApplet = fa;
//	standalone = sa;
//	isFrame = true;
//	initdone = false;
//	servletURL = su;
//    }
//
//    public void init() {
//	// applet, codeBase not given in constructor
//	if(codeBase == null)
//	    codeBase = getCodeBase().toString();
//
//	//  initialize vars from applet parameters
//	if(!isFrame) {
//	    String sa = getParameter("standalone");
//	    if(sa == null)
//		standalone = false;
//	    else
//		standalone = true;
//	    servletURL = getParameter("servletURL");
//	}
//
//	// wait for face applet to load
//	boolean OK = standalone;
//	while(!OK) {
//	    try {
//		faceApplet = (Face2bApplet)this.getAppletContext().getApplet("A");
//	    }
//	    catch (NullPointerException npe) {
//		;
//	    }
//	    finally {
//		if(faceApplet != null)
//		    OK = true;
//	    }
//	    if(!OK) {
//		try {
//		    Thread.sleep(500);
//		}
//		catch (InterruptedException ie) {
//		    ;
//		}
//	    }
//	}
//
//	if(servletURL == null)
//	    servletURL = new String("http://cat.nyu.edu:8000/saveface");
//
//	// initialize vars formerly from applet parameters
//	timemult = TIMEMULTDEFAULT;
//	timediv = 1.0 / timemult;
//
//	if(faceApplet != null)
//	    snapwidth = (int)((float)faceApplet.bounds().width * (float)SNAPHEIGHT / (float)faceApplet.bounds().height);
//	else
//	    snapwidth = 25;
//	animScr = new Scrollbar(Scrollbar.HORIZONTAL);
//	calcshape(600, 400);
//
//	scrPos = 0;
//	animScr.setValues(scrPos, ANIMCOMPWIDTH, 0, animwidth);
//	animScr.setLineIncrement((int)(snapwidth * timediv));
//	animScr.setPageIncrement(ANIMCOMPWIDTH);
//
//	snaps = new Vector();
//	currentSnap = -1;
//	rollSnap = -1;
//	frames = new Vector();
//	rollFrame = -1;
//	preRollTargets = null;
//	doRollOver = false;
//
//	// LAYOUT SETUP
//
//	// this is the only way I could control component sizes and have methods called properly
//	setLayout(null);
//
//	setBackground(ICONBACK);
//
//	snapControlPan = new Panel();
//	snapFacePan = new DrawPanel(this, DRAWSNAP);
//	animControlPan = new Panel();
//	animTimePan = new DrawPanel(this, DRAWNUM);
//	animFacePan = new DrawPanel(this, DRAWANIM);
//	animPan = new Panel();
//	filePan = new Panel();
//	infoPan = new Panel();
//	savePan = new Panel();
//	loadPan = new Panel();
//	msgPan = new Panel();
//
//	// get images, make buttons from some of them
//	try {
//	    Toolkit tk = Toolkit.getDefaultToolkit();
//	    MediaTracker tracker = new MediaTracker(this);
//
//	    URL u = new URL(codeBase + "snap.gif");
//	    Image im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    snap = new GraphicButton(im);
//	    u = new URL(codeBase + "delete3.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    delete = new GraphicButton(im);
//	    u = new URL(codeBase + "clear2.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//
//	    u = new URL(codeBase + "play.gif");
//	    playIm = tk.getImage(u);
//	    tracker.addImage(playIm, 0);
//	    play = new GraphicButton(playIm);
//	    u = new URL(codeBase + "pause.gif");
//	    pauseIm = tk.getImage(u);
//	    tracker.addImage(pauseIm, 0);
//	    u = new URL(codeBase + "stop.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    stop = new GraphicButton(im);
//	    u = new URL(codeBase + "clear1.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    clear = new GraphicButton(im);
//	    u = new URL(codeBase + "save.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    save = new GraphicButton(im);
//	    u = new URL(codeBase + "load.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    load = new GraphicButton(im);
//	    u = new URL(codeBase + "facepoint.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    rollOverBut = new OnOffGraphicButton(im, doRollOver);
//
//	    tracker.waitForID(0);
//	}
//	catch (MalformedURLException mue) {
//	    System.out.println(mue);
//	    mue.printStackTrace();
//	}
//	catch (InterruptedException ie) {
//	    System.out.println(ie);
//	    ie.printStackTrace();
//	}
//
//	snapControlPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//	snapControlPan.add(snap);
//	snapControlPan.add(delete);
//	snapControlPan.reshape(0, TOPSTART, SNAPCOMPWIDTH, ICONCOMPHEIGHT);
//	add(snapControlPan);
//
//	snapFacePan.reshape(0, ICONCOMPHEIGHT + TOPSTART, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	add(snapFacePan);
//
//	animControlPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//	animControlPan.add(play);
//	animControlPan.add(stop);
//	animControlPan.add(new Spacer(40, 32));
//	animControlPan.add(save);
//	animControlPan.add(load);
//	animControlPan.add(clear);
//	animControlPan.add(new Spacer(40, 32));
//	animControlPan.add(rollOverBut);
//	animControlPan.reshape(SNAPCOMPWIDTH, TOPSTART, ANIMCOMPWIDTH, ICONCOMPHEIGHT);
//	add(animControlPan);
//
//	animPan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, ANIMCOMPHEIGHT);
//	add(animPan);
//
//	animPan.setLayout(null);
//	animPan.add(animTimePan);
//	animTimePan.reshape(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	animPan.add(animFacePan);
//	animFacePan.reshape(0, ANIMTIMECOMPHEIGHT, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	animPan.add(animScr);
//	animScr.reshape(0, ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT, ANIMCOMPWIDTH, ANIMSCRHEIGHT);
//
//	fileCard = new CardLayout();
//	filePan.setLayout(fileCard);
//	filePan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, FILECOMPHEIGHT);
//
//	gb = new GridBagLayout();
//	infoPan.setLayout(gb);
//	titleLabDisp = new Label("Title:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleLabDisp, gbc);
//	infoPan.add(titleLabDisp);
//	storyLabDisp = new Label("Story:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.NORTHEAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(storyLabDisp, gbc);
//	infoPan.add(storyLabDisp);
//	titleTextDisp = new TextField();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleTextDisp, gbc);
//	infoPan.add(titleTextDisp);
//	storyTextDisp = new TextArea();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 6, 3);
//	gb.setConstraints(storyTextDisp, gbc);
//	infoPan.add(storyTextDisp);
//	filePan.add("info", infoPan);
//
//	gb = new GridBagLayout();
//	savePan.setLayout(gb);
//	titleLab = new Label("Title:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleLab, gbc);
//	savePan.add(titleLab);
//	storyLab = new Label("Story:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.NORTHEAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(storyLab, gbc);
//	savePan.add(storyLab);
//	titleText = new TextField();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleText, gbc);
//	savePan.add(titleText);
//	storyText = new TextArea();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 6, 3);
//	gb.setConstraints(storyText, gbc);
//	savePan.add(storyText);
//	saveControlPan = new Panel();
//	saveControlPan.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 2;
//	gbc.gridwidth = 2;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gb.setConstraints(saveControlPan, gbc);
//	saveBut = new Button("Save");
//	saveControlPan.add(saveBut);
//	cancelSaveBut = new Button("Cancel");
//	saveControlPan.add(cancelSaveBut);
//	savePan.add(saveControlPan);
//	filePan.add("save", savePan);
//
//	loadPan.setLayout(gb);
//	loadList = new java.awt.List();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.gridwidth = 2;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(loadList, gbc);
//	loadPan.add(loadList);
//	loadBut = new Button("Load");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(loadBut, gbc);
//	loadPan.add(loadBut);
//	cancelLoadBut = new Button("Cancel");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(cancelLoadBut, gbc);
//	loadPan.add(cancelLoadBut);
//	filePan.add("load", loadPan);
//
//	msgPan.setLayout(gb);
//	message = new MessagePanel("");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(message, gbc);
//	msgPan.add(message);
//	contBut = new Button("Continue");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(contBut, gbc);
//	msgPan.add(contBut);
//	filePan.add("msg", msgPan);
//
//	add(filePan);
//
//	//	rollOverBut.setIsOn(doRollOver);
//
//	initdone = true;
//    }
//
//    // take snapshot
//    public void getFaceFrame() {
//	if(snaps.size() < maxsnaps) {
//	    double[][] tar = faceApplet.getTargets();
//	    Image pre = faceApplet.getSnapshotImage(tar);
//	    Image post = scaleSnap(pre);
//	    Graphics ig = post.getGraphics();
//	    ig.drawImage(pre, 0, 0, snapwidth, SNAPHEIGHT, Color.white, null);
//	    pre.flush();
//	    
//	    double w = 0;
//
//	    FaceFrame ff = new FaceFrame(tar, post, w);
//	    snaps.addElement(ff);
//	    draw(DRAWSNAP);
//	}
//    }
//
//    public Image scaleSnap(Image i) {
//	Image after = this.createImage(snapwidth, SNAPHEIGHT);
//	Graphics ig = after.getGraphics();
//	ig.drawImage(i, 0, 0, snapwidth, SNAPHEIGHT, Color.white, null);
//	return after;
//    }
//
//    public void clearSnaps() {
//	snaps = new Vector();
//	currentSnap = -1;
//	draw(DRAWSNAP);
//    }
//
//    public void clearFrames() {
//	frames = new Vector();
//	draw(DRAWANIM);
//    }
//
//    // convert snapshot list to string for saving
//    protected String getSnapString() {
//	String snapString = new String();
//	FaceFrame ff;
//	for(int i = 0; i < snaps.size(); i++) {
//	    ff = (FaceFrame)snaps.elementAt(i);
//	    snapString += ff.toString();
//	}
//
//	return snapString.substring(0, snapString.length() - 1);
//    }
//
//    // convert animation frame list to string for saving
//    protected String getFrameString() {
//	String frameString = new String();
//	FaceFrame ff;
//	for(int i = 0; i < frames.size(); i++) {
//	    ff = (FaceFrame)frames.elementAt(i);
//	    frameString += ff.toString();
//	}
//
//	return frameString.substring(0, frameString.length() - 1);
//    }
//
//    public boolean save(String title, String story) {
//	URL u;
//	URLConnection conn;
//	OutputStream o;
//	DataOutputStream dout;
//
//	try {
//	    // format save info and print to standard output as form submit
//	    String sub = new String(servletURL);
//	    String fd = new String("");
//	    fd += "snaps=" + URLEncoder.encode(getSnapString());
//	    fd += "&frames=" + URLEncoder.encode(getFrameString());
//	    fd += "&name=" + URLEncoder.encode(titleText.getText());
//	    fd += "&story=" + URLEncoder.encode(storyText.getText());
//	    u = new URL(sub);
//	    conn = u.openConnection();
//	    conn.setDoOutput(true);
//	    o = conn.getOutputStream();
//	    dout = new DataOutputStream(o);
//	    dout.writeBytes(fd);
//	    dout.close();
//	    
//	    // read servlet output to make &#*%! servlet think it's a POST
//	    DataInputStream in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    while((line = in.readLine()) != null) {
//		; // this line intentionally left blank
//	    }
//	    in.close();
//	}
//	catch (MalformedURLException m) {
//	    System.out.println(m);
//	    m.printStackTrace();
//	    return false;
//	}
//	catch (IOException io) {
//	    System.out.println(io);
//	    io.printStackTrace();
//	    return false;
//	}
//	return true;
//    }
//
//    // build list of saved animations
//    public boolean getList() {
//	URL u;
//	URLConnection conn;
//	DataInputStream in;
//
//	try {
//	    u = new URL(servletURL);
//	    conn = u.openConnection();
//	    in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    loadList.clear();
//	    seqnos = new Vector();
//
//	    // a sample line, and the sed script to extract the desired information
//	    // <A HREF="javascript:opener.loadanim('-1734480678');window.close();">4</A><BR>
//	    // s/.*'\([-0-9]*\)'[^>]*>\([^<]*\).*/\1 \2/
//	    while((line = in.readLine()) != null) {
//		if(!line.startsWith("<A HREF="))
//		    continue;
//		String seqno;
//		try {
//		    StringTokenizer st = new StringTokenizer(line, "'");
//		    String s = st.nextToken();
//		    seqno = st.nextToken();
//		}
//		catch (NoSuchElementException nse) {
//		    continue;
//		}
//
//		int start = line.indexOf(">") + 1;
//		int end = line.lastIndexOf("<");
//		end = line.lastIndexOf("<", end - 1);
//		String title = line.substring(start, end);
//
//		loadList.addItem(title);
//		seqnos.addElement(seqno);
//	    }
//	}
//	catch (MalformedURLException mu) {
//	    System.out.println(mu);
//	    mu.printStackTrace();
//	    return false;
//	}
//	catch (IOException ie) {
//	    System.out.println(ie);
//	    ie.printStackTrace();
//	    return false;
//	}
//
//	return true;
//    }
//
//    public boolean load(String seqno) {
//	URL u;
//	URLConnection conn;
//	DataInputStream in;
//	StringBuffer text = new StringBuffer();
//
//	// get saved text from server
//	try {
//	    u = new URL(servletURL + "?load=" + seqno);
//	    conn = u.openConnection();
//	    in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    while((line = in.readLine()) != null)
//		text.append(line + "\n");
//	    in.close();
//	}
//	catch (Exception e2) {
//	    System.out.println(e2);
//	    e2.printStackTrace();
//	    return false;
//	}
//
//	// separate text into parts: title, snaps, frames, story
//	String sep = "\n\n";
//	String[] els = new String[4];
//	String textString = text.toString();
//	int count = 0, loc = 0;
//	boolean done = false;
//	do {
//	    int newloc = textString.indexOf(sep, loc);
//	    if(newloc == -1 || count == 3)  { // force 4th string to contain rest of input
//		done = true;
//		newloc = textString.length();
//	    }
//	    els[count++] = textString.substring(loc, newloc);
//	    loc = newloc + sep.length();
//	} while(!done);	    
//
//	currentSnap = -1;
//	snaps = new Vector();
//	frames = new Vector();
//
//	try {
//	    // parse and initialize snaps...
//	    StringTokenizer st = new StringTokenizer(els[1]);
//	    while(st.hasMoreTokens()) {
//		String part = st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		FaceFrame ff = new FaceFrame(part);
//		ff.setSnapshot(scaleSnap(faceApplet.getSnapshotImage(ff.getTargets())));
//		snaps.addElement(ff);
//	    }
//
//	    // ...and frames
//	    st = new StringTokenizer(els[2]);
//	    while(st.hasMoreTokens()) {
//		String part = st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		FaceFrame ff = new FaceFrame(part);
//		ff.setSnapshot(scaleSnap(faceApplet.getSnapshotImage(ff.getTargets())));
//		insertInOrder(frames, ff);
//	    }
//	}
//	catch(NoSuchElementException nse) {
//	    System.out.println(nse);
//	    nse.printStackTrace();
//	    return false;
//	}
//	catch(NullPointerException np) {
//	    System.out.println(np);
//	    np.printStackTrace();
//	    return false;
//	}
//
//	titleText.setText(els[0]);
//	storyText.setText(els[3]);
//	titleTextDisp.setText(els[0]);
//	storyTextDisp.setText(els[3]);
//
//	draw(DRAWALL);
//	return true;
//    }
//
//    // common processing for MOUSE_DRAG and MOUSE_UP
//    private void do_drag(Event e, boolean eraseold, boolean drawnew) {
//	// erase old rectangle or face
//	if(eraseold) {
//	    if(oldinanim) {
//		Graphics g = animFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] oldim = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//		oldim[0] = Math.min(Math.max(oldim[0], 0), ANIMCOMPWIDTH - snapwidth);
//		oldim[1] = 0;
//		g.drawImage(currentFace.getSnapshot(), oldim[0], oldim[1], null);
//		g.setPaintMode();
//	    } else {
//		Graphics g = snapFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] oldrect = getLocalCoords(snapFacePan, oldx - xOffset, oldy - yOffset);
//		g.drawRect(oldrect[0], oldrect[1], snapwidth - 1, SNAPHEIGHT - 1);
//		g.setPaintMode();
//	    }
//	}
//
//	// drag new rectangle or face
//	boolean inanim;
//	if(!wasinanim)
//	    inanim = containsGlobal(animFacePan, e.x, e.y);
//	else
//	    inanim = containsGlobal(animFacePan, getLocationGlobal(animFacePan).x, e.y);
//	if(drawnew) {
//	    if(inanim) {
//		if(deleteFrame == true) {
//		    deleteFrame = false;
//		    frames.removeElementAt(currentFrame);
//		    draw(DRAWANIM);
//		}
//		Graphics g = animFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] t = getLocalCoords(animFacePan, e.x - xOffset, e.y - yOffset);
//		int im[] = new int[2];
//		im[0] = Math.min(Math.max(t[0], 0), ANIMCOMPWIDTH - snapwidth);
//		im[1] = 0;
//		int diff = t[0] - im[0];
//		scrPos += diff;
//		scrPos = Math.min(Math.max(scrPos, animScr.getMinimum()), animScr.getMaximum() - animScr.getVisible());
//		timeArrow = null;
//		draw(DRAWANIM | DRAWNUM);
//		animScr.setValue(scrPos);
//		g.drawImage(currentFace.getSnapshot(), im[0], im[1], null);
//		g.setPaintMode();
//	    } else {
//		Graphics g = snapFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] rect = getLocalCoords(snapFacePan, e.x - xOffset, e.y - yOffset);
//		g.drawRect(rect[0], rect[1], snapwidth - 1, SNAPHEIGHT - 1);
//		g.setPaintMode();
//	    }
//	}
//	oldx = e.x;
//	oldy = e.y;
//	oldinanim = inanim;
//	wasinanim = wasinanim || inanim;
//    }
//
//    public boolean handleEvent(Event e) {
//	// for some reason, a standard Button click generates this code
//	if(e.id == 1001) {
//	    if(e.target == loadBut || e.target == loadList) {
//		int num = loadList.getSelectedIndex();
//		if(num != -1) {
//		    showMsgPan("loading...", false);
//		    if(load((String)seqnos.elementAt(num)))
//			showMsgPan("loading...done", true);
//		    else
//			showMsgPan("load error", true);
//		}
//		return true;
//	    } else if(e.target == cancelLoadBut) {
//		fileCard.show(filePan, "info");
//		return true;
//	    } else if(e.target == saveBut) {
//		String n = titleText.getText();
//		String s = storyText.getText();
//		if(!n.equals("")) {
//		    showMsgPan("saving...", false);
//		    titleTextDisp.setText(n);
//		    storyTextDisp.setText(s);
//		    if(save(n, s))
//			showMsgPan("saving...done", true);
//		    else
//			showMsgPan("save error", true);
//		}
//		return true;
//	    } else if(e.target == cancelSaveBut) {
//		titleTextDisp.setText(titleText.getText());
//		storyTextDisp.setText(storyText.getText());
//		fileCard.show(filePan, "info");
//		return true;
//	    } else if(e.target == contBut) {
//		fileCard.show(filePan, "info");
//		return true;
//	    }
//	} else if(e.id == Event.MOUSE_MOVE) {
//	    if(roll()) {
//		int oldSnap = rollSnap, oldFrame = rollFrame;
//		rollSnap = -1;
//		rollFrame = -1;
//		if(e.target == snapFacePan) {
//		    int[] p = getLocalCoords(snapFacePan, e.x, e.y);
//		    int tempsnap = snapcols * (int)(p[1] / SNAPHEIGHT) + (int)(p[0] / snapwidth);
//		    if(tempsnap < snaps.size()) {
//			rollSnap = tempsnap;
//		    }
//		} else if(e.target == animFacePan) {
//		    int[] p = getLocalCoords(animFacePan, e.x, e.y);
//		    double postime = (double)(p[0] + scrPos) / (timediv * snapwidth);
//		    FaceFrame ff;
//		    for(int i = frames.size() - 1; i >= 0; i--) {
//			ff = (FaceFrame)frames.elementAt(i);
//			if(postime >= ff.getTime() && postime < ff.getTime() + timemult) {
//			    rollFrame = i;
//			    break;
//			}
//		    }
//		}
//		if(rollSnap != oldSnap || rollFrame != oldFrame && !standalone) {
//		    if(rollSnap != -1) {
//			if(oldFrame == -1 && oldSnap == -1)
//			    preRollTargets = faceApplet.getTargets();
//			FaceFrame ff = (FaceFrame)snaps.elementAt(rollSnap);
//			faceApplet.setTargets(ff.getTargets());
//		    } else if(rollFrame != -1) {
//			if(oldSnap == -1 && oldFrame == -1)
//			    preRollTargets = faceApplet.getTargets();
//			FaceFrame ff = (FaceFrame)frames.elementAt(rollFrame);
//			faceApplet.setTargets(ff.getTargets());
//		    } else {
//			if(preRollTargets != null)
//			    faceApplet.setTargets(preRollTargets);
//		    }
//		}
//	    }
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    if(roll()) {
//		if(e.target == snapFacePan || e.target == animFacePan) {
//		    rollSnap = rollFrame = -1;
//		    if(preRollTargets != null && !standalone)
//			faceApplet.setTargets(preRollTargets);
//		}
//	    }
//	} else if(e.id == Event.MOUSE_ENTER) {
//	    if(roll()) {
//		if(e.target == this && !standalone) {
//		    preRollTargets = faceApplet.getTargets();
//		}
//	    }
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(dragMode == DRAGNONE) {
//		if(e.target == snap) {
//		    getFaceFrame();
//		    draw(DRAWSNAP);
//		    return true;
//		} else if(e.target == delete) {
//		    deleteSnap();
//		    return true;
//		} else if(e.target == play) {
//		    startAnim();
//		    return true;
//		} else if(e.target == stop) {
//		    stopAnim();
//		    return true;
//		} else if(e.target == clear) {
//		    clearSnaps();
//		    clearFrames();
//		    titleText.setText("");
//		    titleTextDisp.setText("");
//		    storyText.setText("");
//		    storyTextDisp.setText("");
//		    return true;
//		} else if(e.target == help) {                // legacy stuff, not used
//		    try {
//			URL u = new URL(codeBase + "facehelp.html");
//			//			getAppletContext().showDocument(u, "facehelp");
//		    }
//		    catch (MalformedURLException mu) {
//			System.out.println(mu);
//			mu.printStackTrace();
//		    }
//		    return true;
//		} else if(e.target == load) {
//		    showMsgPan("building list...", false);
//		    if(getList())
//			fileCard.show(filePan, "load");
//		    else
//			showMsgPan("list build error", true);
//		    return true;
//		} else if(e.target == save) {
//		    titleText.setText(titleTextDisp.getText());
//		    storyText.setText(storyTextDisp.getText());
//		    fileCard.show(filePan, "save");
//		    return true;
//		} else if(e.target == rollOverBut) {
//		    doRollOver = rollOverBut.getIsOn();
//		}
//	    } else if(dragMode == DRAGSNAP) {
//		if(dragdraw)
//		    do_drag(e, true, false);
//		if(oldinanim && currentSnap != -1) {
//		    int[] p = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//		    p[0] = Math.min(Math.max(p[0], 0), ANIMCOMPWIDTH - snapwidth);
//		    double newTime = (double)(p[0] + scrPos) / (double)(snapwidth * timediv);
//		    FaceFrame newAnim = new FaceFrame((FaceFrame)snaps.elementAt(currentSnap));
//		    newAnim.setTime(newTime);
//		    insertInOrder(frames, newAnim);
//		    draw(DRAWANIM);
//		}
//	    } else if(dragMode == DRAGANIM) {
//		do_drag(e, true, false);
//		if(currentFrame != -1) {
//		    if(oldinanim) {
//			int[] p = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//			p[0] = Math.min(Math.max(p[0], 0), ANIMCOMPWIDTH - snapwidth);
//			double newTime = (double)(p[0] + scrPos) / (double)(snapwidth * timediv);
//			currentFace.setTime(newTime);
//			insertInOrder(frames, currentFace);
//		    }
//		    draw(DRAWANIM);
//		}
//	    }
//	} else if(e.id == Event.MOUSE_DRAG) {
//	    if(dragMode != DRAGNONE) {
//		do_drag(e, dragdraw, true);
//		dragdraw = true;
//	    }
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    dragMode = DRAGNONE;
//	    dragdraw = false;
//	    oldinanim = false;
//	    wasinanim = false;
//	    if(e.target == snapFacePan) {
//		int[] p = getLocalCoords(snapFacePan, e.x, e.y);
//		int tempsnap = snapcols * (int)(p[1] / SNAPHEIGHT) + (int)(p[0] / snapwidth);
//		int oldSnap = currentSnap;
//		if(tempsnap < snaps.size()) {
//		    currentSnap = tempsnap;
//		    currentFace = (FaceFrame)snaps.elementAt(currentSnap);
//		    dragMode = DRAGSNAP;
//		    xOffset = p[0] % snapwidth;
//		    yOffset = p[1] % SNAPHEIGHT;
//		    //		    draw(DRAWANIM);
//		}
//		if(oldSnap != currentSnap)
//		    draw(DRAWSNAP);
//		if(e.clickCount > 1) {
//		    FaceFrame ff = (FaceFrame)snaps.elementAt(currentSnap);
//		    if(roll())
//			preRollTargets = ff.getTargets();
//		    else
//			faceApplet.setTargets(ff.getTargets());
//		}
//	    } else if(e.target == animFacePan) {
//		int[] p = getLocalCoords(animFacePan, e.x, e.y);
//		double postime = (double)(p[0] + scrPos) / (timediv * snapwidth);
//		currentFrame = -1;
//		deleteFrame = false;
//		FaceFrame ff;
//		for(int i = frames.size() - 1; i >= 0; i--) {
//		    ff = (FaceFrame)frames.elementAt(i);
//		    if(postime >= ff.getTime() && postime < ff.getTime() + timemult) {
//			currentFrame = i;
//			dragMode = DRAGANIM;
//			xOffset = p[0] - (int)Math.round(ff.getTime() * timediv * snapwidth - scrPos);
//			yOffset = p[1];
//			break;
//		    }
//		}
//		if(currentFrame != -1) {
//		    currentFace = (FaceFrame)frames.elementAt(currentFrame);
//		    deleteFrame = true;
//		    if(e.clickCount > 1) {
//			ff = (FaceFrame)frames.elementAt(currentFrame);
//			if(roll())
//			    preRollTargets = ff.getTargets();
//			else
//			    faceApplet.setTargets(ff.getTargets());
//		    }
//		}
//	    }
//	} else if(e.id == Event.SCROLL_LINE_UP || e.id == Event.SCROLL_PAGE_UP ||
//		  e.id == Event.SCROLL_LINE_DOWN || e.id == Event.SCROLL_PAGE_DOWN ||
//		  e.id == Event.SCROLL_ABSOLUTE) {
//	    synchronized(animScr) {
//		scrPos = Math.min(((Integer)e.arg).intValue(), animScr.getMaximum() - animScr.getVisible());
//		timeArrow = null;
//		draw(DRAWNUM | DRAWANIM);
//	    }
//	}
//
//	return super.handleEvent(e);
//    }
//
//    // handle redraw of custom areas:  snapshot, anim timeline, anim frames
//    public void draw(int type) {
//	if(!initdone)
//	    return;
//	if((type & DRAWSNAP) != 0) {
//	    Graphics g = snapFacePan.getGraphics();
//	    g.setColor(SNAPBACK);
//	    g.fillRect(0, 0, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	    g.setColor(ARROWCOLOR);
//	    g.setFont(PALETTEFONT);
//	    FontMetrics fm = g.getFontMetrics(PALETTEFONT);
//	    String ps = new String("Palette");
//	    int x = (SNAPCOMPWIDTH - fm.stringWidth(ps)) / 2;
//	    int y = fm.getAscent();
//	    int border = 5;
//	    g.drawString(ps, x, y + border);
//	    if(paletteArrow == null) {
//		int lw = (SNAPCOMPWIDTH - 2 * border) / 2;                            // width of arrow line
//		int ax = (SNAPCOMPWIDTH / 2) - (lw / 2);                              // x coord of arrow top right
//		int ay = fm.getHeight() + border;                                     // y coord of arrow top right
//		int wy = SNAPCOMPHEIGHT - border - (SNAPCOMPWIDTH - 2 * border) / 2;  // x coord where arrow head starts
//		paletteArrow = new Polygon();
//		paletteArrow.addPoint(ax, ay);
//		paletteArrow.addPoint(ax, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - border, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH / 2, SNAPCOMPHEIGHT - border);
//		paletteArrow.addPoint(border, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - ax, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - ax, ay);
//	    }
//	    g.fillPolygon(paletteArrow);
//	    for(int i = 0; i < snaps.size(); i++) {
//		Image snap = ((FaceFrame)snaps.elementAt(i)).getSnapshot();
//		int[] p = toSnapGrid(i);
//		g.drawImage(snap, p[0], p[1], null);
//	    }
//	    if(currentSnap != -1) {
//		int[] p = toSnapGrid(currentSnap);
//		g.setColor(SELECTBOX);
//		g.drawRect(p[0], p[1], snapwidth-1, SNAPHEIGHT-1);
//	    }
//	}
//
//	if((type & DRAWNUM) != 0) {
//	    Graphics g = animTimePan.getGraphics();
//	    g.setColor(NUMBACK);
//	    g.fillRect(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	    g.setColor(NUMCOLOR);
//	    int clicks = ANIMSECONDS * 2 + 1;
//
//	    for(int i = 0; i < clicks; i++) {
//		g.drawLine(i * framewidth / 2 - scrPos, ANIMTIMECOMPHEIGHT - 4, i * framewidth / 2 - scrPos, ANIMTIMECOMPHEIGHT - 1);
//		if(i % 2 == 0)
//		    g.drawString(i / 2 + "", i * framewidth / 2 - scrPos, getFontMetrics(getFont()).getHeight() + 1);
//	    }
//	}
//
//	if((type & DRAWANIM) != 0) {
//	    Graphics g = animFacePan.getGraphics();
//	    g.setColor(ANIMBACK);
//	    g.fillRect(0, 0, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	    g.setColor(ARROWCOLOR);
//	    g.setFont(TIMEFONT);
//	    FontMetrics fm = g.getFontMetrics(TIMEFONT);
//	    int border = 5;
//	    int y = SNAPHEIGHT / 2 + fm.getAscent() - fm.getHeight() / 2;
//	    String ts = new String("Timeline");
//	    g.drawString(ts, border - scrPos, y);
//	    if(timeArrow == null) {
//		int lh = (SNAPHEIGHT - 2 * border) / 2;                                  // height of arrow line
//		int ax = border * 2 + fm.stringWidth(ts) - scrPos;                       // x coord of arrow start
//		int ay = (SNAPHEIGHT / 2) - (lh / 2);                                    // y coord of arrow line top
//		int wx = animwidth - scrPos - border - (SNAPHEIGHT - 2 * border) / 2;    // x coord where arrow head starts
//		timeArrow = new Polygon();
//		timeArrow.addPoint(ax, ay);
//		timeArrow.addPoint(wx, ay);
//		timeArrow.addPoint(wx, border);
//		timeArrow.addPoint(animwidth - scrPos - border, SNAPHEIGHT / 2);
//		timeArrow.addPoint(wx, SNAPHEIGHT - border);
//		timeArrow.addPoint(wx, SNAPHEIGHT - ay);
//		timeArrow.addPoint(ax, SNAPHEIGHT - ay);
//	    }
//	    g.fillPolygon(timeArrow);
//	    for(int i = 0; i < frames.size(); i++) {
//		FaceFrame ff = (FaceFrame)frames.elementAt(i);
//		Image snap = ff.getSnapshot();
//		double time = ff.getTime();
//		g.drawImage(snap, (int)Math.round(time * timediv * snapwidth) - scrPos, 0, null);
//	    }
//	}
//    }
//
//    public void paint(Graphics g) {
//	if(initdone)
//	    draw(DRAWALL);
//    }
//
//    // recalculate size, logistical vars which change based on frame/applet size
//    private void calcshape(int w, int h) {
//	ANIMCOMPWIDTH = w - SNAPCOMPWIDTH;
//	SNAPCOMPHEIGHT = h - ICONCOMPHEIGHT - TOPSTART;
//	ANIMCOMPHEIGHT = ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT + ANIMSCRHEIGHT + 15;
//	FILECOMPHEIGHT = h - (ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART);
//
//	snapcols = SNAPCOMPWIDTH / snapwidth;
//	snaprows = SNAPCOMPHEIGHT / snapwidth;
//	maxsnaps = snapcols * snaprows;
//	framewidth = (int)(snapwidth * timediv);
//	animwidth = ANIMSECONDS * framewidth;
//    }
//
//    public synchronized void reshape(int x, int y, int w, int h) {
//	super.reshape(x, y, w, h);
//
//	if(initdone) {
//	    calcshape(w, h);
//	    timeArrow = paletteArrow = null;          // force arrow redraw
//
//	    snapControlPan.reshape(0, TOPSTART, SNAPCOMPWIDTH, ICONCOMPHEIGHT);
//	    snapFacePan.reshape(0, ICONCOMPHEIGHT + TOPSTART, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	    animControlPan.reshape(SNAPCOMPWIDTH, TOPSTART, ANIMCOMPWIDTH, ICONCOMPHEIGHT);
//	    animPan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, ANIMCOMPHEIGHT);
//	    animTimePan.reshape(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	    animFacePan.reshape(0, ANIMTIMECOMPHEIGHT, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	    animScr.reshape(0, ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT, ANIMCOMPWIDTH, ANIMSCRHEIGHT);
//	    filePan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, FILECOMPHEIGHT);
//	}
//    }
//
//    public void deleteSnap() {
//	if(currentSnap >= 0 && currentSnap < snaps.size())
//	    snaps.removeElementAt(currentSnap);
//	if(currentSnap == snaps.size())
//	    currentSnap -= 1;
//	draw(DRAWSNAP);
//    }
//
//    public void startAnim() {
//	if(athread == null)
//	    athread = new AnimThread(this, frames, faceApplet, animTimePan.getGraphics(), animScr);
//	else if(athread.isAlive()) {
//	    return;
//	} else {
//	    athread.stop();
//	    athread = new AnimThread(this, frames, faceApplet, animTimePan.getGraphics(), animScr);
//	}
//	athread.start();
//    }
//
//    public void stopAnim() {
//	if(athread != null) {
//	    athread.stop();
//	    try {
//		Thread.sleep(250);
//	    }
//	    catch(InterruptedException ie) {
//		;
//	    }
//	}
//	currentFrame = -1;
//	athread = null;
//	draw(DRAWNUM);
//    }
//
//    // scales an image to snapshot size.  not used.
//    private Image scaleImage(Image before) {
//	int bw = before.getWidth(this), bh = before.getHeight(this);
//	int aw = snapwidth, ah = SNAPHEIGHT;
//	int bp[] = new int[bw * bh], ap[] = new int[aw * ah];
//	double rx = (double)bw / (double)aw, ry = (double)bh / (double)ah;
//
//	PixelGrabber bpp = new PixelGrabber(before, 0, 0, bw, bh, bp, 0, bw);
//        try {
//            bpp.grabPixels();
//        } catch (InterruptedException ie) {
//            System.out.println("interrupted waiting for pixels!");
//            return null;
//        }
//        if ((bpp.status() & ImageObserver.ABORT) != 0) {
//            System.out.println("image fetch aborted or errored");
//            return null;
//        }
//
//	for(int ar = 0; ar < ah; ar++) {
//	    for(int ac = 0; ac < aw; ac++) {
//		double ta = 0, tr = 0, tg = 0, tb = 0;
//		for(int br = (int)Math.floor(ar * ry); br < (int)Math.ceil((ar + 1) * ry); br++)
//		    for(int bc = (int)Math.floor(ac * rx); bc < (int)Math.ceil((ac + 1) * rx); bc++) {
//			double xmin = Math.max(bc, ac * rx);
//			double xmax = Math.min(bc + 1, (ac + 1) * rx);
//			double ymin = Math.max(br, ar * ry);
//			double ymax = Math.min(br + 1, (ar + 1) * ry);
//			double fac = (xmax - xmin) * (ymax - ymin);
//			int pix = br * bw + bc;
//			int r = (bp[pix] & 0xff0000) >> 16;
//			int g = (bp[pix] & 0xff00) >> 8;
//			int b = bp[pix] & 0xff;
//			//System.out.println("ar:" + ar + " ac:" + ac + " br:" + br + " bc:" + bc + " pix:" + pix + ":[" + r + ":" + g + ":" + b + "] fac:" + fac);
//			tr += (double)r * fac;
//			tg += (double)g * fac;
//			tb += (double)b * fac;
//		    }
//		double area = rx * ry;
//		ap[ar * aw + ac] = 0xff000000 | ((int)Math.round(tr / area) << 16) | ((int)Math.round(tg / area) << 8) | (int)Math.round(tb / area);
//		//System.out.print((int)(tr / area) + " ");
//	    }
//	    //System.out.println();
//	}
//
//	Image after = createImage(new MemoryImageSource(aw, ah, ap, 0, aw));
//	System.out.println(after + "  " + after.getWidth(this) + "," + after.getHeight(this));
//	return after;
//    }
//
//    // get coords of component relative to top-level frame
//    public Point getLocationGlobal(Component c) {
//	Point p = c.location();
//	Container cn = c.getParent();
//	while(!(cn == this)) {
//	    Point n = cn.location();
//	    p.x += n.x;
//	    p.y += n.y;
//	    cn = cn.getParent();
//	}
//
//	return p;
//    }
//
//    // get local coords within component of x, y which are specified in top-level frame coords
//    public int[] getLocalCoords(Component c, int x, int y) {
//	Point p = getLocationGlobal(c);
//	int[] n = new int[2];
//	n[0] = x - p.x;
//	n[1] = y - p.y;
//
//	return n;
//    }
//
//    // find the x, y coords where the nth snapshot should be drawn
//    public int[] toSnapGrid(int n) {
//	int[] p = new int[2];
//	p[0] = (n % snapcols) * snapwidth;
//	p[1] = (n / snapcols) * SNAPHEIGHT;
//
//	return p;
//    }
//
//    // does component c contain the point x, y in top-level frame coordinates?
//    public boolean containsGlobal(Component c, int x, int y) {
//	Point p = getLocationGlobal(c);
//	Dimension d = c.size();
//
//	return ((x >= p.x) && (x < p.x + d.width) && (y >= p.y) && (y < p.y + d.height));
//    }
//
//    // insert a face frame into the vector v in its proper order based on its time component
//    protected void insertInOrder(Vector v, FaceFrame ff) {
//	int i = 0;
//	while(i < v.size()) {
//	    if(ff.getTime() <= ((FaceFrame)v.elementAt(i)).getTime())
//		break;
//	    i++;
//	}
//	v.insertElementAt(ff, i);
//    }
//
//    // disable rollovers while animation is active
//    private boolean roll() {
//	if(doRollOver)
//	    if(athread == null)
//		return true;
//	    else if(!athread.isAlive())
//		return true;
//	return false;
//    }
//
//    // display string in message panel, and show continue button if c is true
//    private void showMsgPan(String m, boolean c) {
//	Container t = contBut.getParent();
//	if(c && (t == null))
//	    msgPan.add(contBut);
//	else if(!c && (t != null))
//	    msgPan.remove(contBut);
//	message.setText(m);
//	msgPan.layout();
//	fileCard.show(filePan, "msg");
//    }
//}
//
//// Encapsulates functionality for making a 32x32 icon with a gif
//// and separate borders for default state, rollover, and mousedown
//class GraphicButton extends Panel {
//    public static final int DEFAULT = 0;
//    public static final int ROLLOVER = 1;
//    public static final int CLICK = 2;
//    public static final Color BACKGROUNDColor = new Color(204, 204, 204);
//    public static final Color BORDERColor = Color.black;
//    public static final Color LIGHTColor = Color.white;
//    public static final Color DARKColor = new Color(153, 153, 153);
//
//    Image icon;
//    int mode;
//
//    public GraphicButton(Image im) {
//	super();
//	mode = DEFAULT;
//	icon = im;
//    }
//
//    public boolean handleEvent(Event e) {
//	if(e.id == Event.MOUSE_ENTER) {
//	    mode = ROLLOVER;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    mode = DEFAULT;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    mode = CLICK;
//	    drawBorder();
//	    return false;
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(mode == CLICK) {
//		mode = ROLLOVER;
//		drawBorder();
//	    }
//	    return false;
//	}
//	    
//	return false;
//    }
//
//    public void drawBorder() {
//	Color border, topleft, bottomright;
//
//	switch(mode) {
//	case ROLLOVER:
//	    border = BORDERColor;
//	    topleft = LIGHTColor;
//	    bottomright = DARKColor;
//	    break;
//	case CLICK:
//	    border = BORDERColor;
//	    topleft = DARKColor;
//	    bottomright = LIGHTColor;
//	    break;
//	default:
//	    border = topleft = bottomright = BACKGROUNDColor;
//	    break;
//	}
//
//	Graphics a = this.getGraphics();
//	a.setColor(border);
//	a.drawRect(0, 0, 31, 31);
//	a.setColor(topleft);
//	a.drawLine(1, 1, 30, 1);
//	a.drawLine(1, 2, 29, 2);
//	a.drawLine(1, 1, 1, 29);
//	a.drawLine(2, 1, 2, 28);
//	a.setColor(bottomright);
//	a.drawLine(1, 30, 30, 30);
//	a.drawLine(2, 29, 30, 29);
//	a.drawLine(30, 2, 30, 30);
//	a.drawLine(29, 3, 29, 30);
//    }
//
//    public void setImage(Image im) {
//	icon = im;
//	paint(this.getGraphics());
//    }
//
//    public void paint(Graphics g) {
//	Graphics a = this.getGraphics();
//	a.drawImage(icon, 0, 0, this);
//	drawBorder();
//    }
//
//    public Dimension getPreferredSize() {
//	return new Dimension(32, 32);
//    }
//}
//
//// 32 x 32 icon toggle button
//class OnOffGraphicButton extends GraphicButton {
//    boolean isOn;
//
//    public OnOffGraphicButton(Image im) {
//	this(im, false);
//    }
//
//    public OnOffGraphicButton(Image im, boolean o) {
//	super(im);
//	isOn = o;
//	mode = isOn ? CLICK : DEFAULT;
//    }
//
//    public boolean handleEvent(Event e) {
//	if(e.id == Event.MOUSE_ENTER) {
//	    mode = isOn ? CLICK : ROLLOVER;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    mode = isOn ? CLICK : DEFAULT;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    isOn = !isOn;
//	    mode = isOn ? CLICK : DEFAULT;
//	    drawBorder();
//	    return false;
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(!isOn) {
//		mode = ROLLOVER;
//		drawBorder();
//	    }
//	    return false;
//	}
//	    
//	return false;
//    }
//
//    public boolean getIsOn() {
//	return isOn;
//    }
//
//    public void setIsOn(boolean on) {
//	isOn = on;
//	mode = isOn ? CLICK : DEFAULT;
//	drawBorder();
//    }
//}
//
//// thread to control 'playing' of animation
//// sends messages to face applet, draws tic on timeline
//class AnimThread extends Thread {
//    private Vector frames;
//    private Face2bApplet faceApplet;
//    private long startTime;
//    private double eTime;
//    private int marker;
//    private boolean running;
//    private Graphics g;
//    private GraphAnim callClass;
//    private Scrollbar sb;
//    private int oldScr;
//
//    public AnimThread(GraphAnim cc, Vector fr, Face2bApplet fa, Graphics gr, Scrollbar s) {
//	frames = fr;
//	faceApplet = fa;
//	startTime = System.currentTimeMillis();
//	eTime = 0;
//	marker = 0;
//	running = true;
//	g = gr;
//	callClass = cc;
//	sb = s;
//    }
//
//    public void run() {
//	boolean first = true;
//	double currTime;
//	FaceFrame ff = (FaceFrame)frames.elementAt(marker);
//	oldScr = sb.getValue();
//	while(marker < frames.size()) {
//	    currTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;
//	    while(eTime <= ff.getTime() && ff.getTime() <= currTime) {
//		faceApplet.setTargets(ff.getTargets());
//		marker++;
//		if(marker == frames.size())
//		    break;
//		ff = (FaceFrame)frames.elementAt(marker);
//	    }
//	    synchronized(sb) {
//		drawTic(eTime, currTime, first, false);
//	    }
//
//	    eTime = currTime;
//	    try {
//		sleep(30);
//	    }
//	    catch(InterruptedException ie) {
//		;
//	    }
//	    first = false;
//	}
//
//	drawTic(eTime, 0, first, true);
//	running = false;
//    }
//
//    public void drawTic(double elapsed, double current, boolean first, boolean last) {
//	g.setXORMode(GraphAnim.NUMBACK);
//	g.setColor(Color.white);
//	if(!first && oldScr == sb.getValue()) {
//	    int x = (int)Math.round(elapsed * callClass.timediv * callClass.snapwidth);
//	    g.drawLine(x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 4, x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 1);
//	}
//	if(!last) {
//	    int x = (int)Math.round(current * callClass.timediv * callClass.snapwidth);
//	    g.drawLine(x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 4, x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 1);
//	}
//	g.setPaintMode();
//	oldScr = sb.getValue();
//    }
//
//    public boolean isRunning() {
//	return running;
//    }
//}
//
//// spacer for use in icon bars
//class Spacer extends Panel {
//    private int width, height;
//
//    public Spacer(int w, int h) {
//	super();
//	width = w;
//	height = h;
//    }
//
//    public Dimension getPreferredSize() {
//	return new Dimension(width, height);
//    }
//}
//
//// need to do this so that snapshot and animation face panels get their paint events
//class DrawPanel extends Panel {
//    private int type;
//    private GraphApp rent;
//
//    public DrawPanel(GraphApp g, int t) {
//	rent = g;
//	type = t;
//    }
//
//    public void paint(Graphics g) {
//	rent.draw(type);
//    }
//}
//
//class MessagePanel extends Panel {
//    private Label lab;
//    private BorderLayout lay;
//
//    public MessagePanel(String s) {
//	if(lab == null) {
//	    lab = new Label();
//	    lab.setAlignment(Label.CENTER);
//	    lay = new BorderLayout();
//	    setLayout(lay);
//	    add("Center", lab);
//	}
//	setText(s);
//    }
//
//    public void setText(String s) {
//	lab.setText(s);
//	//	getLayout().invalidateLayout(this);
//    }
//}
