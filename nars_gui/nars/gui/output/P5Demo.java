
package nars.gui.output;

/**
 *
 * @author me
 */


import com.hipposretribution.controlP5.ControlP5;
import com.hipposretribution.controlP5.behaviour.ControlBehavior;
import nars.gui.Window;

/**
 * ControlP5 Behavior ControlBehavior is an abstract class that can be extended using your custom control
 * behaviors. What is a control behavior? Control Behaviors allow you to automate and dynamically change the
 * state or value of a controller. One behavior per controller is currently supported. i case you need to use
 * more that one bahavior, the implementation has to happen on your side - inside your control behavior.
 *
 * find a list of public methods available for the ControlBehavior Controller at the bottom of this sketch.
 *
 * by Andreas Schlegel, 2012 www.sojamo.de/libraries/controlp5
 *
 */

public class P5Demo extends PPanel {
	private static final long serialVersionUID = 3627699347976919729L;

	ControlP5 cp5;

	public int myColorBackground = color(0, 0, 0);

	public int sliderValue = 100;

	public void setup() {
		size(400, 400);
		noStroke();

                frameRate(20f);
                
                
		cp5 = new ControlP5(this);
                
		cp5.addSlider("sliderValue").setRange(0, 255).setValue(128).setPosition(100, 50 + height / 2).setSize(40, 100);

		cp5.addSlider("slider").setRange(100, 255).setValue(128).setPosition(100, 50).setSize(100, 40);

		cp5.addButton("bang").setPosition(40, 50 + height / 2).setSize(40, 40).activateBy(ControlP5.PRESSED);

		// add a custom ControlBehavior to controller bang,
		// class TimerEvent is included in this sketch at the bottom
		// and extends abstract class ControlBehavior.
		cp5.getController("bang").setBehavior(new TimedEvent());

		// use an anonymous class of type ControlBehavior.
		cp5.getController("slider").setBehavior(new ControlBehavior() {
			float a = 0;

			public void update() {
				setValue(sin(a += 0.1) * 50 + 150);
			}
		});
		
		cp5.setBroadcast(true);
                cp5.printControllerMap();
	}

	public void draw() {
            
		background(myColorBackground);
		fill(sliderValue);
		rect(0, 0, width, height / 2);
	}

	public void slider(float theColor) {
		myColorBackground = color(theColor);
		//println("# a slider event. setting background to " + theColor);
	}

	public void bang() {
		//println("# an event received from controller bang.");
		// a bang will set the value of controller sliderValue
		// to a random number between 0 and 255.
		cp5.getController("sliderValue").setValue(random(0, 255));
	}

	// custom ControlBehavior
	public class TimedEvent extends ControlBehavior {
		long myTime;
		int interval = 200;

		public TimedEvent() {
			reset();
		}

		void reset() {
			myTime = millis() + interval;
		}

		public void update() {
			if (millis() > myTime) {
				setValue(1);
				reset();
			}
		}
	}
        
        public static void main(String[] args) {
            Window w = new Window("p5", new P5Demo().newPanel());
            w.setSize(400,400);
            w.setVisible(true);
        }
}