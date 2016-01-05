package com.github.fge.grappa.exceptions;

/**
 * Base exception class for all grammar/parser errors
 */
public class GrappaException extends RuntimeException {
	public GrappaException(Throwable cause) {
		super(cause);
	}

	public GrappaException(String message, Throwable cause) {
		super(message, cause);
	}

	public GrappaException(String message) {
		super(message);
	}
}
