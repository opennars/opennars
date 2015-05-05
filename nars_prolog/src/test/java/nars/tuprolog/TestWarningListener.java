/**
 * 
 */
package nars.tuprolog;

import nars.tuprolog.event.WarningEvent;
import nars.tuprolog.event.WarningListener;

class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}