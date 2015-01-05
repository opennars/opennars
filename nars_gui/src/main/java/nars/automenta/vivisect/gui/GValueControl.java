/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2012 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package automenta.vivisect.gui;

import processing.core.PApplet;

/**
 * Base class for all slider and knob type controls.
 * 
 * This class enables the creation of tick marks and constraining values to 
 * the tick mark values. <br>
 * 
 * It also controls how the values are to be displayed INTEGER, DECIMAL or EXPONENT
 * 
 * @author Peter Lager
 *
 */
public abstract class GValueControl extends GControl {

	protected StyledString ssStartLimit, ssEndLimit, ssValue;

	protected float startLimit = 0, endLimit = 1;
	protected boolean showLimits = false;
	
	protected int valueType = DECIMAL;
	protected int precision = 2;
	protected String unit = "";
	protected boolean showValue = false;
	

	protected float parametricPos = 0.5f, parametricTarget = 0.5f;
	protected float easing  = 1.0f; // must be >= 1.0
	

	protected int nbrTicks = 2;
	protected boolean stickToTicks = false;
	protected boolean showTicks = false;
	
	protected boolean limitsInvalid = true;
	
	// Offset to between mouse and thumb centre
	protected float offset;
	
	public GValueControl(PApplet theApplet, float p0, float p1, float p2, float p3) {
		super(theApplet, p0, p1, p2, p3);
	}
	
	public void pre(){
		if(Math.abs(parametricTarget - parametricPos) > epsilon){
			parametricPos += (parametricTarget - parametricPos) / easing;
			updateDueToValueChanging();
			bufferInvalid = true;
			if(Math.abs(parametricTarget - parametricPos) > epsilon){
				fireEvent(this, GEvent.VALUE_CHANGING);
			}
			else {
				parametricPos = parametricTarget;
				fireEvent(this, GEvent.VALUE_STEADY);
			}
		}
	}
	
	/**
	 * This should be overridden in child classes so they can perform any class specific
	 * actions when the value changes.
	 * Override this in GSlider to change the hotshot poaition.
	 */
	protected void updateDueToValueChanging(){
	}

	/**
	 * Used to format the number into a string for display.
	 * @param number
	 * @return a string representing the number
	 */
	protected String getNumericDisplayString(float number){
		String s = "";
		switch(valueType){
		case INTEGER:
			s = String.format("%d %s", Math.round(number), unit);
			break;
		case DECIMAL:
			s = String.format("%." + precision + "f %s", number, unit);
			break;
		case EXPONENT:
			s = String.format("%." + precision + "e %s", number, unit);
			break;
		}
		return s.trim();
	}
	
	
	/**
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to INTEGER
	 * 
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimits(int start, int end){
		startLimit = start;
		endLimit = end;
		setEpsilon();
		valueType = INTEGER;
		limitsInvalid = true;
		bufferInvalid = true;
	}
	
	/**
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to INTEGER.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimits(int initValue, int start, int end){
		startLimit = start;
		endLimit = end;
		valueType = INTEGER;
		setEpsilon();
		limitsInvalid = true;
		bufferInvalid = true;
		setValue(initValue);
		parametricPos = parametricTarget;
		updateDueToValueChanging();
	}
	
	/**
	 * Sets the range of values to be returned. This method will
	 * assume that you want to set the valueType to DECIMAL
	 * 
	 * @param start
	 * @param end
	 */
	public void setLimits(float start, float end){
		startLimit = start;
		endLimit = end;
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		limitsInvalid = true;
		bufferInvalid = true;
	}
	
	/**
	 * Sets the initial value and the range of values to be returned. This 
	 * method will assume that you want to set the valueType to DECIMAL.
	 * 
	 * @param initValue the initial value
	 * @param start the start value of the range
	 * @param end the end value of the range
	 */
	public void setLimits(float initValue, float start, float end){
		startLimit = start;
		endLimit = end;
		initValue = PApplet.constrain(initValue, start, end);
		if(valueType == INTEGER){
			valueType = DECIMAL;
			setPrecision(1);
		}
		setEpsilon();
		limitsInvalid = true;
		bufferInvalid = true;
		setValue(initValue);
		parametricPos = parametricTarget;
		updateDueToValueChanging();
	}
	
	/**
	 * Set the value for the slider. <br>
	 * The user must ensure that the value is valid for the slider range.
	 * @param v
	 */
	public void setValue(float v){
		if(valueType == INTEGER)
			v = Math.round(v);
		float t = (v - startLimit) / (endLimit - startLimit);
		t = PApplet.constrain(t, 0.0f, 1.0f);
		if(stickToTicks)
			t = findNearestTickValueTo(t);
		parametricTarget = t;
	}
	
	/**
	 * For DECIMAL values this sets the number of decimal places to 
	 * be displayed.
	 * @param nd must be >= 1 otherwise will use 1
	 */
	public void setPrecision(int nd){
		nd = PApplet.constrain(nd, 1, 5);
		if(nd < 1)
			nd = 1;
		if(nd != precision){
			precision = nd;
			setEpsilon();
			limitsInvalid = true;
			bufferInvalid = true;
		}
	}
	
	/**
	 * Make epsilon to match the value of 1 pixel or the precision which ever is the smaller
	 */
	protected void setEpsilon(){
		epsilon = (float) Math.min(0.001, Math.pow(10, -precision));
	}
	
	/**
	 * The units to be displayed with the current and limit values e.g.
	 * kg, m, ($), fps etc. <br>
	 * Do not use long labels such as 'miles per hour' as these take a
	 * lot of space and can look messy.
	 *  
	 * @param units for example  kg, m, ($), fps
	 */
	public void setUnits(String units){
		if(units == null)
			units = "";
		if(!unit.equals(units)){
			unit = units;
			limitsInvalid = true;
			bufferInvalid = true;			
		}
	}
	
	/**
	 * Set the numberFormat, precision and units in one go. <br>
	 * Valid number formats are INTEGER, DECIMAL, EXPONENT <br>
	 * Precision must be >= 1 and is ignored for INTEGER.
	 * 
	 * @param numberFormat INTEGER, DECIMAL or EXPONENT
	 * @param precision must be >= 1
	 * @param unit for example  kg, m, ($), fps
	 */
	public void setNumberFormat(int numberFormat, int precision, String unit){
		this.unit = (unit == null) ? "" : unit;
		setNumberFormat(numberFormat, precision);
	}

	/**
	 * Set the numberFormat and precision in one go. <br>
	 * Valid number formats are INTEGER, DECIMAL, EXPONENT <br>
	 * Precision must be >= 1 and is ignored for INTEGER.
	 * 
	 * @param numberFormat G4P.INTEGER, G4P.DECIMAL orG4P. EXPONENT
	 * @param precision must be >= 1
	 */
	public void setNumberFormat(int numberFormat, int precision){
		switch(numberFormat){
		case INTEGER:
		case DECIMAL:
		case EXPONENT:
			this.valueType = numberFormat;
			break;
		default:
			valueType = DECIMAL;
		}
		setPrecision(precision);
		bufferInvalid = true;
	}
	
	/**
	 * Set the numberFormat and precision in one go. <br>
	 * Valid number formats are INTEGER, DECIMAL, EXPONENT <br>
	 * Precision must be >= 1 and is ignored for INTEGER.
	 * 
	 * @param numberFormat G4P.INTEGER, G4P.DECIMAL or G4P.EXPONENT
	 */
	public void setNumberFormat(int numberFormat){
		switch(numberFormat){
		case INTEGER:
		case DECIMAL:
		case EXPONENT:
			this.valueType = numberFormat;
			break;
		default:
			valueType = DECIMAL;
		}
		bufferInvalid = true;
	}
	
	/**
	 * Get the current value as a float
	 */
	public float getValueF(){
		return startLimit + (endLimit - startLimit) * parametricPos;
	}

	/**
	 * Get the current value as an integer. <br>
	 * DECIMAL and EXPONENT value types will be rounded to the nearest integer.
	 */
	public int getValueI(){
		return Math.round(startLimit + (endLimit - startLimit) * parametricPos);
	}
	
	/**
	 * If we are using labels then this will get the label text
	 * associated with the current value. <br>
	 * If labels have not been set then return null
	 */
	public String getValueS(){
		return getNumericDisplayString(getValueF());
	}

	/**
	 * Get the current value used for easing.
	 * @return the easing
	 */
	public float getEasing() {
		return easing;
	}

	/**
	 * Set the amount of easing to be used when a value is changing. The default value
	 * is 1 (no easing) values > 1 will cause the value to rush from its starting value
	 * and decelerate towards its final values. In other words it smoothes the movement
	 * of the slider thumb or knob rotation.
	 * 
	 * @param easeBy the easing to set
	 */
	public void setEasing(float easeBy) {
		easing = (easeBy < 1) ? 1 : easeBy;
	}

	/**
	 * Get the number of tick marks.
	 * @return the nbrTicks
	 */
	public int getNbrTicks() {
		return nbrTicks;
	}

	/**
	 * The number of ticks must be >= 2 since 2 are required for the slider limits.
	 * 
	 * @param noOfTicks the nbrTicks to set
	 */
	public void setNbrTicks(int noOfTicks) {
		if(noOfTicks < 2)
			noOfTicks = 2;
		if(nbrTicks != noOfTicks){
			nbrTicks = noOfTicks;
			bufferInvalid = true;
			if(stickToTicks)
				parametricTarget = findNearestTickValueTo(parametricPos);
		}
	}

	/**
	 * Is the value constrained to the tick marks?
	 * @return the stickToTicks true if values constrained else false
	 */
	public boolean isStickToTicks() {
		return stickToTicks;
	}

	/**
	 * Specify whether the values are to be constrained to the tick marks or not.
	 * It will automatically display tick marks if set true.
	 * @param stickToTicks true if you want to constrain the values else false
	 */
	public void setStickToTicks(boolean stickToTicks) {
		this.stickToTicks = stickToTicks;
		if(stickToTicks){
			setShowTicks(true);
			parametricTarget = findNearestTickValueTo(parametricPos);
			bufferInvalid = true;
		}
	}

	/**
	 * These are normalised values i.e. between 0.0 and 1.0 inclusive
	 * @param p
	 * @return the parametric value of the nearest tick
	 */
	protected float findNearestTickValueTo(float p){
		float tickSpace = 1.0f / (nbrTicks - 1);
		int tn =  (int) (p / tickSpace + 0.5f);
		return tickSpace * tn;
	}
	
	/**
	 * Are the tick marks visible?
	 * @return the showTicks
	 */
	public boolean isShowTicks() {
		return showTicks;
	}

	/**
	 * Set whether the tick marks are to be displayed or not.
	 * @param showTicks the showTicks to set
	 */
	public void setShowTicks(boolean showTicks) {
		if(this.showTicks != showTicks){
			this.showTicks = showTicks;
			bufferInvalid = true;			
		}
	}
	
	/**
	 * Are the limit values visible?
	 * @return the showLimits
	 */
	public boolean isShowLimits() {
		return showLimits;
	}

	/**
	 * Set whether the limits are to be displayed or not.
	 * @param showLimits the showLimits to set
	 */
	public void setShowLimits(boolean showLimits) {
		this.showLimits = showLimits;
		bufferInvalid = true;
	}

	/**
	 * Is the current value to be displayed?
	 * @return the showValue
	 */
	public boolean isShowValue() {
		return showValue;
	}

	/**
	 * Set whether the current value is to be displayed or not.
	 * @param showValue the showValue to set
	 */
	public void setShowValue(boolean showValue) {
		this.showValue = showValue;
		bufferInvalid = true;
	}

	/**
	 * Convenience method to set what is to be drawn to the screen.
	 * @param opaque show background
	 * @param ticks show tick marks
	 * @param value show current value
	 * @param limits show min and max values (limits)
	 */
	public void setShowDecor(boolean opaque, boolean ticks, boolean value, boolean limits){
		setShowValue(value);
		bufferInvalid = true;
		setOpaque(opaque);
		setShowTicks(ticks);
		setShowLimits(limits);
	}
	
	/**
	 * @return the startLimit
	 */
	public float getStartLimit() {
		return startLimit;
	}

	/**
	 * @return the endLimit
	 */
	public float getEndLimit() {
		return endLimit;
	}

	/**
	 * 
	 * @return the valueType
	 */
	public int getValueType() {
		return valueType;
	}

	/**
	 * Precision used with floating point numbers
	 * @return the precision
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

}
