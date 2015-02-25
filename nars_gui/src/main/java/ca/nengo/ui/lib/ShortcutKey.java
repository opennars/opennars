package ca.nengo.ui.lib;

import ca.nengo.ui.lib.action.StandardAction;

public class ShortcutKey {

	private final StandardAction action;
	private final int keyCode;
	private final int modifiers;

	public ShortcutKey(int modifiers, int keyCode, StandardAction action) {
		super();
		this.modifiers = modifiers;
		this.keyCode = keyCode;
		this.action = action;
	}

	public StandardAction getAction() {
		return action;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getModifiers() {
		return modifiers;
	}

}