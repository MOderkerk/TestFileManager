package de.oderkerk.tools.boot.fileuploader.storage;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
public class StorageException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3074381381781049501L;

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
