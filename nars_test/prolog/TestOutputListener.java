package prolog;

import nars.prolog.event.OutputEvent;
import nars.prolog.event.OutputListener;

class TestOutputListener implements OutputListener {
	
	public String output = "";

	public void onOutput(OutputEvent e) {
		output += e.getMsg();
	}

}
