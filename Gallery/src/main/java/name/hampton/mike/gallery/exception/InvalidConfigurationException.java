package name.hampton.mike.gallery.exception;

public class InvalidConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4827812040272901388L;

	/**
	 * @param message
	 */
	public InvalidConfigurationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidConfigurationException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public InvalidConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

}
