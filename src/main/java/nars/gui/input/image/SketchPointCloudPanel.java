package nars.gui.input.image;

import automenta.vivisect.swing.NWindow;
import nars.NAR;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class SketchPointCloudPanel extends Panel implements MouseListener, MouseMotionListener, ActionListener {

    public static void main(String[] args) {
        NWindow w = new NWindow("Sketch", new SketchPointCloudPanel(null));
        w.setSize(500, 500);
        w.setVisible(true);
    }

    public NAR nar;
    static final int GESTURE_PROCESSED = 0;
    static final int STROKE_COMPLETE = 2;
    static final int STROKE_IN_PROGRESS = 1;
    static final String DEFAULT_USER_DEFINED_STRING = "";
    int state = GESTURE_PROCESSED;

    int _currentStrokeId = 0;
    PointCloudLibrary _library = PointCloudLibrary.getDemoLibrary();
    ArrayList<PointCloudPoint> _currentGesture = new ArrayList<>();
    Label caption = new Label();
    Button clearCanvas = new Button();
    Button deleteUserDefined = new Button();
    Button addUserDefined = new Button();
    Button addStandard = new Button();
    Button addInput = new Button("Add Input");
    TextField userDefinedName = new TextField();
    Choice standardNames = new Choice();
    String name = "";
    double score = 0;
    Image offScreen;
    Color[] lineColors = new Color[30];
    Color defaultColor = Color.WHITE;
    Color backColor=new Color(50,50,50);
    
    public SketchPointCloudPanel(NAR nar) {
        super(new BorderLayout());
        this.nar=nar;
        for (int i = 0; i < lineColors.length; i++) {
            lineColors[i] = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        }

        String[] s = _library.getNames().toArray(new String[0]);

        for (int i = 0; i < s.length; i++) {
            standardNames.add(s[i]);
        }

        setLayout(new BorderLayout());
        Panel tempContainer = new Panel();
        tempContainer.setLayout(new BorderLayout());
        tempContainer.add(caption, BorderLayout.CENTER);
        clearCanvas.setLabel("Clear");
        clearCanvas.addActionListener(this);
        tempContainer.add(addInput,BorderLayout.LINE_START);
        caption.setBackground(backColor);
        tempContainer.setBackground(backColor);
        clearCanvas.setBackground(backColor);
        tempContainer.setForeground(Color.WHITE);
        clearCanvas.setForeground(Color.WHITE);
        
        addInput.addActionListener(this);
        addInput.setBackground(backColor);
        addInput.setForeground(Color.WHITE);
        
        deleteUserDefined.setBackground(backColor);
        deleteUserDefined.setForeground(Color.WHITE);
        
        tempContainer.add(clearCanvas, BorderLayout.EAST);
        add(tempContainer, BorderLayout.NORTH);

        tempContainer = new Panel();
        tempContainer.setLayout(new GridLayout(3, 1));

        Panel veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        //veryTempContainer.add(new Label("Add as example of existing type:"));
        //veryTempContainer.add(standardNames);
        //addStandard.setLabel("Add");
        //addStandard.setEnabled(false);
       // addStandard.addActionListener(this);
        //veryTempContainer.add(addStandard);
      //  veryTempContainer.add(addInput);
        //tempContainer.add(veryTempContainer);

        veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        veryTempContainer.add(new Label("Add as example of type:"));
        veryTempContainer.setForeground(Color.WHITE);
        veryTempContainer.setBackground(backColor);
        userDefinedName.setColumns(10);
        userDefinedName.setText(DEFAULT_USER_DEFINED_STRING);
        veryTempContainer.add(userDefinedName);
        userDefinedName.setBackground(Color.BLACK);
        userDefinedName.setForeground(Color.WHITE);
        addUserDefined.setLabel("Add");
        //addUserDefined.setEnabled(false);
        addUserDefined.addActionListener(this);
        addUserDefined.setForeground(Color.WHITE);
        addUserDefined.setBackground(backColor);

        veryTempContainer.add(addUserDefined);
        tempContainer.add(veryTempContainer);

        veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        veryTempContainer.add(new Label("Delete all types:"));
        veryTempContainer.add(new Label("                    "));
        veryTempContainer.setBackground(backColor);
        veryTempContainer.setForeground(Color.WHITE);
        deleteUserDefined.setLabel("Delete");
        deleteUserDefined.addActionListener(this);
        veryTempContainer.add(deleteUserDefined);
        tempContainer.add(veryTempContainer);

        add(tempContainer, BorderLayout.SOUTH);

        resize(400, 400);
        offScreen = createImage(getSize().width, getSize().height);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public String lastdrawing="";
    public String drawing="";
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearCanvas) {
            clearCanvas();
            return;
        }
        
        if(e.getSource() == addInput && !drawing.equals("")) {
            nar.addInput("<"+drawing.replace(" ","-")+" --> drawn>. :|:");
            if(lastdrawing!=null && !lastdrawing.equals("")) {
                if(Math.abs(coordx-lastcoordx)>10) {
                    String direction=coordx-lastcoordx > 0 ? "left" : "right"; 
                    String opdirection=coordx-lastcoordx > 0 ? "right" : "left"; 
                    nar.addInput("<(*,"+drawing.replace(" ","-")+","+lastdrawing.replace(" ","-")+") --> "+direction+">. :|:");
                    nar.addInput("<(*,"+lastdrawing.replace(" ","-")+","+drawing.replace(" ","-")+") --> "+opdirection+">. :|:");
                }
                if(Math.abs(coordy-lastcoordy)>10) {
                    String direction=coordy-lastcoordy > 0 ? "up" : "down"; 
                    String opdirection=coordy-lastcoordy > 0 ? "down" : "up"; 
                    nar.addInput("<(*,"+drawing.replace(" ","-")+","+lastdrawing.replace(" ","-")+") --> "+direction+">. :|:");
                    nar.addInput("<(*,"+lastdrawing.replace(" ","-")+","+drawing.replace(" ","-")+") --> "+opdirection+">. :|:");
                }
            }
            nar.step(1);
        }

        if (e.getSource() == deleteUserDefined) {
            _library.clear();
            caption.setText("All user defined gestures have been cleared");
            return;
        }

        if (e.getSource() == addStandard) {
            String name = standardNames.getSelectedItem();
            _library.addPointCloud(new PointCloud(name, _currentGesture));
            caption.setText("Gesture added as additional version of " + name);
            return;
        }

        if (e.getSource() == addUserDefined) {
            drawing="";
            String name = userDefinedName.getText();
            if (name.equals(DEFAULT_USER_DEFINED_STRING)) {
                caption.setText("You must enter a name for the gesture");
            } else {
                _library.addPointCloud(new PointCloud(name, _currentGesture));
                caption.setText("Gesture added with name " + name);
                userDefinedName.setText(DEFAULT_USER_DEFINED_STRING);
            }

            return;
        }
    }

    private void clearCanvas() {
        _currentGesture = new ArrayList<>();
        _currentStrokeId = 0;
        caption.setText("");
        addUserDefined.setEnabled(false);
        addStandard.setEnabled(false);
        repaint();
        coordx=0;
        coordy=0;
        lastcoordx=-1;
        lastcoordy=-1;
        lastdrawing="";
    }

    public void mouseEntered(MouseEvent e) //mouse entered canvas
    {
    }

    public void mouseExited(MouseEvent e) //mouse left canvas
    {
    }

    public void mouseClicked(MouseEvent e) //mouse pressed-depressed (no motion in between), if there's motion -> mouseDragged
    {
        if(e.getButton()==MouseEvent.BUTTON3) {
            _currentStrokeId++;
        }
        if(e.getButton()==MouseEvent.BUTTON2) {
            _currentStrokeId++;
        }
    }

    public void update(MouseEvent e) {
       // if(e.getX()<coordx) {
            coordx=e.getX();
            coordy=e.getY();
       // }
        PointCloudPoint p = new PointCloudPoint(e.getX(), e.getY(), _currentStrokeId);
        _currentGesture.add(p);
        repaint();
        e.consume();
    }

    int coordx=0;
    int coordy=0;
    int lastcoordx=-1;
    int lastcoordy=-1;
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1: {
                if (state == GESTURE_PROCESSED) {
                    //_currentGesture = new ArrayList<PointCloudPoint>();
                    lastcoordx=coordx;
                    lastcoordy=coordy;
                    coordx=0;
                    coordy=0;
                }

                state = STROKE_IN_PROGRESS;
                caption.setForeground(lineColors[_currentStrokeId]);
                caption.setText("Capturing stroke: " + (_currentStrokeId + 1));
                update(e);
                return;
            }

            case MouseEvent.BUTTON3: {
                _currentStrokeId++;
                if (state != STROKE_COMPLETE) {
                    return;
                }
                 
                PointCloud c = new PointCloud("input gesture", _currentGesture);
                ArrayList<PointCloudPoint> pts = c.getPoints();
                PointCloudMatchResult r = _library.originalRecognize(c);
                name = r.getName();
                score = r.getScore();
                caption.setForeground(defaultColor);
                caption.setText("Result: " + name + " (" + round(score, 2) + ")");
                lastdrawing=drawing;
                drawing=name;
                state = GESTURE_PROCESSED;
                _currentStrokeId = 0;
                addUserDefined.setEnabled(true);
                addStandard.setEnabled(true);
                _currentStrokeId++;
                return;
            }

            default: {
                _currentStrokeId++;
                return;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        int button = e.getButton();
        _currentStrokeId++;
        switch (button) {
            case MouseEvent.BUTTON1: {
                state = STROKE_COMPLETE;
                caption.setForeground(lineColors[_currentStrokeId]);
                caption.setText("Stroke " + (_currentStrokeId + 1) + " recorded");
                update(e);
                return;
            }

            default: {
                _currentStrokeId++;
                return;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        state = STROKE_IN_PROGRESS;
        update(e);
        if(e.getButton()==MouseEvent.BUTTON3 || e.getButton()==MouseEvent.BUTTON2)
            _currentStrokeId++;
    }

    public void paint(Graphics _g) {
        Graphics2D g = (Graphics2D)_g;
        
        int pointCount = _currentGesture.size();
        if (pointCount < 2) {
            return;
        }

        int lineColorIndex = 0;
        g.setColor(lineColors[lineColorIndex]);
        g.setStroke(new BasicStroke(4));

        for (int i = 0; i < pointCount - 1; i++) {
            PointCloudPoint p1 = _currentGesture.get(i);
            PointCloudPoint p2 = _currentGesture.get(i + 1);
            if (p1.getID() == p2.getID()) {
                g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                //g.drawOval((int) p1.getX(), (int) p1.getY(), 4, 4); //only works like this, others one creates a wrong line
            } else {
                i++;
                g.setColor(lineColors[++lineColorIndex]);
                continue;
            }
        }
    }

    private double round(double n, double d) // round 'n' to 'd' decimals
    {
        d = Math.pow(10, d);
        return Math.round(n * d) / d;
    }

}
