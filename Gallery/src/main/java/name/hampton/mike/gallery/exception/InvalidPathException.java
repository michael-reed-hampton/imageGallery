/**
 * 
 */
package name.hampton.mike.gallery.exception;

/**
 * @author mike.hampton
 *
 */
public class InvalidPathException extends InvalidConfigurationException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2034841338548620461L;
	private String input;

	/**
	 * @param message
	 */
	public InvalidPathException(String path, String message) {
		this(message + ", path ='" + path + "'");
		setInput(path);
	}

	/**
	 * @param message
	 */
	public InvalidPathException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidPathException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidPathException(String path, String message, Throwable cause) {
		this(message + ", path ='" + path + "'", cause);
		setInput(path);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidPathException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}
}
