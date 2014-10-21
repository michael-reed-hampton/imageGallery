package name.hampton.mike.search;


/**
 * Exception to catch all types of communication / parsing issues associated with talking to a Search Interface
 * 
 * @author mike.hampton
 *
 */
public class SearchException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7639551513212629198L;
	
	public SearchException() {
		super();
	}

	public SearchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchException(String message) {
		super(message);
	}

	public SearchException(Throwable cause) {
		super(cause);
	}

}
