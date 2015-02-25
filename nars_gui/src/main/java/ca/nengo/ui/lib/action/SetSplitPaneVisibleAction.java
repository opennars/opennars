package ca.nengo.ui.lib.action;

import ca.nengo.ui.lib.AuxillarySplitPane;

public class SetSplitPaneVisibleAction extends StandardAction {

	private static final long serialVersionUID = 1L;
	private final boolean visible;
	private final AuxillarySplitPane splitPane;

	public SetSplitPaneVisibleAction(String actionName, AuxillarySplitPane splitPane, boolean visible) {
		super(actionName);
		this.visible = visible;
		this.splitPane = splitPane;
	}

	@Override
	protected void action() throws ActionException {
		splitPane.setAuxVisible(visible);
	}

}