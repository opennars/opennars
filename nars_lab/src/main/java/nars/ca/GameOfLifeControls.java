/**
 * The control bar at the bottom.
 * Is put in a seperate object, so it can be replaced by another UI, e.g. on a J2ME phone. 
 * Copyright 1996-2004 Edwin Martin <edwin@bitstorm.nl>
 * @author Edwin Martin
 */
 
package nars.ca;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Vector;


/**
 * GUI-controls of the Game of Life.
 * It contains controls like Shape, zoom and speed selector, next and start/stop-button.
 * It is a seperate class, so it can be replaced by another implementation for e.g. mobile phones or PDA's.
 * Communicates via the GameOfLifeControlsListener.
 * @author Edwin Martin
 *
 */
public class GameOfLifeControls extends Panel {
	private Label genLabel;
	private final String genLabelText = "Generations: ";
	private final String nextLabelText = "Next";
	private final String startLabelText = "Start";
	private final String stopLabelText = "Stop";
	public static final String SLOW = "Slow";
	public static final String FAST = "Fast";
	public static final String HYPER = "Hyper";
	public static final String BIG = "Big";
	public static final String MEDIUM = "Medium";
	public static final String SMALL = "Small";
	public static final int SIZE_BIG = 11;
	public static final int SIZE_MEDIUM = 7;
	public static final int SIZE_SMALL = 3;
	private Button startstopButton;
	private Button nextButton;
	private Vector listeners;
	private Choice shapesChoice;
	private Choice zoomChoice;

	/**
	 * Contructs the controls.
	 */
	public GameOfLifeControls() {
		listeners = new Vector();

		// pulldown menu with shapes
		shapesChoice = new Choice();
	
		// Put names of shapes in menu
		Shape[] shapes = ShapeCollection.getShapes();
		for ( int i = 0; i < shapes.length; i++ )
			shapesChoice.addItem( shapes[i].getName() );

		// when shape is selected
		shapesChoice.addItemListener(
			new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					shapeSelected( (String) e.getItem() );
				}
			}
		);
	
		// pulldown menu with speeds
		Choice speedChoice = new Choice();
	
		// add speeds
		speedChoice.addItem(SLOW);
		speedChoice.addItem(FAST);
		speedChoice.addItem(HYPER);
	
		// when item is selected
		speedChoice.addItemListener(
			new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					String arg = (String) e.getItem();
					if (SLOW.equals(arg)) // slow
						speedChanged(1000);
					else if (FAST.equals(arg)) // fast
						speedChanged(100);
					else if (HYPER.equals(arg)) // hyperspeed
						speedChanged(10);
				}
			}
		);
	
		// pulldown menu with speeds
		zoomChoice = new Choice();
	
		// add speeds
		zoomChoice.addItem(BIG);
		zoomChoice.addItem(MEDIUM);
		zoomChoice.addItem(SMALL);
	
		// when item is selected
		zoomChoice.addItemListener(
			new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					String arg = (String) e.getItem();
					if (BIG.equals(arg))
						zoomChanged(SIZE_BIG);
					else if (MEDIUM.equals(arg))
						zoomChanged(SIZE_MEDIUM);
					else if (SMALL.equals(arg))
						zoomChanged(SIZE_SMALL);
				}
			}
		);
	
		// number of generations
		genLabel = new Label(genLabelText+"         ");
	
		// start and stop buttom
		startstopButton = new Button(startLabelText);
			
		// when start/stop button is clicked
		startstopButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startStopButtonClicked();
				}
			}
		);
	
		// next generation button
		nextButton = new Button(nextLabelText);
			
		// when next button is clicked
		nextButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					nextButtonClicked();
				}
			}
		);
	
		// create panel with controls
		this.add(shapesChoice);
		this.add(nextButton);
		this.add(startstopButton);
		this.add(speedChoice);
		this.add(zoomChoice);
		this.add(genLabel);
		this.validate();
	}
	

	/**
	 * Add listener for this control
	 * @param listener Listener object
	 */
	public void addGameOfLifeControlsListener( GameOfLifeControlsListener listener ) {
		listeners.addElement( listener );
	}

	/**
	 * Remove listener from this control
	 * @param listener Listener object
	 */
	public void removeGameOfLifeControlsListener( GameOfLifeControlsListener listener ) {
		listeners.removeElement( listener );
	}

	/**
	 * Set the number of generations in the control bar.
	 * @param generations number of generations
	 */
	public void setGeneration( int generations ) {
		genLabel.setText(genLabelText + generations + "         ");
	}
	
	/**
	 * Start-button is activated.
	 */
	public void start() {
		startstopButton.setLabel(stopLabelText);
		nextButton.disable();
		shapesChoice.disable();
	}

	/**
	 * Stop-button is activated.
	 */
	public void stop() {
		startstopButton.setLabel(startLabelText);
		nextButton.enable();
		shapesChoice.enable();
	}

	/**
	 * Called when the start/stop-button is clicked.
	 * Notify event-listeners.
	 */
	public void startStopButtonClicked() {
		GameOfLifeControlsEvent event = new GameOfLifeControlsEvent( this );
		for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
			((GameOfLifeControlsListener) e.nextElement()).startStopButtonClicked( event );
	}

	/**
	 * Called when the next-button is clicked.
	 * Notify event-listeners.
	 */
	public void nextButtonClicked() {
		GameOfLifeControlsEvent event = new GameOfLifeControlsEvent( this );
		for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
			((GameOfLifeControlsListener) e.nextElement()).nextButtonClicked( event );
	}

	/**
	 * Called when a new speed from the speed pull down is selected.
	 * Notify event-listeners.
	 */
	public void speedChanged( int speed ) {
		GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getSpeedChangedEvent( this, speed );
		for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
			((GameOfLifeControlsListener) e.nextElement()).speedChanged( event );
	}

	/**
	 * Called when a new zoom from the zoom pull down is selected.
	 * Notify event-listeners.
	 */
	public void zoomChanged( int zoom ) {
		GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getZoomChangedEvent( this, zoom );
		for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
			((GameOfLifeControlsListener) e.nextElement()).zoomChanged( event );
	}

	/**
	 * Called when a new shape from the shape pull down is selected.
	 * Notify event-listeners.
	 */
	public void shapeSelected( String shapeName ) {
		GameOfLifeControlsEvent event = GameOfLifeControlsEvent.getShapeSelectedEvent( this, shapeName );
		for ( Enumeration e = listeners.elements(); e.hasMoreElements(); )
			((GameOfLifeControlsListener) e.nextElement()).shapeSelected( event );
	}
	
	/**
	 * Called when a new cell size from the zoom pull down is selected.
	 * Notify event-listeners.
	 */
	public void setZoom( String n ) {
		zoomChoice.select(n);
	}


}