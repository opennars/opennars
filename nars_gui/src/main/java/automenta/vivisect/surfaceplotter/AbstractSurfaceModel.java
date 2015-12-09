package automenta.vivisect.surfaceplotter;

import automenta.vivisect.surfaceplotter.surface.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;


/**
 * {@link AbstractSurfaceModel} provides a writable implementation of the {@link SurfaceModel} interface.
 * Writting is available throught setters for any properties, and through a simple "Plotter" interface, to fill the curves.
 */
public class AbstractSurfaceModel implements SurfaceModel {
    private SurfaceVertex[][] vertex;

	/**
	 * Interface returned by this object to write values in this model
	 * 
	 * @author eric
	 */
	public interface Plotter {
		int getHeight();

		int getWidth();

		float getX(int i);

		float getY(int j);

		void setValue(int i, int j, float v1, float v2);

	}

	/**
	 * Parses defined functions and calculates surface vertices
	 */
	class PlotterImpl implements Plotter {
		int calcDivisions;
		boolean f1, f2;
		int i, j, total;
		int imgheight = 0;
		int imgwidth = 0;

		float min1, max1, min2, max2;
		int[] pixels = null;
		float stepx, stepy;
		float xfactor;
		float xi, xx, yi, yx;

		float yfactor;

		public PlotterImpl() {
			// reads the calcDivision that will be used
			calcDivisions = getCalcDivisions();
			setDataAvailable(false); // clean space
			total = (calcDivisions + 1) * (calcDivisions + 1); // compute total size
			f1 = hasFunction1;
			f2 = hasFunction2; // define the size of the plot
			surfaceVertex = allocateMemory(f1, f2, total); // allocate surfaceVertex
			setSurfaceVertex(surfaceVertex); // define as the current surfaceVertex
			setDataAvailable(true);
			min1 = max1 = min2 = max2 = Float.NaN;
			getProjector();
			try {
				xi = getXMin();
				yi = getYMin();
				xx = getXMax();
				yx = getYMax();
				if ((xi >= xx) || (yi >= yx))
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				setMessage("Error in ranges");
				return;
			}
			stepx = (xx - xi) / calcDivisions;
			stepy = (yx - yi) / calcDivisions;
			xfactor = 20 / (xx - xi);
			yfactor = 20 / (yx - yi);

			// fill the surface surfaceVertex with NaN
			for (int i = 0; i <= calcDivisions; i++)
				for (int j = 0; j <= calcDivisions; j++) {
					int k = i * (calcDivisions + 1) + j;

					float x = getX(i);
					float y = getY(j);
					if (f1) {
						surfaceVertex[0][k] = new SurfaceVertex((x - xi) * xfactor - 10, (y - yi) * yfactor - 10, Float.NaN);
					}
					if (f2) {
						surfaceVertex[1][k] = new SurfaceVertex((x - xi) * xfactor - 10, (y - yi) * yfactor - 10, Float.NaN);
					}
				}

		}

		public int getHeight() {
			return calcDivisions + 1;
		}

		public int getWidth() {
			return calcDivisions + 1;
		}

		/**
		 * Get the x float value that can be used to compute the fonction at
		 * position i.
		 * 
		 * @param i
		 *            index 0<=i<=calcDivisions.
		 * @author eric
		 */
		public float getX(int i) {
			return xi + i * stepx;
		}

		/**
		 * Get the x float value that can be used to compute the fonction at
		 * position i.
		 * 
		 * @param j
		 *            index 0<=j<=calcDivisions.
		 * @author eric
		 */
		public float getY(int j) {
			return yi + j * stepy;
		}

		/**
		 * Put an actual value at the (i,j) position for both first and second curve
		 * 
		 * @param i
		 *            index 0<=i<=calcDivisions.
		 * @param j
		 *            index 0<=j<=calcDivisions.
		 * @param v
		 *            value at that point.
		 * @see package.class
		 * @author eric
		 */
		public void setValue(int i, int j, float v1, float v2) {
			// v contains the value, and i, j the coordinate in the array
			float x = getX(i);
			float y = getY(j);
			int k = i * (calcDivisions + 1) + j;
			if (f1) {

				// v = compute(x,y);
				if (Float.isInfinite(v1))
					v1 = Float.NaN;
				if (!Float.isNaN(v1)) {
					if (Float.isNaN(max1) || (v1 > max1))
						max1 = v1;
					else if (Float.isNaN(min1) || (v1 < min1))
						min1 = v1;
				}
				surfaceVertex[0][k] = new SurfaceVertex((x - xi) * xfactor - 10, (y - yi) * yfactor - 10, v1);
			}
			if (f2) {
				// v = (float)parser2.evaluate();
				if (Float.isInfinite(v2))
					v2 = Float.NaN;
				if (!Float.isNaN(v2)) {
					if (Float.isNaN(max2) || (v2 > max2))
						max2 = v2;
					else if (Float.isNaN(min2) || (v2 < min2))
						min2 = v2;
				}
				surfaceVertex[1][k] = new SurfaceVertex((x - xi) * xfactor - 10, (y - yi) * yfactor - 10, v2);
			}
			z1Min = (float) floor(min1, 2);
			z1Max = (float) ceil(max1, 2);
			z2Min = (float) floor(min2, 2);
			z2Max = (float) ceil(max2, 2);

			autoScale();
			fireStateChanged();
		}
	}

	private static final int INIT_CALC_DIV = 20;
	private static final int INIT_DISP_DIV = 20;

	/**
	 * internally used to ceil values
	 * 
	 * @param d
	 * @param digits
	 * @return
	 */
	public static synchronized double ceil(double d, int digits) {
		if (d == 0)
			return d;
		long og = (long) Math.ceil((Math.log(Math.abs(d)) / Math.log(10)));
		double factor = Math.pow(10, digits - og);
		double res = Math.ceil((d * factor)) / factor;
		return res;
	}

	/**
	 * internally used to floor values
	 * 
	 * @param d
	 * @param digits
	 * @return
	 */
	public static synchronized double floor(double d, int digits) {
		if (d == 0)
			return d;
		// computes order of magnitude
		long og = (long) Math.ceil((Math.log(Math.abs(d)) / Math.log(10)));

		double factor = Math.pow(10, digits - og);
		// the matissa
		double res = Math.floor((d * factor)) / factor;
		// res contains the closed power of ten
		return res;
	}

	protected boolean autoScaleZ = true;

	/**
	 * Determines whether to show bounding box.
	 * 
	 * @return <code>true</code> if to show bounding box
	 */
	protected boolean boxed;
	protected int calcDivisions = INIT_CALC_DIV;
	protected ColorModelSet colorModel;
	protected int contourLines;
	/**
	 * Sets data availability flag
	 */
	protected boolean dataAvailable;
	protected int dispDivisions = INIT_DISP_DIV;
	/**
	 * Determines whether to show face grids.
	 * 
	 * @return <code>true</code> if to show face grids
	 */
	protected boolean displayGrids;
	/**
	 * Determines whether to show x-y ticks.
	 * 
	 * @return <code>true</code> if to show x-y ticks
	 */
	protected boolean displayXY;

	/**
	 * Determines whether to show z ticks.
	 * 
	 * @return <code>true</code> if to show z ticks
	 */
	protected boolean displayZ;

	/**
	 * Determines whether the delay regeneration checkbox is checked.
	 * 
	 * @return <code>true</code> if the checkbox is checked, <code>false</code> otherwise
	 */
	protected boolean expectDelay = false;

	/**
	 * Determines whether the first function is selected.
	 * 
	 * @return <code>true</code> if the first function is checked, <code>false</code> otherwise
	 */

	protected boolean hasFunction1 = true;

	/**
	 * Determines whether the first function is selected.
	 * 
	 * @return <code>true</code> if the first function is checked, <code>false</code> otherwise
	 */
	protected boolean hasFunction2 = true;

	javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

	/**
	 * Determines whether to show x-y mesh.
	 * 
	 * @return <code>true</code> if to show x-y mesh
	 */
	protected boolean mesh;

	protected PlotColor plotColor;

	protected boolean plotFunction1 = hasFunction1;

	protected boolean plotFunction2 = hasFunction2;

	protected PlotType plotType = PlotType.SURFACE;

	private Projector projector;

	protected PropertyChangeSupport property;

	/**
	 * Determines whether to scale axes and bounding box.
	 * 
	 * @return <code>true</code> if to scale bounding box
	 */

	protected boolean scaleBox;

	protected SurfaceVertex[][] surfaceVertex;
	protected float xMax;

	protected float xMin;
	protected float yMax;

	protected float yMin;
	protected float z1Max;// the max computed

	protected float z1Min;// the min computed
	protected float z2Max;// the max computed

	protected float z2Min;// the min computed
	protected float zMax;

	protected float zMin;

	/**
	 * Empty Surface Model
	 */
	public AbstractSurfaceModel() {
		super();
		property = new SwingPropertyChangeSupport(this);
		setColorModel(new ColorModelSet());
	}

	public void addChangeListener(ChangeListener ol) {
		listenerList.add(ChangeListener.class, ol);
	}

	public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
		property.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		property.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Allocates Memory
	 */

	private SurfaceVertex[][] allocateMemory(boolean f1, boolean f2, int total) {
		if ((vertex != null)  && (vertex[0].length >= total))
                    return vertex;
		try {
			vertex = new SurfaceVertex[2][total];
			if (!f1)
				vertex[0] = null;
			if (!f2)
				vertex[1] = null;
		} catch (OutOfMemoryError e) {
			setMessage("Not enough memory");
		} catch (Exception e) {
			setMessage("Error: " + e.toString());
		}
		return vertex;
	}

	/**
	 * Autoscale based on actual values
	 */
	public void autoScale() {
		// compute auto scale and repaint
		if (!autoScaleZ)
			return;
		if (plotFunction1 && plotFunction2) {
			setZMin(Math.min(z1Min, z2Min));
			setZMax(Math.max(z1Max, z2Max));
		} else {
			if (plotFunction1) {
				setZMin(z1Min);
				setZMax(z1Max);
			}
			if (plotFunction2) {
				setZMin(z2Min);
				setZMax(z2Max);
			}
		}
	}

	public void exportCSV(File file) throws IOException {

		if (file == null)
			return;
		java.io.FileWriter w = new java.io.FileWriter(file);
		float stepx, stepy, x, y, v;
		float xi, xx, yi, yx;
		float min, max;
		boolean f1, f2;
		int i, j, k, total;

		f1 = true;
		f2 = true; // until no method is defined to set functions ...
		// image conversion

		int[] pixels = null;
		int imgwidth = 0;
		int imgheight = 0;

		try {
			xi = getXMin();
			yi = getYMin();
			xx = getXMax();
			yx = getYMax();
			if ((xi >= xx) || (yi >= yx))
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			setMessage("Error in ranges");
			return;
		}

		calcDivisions = getCalcDivisions();
		// func1calc = f1; func2calc = f2;

		stepx = (xx - xi) / calcDivisions;
		stepy = (yx - yi) / calcDivisions;

		total = (calcDivisions + 1) * (calcDivisions + 1);
		if (surfaceVertex == null)
			return;

		max = Float.NaN;
		min = Float.NaN;

		// canvas.destroyImage();
		i = 0;
		j = 0;
		k = 0;
		x = xi;
		y = yi;

		float xfactor = 20 / (xx - xi);
		float yfactor = 20 / (yx - yi);

		w.write("X\\Y->Z;");
		while (j <= calcDivisions) {

			w.write(Float.toString(y));
			if (j != calcDivisions)
				w.write(';');
			j++;
			y += stepy;
			k++;
		}
		w.write("\n");
		// first line written
		i = 0;
		j = 0;
		k = 0;
		x = xi;
		y = yi;

		while (i <= calcDivisions) {
			w.write(Float.toString(x));
			w.write(';');
			while (j <= calcDivisions) {
				w.write(Float.toString(surfaceVertex[0][k].z));
				if (j != calcDivisions)
					w.write(';');
				j++;
				y += stepy;
				k++;
				// setMessage("Calculating : " + k*100/total + "% completed");
			}
			w.write('\n');
			// first line written
			j = 0;
			y = yi;
			i++;
			x += stepx;
		}
		w.flush();
		w.close();

	}

	private void fireAllFunction(boolean oldHas1, boolean oldHas2) {
		property.firePropertyChange("firstFunctionOnly", (!oldHas2) && oldHas1, (!plotFunction2) && plotFunction1);
		property.firePropertyChange("secondFunctionOnly", (!oldHas1) && oldHas2, (!plotFunction1) && plotFunction2);
		property.firePropertyChange("bothFunction", oldHas1 && oldHas2, plotFunction1 && plotFunction2);
		autoScale();

	}

	private void fireAllMode(PlotColor oldValue, PlotColor newValue) {
		for (PlotColor c : PlotColor.values())
			property.firePropertyChange(c.getPropertyName(), oldValue == c, newValue == c);
	}

	private void fireAllType(PlotType oldValue, PlotType newValue) {
		for (PlotType c : PlotType.values())
			property.firePropertyChange(c.getPropertyName(), oldValue == c, newValue == c);
	}

	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ChangeEvent e = null;
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new ChangeEvent(this);
				((ChangeListener) listeners[i + 1]).stateChanged(e);
			}
		}
	}

	public int getCalcDivisions() {
		return calcDivisions;
	}

	public SurfaceColor getColorModel() {
		return colorModel;
	}

	public int getContourLines() {
		return contourLines;
	}

	public int getDispDivisions() {
		if (dispDivisions > calcDivisions)
			dispDivisions = calcDivisions;
		while ((calcDivisions % dispDivisions) != 0)
			dispDivisions++;
		return dispDivisions;
	}

	public PlotColor getPlotColor() {
		return plotColor;
	}

	public PlotType getPlotType() {
		return plotType;
	}

	public Projector getProjector() {
		if (projector == null) {
			projector = new Projector();
			projector.setDistance(70);
			projector.set2DScaling(15);
			projector.setRotationAngle(125);
			projector.setElevationAngle(10);
		}
		return projector;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		if (property == null)
			property = new SwingPropertyChangeSupport(this);
		return property;
	}

	public SurfaceVertex[][] getSurfaceVertex() {
		return surfaceVertex;
	}

	public float getXMax() {
		return xMax;
	}

	public float getXMin() {
		return xMin;
	}

	public float getYMax() {
		return yMax;
	}

	public float getYMin() {
		return yMin;
	}

	public float getZMax() {
		return zMax;
	}

	public float getZMin() {
		return zMin;
	}

	public boolean isAutoScaleZ() {
		return autoScaleZ;
	}

	public boolean isBothFunction() {
		return plotFunction1 && plotFunction2;
	}

	public boolean isBoxed() {
		return boxed;
	}

	public boolean isContourType() {
		return plotType == PlotType.CONTOUR;
	}

	public boolean isDataAvailable() {
		return dataAvailable;
	}

	public boolean isDensityType() {
		return plotType == PlotType.DENSITY;
	}

	public boolean isDisplayGrids() {
		return displayGrids;
	}

	public boolean isDisplayXY() {
		return displayXY;
	}

	public boolean isDisplayZ() {
		return displayZ;
	}

	public boolean isDualShadeMode() {
		return plotColor == PlotColor.DUALSHADE;
	}

	public boolean isExpectDelay() {
		return expectDelay;
	}

	public boolean isFirstFunctionOnly() {
		return plotFunction1 && !plotFunction2;
	}

	public boolean isFogMode() {
		return plotColor == PlotColor.FOG;
	}

	public boolean isGrayScaleMode() {
		return plotColor == PlotColor.GRAYSCALE;
	}

	public boolean isHiddenMode() {
		return plotColor == PlotColor.OPAQUE;
	}

	public boolean isMesh() {
		return mesh;
	}

	public boolean isPlotFunction1() {
		return plotFunction1;
	}

	public boolean isPlotFunction2() {
		return plotFunction2;
	}

	public boolean isScaleBox() {
		return scaleBox;
	}

	public boolean isSecondFunctionOnly() {
		return (!plotFunction1) && plotFunction2;
	}

	public boolean isSpectrumMode() {
		return plotColor == PlotColor.SPECTRUM;
	}

	public boolean isSurfaceType() {
		return plotType == PlotType.SURFACE;
	}

	public boolean isWireframeType() {
		return plotType == PlotType.WIREFRAME;
	}

	/**
	 * factory to get a plotter, i.e. the best way to append data to this surface model
	 * 
	 * @param calcDivisions
	 * @return
	 */
	public Plotter newPlotter(int calcDivisions) {
		setCalcDivisions(calcDivisions);
		return new PlotterImpl();
	}

	public void removeChangeListener(ChangeListener ol) {
		listenerList.remove(ChangeListener.class, ol);
	}

	public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
		property.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
		property.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * Called when automatic rotation stops
	 */

	public void rotationStops() {

		// setting_panel.rotationStops();
	}

	public void setAutoScaleZ(boolean autoScaleZ) {
		getPropertyChangeSupport().firePropertyChange("this.autoScaleZ", this.autoScaleZ, this.autoScaleZ = autoScaleZ);
		autoScale();
	}

	public void setBothFunction(boolean val) {
		setPlotFunction12(val, val);
	}

	public void setBoxed(boolean boxed) {
		getPropertyChangeSupport().firePropertyChange("boxed", this.boxed, this.boxed = boxed);
	}

	protected void setColorModel(ColorModelSet colorModel) {
		getPropertyChangeSupport().firePropertyChange("colorModel", this.colorModel, this.colorModel = colorModel);
		if (colorModel != null) {
			colorModel.setPlotColor(plotColor); // this shouls be handled by the model itself, without any
			colorModel.setPlotType(plotType);
		}
	}

	public void setContourLines(int contourLines) {
		getPropertyChangeSupport().firePropertyChange("contourLines", this.contourLines, this.contourLines = contourLines);
	}

	public void setContourType(boolean val) {
		setPlotType(val ? PlotType.CONTOUR : PlotType.SURFACE);
	}

	public void setDataAvailable(boolean dataAvailable) {
		getPropertyChangeSupport().firePropertyChange("dataAvailable", this.dataAvailable, this.dataAvailable = dataAvailable);
	}

	public void setDensityType(boolean val) {
		setPlotType(val ? PlotType.DENSITY : PlotType.SURFACE);
	}

	public void setDispDivisions(int dispDivisions) {
		getPropertyChangeSupport().firePropertyChange("dispDivisions", this.dispDivisions, this.dispDivisions = dispDivisions);
	}

	public void setDualShadeMode(boolean val) {
		setPlotColor(val ? PlotColor.DUALSHADE : PlotColor.SPECTRUM);
	}

	public void setFirstFunctionOnly(boolean val) {
		setPlotFunction12(val, !val);
	}

	public void setFogMode(boolean val) {
		setPlotColor(val ? PlotColor.FOG : PlotColor.SPECTRUM);
	}

	public void setGrayScaleMode(boolean val) {
		setPlotColor(val ? PlotColor.GRAYSCALE : PlotColor.SPECTRUM);
	}

	public void setHiddenMode(boolean val) {
		setPlotColor(val ? PlotColor.OPAQUE : PlotColor.SPECTRUM);
	}

	/**
	 * Sets the text of status line
	 * 
	 * @param text
	 *            new text to be displayed
	 */

	public void setMessage(String text) {
		// @todo
		// System.out.println("Message"+text);
	}

	public void setPlotFunction1(boolean plotFunction1) {
		setPlotFunction12(plotFunction1, plotFunction2);
	}

	public void setPlotColor(PlotColor plotColor) {
		PlotColor old = this.plotColor;
		getPropertyChangeSupport().firePropertyChange("plotColor", this.plotColor, this.plotColor = plotColor);
		fireAllMode(old, this.plotColor);
		if (colorModel != null)
			colorModel.setPlotColor(plotColor); // this should be handled by the model itself, without any
	}

	public void setPlotFunction12(boolean p1, boolean p2) {
		boolean o1 = this.plotFunction1;
		boolean o2 = this.plotFunction2;

		this.plotFunction1 = hasFunction1 && p1;
		property.firePropertyChange("plotFunction1", o1, p1);

		this.plotFunction2 = hasFunction2 && p2;
		property.firePropertyChange("plotFunction1", o2, p2);
		fireAllFunction(o1, o2);
	}

	public void setPlotFunction2(boolean v) {
		setPlotFunction12(plotFunction1, plotFunction2);
	}

	public void setPlotType(PlotType plotType) {
		PlotType o = this.plotType;
		this.plotType = plotType;
		if (colorModel != null)
			colorModel.setPlotType(plotType); // this should be handled by the model itself, without any
		property.firePropertyChange("plotType", o, this.plotType);
		fireAllType(o, this.plotType);
	}

	public void setSecondFunctionOnly(boolean val) {
		setPlotFunction12(!val, val);
	}

	public void setSpectrumMode(boolean val) {
		setPlotColor(val ? PlotColor.SPECTRUM : PlotColor.GRAYSCALE);
	}

	public void setSurfaceType(boolean val) {
		setPlotType(val ? PlotType.SURFACE : PlotType.WIREFRAME);
	}

	public void setSurfaceVertex(SurfaceVertex[][] surfaceVertex) {
		getPropertyChangeSupport().firePropertyChange("surfaceVertex", this.surfaceVertex, this.surfaceVertex = surfaceVertex);
	}

	public void setWireframeType(boolean val) {
		if (val)
			setPlotType(PlotType.WIREFRAME);
		else
			setPlotType(PlotType.SURFACE);
	}

	public void setXMax(float xMax) {
		getPropertyChangeSupport().firePropertyChange("xMax", this.xMax, this.xMax = xMax);
	}

	public void setXMin(float xMin) {
		getPropertyChangeSupport().firePropertyChange("xMin", this.xMin, this.xMin = xMin);
	}

	public void setYMax(float yMax) {
		getPropertyChangeSupport().firePropertyChange("yMax", this.yMax, this.yMax = yMax);
	}

	public void setYMin(float yMin) {
		getPropertyChangeSupport().firePropertyChange("yMin", this.yMin, this.yMin = yMin);
	}

	public void setZMax(float zMax) {
		if (zMax <= zMin)
			return;
		getPropertyChangeSupport().firePropertyChange("zMax", this.zMax, this.zMax = zMax);
	}

	public void setZMin(float zMin) {
		if (zMin >= zMax)
			return;
		getPropertyChangeSupport().firePropertyChange("zMin", this.zMin, this.zMin = zMin);
	}

	public void setDisplayGrids(boolean displayGrids) {
		getPropertyChangeSupport().firePropertyChange("displayGrids", this.displayGrids, this.displayGrids = displayGrids);
	}

	public void setDisplayXY(boolean displayXY) {
		getPropertyChangeSupport().firePropertyChange("displayXY", this.displayXY, this.displayXY = displayXY);
	}

	public void setDisplayZ(boolean displayZ) {
		getPropertyChangeSupport().firePropertyChange("displayZ", this.displayZ, this.displayZ = displayZ);
	}

	public void setExpectDelay(boolean expectDelay) {
		getPropertyChangeSupport().firePropertyChange("expectDelay", this.expectDelay, this.expectDelay = expectDelay);
	}

	public void setMesh(boolean mesh) {
		getPropertyChangeSupport().firePropertyChange("mesh", this.mesh, this.mesh = mesh);
	}

	public void setScaleBox(boolean scaleBox) {
		getPropertyChangeSupport().firePropertyChange("scaleBox", this.scaleBox, this.scaleBox = scaleBox);
	}

	public void toggleAutoScaleZ() {
		setAutoScaleZ(!isAutoScaleZ());
	}

	public void toggleBoxed() {
		setBoxed(!isBoxed());
	}
	
	

	public void setCalcDivisions(int calcDivisions) {
		getPropertyChangeSupport().firePropertyChange("calcDivisions", this.calcDivisions, this.calcDivisions = calcDivisions);
	}

	/**
	 * Processes menu events
	 * 
	 * @param item
	 *            the selected menu item
	 */

	public void toggleDisplayGrids() {
		setDisplayGrids(!isDisplayGrids());
	}

	/**
	 * Sets file name
	 */

	public void toggleDisplayXY() {
		setDisplayXY(!isDisplayXY());
	}

	public void toggleDisplayZ() {
		setDisplayZ(!isDisplayZ());
	}

	public void toggleExpectDelay() {
		setExpectDelay(!isExpectDelay());
	}

	public void toggleMesh() {
		setMesh(!isMesh());
	}

	public void togglePlotFunction1() {
		setPlotFunction1(!isPlotFunction1());

	}

	public void togglePlotFunction2() {
		setPlotFunction2(!isPlotFunction2());

	}

	public void toggleScaleBox() {
		setScaleBox(!isScaleBox());
	}

}// end of class
