package ca.nengo.ui.lib.object.activity;

import ca.nengo.ui.lib.util.UIEnvironment;

import javax.swing.*;

/**
 * Displays a task message in the status bar which appears for a finite
 * duration.
 * 
 * @author Shu Wu
 */
public class TransientStatusMessage {

	private final long myDuration;

	private final String myMessage;

	public TransientStatusMessage(String msg, long duration) {
		super();
		myMessage = msg;
		myDuration = duration;

		Thread myMsgRunner = new Thread() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						UIEnvironment.getInstance().getUniverse().addTaskStatusMsg(myMessage);
					}
				});
				try {
					Thread.sleep(myDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						UIEnvironment.getInstance().getUniverse().removeTaskStatusMsg(myMessage);
					}
				});

			}
		};
		myMsgRunner.start();

	}

}
