/*----------------------------------------------------------------------------------------*
 * SurfaceModel.java                                                                      *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                   bug fixed       21 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
 *                                                                                        *
 * Copyright (c) Yanto Suryono <yanto@fedu.uec.ac.jp>                                     *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU Lesser General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU Lesser General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *            
Modified : Eric : remove every graphical stuff to get rid of Frame
 *----------------------------------------------------------------------------------------*/
package automenta.vivisect.surfaceplotter.surface;


import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;

/**
 * The model used to display any surface in JSurface
 */

public interface SurfaceModel
{
	
	enum PlotType{
		SURFACE("surfaceType"), 
		WIREFRAME("wireframeType"), 
		DENSITY("densityType"), 
		CONTOUR("contourType");
		
		final String att;
		PlotType(String att){this.att = att;}
		public String getPropertyName() {return att;}
	
		}
    //TODO replace with enum
	//plot type constant 
	
	enum PlotColor{
		OPAQUE("hiddenMode"), 
		SPECTRUM("spectrumMode"), 
		DUALSHADE("dualShadeMode"), 
		GRAYSCALE("grayScaleMode"), 
		FOG("fogMode");
		
		final String att;
		PlotColor(String att){this.att = att;}
		public String getPropertyName() {return att;}
	
		
			
	}
    //TODO replace with enums
	// plot color constant
	
	//events
	void addPropertyChangeListener(PropertyChangeListener listener);
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
	void removePropertyChangeListener(PropertyChangeListener listener);
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
	void addChangeListener(ChangeListener listener);
	void removeChangeListener(ChangeListener listener);
	
	
	
	SurfaceVertex[][] getSurfaceVertex(); //maybe provide a less brutal parameter passing, but
	//I have to ber careful, there is performance at stake
	
	Projector getProjector(); //project is kind of "point of view"
	
	
	boolean isAutoScaleZ();
	PlotType getPlotType();
	PlotColor getPlotColor();
	int getCalcDivisions();
	int getContourLines();
	int getDispDivisions();
	float getXMin();
	float getYMin();
	float getZMin();
	float getXMax();
	float getYMax();
	float getZMax();
	SurfaceColor getColorModel(); // not the right place, but JSurface does not work with any colorset, should be removed lately

	/**
	 * Determines whether the delay regeneration checkbox is checked.
	 *
	 * @return <code>true</code> if the checkbox is checked, 
	 *         <code>false</code> otherwise
	 */
	boolean isExpectDelay();
	
	/**
	 * Determines whether to show bounding box.
	 *
	 * @return <code>true</code> if to show bounding box
	 */
	boolean isBoxed();
	
	/**
	 * Determines whether to show x-y mesh.
	 *
	 * @return <code>true</code> if to show x-y mesh
	 */
	boolean isMesh();
	/**
	 * Determines whether to scale axes and bounding box.
	 *
	 * @return <code>true</code> if to scale bounding box
	 */

	boolean isScaleBox();
	
	/**
	 * Determines whether to show x-y ticks.
	 *
	 * @return <code>true</code> if to show x-y ticks
	 */
	boolean isDisplayXY();
	/**
	 * Determines whether to show z ticks.
	 *
	 * @return <code>true</code> if to show z ticks
	 */
	boolean isDisplayZ();
	/**
	 * Determines whether to show face grids.
	 *
	 * @return <code>true</code> if to show face grids
	 */
	boolean isDisplayGrids();
	/**
	 * Determines whether the first function is selected.
	 *
	 * @return <code>true</code> if the first function is checked, 
	 *         <code>false</code> otherwise
	 */
	boolean isPlotFunction1();
	
	/**
	 * Determines whether the first function is selected.
	 *
	 * @return <code>true</code> if the first function is checked, 
	 *         <code>false</code> otherwise
	 */

	boolean isPlotFunction2();
	
	/**
	 * Sets data availability flag
	 */
	boolean isDataAvailable();
		
	
}

