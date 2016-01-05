package com.github.fge.grappa.exceptions;

/**
 * Exception thrown when an invalid combination of rules in a grammar is
 * detected at build time
 */
public final class InvalidGrammarException extends GrappaException {
	public InvalidGrammarException(Throwable cause) {
		super(cause);
	}

	public InvalidGrammarException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidGrammarException(String message) {
		super(message);
	}
}
