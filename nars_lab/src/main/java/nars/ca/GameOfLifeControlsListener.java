/*
 * Created on 8-sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package nars.ca;

import java.util.EventListener;

/**
 * Listener interface for GameOfLifeControls.
 * The idea behind this interface is that the controls can be replaced by something else for
 * e.g. smart phones and PDA's.
 * @see GameOfLifeControls
 * @author Edwin Martin
 */
public interface GameOfLifeControlsListener extends EventListener {
	/**
	 * The Start/Stop button is clicked.
	 * @param e event object
	 */
	public void startStopButtonClicked(GameOfLifeControlsEvent e);

	/**
	 * The Next button is clicked.
	 * @param e event object
	 */
	public void nextButtonClicked(GameOfLifeControlsEvent e);

	/**
	 * A new speed is selected.
	 * @param e event object
	 */
	public void speedChanged(GameOfLifeControlsEvent e);

	/**
	 * A new cell size is selected.
	 * @param e event object
	 */
	public void zoomChanged(GameOfLifeControlsEvent e);

	/**
	 * A new shape is selected.
	 * @param e event object
	 */
	public void shapeSelected(GameOfLifeControlsEvent e);
}
