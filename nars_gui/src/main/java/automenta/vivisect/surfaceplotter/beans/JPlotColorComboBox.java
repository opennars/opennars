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

import automenta.vivisect.surfaceplotter.surface.SurfaceModel.PlotColor;

/**
 * @author eric
 *
 */
public class JPlotColorComboBox extends JEnumComboBox<PlotColor> {

//	String hiddenModeLabel     = PlotColor.OPAQUE.getPropertyName();
//	String spectrumModeLabel   = PlotColor.SPECTRUM.getPropertyName();
//	String dualShadeModeLabel  = PlotColor.DUALSHADE.getPropertyName();
//	String grayScaleModeLabel  = PlotColor.GRAYSCALE.getPropertyName();
//	String fogModeLabel        = PlotColor.FOG.getPropertyName();
//	
	
	
	String[] labels;
	
	/**
	 * 
	 */
	public JPlotColorComboBox() {
		super(PlotColor.values(), "plotColor");
		labels = new String[PlotColor.values().length];
		for (int i = 0; i < PlotColor.values().length; i++)
			labels[i] = PlotColor.values()[i].getPropertyName() ;
	}

	
	
	
	@Override protected String getEnumLabel(PlotColor value) {
		return labels[value.ordinal()];
	}
	
	protected String setEnumLabel(PlotColor value, String newValue) {
		labels[value.ordinal()] = newValue;
		return newValue;
	}




	public String getHiddenModeLabel() {
		return getEnumLabel(PlotColor.OPAQUE);
	}

	public void setHiddenModeLabel(String hiddenModeLabel) {
		firePropertyChange("hiddenModeLabel", getHiddenModeLabel(), setEnumLabel(PlotColor.OPAQUE, hiddenModeLabel));
	}
	

	public String getSpectrumModeLabel() {
		return getEnumLabel(PlotColor.SPECTRUM);
		
	}

	public void setSpectrumModeLabel(String spectrumModeLabel) {
		firePropertyChange("spectrumModeLabel", getSpectrumModeLabel(), setEnumLabel(PlotColor.SPECTRUM, spectrumModeLabel));
	}

	public String getDualShadeModeLabel() {
		return getEnumLabel(PlotColor.DUALSHADE);
		
	}

	public void setDualShadeModeLabel(String dualShadeModeLabel) {
		firePropertyChange("dualShadeModeLabel", getDualShadeModeLabel(), setEnumLabel(PlotColor.DUALSHADE,dualShadeModeLabel));
	}

	public String getGrayScaleModeLabel() {
		return getEnumLabel(PlotColor.GRAYSCALE);
	}

	public void setGrayScaleModeLabel(String grayScaleModeLabel) {
		firePropertyChange("grayScaleModeLabel", getGrayScaleModeLabel(), setEnumLabel(PlotColor.GRAYSCALE,grayScaleModeLabel));
	}

	public String getFogModeLabel() {
		return getEnumLabel(PlotColor.FOG);
	}

	public void setFogModeLabel(String fogModeLabel) {
		firePropertyChange("fogModeLabel", getFogModeLabel(), setEnumLabel(PlotColor.FOG,fogModeLabel));
	}
	
}
