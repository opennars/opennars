package automenta.vivisect.surfaceplotter.surface;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Abstract implementation that handles everything but the surfaceVertex array
 * 
 * @author eric
 *
 */
public abstract class AbstractSurfaceModel implements SurfaceModel{

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
	 * internally used to ceil values
	 * 
	 * @param d
	 * @param digits
	 * @return
	 */
	public static synchronized double ceil(double d, int digits) {
		if (d == 0)
			return 0;
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
			return 0;
		// computes order of magnitude
		long og = (long) Math.ceil((Math.log(Math.abs(d)) / Math.log(10)));

		double factor = Math.pow(10, digits - og);
		// the matissa
		double res = Math.floor((d * factor)) / factor;
		// res contains the closed power of ten
		return res;
	}

	private static final int INIT_CALC_DIV = 20;
	private static final int INIT_DISP_DIV = 20;

	
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

	protected EventListenerList listenerList = new EventListenerList();

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

	protected float xMax = 1.0f;

	protected float xMin;
	protected float yMax = 1.0f;

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
	@SuppressWarnings("ConstructorNotProtectedInAbstractClass")
	public AbstractSurfaceModel() {
		property = new SwingPropertyChangeSupport(this);
		setColorModel(new ColorModelSet());
		
		setCalcDivisions(50);
		setDispDivisions(50);
		setContourLines(10);

		setXMin(-3);
		setXMax(3);
		setYMin(-3);
		setYMax(3);

		setBoxed(false);
		setDisplayXY(false);
		setExpectDelay(false);
		setAutoScaleZ(true);
		setDisplayZ(false);
		setMesh(true);
		setPlotType(PlotType.SURFACE);
		setFirstFunctionOnly(true);
		setPlotColor(PlotColor.SPECTRUM);
	}

	@Override
	public void addChangeListener(ChangeListener ol) {
		listenerList.add(ChangeListener.class, ol);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		property.addPropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		property.addPropertyChangeListener(propertyName, listener);
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

	@SuppressWarnings("HardcodedFileSeparator")
	public void exportCSV(File file) throws IOException {
		 SurfaceVertex[][] surfaceVertex = getSurfaceVertex();
		if (file == null)
			return;
		FileWriter w = new FileWriter(file);
		float stepx, stepy, x, y;
		float xi, xx, yi, yx;
		int i, j, k;

		
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

		if (surfaceVertex == null)
			return;
		i = 0;
		j = 0;
		k = 0;
		x = xi;
		y = yi;

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

	public void fireStateChanged() {
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

	@Override
	public int getCalcDivisions() {
		return calcDivisions;
	}

	@Override
	public SurfaceColor getColorModel() {
		return colorModel;
	}

	@Override
	public int getContourLines() {
		return contourLines;
	}

	@Override
	public int getDispDivisions() {
		if (dispDivisions > calcDivisions)
			dispDivisions = calcDivisions;
		while ((calcDivisions % dispDivisions) != 0)
			dispDivisions++;
		return dispDivisions;
	}

	@Override
	public PlotColor getPlotColor() {
		return plotColor;
	}

	@Override
	public PlotType getPlotType() {
		return plotType;
	}

	@Override
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

	@Override
	public float getXMax() {
		return xMax;
	}

	@Override
	public float getXMin() {
		return xMin;
	}

	@Override
	public float getYMax() {
		return yMax;
	}

	@Override
	public float getYMin() {
		return yMin;
	}

	@Override
	public float getZMax() {
		return zMax;
	}

	@Override
	public float getZMin() {
		return zMin;
	}

	@Override
	public boolean isAutoScaleZ() {
		return autoScaleZ;
	}

	public boolean isBothFunction() {
		return plotFunction1 && plotFunction2;
	}

	@Override
	public boolean isBoxed() {
		return boxed;
	}

	public boolean isContourType() {
		return plotType == PlotType.CONTOUR;
	}

	@Override
	public boolean isDataAvailable() {
		return dataAvailable;
	}

	public boolean isDensityType() {
		return plotType == PlotType.DENSITY;
	}

	@Override
	public boolean isDisplayGrids() {
		return displayGrids;
	}

	@Override
	public boolean isDisplayXY() {
		return displayXY;
	}

	@Override
	public boolean isDisplayZ() {
		return displayZ;
	}

	public boolean isDualShadeMode() {
		return plotColor == PlotColor.DUALSHADE;
	}

	@Override
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

	@Override
	public boolean isMesh() {
		return mesh;
	}

	@Override
	public boolean isPlotFunction1() {
		return plotFunction1;
	}

	@Override
	public boolean isPlotFunction2() {
		return plotFunction2;
	}

	@Override
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

	@Override
	public void removeChangeListener(ChangeListener ol) {
		listenerList.remove(ChangeListener.class, ol);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		property.removePropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
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
		boolean o1 = plotFunction1;
		boolean o2 = plotFunction2;

		plotFunction1 = hasFunction1 && p1;
		property.firePropertyChange("plotFunction1", o1, p1);

		plotFunction2 = hasFunction2 && p2;
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

}
