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

 


/**
 * The model used to display any surface in JSurface
 */

public interface SurfaceModel
{
	
	public enum PlotType{
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
	
	public enum PlotColor{
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
	public void addPropertyChangeListener(java.beans.PropertyChangeListener listener);
	public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener);
	public void removePropertyChangeListener(java.beans.PropertyChangeListener listener);
	public void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener);
	public void addChangeListener(javax.swing.event.ChangeListener listener);
	public void removeChangeListener(javax.swing.event.ChangeListener listener);
	
	
	
	public SurfaceVertex[][] getSurfaceVertex(); //maybe provide a less brutal parameter passing, but 
	//I have to ber careful, there is performance at stake
	
	public Projector getProjector(); //project is kind of "point of view"
	
	
	public boolean isAutoScaleZ();
	public PlotType getPlotType();
	public PlotColor getPlotColor();
	public int getCalcDivisions();
	public int getContourLines();
	public int getDispDivisions(); 
	public float getXMin();
	public float getYMin();
	public float getZMin();
	public float getXMax();
	public float getYMax();
	public float getZMax();
	public SurfaceColor getColorModel(); // not the right place, but JSurface does not work with any colorset, should be removed lately

	/**
	 * Determines whether the delay regeneration checkbox is checked.
	 *
	 * @return <code>true</code> if the checkbox is checked, 
	 *         <code>false</code> otherwise
	 */
	public boolean isExpectDelay();
	
	/**
	 * Determines whether to show bounding box.
	 *
	 * @return <code>true</code> if to show bounding box
	 */
	public boolean isBoxed();
	
	/**
	 * Determines whether to show x-y mesh.
	 *
	 * @return <code>true</code> if to show x-y mesh
	 */
	public boolean isMesh();
	/**
	 * Determines whether to scale axes and bounding box.
	 *
	 * @return <code>true</code> if to scale bounding box
	 */
	
	public boolean isScaleBox();
	
	/**
	 * Determines whether to show x-y ticks.
	 *
	 * @return <code>true</code> if to show x-y ticks
	 */
	public boolean isDisplayXY();
	/**
	 * Determines whether to show z ticks.
	 *
	 * @return <code>true</code> if to show z ticks
	 */
	public boolean isDisplayZ();
	/**
	 * Determines whether to show face grids.
	 *
	 * @return <code>true</code> if to show face grids
	 */
	public boolean isDisplayGrids();
	/**
	 * Determines whether the first function is selected.
	 *
	 * @return <code>true</code> if the first function is checked, 
	 *         <code>false</code> otherwise
	 */
	public boolean isPlotFunction1();
	
	/**
	 * Determines whether the first function is selected.
	 *
	 * @return <code>true</code> if the first function is checked, 
	 *         <code>false</code> otherwise
	 */
	
	public boolean isPlotFunction2();
	
	/**
	 * Sets data availability flag
	 */
	public boolean isDataAvailable();
		
	
}

