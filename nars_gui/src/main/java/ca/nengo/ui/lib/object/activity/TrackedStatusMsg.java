package ca.nengo.ui.lib.object.activity;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.primitive.Text;

/**
 * Displays and removes a task message from the application status bar
 * 
 * @author Shu Wu
 */
public class TrackedStatusMsg {
	private String taskName;
	Text taskText;

	public TrackedStatusMsg(String taskName) {
		this(taskName, null);
	}

	public TrackedStatusMsg(String taskName, WorldObjectImpl wo) {
		super();

		if (wo != null) {
			taskText = new Text(taskName);
			taskText.setPaint(NengoStyle.COLOR_NOTIFICATION);
			taskText.setOffset(0, -taskText.getHeight());
			wo.addChild(taskText);

			setTaskName(wo.name() + ": " + taskName);
		} else {
			setTaskName(taskName);
		}
		init();
	}

	private void init() {
		 UIEnvironment.getInstance().getUniverse().addTaskStatusMsg(getTaskName());

	}

	protected String getTaskName() {
		return taskName;
	}

	protected void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * Removes the task message from the application status bar.
	 */
	public void finished() {
		UIEnvironment.getInstance().getUniverse().removeTaskStatusMsg(getTaskName());

		if (taskText != null) {
			taskText.removeFromParent();
		}
	}
}
