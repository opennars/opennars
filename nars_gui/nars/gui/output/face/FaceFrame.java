package nars.gui.output.face;


import java.awt.Image;
import java.util.StringTokenizer;

public class FaceFrame extends Object {
    private double[][] targets;
    private Image snapshot;
    private double when;

    public FaceFrame(double[][] t, Image snap, double w) {
	targets = new double[2][t[0].length];
	for(int s = 0; s < 2; s++)
	    for(int v = 0; v < t[0].length; v++)
		targets[s][v] = t[s][v];
	snapshot = snap;
	when = w;
    }
    
    public FaceFrame(FaceFrame f) {
	targets = f.getTargets();
	snapshot = f.getSnapshot();
	when = f.getTime();
    }

    public FaceFrame(String s) {
	StringTokenizer outside = new StringTokenizer(s, "|\n");
	String w = outside.nextToken();
	when = Double.valueOf(w).doubleValue();

	String val = outside.nextToken();
	StringTokenizer inside = new StringTokenizer(val, ",");
	targets = new double[2][inside.countTokens()];
	int i = 0;
	String v;
	while(inside.hasMoreTokens()) {
	    v = inside.nextToken();
	    targets[0][i] = Double.valueOf(v).doubleValue();
	    i++;
	}

	val = outside.nextToken();
	inside = new StringTokenizer(val, ",");
	i = 0;
	while(inside.hasMoreTokens()) {
	    v = inside.nextToken();
	    targets[1][i] = Double.valueOf(v).doubleValue();
	    i++;
	}
	snapshot = null;
    }

    public double[][] getTargets() {
	return targets;
    }

    public Image getSnapshot() {
	return snapshot;
    }

    public void setSnapshot(Image snap) {
	snapshot = snap;
    }

    public double getTime() {
	return when;
    }

    public void setTime(double t) {
	when = t;
    }

    public String toString() {
	String retVal = new String();
	retVal += when + "\n";
	for(int s = 0; s < 2; s++) {
	    for(int v = 0; v < targets[s].length; v++) {
		if(v != 0)
		    retVal += ",";
		retVal += "" + targets[s][v];
	    }
	    retVal += "\n";
	}
	return retVal;
    }
}
