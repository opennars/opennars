/**
 * 
 */
package prolog;

import nars.prolog.event.WarningEvent;
import nars.prolog.event.WarningListener;

class TestWarningListener implements WarningListener {
	public String warning;
	public void onWarning(WarningEvent e) {
		warning = e.getMsg();
	}
}