package nars.tuprolog;

import nars.tuprolog.event.OutputEvent;
import nars.tuprolog.event.OutputListener;

class TestOutputListener implements OutputListener {
	
	public String output = "";

	public void onOutput(OutputEvent e) {
		output += e.getMsg();
	}

}
