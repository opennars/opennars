package nars.gui.input.image;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import nars.gui.Window;

public class SketchPointCloudPanel extends Panel implements MouseListener, MouseMotionListener, ActionListener {

    public static void main(String[] args) {
        Window w = new Window("Sketch", new SketchPointCloudPanel());
        w.setSize(500, 500);
        w.setVisible(true);
    }

    static final int GESTURE_PROCESSED = 0;
    static final int STROKE_COMPLETE = 2;
    static final int STROKE_IN_PROGRESS = 1;
    static final String DEFAULT_USER_DEFINED_STRING = "Type name here...";
    int state = GESTURE_PROCESSED;

    int _currentStrokeId = 0;
    PointCloudLibrary _library = PointCloudLibrary.getDemoLibrary();
    ArrayList<PointCloudPoint> _currentGesture = new ArrayList<PointCloudPoint>();
    Label caption = new Label();
    Button clearCanvas = new Button();
    Button deleteUserDefined = new Button();
    Button addUserDefined = new Button();
    Button addStandard = new Button();
    TextField userDefinedName = new TextField();
    Choice standardNames = new Choice();
    String name = "";
    double score = 0;
    Image offScreen;
    Color[] lineColors = new Color[30];
    Color defaultColor = new Color(0f, 0f, 0f);

    public SketchPointCloudPanel() {
        super(new BorderLayout());
        
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
        caption.setBackground(Color.YELLOW);
        clearCanvas.setLabel("Clear");
        clearCanvas.addActionListener(this);
        tempContainer.add(clearCanvas, BorderLayout.EAST);
        add(tempContainer, BorderLayout.NORTH);

        tempContainer = new Panel();
        tempContainer.setLayout(new GridLayout(3, 1));

        Panel veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        veryTempContainer.add(new Label("Add as example of existing type:"));
        veryTempContainer.add(standardNames);
        addStandard.setLabel("Add");
        addStandard.setEnabled(false);
        addStandard.addActionListener(this);
        veryTempContainer.add(addStandard);
        tempContainer.add(veryTempContainer);

        veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        veryTempContainer.add(new Label("Add as example of custom type:"));
        userDefinedName.setText(DEFAULT_USER_DEFINED_STRING);
        veryTempContainer.add(userDefinedName);
        addUserDefined.setLabel("Add");
        addUserDefined.setEnabled(false);
        addUserDefined.addActionListener(this);
        veryTempContainer.add(addUserDefined);
        tempContainer.add(veryTempContainer);

        veryTempContainer = new Panel();
        veryTempContainer.setLayout(new FlowLayout());
        veryTempContainer.add(new Label("Delete all user-defined gestures:"));
        veryTempContainer.add(new Label("                    "));
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

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearCanvas) {
            clearCanvas();
            return;
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
        _currentGesture = new ArrayList<PointCloudPoint>();
        _currentStrokeId = 0;
        caption.setText("");
        addUserDefined.setEnabled(false);
        addStandard.setEnabled(false);
        repaint();
    }

    public void mouseEntered(MouseEvent e) //mouse entered canvas
    {
    }

    public void mouseExited(MouseEvent e) //mouse left canvas
    {
    }

    public void mouseClicked(MouseEvent e) //mouse pressed-depressed (no motion in between), if there's motion -> mouseDragged
    {
    }

    public void update(MouseEvent e) {
        PointCloudPoint p = new PointCloudPoint(e.getX(), e.getY(), _currentStrokeId);
        _currentGesture.add(p);
        repaint();
        e.consume();
    }

    public void mousePressed(MouseEvent e) {
        int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1: {
                if (state == GESTURE_PROCESSED) {
                    _currentGesture = new ArrayList<PointCloudPoint>();
                }

                state = STROKE_IN_PROGRESS;
                caption.setForeground(lineColors[_currentStrokeId]);
                caption.setText("Capturing stroke: " + (_currentStrokeId + 1));
                update(e);
                return;
            }

            case MouseEvent.BUTTON3: {
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
                state = GESTURE_PROCESSED;
                _currentStrokeId = 0;
                addUserDefined.setEnabled(true);
                addStandard.setEnabled(true);
                return;
            }

            default: {
                return;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1: {
                state = STROKE_COMPLETE;
                caption.setForeground(lineColors[_currentStrokeId]);
                caption.setText("Stroke " + (_currentStrokeId + 1) + " recorded");
                update(e);
                _currentStrokeId++;
                return;
            }

            default: {
                return;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        state = STROKE_IN_PROGRESS;
        update(e);
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
