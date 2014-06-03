package com.googlecode.opennars.language;

public class NumericLiteral extends Literal {
	
	public enum TYPE {
		INTEGER,
		DOUBLE;
	};
	
	private double num;
	private TYPE type;

	public NumericLiteral(String name) {
		super(name);
		this.num = Double.parseDouble(name);
	}
	
	public NumericLiteral(int num) {
		super(Integer.toString(num));
		this.num = num;
		this.type = TYPE.INTEGER;
	}
	
	public NumericLiteral(double num) {
		super(Double.toString(num));
		this.num = num;
		this.type = TYPE.DOUBLE;
	}

	public TYPE getType() {
		return type;
	}
	
}
