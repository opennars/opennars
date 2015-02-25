package ca.nengo.ui.lib.object.activity;

import ca.nengo.ui.AbstractNengo;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;

import javax.swing.*;

/**
 * An action which is tracked by the UI. Since tracked actions are slow and have
 * UI messages associated with them, they do never execute inside the Swing
 * dispatcher thread.
 * 
 * @author Shu Wu
 */
public abstract class TrackedAction extends StandardAction {

	private static final long serialVersionUID = 1L;

	private final String taskName;

	private TrackedStatusMsg trackedMsg;

	private final WorldObjectImpl wo;

	public TrackedAction(String taskName) {
		this(taskName, null);

	}

	public TrackedAction(String taskName, WorldObjectImpl wo) {
		super(taskName, null, false);
		this.taskName = taskName;
		this.wo = wo;

	}

	@Override
	public void doAction() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				trackedMsg = new TrackedStatusMsg(taskName, wo);
			}
		});
		
    	AbstractNengo.getInstance().getProgressIndicator().start(taskName);
		super.doAction();

	}
	
	protected void doActionInternal() {
    	AbstractNengo.getInstance().getProgressIndicator().setThread();
  		super.doActionInternal();
	}

	@Override
	protected void postAction() {
		super.postAction();
    	AbstractNengo.getInstance().getProgressIndicator().stop();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				trackedMsg.finished();
			}
		});
	}
	
}
