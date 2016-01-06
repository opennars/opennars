package nars.op.software.scheme.exception;

public class VariableNotDefinedException extends RuntimeException {
	public VariableNotDefinedException(String symbolName) {
		super(String.format("Variable '%s' not defined", symbolName));
	}
}
