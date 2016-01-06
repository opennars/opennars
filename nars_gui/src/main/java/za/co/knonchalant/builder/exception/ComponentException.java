package za.co.knonchalant.builder.exception;

/**
 * General exception for a problem encountered generating GUIs.
 */
public class ComponentException extends RuntimeException {
	public ComponentException(String message) {
		super(message);
	}
}
