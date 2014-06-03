package com.googlecode.opennars.language;

public class BooleanLiteral extends Literal {

	private boolean truthval;

	public BooleanLiteral(String name) {
		super(name);
		this.truthval = Boolean.parseBoolean(name);
	}
	
	public BooleanLiteral(boolean tv) {
		super(Boolean.toString(tv));
		this.truthval = tv;
	}

}
