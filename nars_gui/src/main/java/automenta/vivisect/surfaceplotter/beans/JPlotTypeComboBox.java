/*
       ____  _____  ___  ____    __      ____  _____  _    _  ____  ____ 
      (  _ \(  _  )/ __)( ___)  /__\    (  _ \(  _  )( \/\/ )( ___)(  _ \
       )(_) ))(_)(( (__  )__)  /(__)\    )___/ )(_)(  )    (  )__)  )   /
      (____/(_____)\___)(____)(__)(__)  (__)  (_____)(__/\__)(____)(_)\_)

* Created 20 mai 2011 by : eric@doceapower.com
* Copyright Docea Power 2011
* Any reproduction or distribution prohibited without express written permission from Docea Power
***************************************************************************
*/
package automenta.vivisect.surfaceplotter.beans;

import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotType;

/**
 * @author eric
 *
 */
public class JPlotTypeComboBox extends JEnumComboBox<PlotType> {
	
	
	
	String[] labels;
	
	/**
	 * 
	 */
	public JPlotTypeComboBox() {
		super(PlotType.values(), "plotType");
		labels = new String[PlotType.values().length];
		for (int i = 0; i < PlotType.values().length; i++)
			labels[i] = PlotType.values()[i].getPropertyName() ;
	}

	
	
	
	@Override protected String getEnumLabel(PlotType value) {
		return labels[value.ordinal()];
	}
	
	protected String setEnumLabel(PlotType value, String newValue) {
		labels[value.ordinal()] = newValue;
		return newValue;
	}

	
	public String getWireframeLabel() {
		return getEnumLabel(PlotType.WIREFRAME);
	}

	public void setWireframeLabel(String wireframeLabel) {
		firePropertyChange("wireframeLabel", getWireframeLabel(), setEnumLabel(PlotType.WIREFRAME, wireframeLabel));
	}
	
	public String getSurfaceLabel() {
		return getEnumLabel(PlotType.SURFACE);
	}

	public void setSurfaceLabel(String surfaceLabel) {
		firePropertyChange("surfaceLabel", getSurfaceLabel(), setEnumLabel(PlotType.SURFACE, surfaceLabel));
	}

	public String getDensityLabel() {
		return getEnumLabel(PlotType.DENSITY);
	}

	public void setDensityLabel(String densityLabel) {
		firePropertyChange("densityLabel", getDensityLabel(), setEnumLabel(PlotType.DENSITY, densityLabel));
	}
	
	
	public String getContourLabel() {
		return getEnumLabel(PlotType.CONTOUR);
	}

	public void setContourLabel(String contourLabel) {
		firePropertyChange("contourLabel", getContourLabel(), setEnumLabel(PlotType.CONTOUR, contourLabel));
	}
	
	

}
