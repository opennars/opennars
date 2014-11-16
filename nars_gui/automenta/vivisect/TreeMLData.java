package automenta.vivisect;

import java.awt.Color;
import java.util.TreeMap;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLDataCentroid;
import org.encog.util.kmeans.Centroid;


/**
 * Used by Chart, a chart data set is a container to store chart data.
 */
public class TreeMLData implements MLData {

    //TODO use a primitive collection
    public final TreeMap<Integer,Double> values;
    
    protected Color colour;
	//protected double strokeWeight = 1;
    //protected int[] colors = new int[0];

    boolean resetRangeEachCycle = true;
    public final String label;
    protected final int capacity;
    private double[] specificMinMax;
    
    private boolean specificRange;
    double defaultValue = Double.NaN;

    /** initializes with no fixed capacity */
    public TreeMLData(String theName, Color color) {
        this(theName, color, -1);
    }
    
    public TreeMLData(TreeMLData t) {
        this.label = t.label;
        this.colour = t.colour;
        this.capacity = t.capacity;
        this.values = t.values;
    }
    
    public TreeMLData(String theName, Color color, int historySize) {
        label = theName;
        colour = color;
        capacity = historySize;
        values = new TreeMap();
    }

    public TreeMLData setRange(double min, double max) {
        this.specificRange = true;
        this.specificMinMax = new double[] { min, max };
        return this;
    }

    public Color getColor() {
        return colour;
    }

    public int getStart() { 
        if (values.isEmpty())
            return 0;
        return values.firstKey(); 
    }
    public int getEnd() { 
        if (values.isEmpty())
            return 0;
        return values.lastKey(); 
    }

    @Override
    public void clear() {
        values.clear();
    }

    

    @Override
    public void add(final int t, final double f) {
        setData(t, f);
    }
    
    @Override
    public void setData(final int t, final double f) {
        
        values.put(t, f);

        if (capacity!=-1) {
            while (values.size() > capacity) {
                //TODO configurable removal policy                
                values.remove(values.firstKey());            
            }
        }
        
    }

    /** clears the values and sets the data as if it were an array, starting at index 0 */
    @Override public void setData(double[] doubles) {
        values.clear();
        
        int j = 0;
        for (double d : doubles) {
            setData(j++, d);
        }
    }
    
    

    public double getSpecificMin() {        
        return specificMinMax[0];
    }

    public double getSpecificMax() {        
        return specificMinMax[1];
    }

    @Override
    public double getData(int t) {
        Double f = values.get(t);
        if (f == null) {
            return defaultValue;
        }
        return f;
    }

    @Override
    public double[] getData() {
        int size = size();
        double[] n = new double[size];
        int j = 0;
        for (int i = getStart(); i <= getEnd(); i++) {
            n[j++] = getData(i);
        }
        return n;
    }

    @Override
    public TreeMLData clone() {
        return new TreeMLData(this);
    }

    
    
    public double[] getMinMax(int start, int end) {
        if (specificRange)
            return specificMinMax;
        
        double min=Double.POSITIVE_INFINITY, max=Double.NEGATIVE_INFINITY;
        for (int i = start; i < end; i++) {
            
            Double v = values.get(i);
            if (v == null)
                continue;
            
            if (i == start)
                min = max = v;
            else {
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }
        return new double[] { min, max };
    }

    @Override
    public int size() {
        return (int) (getEnd() - getStart());
    }

    
    public double[] getMinMax() {
        return getMinMax(getStart(), getEnd());
    }

    @Override
    public Centroid<MLData> createCentroid() {
        return new BasicMLDataCentroid(this);
    }

    public void push(double v) {
        if (values.isEmpty())
            setData(0, v);
        else
            setData(getEnd()+1, v);
    }


    
    public static class FirstOrderDifferenceTimeSeries extends TreeMLData {

        public final TreeMLData data;

        public FirstOrderDifferenceTimeSeries(String name, TreeMLData s) {
            super(name, Video.getColor(name, 0.8f, 0.8f), 1);
            this.data = s;
        }

        @Override
        public double getData(final int t) {
            double prev = data.getData(t - 1);
            if (Double.isNaN(prev)) {
                return 0;
            }

            double cur = data.getData(t);
            if (Double.isNaN(cur)) {
                return 0;
            }

            return cur - prev;
        }

    }

}
//
///**
// * Use charts to display double array data as line chart, yet experimental, but see the ControlP5chart example
// * for more details.
// * 
// * @example controllers/ControlP5chart
// */
//public class Chart extends Controller<Chart> {
//
//	public final static int LINE = 0;
//
//	public final static int BAR = 1;
//
//	public final static int BAR_CENTERED = 2;
//
//	public final static int HISTOGRAM = 3;
//
//	public final static int PIE = 4;
//
//	public final static int AREA = 5;
//
//	protected final LinkedHashMap<String, ChartDataSet> _myDataSet;
//
//	protected double resolution = 1;
//
//	protected double strokeWeight = 1;
//
//	protected double _myMin = 0;
//
//	protected double _myMax = 1;
//
//	/**
//	 * Convenience constructor to extend Chart.
//	 * 
//	 * @example use/ControlP5extendController
//	 * @param theControlP5
//	 * @param theName
//	 */
//	public Chart(ControlP5 theControlP5, String theName) {
//		this(theControlP5, theControlP5.getDefaultTab(), theName, 0, 0, 200, 100);
//		theControlP5.register(theControlP5.papplet, theName, this);
//	}
//
//	public Chart(ControlP5 theControlP5, ControllerGroup<?> theParent, String theName, double theX, double theY,
//			int theWidth, int theHeight) {
//		super(theControlP5, theParent, theName, theX, theY, theWidth, theHeight);
//		setRange(0, theHeight);
//		_myDataSet = new LinkedHashMap<String, ChartDataSet>();
//	}
//
//	public Chart setRange(double theMin, double theMax) {
//		_myMin = theMin;
//		_myMax = theMax;
//		return this;
//	}
//
//	public Chart setColors(String theSetIndex, int... theColors) {
//		getDataSet().get(theSetIndex).setColors(theColors);
//		return this;
//	}
//
//	public Chart addData(ChartData theItem) {
//		return addData(getFirstDataSetIndex(), theItem);
//	}
//
//	private String getFirstDataSetIndex() {
//		return getDataSet().keySet().iterator().next();
//	}
//
//	public Chart addData(String theSetIndex, ChartData theItem) {
//		getDataSet(theSetIndex).add(theItem);
//		return this;
//	}
//
//	public Chart addData(double theValue) {
//		ChartData cdi = new ChartData(theValue);
//		getDataSet(getFirstDataSetIndex()).add(cdi);
//		return this;
//	}
//
//	public Chart addData(String theSetIndex, double theValue) {
//		ChartData cdi = new ChartData(theValue);
//		getDataSet(theSetIndex).add(cdi);
//		return this;
//	}
//
//	public Chart addData(ChartDataSet theChartData, double theValue) {
//		ChartData cdi = new ChartData(theValue);
//		theChartData.add(cdi);
//		return this;
//	}
//
//	// array operations see syntax
//	// http://www.w3schools.com/jsref/jsref_obj_array.asp
//
//	/**
//	 * adds a new double at the beginning of the data set.
//	 */
//	public Chart unshift(double theValue) {
//		return unshift(getFirstDataSetIndex(), theValue);
//	}
//
//	public Chart unshift(String theSetIndex, double theValue) {
//		if (getDataSet(theSetIndex).size() > (width / resolution)) {
//			removeLast(theSetIndex);
//		}
//		return addFirst(theSetIndex, theValue);
//	}
//
//	public Chart add(double theValue) {
//		return add(getFirstDataSetIndex(), theValue);
//	}
//
//	public Chart add(String theSetIndex, double theValue) {
//		if (getDataSet(theSetIndex).size() > (width / resolution)) {
//			removeFirst(theSetIndex);
//		}
//		return addLast(theSetIndex, theValue);
//	}
//
//	public Chart addFirst(double theValue) {
//		return addFirst(getFirstDataSetIndex(), theValue);
//	}
//
//	public Chart addFirst(String theSetIndex, double theValue) {
//		ChartData cdi = new ChartData(theValue);
//		getDataSet(theSetIndex).add(0, cdi);
//		return this;
//	}
//
//	public Chart addLast(double theValue) {
//		return addLast(getFirstDataSetIndex(), theValue);
//	}
//
//	public Chart addLast(String theSetIndex, double theValue) {
//		ChartData cdi = new ChartData(theValue);
//		getDataSet(theSetIndex).add(cdi);
//		return this;
//	}
//
//	public Chart removeLast() {
//		return removeLast(getFirstDataSetIndex());
//	}
//
//	public Chart removeLast(String theSetIndex) {
//		return removeData(theSetIndex, getDataSet(theSetIndex).size() - 1);
//	}
//
//	public Chart removeFirst() {
//		return removeFirst(getFirstDataSetIndex());
//	}
//
//	public Chart removeFirst(String theSetIndex) {
//		return removeData(theSetIndex, 0);
//	}
//
//	public Chart removeData(ChartData theItem) {
//		removeData(getFirstDataSetIndex(), theItem);
//		return this;
//	}
//
//	public Chart removeData(String theSetIndex, ChartData theItem) {
//		getDataSet(theSetIndex).remove(theItem);
//		return this;
//	}
//
//	public Chart removeData(int theItemIndex) {
//		removeData(getFirstDataSetIndex(), theItemIndex);
//		return this;
//	}
//
//	public Chart removeData(String theSetIndex, int theItemIndex) {
//		if (getDataSet(theSetIndex).size() < 1) {
//			return this;
//		}
//		getDataSet(theSetIndex).remove(theItemIndex);
//		return this;
//	}
//
//	public Chart setData(int theItemIndex, ChartData theItem) {
//		getDataSet(getFirstDataSetIndex()).set(theItemIndex, theItem);
//		return this;
//	}
//
//	public Chart setData(String theSetItem, int theItemIndex, ChartData theItem) {
//		getDataSet(theSetItem).set(theItemIndex, theItem);
//		return this;
//	}
//
//	public Chart addDataSet(String theName) {
//		getDataSet().put(theName, new ChartDataSet(theName));
//		return this;
//	}
//
//	public Chart setDataSet(ChartDataSet theItems) {
//		setDataSet(getFirstDataSetIndex(), theItems);
//		return this;
//	}
//
//	public Chart setDataSet(String theSetIndex, ChartDataSet theChartData) {
//		getDataSet().put(theSetIndex, theChartData);
//		return this;
//	}
//
//	public Chart removeDataSet(String theIndex) {
//		getDataSet().remove(theIndex);
//		return this;
//	}
//
//	public Chart setData(double... theValues) {
//		setData(getFirstDataSetIndex(), theValues);
//		return this;
//	}
//
//	public Chart setData(String theSetIndex, double... theValues) {
//		if (getDataSet().get(theSetIndex).size() != theValues.length) {
//			getDataSet().get(theSetIndex).clear();
//			for (int i = 0; i < theValues.length; i++) {
//				getDataSet().get(theSetIndex).add(new ChartData(0));
//			}
//		}
//		int n = 0;
//		resolution = (double) width / (getDataSet().get(theSetIndex).size() - 1);
//		for (double f : theValues) {
//			getDataSet().get(theSetIndex).get(n++).setValue(f);
//		}
//		return this;
//	}
//
//	public Chart updateData(double... theValues) {
//		return setData(theValues);
//	}
//
//	public Chart updateData(String theSetIndex, double... theValues) {
//		return setData(theSetIndex, theValues);
//	}
//
//	public LinkedHashMap<String, ChartDataSet> getDataSet() {
//		return _myDataSet;
//	}
//
//	public ChartDataSet getDataSet(String theIndex) {
//		return getDataSet().get(theIndex);
//	}
//
//	public double[] getValuesFrom(String theIndex) {
//		return getDataSet(theIndex).getValues();
//	}
//
//	public ChartData getData(String theIndex, int theItemIndex) {
//		return getDataSet(theIndex).get(theItemIndex);
//	}
//
//	public int size() {
//		return getDataSet().size();
//	}
//
//	@Override
//	public void onEnter() {
//	}
//
//	@Override
//	public void onLeave() {
//	}
//
//	@Override
//	public Chart setValue(double theValue) {
//		// TODO Auto-generated method stub
//		return this;
//	}
//
//	public Chart setStrokeWeight(double theWeight) {
//		strokeWeight = theWeight;
//		for (ChartDataSet c : getDataSet().values()) {
//			c.setStrokeWeight(theWeight);
//		}
//		return this;
//	}
//
//	public double getStrokeWeight() {
//		return strokeWeight;
//	}
//
//	/**
//	 * ?
//	 * 
//	 * @param theValue
//	 * @return
//	 */
//	public Chart setResolution(int theValue) {
//		resolution = theValue;
//		return this;
//	}
//
//	public int getResolution() {
//		return (int) resolution;
//	}
//
//	/**
//	 * @exclude
//	 */
//	@ControlP5.Invisible
//	public Chart updateDisplayMode(ControllerViewType theMode) {
//		displayMode = theMode;
//		switch (theMode) {
//		case DEFAULT:
//			controllerView = new ChartViewPie();
//			break;
//		case IMAGE:
//			// _myDisplay = new ChartImageDisplay();
//			break;
//		case SPRITE:
//			// _myDisplay = new ChartSpriteDisplay();
//			break;
//		case CUSTOM:
//		default:
//			break;
//		}
//		return this;
//	}
//
//	public class ChartViewBar implements ControllerView<Chart> {
//
//		@Override
//		public void display(PApplet theApplet, Chart theController) {
//			theApplet.pushStyle();
//			theApplet.fill(getColour().getBackground());
//			theApplet.rect(0, 0, getWidth(), getHeight());
//			theApplet.noStroke();
//
//			Iterator<String> it = getDataSet().keySet().iterator();
//			String index = null;
//			double o = 0;
//			while (it.hasNext()) {
//				index = it.next();
//				double s = getDataSet(index).size();
//				for (int i = 0; i < s; i++) {
//					theApplet.fill(getDataSet(index).getColor(i));
//					double ww = ((width / s));
//					double hh = PApplet.map(getDataSet(index).get(i).getData(), _myMin, _myMax, 0, getHeight());
//					theApplet.rect(o + i * ww, getHeight(), (ww / getDataSet().size()),
//						-PApplet.min(getHeight(), PApplet.max(0, hh)));
//				}
//				o += ((width / s)) / getDataSet().size();
//			}
//			theApplet.popStyle();
//		}
//	}
//
//	public class ChartViewBarCentered implements ControllerView<Chart> {
//
//		@Override
//		public void display(PApplet theApplet, Chart theController) {
//			theApplet.pushStyle();
//			theApplet.fill(getColour().getBackground());
//			theApplet.rect(0, 0, getWidth(), getHeight());
//			theApplet.noStroke();
//
//			Iterator<String> it = getDataSet().keySet().iterator();
//			String index = null;
//			double o = 0;
//			int n = 4;
//			int off = (getDataSet().size() - 1) * n;
//			while (it.hasNext()) {
//				index = it.next();
//				int s = getDataSet(index).size();
//				double step = (double) width / (double) (s);
//				double ww = step - (width % step);
//				ww -= 1;
//				ww = PApplet.max(1, ww);
//
//				for (int i = 0; i < s; i++) {
//					theApplet.fill(getDataSet(index).getColor(i));
//					ww = ((width / s) * 0.5f);
//					double hh = PApplet.map(getDataSet(index).get(i).getData(), _myMin, _myMax, 0, getHeight());
//					theApplet.rect(-off / 2 + o + i * ((width / s)) + ww / 2, getHeight(), ww,
//						-PApplet.min(getHeight(), PApplet.max(0, hh)));
//				}
//				o += n;
//			}
//			theApplet.popStyle();
//		}
//	}
//
//	public class ChartViewLine implements ControllerView<Chart> {
//
//		@Override
//		public void display(PApplet theApplet, Chart theController) {
//
//			theApplet.pushStyle();
//			theApplet.fill(getColour().getBackground());
//			theApplet.rect(0, 0, getWidth(), getHeight());
//			theApplet.noFill();
//			Iterator<String> it = getDataSet().keySet().iterator();
//			String index = null;
//			while (it.hasNext()) {
//				index = it.next();
//				theApplet.stroke(getDataSet(index).getColor(0));
//				theApplet.strokeWeight(getDataSet(index).getStrokeWeight());
//
//				theApplet.beginShape();
//				double res = ((double) getWidth()) / (getDataSet(index).size() - 1);
//				for (int i = 0; i < getDataSet(index).size(); i++) {
//					double hh = PApplet.map(getDataSet(index).get(i).getData(), _myMin, _myMax, getHeight(), 0);
//					theApplet.vertex(i * res, PApplet.min(getHeight(), PApplet.max(0, hh)));
//				}
//				theApplet.endShape();
//			}
//			theApplet.noStroke();
//			theApplet.popStyle();
//		}
//	}
//
//	public class ChartViewArea implements ControllerView<Chart> {
//
//		@Override
//		public void display(PApplet theApplet, Chart theController) {
//
//			theApplet.pushStyle();
//			theApplet.fill(getColour().getBackground());
//			theApplet.rect(0, 0, getWidth(), getHeight());
//			theApplet.noStroke();
//
//			Iterator<String> it = getDataSet().keySet().iterator();
//			String index = null;
//			while (it.hasNext()) {
//				index = it.next();
//				double res = ((double) getWidth()) / (getDataSet(index).size() - 1);
//
//				theApplet.fill(getDataSet(index).getColor(0));
//				theApplet.beginShape();
//				theApplet.vertex(0, getHeight());
//
//				for (int i = 0; i < getDataSet(index).size(); i++) {
//					double hh = PApplet.map(getDataSet(index).get(i).getData(), _myMin, _myMax, getHeight(), 0);
//					theApplet.vertex(i * res, PApplet.min(getHeight(), PApplet.max(0, hh)));
//				}
//				theApplet.vertex(getWidth(), getHeight());
//				theApplet.endShape(PConstants.CLOSE);
//			}
//			theApplet.noStroke();
//			theApplet.popStyle();
//		}
//	}
//
//	public class ChartViewPie implements ControllerView<Chart> {
//
//		@Override
//		public void display(PApplet theApplet, Chart theController) {
//			theApplet.pushStyle();
//			theApplet.pushMatrix();
//
//			Iterator<String> it = getDataSet().keySet().iterator();
//			String index = null;
//			while (it.hasNext()) {
//				index = it.next();
//				double total = 0;
//				for (int i = 0; i < getDataSet(index).size(); i++) {
//					total += getDataSet(index).get(i).getData();
//				}
//
//				double segment = ControlP5.TWO_PI / total;
//				double angle = -ControlP5.HALF_PI;
//
//				theApplet.noStroke();
//				for (int i = 0; i < getDataSet(index).size(); i++) {
//					theApplet.fill(getDataSet(index).getColor(i));
//					double nextAngle = angle + getDataSet(index).get(i).getData() * segment;
//
//					// a tiny offset to even out render artifacts when in smooth() mode.
//					double a = PApplet.max(0, PApplet.map(getWidth(), 0, 200, 0.05f, 0.01f));
//
//					theApplet.arc(0, 0, getWidth(), getHeight(), angle - a, nextAngle);
//					angle = nextAngle;
//				}
//				theApplet.translate(0, (getHeight() + 10));
//			}
//			theApplet.popMatrix();
//			theApplet.popStyle();
//		}
//	}
//
//	public Chart setView(int theType) {
//		switch (theType) {
//		case (PIE):
//			setView(new ChartViewPie());
//			break;
//		case (LINE):
//			setView(new ChartViewLine());
//			break;
//		case (BAR):
//			setView(new ChartViewBar());
//			break;
//		case (BAR_CENTERED):
//			setView(new ChartViewBarCentered());
//			break;
//		case (AREA):
//			setView(new ChartViewArea());
//			break;
//		default:
//			System.out.println("Sorry, this ChartView does not exist");
//			break;
//		}
//		return this;
//	}
//
//	@Override
//	public String getInfo() {
//		return "type:\tChart\n" + super.toString();
//	}
//
//	@Override
//	public String toString() {
//		return super.toString() + " [ " + getData() + " ]" + " Chart " + "(" + this.getClass().getSuperclass() + ")";
//	}
//
//}

/*
 * NOTES what is the difference in meaning between chart and graph
 * http://answers.yahoo.com/question/index?qid=20090101193325AA3mgMl
 */
